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
 * Created on 2020-09-23, 19:20
 */
package com.marcnuri.yakc.quickstarts.dashboard.persistentvolumeclaims;

import com.marcnuri.yakc.model.io.k8s.api.core.v1.PersistentVolumeClaim;
import io.quarkus.runtime.annotations.RegisterForReflection;

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

import java.io.IOException;
import java.util.List;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class PersistentVolumeClaimResource {

  private final PersistentVolumeClaimService persistentVolumeClaimService;

  @Inject
  public PersistentVolumeClaimResource(
    PersistentVolumeClaimService persistentVolumeClaimService) {
    this.persistentVolumeClaimService = persistentVolumeClaimService;
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<PersistentVolumeClaim> get() throws IOException {
    return persistentVolumeClaimService.get();
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name)
    throws IOException {

    persistentVolumeClaimService.delete(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public PersistentVolumeClaim update(@PathParam("namespace") String namespace, @PathParam("name") String name, PersistentVolumeClaim persistentVolumeClaim)
    throws IOException {

    return persistentVolumeClaimService.update(name, namespace, persistentVolumeClaim);
  }
}
