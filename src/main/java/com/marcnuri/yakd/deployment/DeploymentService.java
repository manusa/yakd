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
 * Created on 2020-09-12, 7:34
 */
package com.marcnuri.yakd.deployment;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.WatchEvent;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DeploymentService implements Watchable<Deployment> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public DeploymentService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Multi<WatchEvent<Deployment>> watch() {
    return ClientUtil.tryInOrder(
      () -> {
        kubernetesClient.apps().deployments().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return ClientUtil.toMulti(kubernetesClient.apps().deployments().inAnyNamespace());
      },
      () -> ClientUtil.toMulti(kubernetesClient.apps().deployments()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void deleteDeployment(String name, String namespace) {
    kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).delete();
  }

  public Deployment updateDeployment(String name, String namespace, Deployment deployment) {
    return kubernetesClient.apps().deployments().inNamespace(namespace)
      .resource(new DeploymentBuilder(deployment).editMetadata().withName(name).endMetadata().build()).update();
  }

  public Deployment restart(String name, String namespace) {
    return kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).rolling().restart();
  }

  public Deployment updateReplicas(String name, String namespace, Integer replicas) {
    final Deployment toPatch = new DeploymentBuilder().withNewSpec().withReplicas(replicas).endSpec().build();
    return kubernetesClient.apps().deployments().inNamespace(namespace).withName(name)
      .patch(PatchContext.of(PatchType.JSON_MERGE), toPatch);
  }
}
