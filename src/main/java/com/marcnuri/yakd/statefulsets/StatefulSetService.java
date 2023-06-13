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
 * Created on 2020-10-25, 16:48
 */
package com.marcnuri.yakd.statefulsets;

import com.marcnuri.yakd.watch.WatchEvent;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.toMulti;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;

@Singleton
public class StatefulSetService implements Watchable<StatefulSet> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public StatefulSetService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Multi<WatchEvent<StatefulSet>> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.apps().statefulSets().inAnyNamespace().list(LIMIT_1);
        return toMulti(kubernetesClient.apps().statefulSets().inAnyNamespace());
      },
      () -> toMulti(kubernetesClient.apps().statefulSets().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void deleteStatefulSet(String name, String namespace) {
    kubernetesClient.apps().statefulSets().inNamespace(namespace).withName(name).delete();
  }

  public StatefulSet updateStatefulSet(String name, String namespace, StatefulSet statefulset) {
    return kubernetesClient.apps().statefulSets().inNamespace(namespace)
      .resource(new StatefulSetBuilder(statefulset).editMetadata().withName(name).endMetadata().build())
      .update();
  }

  public StatefulSet restart(String name, String namespace) {
    return kubernetesClient.apps().statefulSets().inNamespace(namespace).withName(name).rolling().restart();
  }

  public StatefulSet updateReplicas(String name, String namespace, Integer replicas) {
    final StatefulSet toPatch = new StatefulSetBuilder().withNewSpec().withReplicas(replicas).endSpec().build();
    return kubernetesClient.apps().statefulSets().inNamespace(namespace).withName(name)
      .patch(PatchContext.of(PatchType.JSON_MERGE), toPatch);
  }
}
