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
 * Created on 2023-06-14, 15:00
 */
package com.marcnuri.yakd.fabric8;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.WatchEvent;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.Watchable;
import io.smallrye.mutiny.subscription.MultiEmitter;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Function;

public class WatchableSubscriber<T extends HasMetadata, Y> implements Subscriber<Y> {
  private static final long DEFAULT_WATCHER_TIMEOUT_SECONDS = 900L;

  private final Watchable<T> watchable;
  private final Function<T, Y> mapper;

  public WatchableSubscriber(Watchable<T> watchable, Function<T, Y> mapper) {
    this.watchable = watchable;
    this.mapper = mapper;
  }

  @Override
  public Closeable subscribe(Consumer<WatcherException> close, MultiEmitter<? super WatchEvent<Y>> emitter) {
    // Kubernetes Watches are not reliable, the API server might eventually stop producing events while
    // maintaining the connection open. Client Go does something similar.
    // We should add a timeout of less than 20 minutes, plus a jitter to avoid all services reconnecting at once
    final var jitter = (long) (Math.random() * 9 + 1);
    return watchable.watch(
      new ListOptionsBuilder().withTimeoutSeconds(DEFAULT_WATCHER_TIMEOUT_SECONDS + jitter).build(),
      new WatchEventEmitter<>(mapper, close, emitter)
    );
  }

  public static <T extends HasMetadata> WatchableSubscriber<T, T> subscriber(Watchable<T> watchable) {
    return new WatchableSubscriber<>(watchable, Function.identity());
  }

  public static <T extends HasMetadata, Y> WatchableSubscriber<T, Y> subscriber(Watchable<T> watchable, Function<T, Y> mapper) {
    return new WatchableSubscriber<>(watchable, mapper);
  }

  private record WatchEventEmitter<T extends HasMetadata, Y>(
    Function<T, Y> mapper, Consumer<WatcherException> close, MultiEmitter<? super WatchEvent<Y>> emitter) implements Watcher<T> {

    @Override
    public void eventReceived(Action action, T resource) {
      emitter.emit(new WatchEvent<>(action, mapper.apply(resource)));
    }

    @Override
    public void onClose() {
      close.accept(null);
    }

    @Override
    public void onClose(WatcherException cause) {
      close.accept(cause);
    }
  }

}
