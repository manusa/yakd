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
 * Created on 2020-09-06, 8:37
 */
package com.marcnuri.yakd.quickstarts.dashboard.pod;

import com.marcnuri.yakc.model.io.k8s.api.core.v1.Pod;
import com.marcnuri.yakc.model.io.k8s.metrics.pkg.apis.metrics.v1beta1.PodMetrics;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.reactivex.BackpressureStrategy;
import io.smallrye.mutiny.Multi;
import io.vertx.core.http.HttpServerResponse;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.io.IOException;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class PodResource {

  private static final Logger LOG = LoggerFactory.getLogger(PodResource.class);

  private final PodService podService;

  @Inject
  public PodResource(PodService podService) {
    this.podService = podService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public Pod get(@PathParam("namespace") String namespace, @PathParam("name") String name)
    throws IOException {
    return podService.getPod(name, namespace);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}/metrics")
  public PodMetrics getPodMetrics(@PathParam("namespace") String namespace, @PathParam("name") String name)
    throws IOException {
    return podService.getPodMetrics(name, namespace);
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name)
    throws IOException {

    podService.deletePod(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public Pod update(@PathParam("namespace") String namespace, @PathParam("name") String name, Pod pod)
    throws IOException {

    return podService.updatePod(name, namespace, pod);
  }

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}/logs/{container}")
  public void getLogs(
    @Context HttpServerResponse response, @Context Sse sse, @Context SseEventSink sseEventSink,
    @PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("container") String container) {

    Multi.createFrom()
      .publisher(AdaptersToFlow.publisher(podService.getPodContainerLog(container, name, namespace).toFlowable(BackpressureStrategy.BUFFER)))
      .subscribe()
      .with(
        subscription -> {
          response.closeHandler(v -> subscription.cancel());
          subscription.request(Long.MAX_VALUE);
        },
        logEntry -> sseEventSink.send(sse.newEvent(logEntry)),
        throwable -> LOG.warn("Pod log subscription closed: {}", throwable.getMessage()),
        () -> sseEventSink.send(sse.newEvent("log-complete", ""))
      );
  }
}
