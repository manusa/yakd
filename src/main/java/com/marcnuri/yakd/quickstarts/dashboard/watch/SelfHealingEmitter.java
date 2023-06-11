/*
 * Copyright 2023 Marc Nuri
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
 * Created on 2023-06-10, 8:00
 */
package com.marcnuri.yakd.quickstarts.dashboard.watch;

import io.fabric8.kubernetes.client.Watcher;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.subscription.MultiEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class allows to funnel and merge all the Watchable events into a single MultiEmitter stream.
 * The main advantage is that it will automatically resubscribe to the Watchable if it's stopped for any
 * unforeseen reason.
 * <p>
 * In addition, it will also schedule re-subscriptions to Watchables that were not available at the moment the
 * emitter was initially subscribed.
 */
public class SelfHealingEmitter implements Consumer<MultiEmitter<? super WatchEvent<?>>> {

  private static final Logger LOG = LoggerFactory.getLogger(SelfHealingEmitter.class);
  private final List<Watchable<?>> watchables;
  private final Map<Class<?>, Cancellable> emitters;
  private final ScheduledExecutorService executorService;

  public SelfHealingEmitter(ScheduledExecutorService executorService, List<Watchable<?>> watchables) {
    this.executorService = executorService;
    this.watchables = watchables;
    emitters = new ConcurrentHashMap<>();
  }

  @Override
  public void accept(MultiEmitter<? super WatchEvent<?>> emitter) {
    watchables.forEach(watchable -> executorService.execute(() -> subscribe(watchable, emitter)));
    emitter.onTermination(() -> {
      LOG.debug("SelfHealingEmitter stopped downstream, cleaning all resources");
      emitters.values().forEach(Cancellable::cancel);
    });
  }

  private void subscribe(Watchable<?> watchable, MultiEmitter<? super WatchEvent<?>> emitter) {
    if (!emitter.isCancelled() && watchable.getAvailabilityCheckFunction().map(Supplier::get).orElse(true)) {
      emitters.computeIfPresent(watchable.getClass(), (k, v) -> {
        v.cancel();
        return null;
      });
      final Consumer<Throwable> heal = throwable -> {
        // Fabric8 Watchers automatically reconnect on timeout, so we only need to heal on other errors or completions
        if (!emitter.isCancelled() && watchable.isRetrySubscription()) {
          LOG.debug("Watchable {} stopped, self healing with delay of {} seconds",
            watchable.getType(), watchable.getSelfHealingDelay().getSeconds());
          emitter.emit(new WatchEvent<>(Watcher.Action.ERROR, new RequestRestartError(watchable, throwable)));
          executorService.schedule(() ->
            subscribe(watchable, emitter), watchable.getSelfHealingDelay().getSeconds(), TimeUnit.SECONDS);
        } else if (LOG.isDebugEnabled()) {
          LOG.debug("Watchable {} stopped", watchable.getType());
        }
      };
      final var watchedEmitter = watchable.watch()
        .onFailure().invoke(heal)
        .onCompletion().invoke(() -> heal.accept(null))
        .subscribe().with(emitter::emit);
      emitters.put(watchable.getClass(), watchedEmitter);
    } else if (!emitter.isCancelled()) {
      LOG.debug("Watchable {} is not available, retrying in {} seconds",
        watchable.getType(), watchable.getRetrySubscriptionDelay().getSeconds());
      executorService.schedule(() ->
        subscribe(watchable, emitter), watchable.getRetrySubscriptionDelay().getSeconds(), TimeUnit.SECONDS);
    }
  }
}
