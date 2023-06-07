package com.marcnuri.yakd.quickstarts.dashboard.fabric8;

import com.marcnuri.yakc.api.WatchEvent;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

// TODO: change from WatchEvent to whatever class we use on Fabric8
public class InformerOnSubscribe<T> implements ObservableOnSubscribe<WatchEvent<T>>, Disposable {

  private final Function<ResourceEventHandler<T>, SharedIndexInformer<T>> informerFactory;
  private final AtomicReference<SharedIndexInformer<T>> disposable = new AtomicReference<>();
  private final AtomicBoolean disposed = new AtomicBoolean(false);

  public InformerOnSubscribe(Function<ResourceEventHandler<T>, SharedIndexInformer<T>> informerFactory) {
    this.informerFactory = informerFactory;
  }

  @Override
  public void subscribe(@NonNull ObservableEmitter<WatchEvent<T>> emitter) throws Exception {
    disposable.set(informerFactory.apply(new WatchEventEmitter<>(emitter)));
  }

  @Override
  public void dispose() {
    disposable.get().close();
    disposed.set(true);
  }

  @Override
  public boolean isDisposed() {
    return disposed.get();
  }

  public static <T> Observable<WatchEvent<T>> observable(Function<ResourceEventHandler<T>, SharedIndexInformer<T>> informerFactory) {
    return Observable.create(new InformerOnSubscribe<>(informerFactory));
  }
  private record WatchEventEmitter<T>(ObservableEmitter<WatchEvent<T>> emitter) implements ResourceEventHandler<T> {

    @Override
    public void onAdd(T obj) {
      emitter.onNext(new WatchEvent<>(WatchEvent.Type.ADDED, obj));
    }

    @Override
    public void onUpdate(T oldObj, T newObj) {
      emitter.onNext(new WatchEvent<>(WatchEvent.Type.MODIFIED, newObj));
    }

    @Override
    public void onDelete(T obj, boolean finalStateUnknown) {
      emitter.onNext(new WatchEvent<>(WatchEvent.Type.DELETED, obj));
    }
  }

}
