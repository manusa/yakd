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
 * Created on 2020-11-25, 19:32
 */
package com.marcnuri.yakd.quickstarts.dashboard.customresources;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
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
public class CustomResourceResource {

  private final CustomResourceService customResourceService;

  @Inject
  public CustomResourceResource(CustomResourceService customResourceService) {
    this.customResourceService = customResourceService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{group}/{version}/{plural}")
  public List<GenericKubernetesResource> get(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("plural") String plural
  ) {
    return customResourceService.get(group, version, plural);
  }

  @DELETE
  @Path("/{group}/{version}/{plural}/{name}")
  public Response delete(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("plural") String plural,
    @PathParam("name") String name
  ) {
    customResourceService.deleteCustomResource(group, version, plural, name);
    return Response.noContent().build();
  }

  @DELETE
  @Path("/{group}/{version}/{plural}/namespaces/{namespace}/{name}")
  public Response deleteNamespaced(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("namespace") String namespace,
    @PathParam("plural") String plural,
    @PathParam("name") String name
  ) {
    customResourceService.deleteNamespacedCustomResource(group, version, namespace, plural, name);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{group}/{version}/{plural}/{name}")
  public GenericKubernetesResource update(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("plural") String plural,
    @PathParam("name") String name,
    GenericKubernetesResource resource
  ) {
    return customResourceService.replaceCustomResource(group, version, plural, name, resource);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{group}/{version}/{plural}/namespaces/{namespace}/{name}")
  public GenericKubernetesResource updateNamespaced(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("namespace") String namespace,
    @PathParam("plural") String plural,
    @PathParam("name") String name,
    GenericKubernetesResource resource
  ) {
    return customResourceService.replaceNamespacedCustomResource(group, version, namespace, plural, name, resource);
  }
}
