package com.marcnuri.yakd.quickstarts.dashboard;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

@Provider
public class KubernetesClientExceptionMapper implements ExceptionMapper<KubernetesClientException> {
  @Override
  public Response toResponse(KubernetesClientException exception) {
    final var code = exception.getCode() >= 400 || exception.getCode() <= 599 ? exception.getCode() : 500;
    final var type = exception.getCause() != null ? exception.getCause().getClass().getName() : KubernetesClientException.class.getName();
    final var entity = Optional.ofNullable(exception.getStatus())
      .map(Status::getMessage)
      .orElse(exception.getMessage());
    return Response
      .status(code)
      .header("YAKD-Exception-Type", type)
      .type(MediaType.TEXT_PLAIN_TYPE)
      .entity(entity)
      .build();
  }
}
