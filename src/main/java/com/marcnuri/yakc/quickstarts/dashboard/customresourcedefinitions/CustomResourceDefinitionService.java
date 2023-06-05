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
 * Created on 2020-11-22, 19:25
 */
package com.marcnuri.yakc.quickstarts.dashboard.customresourcedefinitions;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.apiextensions.v1.ApiextensionsV1Api;
import com.marcnuri.yakc.model.io.k8s.apiextensionsapiserver.pkg.apis.apiextensions.v1.CustomResourceDefinition;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class CustomResourceDefinitionService implements Watchable<CustomResourceDefinition> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public CustomResourceDefinitionService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<CustomResourceDefinition>> watch() throws IOException {
    return kubernetesClient.create(ApiextensionsV1Api.class).listCustomResourceDefinition().watch();
  }

  public CustomResourceDefinition delete(String name) throws IOException {
    return kubernetesClient.create(ApiextensionsV1Api.class).deleteCustomResourceDefinition(name).get(CustomResourceDefinition.class);
  }

  public CustomResourceDefinition update(String name, CustomResourceDefinition customResourceDefinition) throws IOException {
    return kubernetesClient.create(ApiextensionsV1Api.class)
      .replaceCustomResourceDefinition(name, customResourceDefinition).get();
  }
}
