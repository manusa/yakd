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
 * Created on 2020-11-28, 8:25
 */
package com.marcnuri.yakc.quickstarts.dashboard.customresources;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;

@Singleton
public class CustomResourceService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public CustomResourceService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<UntypedResource> get(String group, String version, String plural) throws IOException {
    final CustomResourceApi api = kubernetesClient.create(CustomResourceApi.class);
    return tryWithFallback(
      () -> api.listCustomResourceForAllNamespaces(group, version, plural).get(),
      () -> api.listNamespacedCustomResource(group, version, kubernetesClient.getConfiguration().getNamespace(), plural).get()
    ).getItems();
  }

  public Status deleteCustomResource(String group, String version, String plural, String name) throws IOException {
    return kubernetesClient.create(CustomResourceApi.class).deleteCustomResource(group, version, plural, name).get();
  }

  public Status deleteNamespacedCustomResource(
    String group, String version, String namespace, String plural, String name) throws IOException {

    return kubernetesClient.create(CustomResourceApi.class)
      .deleteNamespacedCustomResource(group, version, namespace, plural, name).get();
  }

  public UntypedResource replaceCustomResource(
    String group, String version, String plural, String name, UntypedResource customResource) throws IOException {

    return kubernetesClient.create(CustomResourceApi.class)
      .replaceCustomResource(group, version, plural, name, customResource).get();
  }

  public UntypedResource replaceNamespacedCustomResource(
    String group, String version, String namespace, String plural, String name, UntypedResource customResource)
    throws IOException {

    return kubernetesClient.create(CustomResourceApi.class)
      .replaceNamespacedCustomResource(group, version, namespace, plural, name, customResource).get();
  }
}
