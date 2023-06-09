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
package com.marcnuri.yakd.quickstarts.dashboard.customresources;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class CustomResourceService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public CustomResourceService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<GenericKubernetesResource> get(String group, String version, String plural) {
    return tryInOrder(
      () -> kubernetesClient.genericKubernetesResources(clusterContext(group, version, plural)).list().getItems(),
      () -> kubernetesClient.genericKubernetesResources(namespacedContext(group, version, plural))
        .inAnyNamespace().list().getItems()
    );
  }

  public void deleteCustomResource(String group, String version, String plural, String name) {
    kubernetesClient.genericKubernetesResources(clusterContext(group, version, plural)).withName(name).delete();
  }

  public void deleteNamespacedCustomResource(String group, String version, String namespace, String plural, String name) {
    kubernetesClient.genericKubernetesResources(namespacedContext(group, version, plural))
      .inNamespace(namespace).withName(name).delete();
  }

  public GenericKubernetesResource replaceCustomResource(
    String group, String version, String plural, String name, GenericKubernetesResource resource) {

    return kubernetesClient.genericKubernetesResources(clusterContext(group, version, plural))
      .resource(new GenericKubernetesResourceBuilder(resource).editOrNewMetadata().withName(name).endMetadata().build())
      .update();
  }

  public GenericKubernetesResource replaceNamespacedCustomResource(
    String group, String version, String namespace, String plural, String name, GenericKubernetesResource resource) {

    return kubernetesClient.genericKubernetesResources(namespacedContext(group, version, plural)).inNamespace(namespace)
      .resource(new GenericKubernetesResourceBuilder(resource).editOrNewMetadata().withName(name).endMetadata().build())
      .update();
  }

  private static ResourceDefinitionContext clusterContext(String group, String version, String plural) {
    return new ResourceDefinitionContext.Builder()
      .withNamespaced(false).withGroup(group).withVersion(version).withPlural(plural).build();
  }

  private static ResourceDefinitionContext namespacedContext(String group, String version, String plural) {
    return new ResourceDefinitionContext.Builder()
      .withNamespaced(true).withGroup(group).withVersion(version).withPlural(plural).build();
  }
}
