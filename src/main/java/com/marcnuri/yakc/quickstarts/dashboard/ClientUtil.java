/*
 * ClientUtil.java
 *
 * Created on 2020-10-05, 19:28
 */
package com.marcnuri.yakc.quickstarts.dashboard;

import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.ForbiddenException;
import com.marcnuri.yakc.api.KubernetesCall;
import com.marcnuri.yakc.api.KubernetesException;
import io.reactivex.Observable;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Marc Nuri on 2020-10-05.
 */
public class ClientUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ClientUtil.class);

  @SafeVarargs
  public static <T> T tryWithFallback(ClientFunction<T>... functions) throws IOException {
    IOException exception = new IOException(
      "This exception should be replaced if caught and finally thrown");
    for (ClientFunction<T> func : functions) {
      try {
        return func.call();
      } catch (ClientErrorException ex) {
        exception = ex;
      } catch (IllegalArgumentException invalidRetrofitCallSuchAsNullNamespace) {
        // TODO: improve overall behavior
      }
    }
    throw exception;
  }

  public static <T> T ignoreForbidden(ClientFunction<T> function, T defaultIfForbidden) throws IOException {
    try {
      return function.call();
    } catch (ForbiddenException ex) {
      LOG.debug("Access to resource is forbidden, ignoring:\n{}", ex.getMessage());
      return defaultIfForbidden;
    }
  }

  public static <T> Observable<T> justWithNoComplete(T object) {
    return Observable.create(emitter -> emitter.onNext(object));
  }

  /**
   * Function to execute a Raw call on a {@link KubernetesCall} and make sure
   * that the response is closed (Prevents leak).
   *
   * <p> If the call is not successful, a KubernetesException is thrown.
   *
   * @param call on which to perform the raw execute call.
   * @return the closed Response.
   */
  public static ClientFunction<Response> executeRaw(KubernetesCall<?> call) {
    return () -> {
      try (var response = call.executeRaw()) {
        if (response.isSuccessful()) {
          return response;
        }
        throw KubernetesException.forResponse("Error executing call", response);
      }
    };
  }

  @FunctionalInterface
  public interface ClientFunction<T> {
    T call() throws IOException;
  }
}
