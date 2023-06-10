package com.marcnuri.yakd.quickstarts.dashboard.fabric8;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
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
      emitter.emit(new WatchEvent<>(WatchEvent.Type.ADDED, obj));
    }

    @Override
    public void onUpdate(T oldObj, T newObj) {
      emitter.emit(new WatchEvent<>(WatchEvent.Type.MODIFIED, newObj));
    }

    @Override
    public void onDelete(T obj, boolean finalStateUnknown) {
      emitter.emit(new WatchEvent<>(WatchEvent.Type.DELETED, obj));
    }
  }

}
