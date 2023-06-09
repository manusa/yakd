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
 * Created on 2020-11-14, 19:09
 */
package com.marcnuri.yakd.apis;

import java.util.List;

import io.quarkus.vertx.http.Compressed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.runtime.annotations.RegisterForReflection;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class ApisResource {

  private final ApisService apisService;

  @Inject
  public ApisResource(ApisService apisService) {
    this.apisService = apisService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/groups")
  @Compressed
  public List<String> getApiGroups() {
    return apisService.getApiGroups();
  }
}
