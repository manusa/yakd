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
 * Created on 2020-10-11, 8:34
 */
package com.marcnuri.yakd.namespaces;

import io.fabric8.kubernetes.api.model.Namespace;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Singleton;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class NamespaceResource {

  private final NamespaceService namespaceService;

  public NamespaceResource(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Namespace> get() {
    return namespaceService.get();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{name}")
  public Response delete(@PathParam("name") String name) {
    namespaceService.deleteNamespace(name);
    return Response.noContent().build();
  }
}
