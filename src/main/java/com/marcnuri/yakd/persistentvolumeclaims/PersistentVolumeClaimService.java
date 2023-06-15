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
package com.marcnuri.yakd.persistentvolumeclaims;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class PersistentVolumeClaimService implements Watchable<PersistentVolumeClaim> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public PersistentVolumeClaimService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<PersistentVolumeClaim> get() {
    return tryInOrder(
      () -> kubernetesClient.persistentVolumeClaims().inAnyNamespace().list().getItems(),
      () -> kubernetesClient.persistentVolumeClaims()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()).list().getItems()
    );
  }

  @Override
  public Subscriber<PersistentVolumeClaim> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.persistentVolumeClaims().inAnyNamespace().list(LIMIT_1);
        return subscriber(kubernetesClient.persistentVolumeClaims().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.persistentVolumeClaims()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.persistentVolumeClaims().inNamespace(namespace).withName(name).delete();
  }

  public PersistentVolumeClaim update(String name, String namespace, PersistentVolumeClaim persistentVolumeClaim) {
    return kubernetesClient.persistentVolumeClaims().inNamespace(namespace)
      .resource(new PersistentVolumeClaimBuilder(persistentVolumeClaim).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
