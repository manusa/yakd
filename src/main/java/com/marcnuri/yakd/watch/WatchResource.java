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
 * Created on 2020-09-07, 13:43
 */
package com.marcnuri.yakd.watch;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.http.Compressed;
import io.smallrye.mutiny.Multi;
import io.vertx.core.http.HttpServerResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class WatchResource {

  private static final Logger LOG = LoggerFactory.getLogger(WatchResource.class);

  private final WatchService watchService;
  private final ExecutorService subscribeExecutor;

  @Inject
  public WatchResource(WatchService watchService) {
    this.watchService = watchService;
    subscribeExecutor = Executors.newCachedThreadPool();
  }

  void onShutdown(@Observes ShutdownEvent event) {
    subscribeExecutor.shutdown();
  }

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  @Compressed
  public Multi<WatchEvent<?>> get(@Context HttpServerResponse response) {
    return watchService.newWatch()
      .runSubscriptionOn(subscribeExecutor)
      .onSubscription()
      .invoke(subscription -> {
        response.closeHandler(v -> subscription.cancel());
        subscription.request(Long.MAX_VALUE); // unbounded -> request with no backpressure
      })
      .onFailure()
      .invoke(throwable ->  LOG.warn("Watch subscription closed: {}", throwable.getMessage()))
      .onCompletion()
      .invoke(() ->  LOG.debug("Watch subscription closed gracefully"));
  }
}
