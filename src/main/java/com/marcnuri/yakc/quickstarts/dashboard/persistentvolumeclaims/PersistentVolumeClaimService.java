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
 * Created on 2020-11-01, 16:38
 */
package com.marcnuri.yakc.quickstarts.dashboard.persistentvolumeclaims;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.PersistentVolumeClaim;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.List;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class PersistentVolumeClaimService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public PersistentVolumeClaimService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<PersistentVolumeClaim> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(CoreV1Api.class).listPersistentVolumeClaimForAllNamespaces().get().getItems(),
      () -> kubernetesClient.create(CoreV1Api.class)
        .listNamespacedPersistentVolumeClaim(kubernetesClient.getConfiguration().getNamespace()).get().getItems()
    );
  }

  public PersistentVolumeClaim delete(String name, String namespace) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).deleteNamespacedPersistentVolumeClaim(name, namespace).get();
  }

  public PersistentVolumeClaim update(String name, String namespace, PersistentVolumeClaim persistentVolumeClaim) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNamespacedPersistentVolumeClaim(name, namespace, persistentVolumeClaim).get();
  }
}
