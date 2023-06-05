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
package com.marcnuri.yakc.quickstarts.dashboard.customresources;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.runtime.annotations.RegisterForReflection;

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
  public List<UntypedResource> get(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("plural") String plural
  ) throws IOException {
    return customResourceService.get(group, version, plural);
  }

  @DELETE
  @Path("/{group}/{version}/{plural}/{name}")
  public Response delete(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("plural") String plural,
    @PathParam("name") String name
  ) throws IOException {
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
  ) throws IOException {
    customResourceService.deleteNamespacedCustomResource(group, version, namespace, plural, name);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{group}/{version}/{plural}/{name}")
  public UntypedResource update(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("plural") String plural,
    @PathParam("name") String name,
    UntypedResource customResource
  ) throws IOException {
    return customResourceService.replaceCustomResource(group, version, plural, name, customResource);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{group}/{version}/{plural}/namespaces/{namespace}/{name}")
  public UntypedResource updateNamespaced(
    @PathParam("group") String group,
    @PathParam("version") String version,
    @PathParam("namespace") String namespace,
    @PathParam("plural") String plural,
    @PathParam("name") String name,
    UntypedResource customResource
  ) throws IOException {
    return customResourceService.replaceNamespacedCustomResource(group, version, namespace, plural, name, customResource);
  }
}
