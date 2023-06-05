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
 * Created on 2021-01-01, 18:37
 */
package com.marcnuri.yakc.quickstarts.dashboard.jobs;

import com.marcnuri.yakc.model.io.k8s.api.batch.v1.Job;
import io.quarkus.runtime.annotations.RegisterForReflection;

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
import java.io.IOException;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class JobResource {

  private final JobService jobService;

  @Inject
  public JobResource(JobService jobService) {
    this.jobService = jobService;
  }

  @DELETE
  @Path("/{namespace}/{name}")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name)
    throws IOException {

    jobService.delete(name, namespace);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{namespace}/{name}")
  public Job update(
    @PathParam("namespace") String namespace, @PathParam("name") String name, Job job)
    throws IOException {

    return jobService.update(name, namespace, job);
  }

}
