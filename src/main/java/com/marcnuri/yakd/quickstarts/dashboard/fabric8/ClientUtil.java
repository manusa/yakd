package com.marcnuri.yakd.quickstarts.dashboard.fabric8;

import com.marcnuri.yakc.api.WatchEvent;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.Informable;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ClientUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ClientUtil.class);

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

  public static <T> T ignoreForbidden(Supplier<T> function, T defaultIfForbidden) {
    try {
      return function.get();
    } catch (KubernetesClientException ex) {
      if (ex.getCode() != 403) {
        throw ex;
      }
      LOG.debug("Access to resource is forbidden, ignoring:\n{}", ex.getMessage());
      return defaultIfForbidden;
    }
  }

  public static <T> Observable<WatchEvent<T>> observable(Informable<T> informable) {
    return InformerOnSubscribe.observable(informable::inform);
  }
}
