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
 * Created on 2021-01-02, 17:33
 */
package com.marcnuri.yakd.cronjobs;

import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class CronJobResource {

  private final CronJobService cronJobService;

  @Inject
  public CronJobResource(CronJobService cronJobService) {
    this.cronJobService = cronJobService;
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    cronJobService.delete(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public CronJob update(
    @PathParam("namespace") String namespace, @PathParam("name") String name, CronJob cronJob
  ) {
    return cronJobService.update(name, namespace, cronJob);
  }

  @PUT
  @Path("/{namespace}/{name}/spec/suspend/{suspend}")
  public Response updateSuspend(
    @PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("suspend") boolean suspend
  ) {
    cronJobService.updateSuspend(name, namespace, suspend);
    return Response.noContent().build();
  }

  @PUT
  @Path("/{namespace}/{name}/trigger")
  public Response trigger(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    cronJobService.trigger(name, namespace);
    return Response.noContent().build();
  }
}
