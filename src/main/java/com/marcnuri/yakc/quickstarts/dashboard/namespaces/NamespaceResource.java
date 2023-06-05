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
package com.marcnuri.yakc.quickstarts.dashboard.namespaces;

import com.marcnuri.yakc.model.io.k8s.api.core.v1.Namespace;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
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
  public List<Namespace> get() throws IOException {
    return namespaceService.get();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{name}")
  public Namespace delete(@PathParam("name") String name) throws IOException {
    return namespaceService.deleteNamespace(name);
  }
}
