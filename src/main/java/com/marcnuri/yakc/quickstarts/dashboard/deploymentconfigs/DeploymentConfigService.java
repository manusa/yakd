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
package com.marcnuri.yakc.quickstarts.dashboard.deploymentconfigs;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.appsopenshiftio.v1.AppsOpenshiftIoV1Api;
import com.marcnuri.yakc.model.com.github.openshift.api.apps.v1.DeploymentConfig;
import com.marcnuri.yakc.model.com.github.openshift.api.apps.v1.DeploymentConfigSpec;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.PodTemplateSpec;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.ClientFunction;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.executeRaw;
import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class DeploymentConfigService implements Watchable<DeploymentConfig> {

  private final AppsOpenshiftIoV1Api apps;
  private final String namespace;

  @Inject
  public DeploymentConfigService(KubernetesClient kubernetesClient) {
    apps = kubernetesClient.create(AppsOpenshiftIoV1Api.class);
    namespace = kubernetesClient.getConfiguration().getNamespace();
  }

  private static DeploymentConfig emptyDeploymentConfig() {
    final DeploymentConfig ret = new DeploymentConfig();
    ret.setSpec(new DeploymentConfigSpec());
    return ret;
  }

  @Override
  public Optional<ClientFunction<?>> getAvailabilityCheckFunction() {
    return Optional.of(executeRaw(apps.getAPIResources()));
  }

  @Override
  public Observable<WatchEvent<DeploymentConfig>> watch() throws IOException {
    return tryWithFallback(
      () -> {
        apps.listDeploymentConfigForAllNamespaces(new AppsOpenshiftIoV1Api.ListDeploymentConfigForAllNamespaces().limit(1))
          .get();
        return apps.listDeploymentConfigForAllNamespaces().watch();
      },
      () -> apps.listNamespacedDeploymentConfig(namespace).watch()
    );
  }

  public Status delete(String name, String namespace) throws IOException {
    return apps.deleteNamespacedDeploymentConfig(name, namespace).get();
  }

  public DeploymentConfig restart(String name, String namespace) throws IOException {
    final DeploymentConfig toPatch = emptyDeploymentConfig();
    toPatch.getSpec().setTemplate(PodTemplateSpec.builder()
      .metadata(ObjectMeta.builder()
        .putInAnnotations("yakc.marcnuri.com/restartedAt", Instant.now().toString())
        .build())
      .build());
    return apps.patchNamespacedDeploymentConfig(name, namespace, toPatch)
      .get();
  }

  public DeploymentConfig update(String name, String namespace, DeploymentConfig deploymentConfig) throws IOException {
    return apps.replaceNamespacedDeploymentConfig(name, namespace, deploymentConfig).get();
  }

  public DeploymentConfig updateReplicas(String name, String namespace, Integer replicas) throws IOException {
    final DeploymentConfig toPatch = emptyDeploymentConfig();
    toPatch.getSpec().setReplicas(replicas);
    return apps.patchNamespacedDeploymentConfig(name, namespace,
      toPatch).get();
  }
}
