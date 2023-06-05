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
package com.marcnuri.yakc.quickstarts.dashboard.apis;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
  public List<String> getApiGroups() throws IOException {
    return apisService.getApiGroups();
  }
}
