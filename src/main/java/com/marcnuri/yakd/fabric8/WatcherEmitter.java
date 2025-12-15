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
 * Created on 2023-06-10, 20:25
 */
package com.marcnuri.yakd.fabric8;

import com.marcnuri.yakd.watch.WatchEvent;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.Watchable;
import io.smallrye.mutiny.subscription.MultiEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class WatcherEmitter<T> implements Consumer<MultiEmitter<? super WatchEvent<T>>> {

  private static final Logger LOG = LoggerFactory.getLogger(WatcherEmitter.class);
  private static final long DEFAULT_WATCHER_TIMEOUT_SECONDS = 900L;

  private final Watchable<T> watchable;

  public WatcherEmitter(Watchable<T> watchable) {
    this.watchable = watchable;
  }


  @Override
  public void accept(MultiEmitter<? super WatchEvent<T>> emitter) {
    // Kubernetes Watches are not reliable, the API server might eventually stop producing events while
    // maintaining the connection open. Client Go does something similar.
    // We should add a timeout of less than 20 minutes, plus a jitter to avoid all services reconneecting at once
    final var jitter = (long) (Math.random() * 9 + 1);
    final var watch = watchable.watch(
      new ListOptionsBuilder().withTimeoutSeconds(DEFAULT_WATCHER_TIMEOUT_SECONDS + jitter).build(),
      new WatchEventEmitter<>(emitter)
    );
    // Stop the watch in case the emitter stops
    emitter.onTermination(watch::close);
  }

  private record WatchEventEmitter<T>(MultiEmitter<? super WatchEvent<T>> emitter) implements Watcher<T> {

    @Override
    public void eventReceived(Action action, T resource) {
      emitter.emit(new WatchEvent<>(action, resource));
    }

    @Override
    public void onClose() {
//      LOG.debug("Watcher for {} stopped with exception {}", informer.getApiTypeClass(), ex.getMessage());
      emitter.complete();
    }

    @Override
    public void onClose(WatcherException cause) {
      emitter.fail(cause);
    }
  }
}
