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
 * Created on 2020-11-14, 19:07
 */
package com.marcnuri.yakc.quickstarts.dashboard.apis;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.apis.ApisApi;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.APIGroup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ApisService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ApisService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<String> getApiGroups() throws IOException {
    return kubernetesClient.create(ApisApi.class).getAPIVersions().get().getGroups().stream()
      .map(APIGroup::getName).collect(Collectors.toList());
  }
}
