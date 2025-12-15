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
 * Created on 2023-06-10, 8:00
 */
package com.marcnuri.yakd.watch;

import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.smallrye.mutiny.subscription.MultiEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelfHealingWatchableEmitter implements Consumer<MultiEmitter<? super WatchEvent<?>>> {

  private static final Logger LOG = LoggerFactory.getLogger(SelfHealingWatchableEmitter.class);
  private final ScheduledExecutorService executorService;
  private final List<Watchable<?>> watchables;
  private final Map<Class<?>, Closeable> activeWatches;

  public SelfHealingWatchableEmitter(ScheduledExecutorService executorService, List<Watchable<?>> watchables) {
    this.executorService = executorService;
    this.watchables = watchables;
    activeWatches = new ConcurrentHashMap<>();
  }

  @Override
  public void accept(MultiEmitter<? super WatchEvent<?>> emitter) {
    watchables.forEach(watchable -> executorService.execute(() -> subscribe(watchable, emitter)));
    emitter.onTermination(() -> {
      LOG.debug("WatchEvent emitter stopped downstream, cleaning all resources");
      activeWatches.values().forEach(SelfHealingWatchableEmitter::close);
    });
  }


  private void subscribe(Watchable<?> watchable, MultiEmitter<? super WatchEvent<?>> emitter) {
    activeWatches.computeIfPresent(watchable.getClass(), (k, v) -> {
      LOG.debug("Watchable {} already subscribed, cancelling previous subscription", watchable.getType());
      close(v);
      return null;
    });
    if (!emitter.isCancelled() && watchable.getAvailabilityCheckFunction().map(Supplier::get).orElse(true)) {
      final Consumer<WatcherException> heal = throwable -> {
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
      final var watch = watchable.watch().subscribe(heal, emitter);
      activeWatches.put(watchable.getClass(), watch);
    } else if (!emitter.isCancelled()) {
      LOG.debug("Watchable {} is not available, retrying in {} seconds",
        watchable.getType(), watchable.getRetrySubscriptionDelay().getSeconds());
      executorService.schedule(() ->
        subscribe(watchable, emitter), watchable.getRetrySubscriptionDelay().getSeconds(), TimeUnit.SECONDS);
    }
  }

  private static void close(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      LOG.debug("Error closing watch", e);
    }
  }
}
