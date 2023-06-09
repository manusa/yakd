package com.marcnuri.yakd.quickstarts.dashboard.fabric8;

import com.marcnuri.yakc.api.WatchEvent;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.Informable;
import io.reactivex.Observable;

import java.util.function.Supplier;

public class ClientUtil {

  private ClientUtil() {
  }

  public static final ListOptions LIMIT_1 = new ListOptionsBuilder().withLimit(1L).build();

  @SafeVarargs
  public static <T> T tryInOrder(Supplier<T>... functions) {
    KubernetesClientException lastException = new KubernetesClientException(
      "Error while executing Kubernetes Client function");
    for (var function : functions) {
      try {
        return function.get();
      } catch (KubernetesClientException ex) {
        lastException = ex;
      }
    }
    throw lastException;
  }

  public static <T> Observable<WatchEvent<T>> observable(Informable<T> informable) {
    return InformerOnSubscribe.observable(informable::inform);
  }
}
