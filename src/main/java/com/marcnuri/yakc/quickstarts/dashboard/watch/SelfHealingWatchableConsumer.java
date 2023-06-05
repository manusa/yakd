/*
 * Copyright 2020 Marc Nuri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 2020-12-31, 8:00
 */
package com.marcnuri.yakc.quickstarts.dashboard.watch;

import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.model.Model;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The main purpose of this class is to prevent Self-healing watcher subscriptions to keep
 * re-subscribing even if the underlying connection is closed by the client.
 *
 * <p> It also allows active subscriptions to detect this situation and automatically dispose
 * the subscription threads when the connection is closed by the client.
 *
 * <p> The class was added as a result/consequence of adding the quarkus-undertow extension, since RESTEasy now
 * runs as a filter with asynchronous non-blocking NIO threads and IOExceptions are silently discarded.
 *
 * <p> In the previous scenario (RESTEasy runs on top of Vert.x HTTP server), whenever the underlying connection
 * is closed by  * the client, the IOException cascades and all of the subscription Threads are automatically
 * interrupted.
 *
 * @see <a href="https://quarkus.io/guides/http-reference">QUARKUS - HTTP REFERENCE</a>
 * @see <a href="https://quarkus.io/guides/rest-json#servlet-compatibility">RESTEasy Servlet</a>
 */
public class SelfHealingWatchableConsumer implements java.util.function.Consumer<MultiEmitter<Object>> {

  private static final Logger LOG = LoggerFactory.getLogger(SelfHealingWatchableConsumer.class);

  private final List<Watchable<? extends Model>> watchables;
  private final ExecutorService subscribeExecutor;
  private final ExecutorService observableExecutor;
  private final Map<Class<?>, Disposable> deferredWatchables;
  private final Map<Class<?>, Disposable> selfHealingSubscriptions;
  private final ApiAvailability apiAvailability;
  private final HttpServerExchange exchange;
  private volatile boolean disposed;

  public SelfHealingWatchableConsumer(List<Watchable<? extends Model>> watchables) {
    this.watchables = watchables;
    subscribeExecutor = Executors.newCachedThreadPool();
    observableExecutor = Executors.newCachedThreadPool();
    deferredWatchables = new ConcurrentHashMap<>();
    selfHealingSubscriptions = new ConcurrentHashMap<>();
    apiAvailability = new ApiAvailability();
    exchange = ServletRequestContext.requireCurrent().getOriginalRequest().getExchange();
  }

  @Override
  public void accept(MultiEmitter<Object> multiEmitter) {
    for (Watchable<? extends Model> watchable : watchables) {
      deferredWatchables.put(watchable.getClass(),
        deferredWatchObservable(watchable).subscribeOn(Schedulers.from(subscribeExecutor)).subscribe(
          multiEmitter::emit,
          exception -> {
            LOG.error("Self Healing Watch subscription process failed for {}: {}", watchable.getType(), exception.getMessage());
            multiEmitter.fail(exception);
            dispose();
          }
        )
      );
    }
  }

  /**
   * Encapsulates the Watchable Observable subscription within another Observable
   * so that the individual subscription process for each watchable can be processed in separate
   * threads in a non-blocking way.
   */
  private Observable<Object> deferredWatchObservable(
    Watchable<? extends Model> watchable
  ) {
    return Observable.create(emitter -> {
      boolean subscribed;
      do {
        subscribed = subscribe(emitter, watchable);
        if (!subscribed && watchable.isRetrySubscription()) {
          LOG.error("Initial subscription for {} failed, retrying...", watchable.getType());
          TimeUnit.SECONDS.sleep(watchable.getRetrySubscriptionDelay().getSeconds());
        }
      } while (!shouldStopAndDispose(emitter) && !subscribed && watchable.isRetrySubscription());
    });
  }

  /**
   * Dispose all the currently active subscriptions.
   *
   * <p> Should clean up all the active threads and Watch connections to the cluster.
   */
  public synchronized int dispose() {
    if (!disposed) {
      disposed = true;
      deferredWatchables.values().forEach(Disposable::dispose);
      subscribeExecutor.shutdown();
      selfHealingSubscriptions.values().forEach(Disposable::dispose);
      observableExecutor.shutdown();
    }
    return deferredWatchables.size();
  }

  /**
   * Checks if the current response is complete (closed by client), disposing the rest
   * of active emitters in case the client is no longer listening.
   *
   * <p> Returns true if the current emitter was disposed by another Observable or if
   * the client closed the connection or is no longer listening.
   *
   * @param emitter the active deferredWatchableEmitter
   * @return if the current Observable should stop self-healing
   */
  private boolean shouldStopAndDispose(ObservableEmitter<?> emitter) {
    if (!disposed && exchange.isResponseComplete()) {
      dispose();
    }
    return disposed || emitter.isDisposed() || exchange.isResponseComplete();
  }

  private boolean subscribe(
    ObservableEmitter<Object> emitter, Watchable<? extends Model> watchable
  ) {
    Optional.ofNullable(selfHealingSubscriptions.get(watchable.getClass())).ifPresent(Disposable::dispose);
    if (shouldStopAndDispose(emitter)) {
      return false;
    }
    try {
      final Observable<?> observable;
      if (apiAvailability.isAvailable(watchable)) {
        observable = watchable.watch();
      } else {
        observable = Observable.<WatchEvent<? extends Model>>empty()
          .delay(watchable.getRetrySubscriptionDelay().getSeconds(), TimeUnit.SECONDS);
      }
      selfHealingSubscriptions.put(watchable.getClass(),
        observable.subscribeOn(Schedulers.from(observableExecutor)).subscribe(
          selfHealingOnNextConsumer(emitter),
          selfHealingErrorConsumer(emitter, watchable),
          selfHealingOnCompleteAction(emitter, watchable)
        ));
      LOG.debug("Subscribed to {}", watchable.getType());
      return true;
    } catch (Exception ex) {
      LOG.info("Error when starting subscription for {}", watchable.getClass(), ex);
      emitter.onComplete();
      dispose();
    }
    return false;
  }

  private Consumer<Object> selfHealingOnNextConsumer(ObservableEmitter<Object> emitter) {
    return we -> {
      if (shouldStopAndDispose(emitter)) {
        return;
      }
      emitter.onNext(we);
    };
  }

  private Consumer<Throwable> selfHealingErrorConsumer(
    ObservableEmitter<Object> emitter, Watchable<? extends Model> watchable
  ) {
    return error -> {
      LOG.debug("Subscription error for watchable {}: {}, reconnecting...",
        watchable.getClass(), error.getMessage());
      if (!disposed ) {
        emitter.onNext(new WatchEvent<>(WatchEvent.Type.ERROR, new RequestRestartError(watchable, error)));
        TimeUnit.SECONDS.sleep(watchable.getSelfHealingDelay().toSeconds());
        subscribe(emitter, watchable);
      }
    };
  }

  private io.reactivex.functions.Action selfHealingOnCompleteAction(
    ObservableEmitter<Object> emitter, Watchable<? extends Model> watchable
  ) {
    return () -> {
      LOG.debug("Subscription complete for watchable {}, reconnecting...", watchable.getClass());
      if (!disposed) {
        emitter.onNext(new WatchEvent<>(WatchEvent.Type.ERROR, new RequestRestartError(watchable, null)));
        TimeUnit.SECONDS.sleep(watchable.getSelfHealingDelay().toSeconds());
        subscribe(emitter, watchable);
      }
    };
  }
}
