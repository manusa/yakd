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
package com.marcnuri.yakc.quickstarts.dashboard.deployment;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api.ListDeploymentForAllNamespaces;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.Deployment;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.DeploymentSpec;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.PodTemplateSpec;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Singleton
public class DeploymentService implements Watchable<Deployment> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public DeploymentService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Deployment>> watch() throws IOException {
    final AppsV1Api apps = kubernetesClient.create(AppsV1Api.class);
    try {
      apps.listDeploymentForAllNamespaces(new ListDeploymentForAllNamespaces().limit(1)).get();
      return apps.listDeploymentForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return apps.listNamespacedDeployment(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public Status deleteDeployment(String name, String namespace) throws IOException {
    return kubernetesClient.create(AppsV1Api.class).deleteNamespacedDeployment(name, namespace).get();
  }

  public Deployment updateDeployment(String name, String namespace, Deployment deployment) throws IOException {
    return kubernetesClient.create(AppsV1Api.class).replaceNamespacedDeployment(name, namespace, deployment).get();
  }

  public Deployment restart(String name, String namespace) throws IOException {
    final Deployment toPatch = emptyDeployment();
    toPatch.getSpec().setTemplate(PodTemplateSpec.builder()
      .metadata(ObjectMeta.builder()
        .putInAnnotations("yakc.marcnuri.com/restartedAt", Instant.now().toString())
        .build())
      .build());
    return kubernetesClient.create(AppsV1Api.class)
      .patchNamespacedDeployment(name, namespace, toPatch)
      .get();
  }

  public Deployment updateReplicas(String name, String namespace, Integer replicas) throws IOException {
    final Deployment toPatch = emptyDeployment();
    toPatch.getSpec().setReplicas(replicas);
    return kubernetesClient.create(AppsV1Api.class).patchNamespacedDeployment(name, namespace,
      toPatch).get();
  }

  private static Deployment emptyDeployment() {
    final Deployment ret = new Deployment();
    ret.setSpec(new DeploymentSpec());
    return ret;
  }
}
