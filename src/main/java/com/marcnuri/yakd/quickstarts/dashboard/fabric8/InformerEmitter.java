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
 * Created on 2023-06-10, 9:25
 */
package com.marcnuri.yakd.quickstarts.dashboard.fabric8;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.smallrye.mutiny.subscription.MultiEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class InformerEmitter<T> implements Consumer<MultiEmitter<? super WatchEvent<T>>> {

  private static final Logger LOG = LoggerFactory.getLogger(InformerEmitter.class);

  private final Function<ResourceEventHandler<? super T>, SharedIndexInformer<T>> informerFactory;

  public InformerEmitter(Function<ResourceEventHandler<? super T>, SharedIndexInformer<T>> informerFactory) {
    this.informerFactory = informerFactory;
  }

  @Override
  public void accept(MultiEmitter<? super WatchEvent<T>> emitter) {
    final var informer = informerFactory.apply(new WatchEventEmitter<>(emitter));
    // Stop the emitter in case the informer stops
    informer.stopped().whenComplete((v, ex) -> {
      if (ex != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Informer for {} stopped with exception {}", informer.getApiTypeClass(), ex.getMessage());
        }
        emitter.fail(ex);
      }
      emitter.complete();
    });
    // Stop the informer in case the emitter stops
    emitter.onTermination(informer::close);
  }

  private record WatchEventEmitter<T>(MultiEmitter<? super WatchEvent<T>> emitter) implements ResourceEventHandler<T> {

    @Override
    public void onAdd(T obj) {
      emitter.emit(new WatchEvent<>(Watcher.Action.ADDED, obj));
    }

    @Override
    public void onUpdate(T oldObj, T newObj) {
      emitter.emit(new WatchEvent<>(Watcher.Action.MODIFIED, newObj));
    }

    @Override
    public void onDelete(T obj, boolean finalStateUnknown) {
      emitter.emit(new WatchEvent<>(Watcher.Action.DELETED, obj));
    }
  }

}
