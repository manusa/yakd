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
 * Created on 2020-09-12, 8:41
 */
package com.marcnuri.yakd.deployment;

import io.fabric8.kubernetes.api.model.apps.Deployment;
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
public class DeploymentResource {

  private final DeploymentService deploymentService;

  @Inject
  public DeploymentResource(DeploymentService deploymentService) {
    this.deploymentService = deploymentService;
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    deploymentService.deleteDeployment(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public Deployment update(@PathParam("namespace") String namespace, @PathParam("name") String name, Deployment deployment) {
    return deploymentService.updateDeployment(name, namespace, deployment);
  }

  @PUT
  @Path("/{namespace}/{name}/restart")
  public Response restart(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    deploymentService.restart(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Path("/{namespace}/{name}/spec/replicas/{replicas}")
  public Response updateReplicas(
    @PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("replicas") int replicas) {

    deploymentService.updateReplicas(name, namespace, replicas);
    return Response.noContent().build();
  }
}
