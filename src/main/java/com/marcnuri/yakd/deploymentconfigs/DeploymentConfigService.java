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
 * Created on 2020-11-15, 16:59
 */
package com.marcnuri.yakd.deploymentconfigs;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.WatchEvent;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class DeploymentConfigService implements Watchable<DeploymentConfig> {

  private final OpenShiftClient openShiftClient;

  @Inject
  public DeploymentConfigService(KubernetesClient kubernetesClient) {
    this.openShiftClient = kubernetesClient.adapt(OpenShiftClient.class);
  }

  @Override
  public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
    return Optional.of(() -> openShiftClient.supports(DeploymentConfig.class));
  }

  @Override
  public Multi<WatchEvent<DeploymentConfig>> watch() {
    return ClientUtil.tryInOrder(
      () -> {
        openShiftClient.deploymentConfigs().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return ClientUtil.toMulti(openShiftClient.deploymentConfigs().inAnyNamespace());
      },
      () -> ClientUtil.toMulti(openShiftClient.deploymentConfigs().inNamespace(openShiftClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    openShiftClient.deploymentConfigs().inNamespace(namespace).withName(name).delete();
  }

  public DeploymentConfig restart(String name, String namespace) {
    final var toPatch = new DeploymentConfigBuilder()
      .withNewSpec().withNewTemplate()
      .withNewMetadata().addToAnnotations("yakd.marcnuri.com/restartedAt", Instant.now().toString()).endMetadata()
      .endTemplate()
      .endSpec()
      .build();
    // TODO might be removable after Kubernetes Client 6.7.0
    // n.b. ensure triggers are not removed (empty list)
    toPatch.getSpec().setTriggers(openShiftClient.deploymentConfigs().inNamespace(namespace).withName(name).get().getSpec().getTriggers());
    return openShiftClient.deploymentConfigs().inNamespace(namespace).withName(name)
      .patch(PatchContext.of(PatchType.JSON_MERGE), toPatch);
  }

  public DeploymentConfig update(String name, String namespace, DeploymentConfig deploymentConfig) {
    return openShiftClient.deploymentConfigs().inNamespace(namespace)
      .resource(new DeploymentConfigBuilder(deploymentConfig).editMetadata().withName(name).endMetadata().build()).update();
  }

  public DeploymentConfig updateReplicas(String name, String namespace, Integer replicas) {
    final DeploymentConfig toPatch = new DeploymentConfigBuilder().withNewSpec().withReplicas(replicas).endSpec().build();
    // TODO might be removable after Kubernetes Client 6.7.0
    // n.b. ensure triggers are not removed (empty list)
    toPatch.getSpec().setTriggers(openShiftClient.deploymentConfigs().inNamespace(namespace).withName(name).get().getSpec().getTriggers());
    return openShiftClient.deploymentConfigs().inNamespace(namespace).withName(name)
      .patch(PatchContext.of(PatchType.JSON_MERGE), toPatch);
  }
}
