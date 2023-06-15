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
package com.marcnuri.yakd.customresourcedefinitions;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class CustomResourceDefinitionService implements Watchable<CustomResourceDefinition> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public CustomResourceDefinitionService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<CustomResourceDefinition> watch() {
    return subscriber(kubernetesClient.apiextensions().v1().customResourceDefinitions());
  }

  public void delete(String name) {
    kubernetesClient.apiextensions().v1().customResourceDefinitions().withName(name).delete();
  }

  public CustomResourceDefinition update(String name, CustomResourceDefinition customResourceDefinition) {
    return kubernetesClient.apiextensions().v1().customResourceDefinitions().resource(
        new CustomResourceDefinitionBuilder(customResourceDefinition).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
