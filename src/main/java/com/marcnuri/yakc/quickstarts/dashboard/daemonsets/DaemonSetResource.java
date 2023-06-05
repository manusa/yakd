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
 * Created on 2020-12-06, 7:22
 */
package com.marcnuri.yakc.quickstarts.dashboard.daemonsets;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.marcnuri.yakc.model.io.k8s.api.apps.v1.DaemonSet;

import io.quarkus.runtime.annotations.RegisterForReflection;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class DaemonSetResource {

  private final DaemonSetService daemonSetService;

  @Inject
  public DaemonSetResource(DaemonSetService daemonSetService) {
    this.daemonSetService = daemonSetService;
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name)
    throws IOException {

    daemonSetService.delete(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public DaemonSet update(
    @PathParam("namespace") String namespace, @PathParam("name") String name, DaemonSet daemonSet)
    throws IOException {

    return daemonSetService.update(name, namespace, daemonSet);
  }

  @PUT
  @Path("/{namespace}/{name}/restart")
  public Response restart(
    @PathParam("namespace") String namespace, @PathParam("name") String name) throws IOException {

    daemonSetService.restart(name, namespace);
    return Response.noContent().build();
  }
}
