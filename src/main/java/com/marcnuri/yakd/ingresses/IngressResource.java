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
 * Created on 2020-10-04, 7:43
 */
package com.marcnuri.yakd.ingresses;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.http.Compressed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class IngressResource {

  private final IngressService ingressService;

  @Inject
  public IngressResource(IngressService ingressService) {
    this.ingressService = ingressService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Compressed
  public List<Ingress> get() {
    return ingressService.get();
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    ingressService.delete(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public Ingress update(@PathParam("namespace") String namespace, @PathParam("name") String name, Ingress ingress) {
    return ingressService.updateIngress(name, namespace, ingress);
  }
}
