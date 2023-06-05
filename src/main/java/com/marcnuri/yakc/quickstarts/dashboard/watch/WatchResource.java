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
package com.marcnuri.yakc.quickstarts.dashboard.watch;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.jboss.resteasy.annotations.SseElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class WatchResource {

  private static final Logger LOG = LoggerFactory.getLogger(WatchResource.class);

  private final WatchService watchService;
  private final ObjectMapper objectMapper;
  private final ExecutorService subscribeExecutor;

  @Inject
  public WatchResource(WatchService watchService, ObjectMapper objectMapper) {
    this.watchService = watchService;
    this.objectMapper = objectMapper;
    subscribeExecutor = Executors.newCachedThreadPool();
  }

  void onShutdown(@Observes ShutdownEvent event) {
    subscribeExecutor.shutdown();
  }

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType(MediaType.APPLICATION_JSON)
  public void get(@Context Sse sse, @Context SseEventSink sseEventSink) {
    watchService.newWatch().runSubscriptionOn(subscribeExecutor).subscribe().with(we -> {
      try {
        if (!sseEventSink.isClosed()) {
          sseEventSink.send(sse.newEvent(objectMapper.writeValueAsString(we)));
        }
      } catch (Exception e) {
        LOG.error("Error serializing object", e);
      }
    });
  }
}
