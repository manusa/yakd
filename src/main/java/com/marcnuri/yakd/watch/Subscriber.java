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
package com.marcnuri.yakd.watch;

import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.smallrye.mutiny.subscription.MultiEmitter;

import java.io.Closeable;
import java.util.function.Consumer;

public interface Subscriber<T> {

  Closeable subscribe(Consumer<WatcherException> close, MultiEmitter<? super WatchEvent<T>> emitter);

  @SuppressWarnings("unchecked")
  static <T> Subscriber<T> empty() {
    return new SimpleSubscriber<>();
  }

  @SafeVarargs
  static <T> Subscriber<T> items(T... items) {
    return new SimpleSubscriber<>(items);
  }

  final class SimpleSubscriber<T> implements Subscriber<T>, Closeable {

    private T[] items;

    @SafeVarargs
    public SimpleSubscriber(T... items) {
      this.items = items;
    }

    @Override
    public Closeable subscribe(Consumer<WatcherException> close, MultiEmitter<? super WatchEvent<T>> emitter) {
      for (T item : items) {
        emitter.emit(new WatchEvent<>(Watcher.Action.ADDED, item));
      }
      return this;
    }

    @Override
    public void close() {
      items = null;
    }
  }
}
