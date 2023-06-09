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
 * Created on 2020-12-05, 20:01
 */
package com.marcnuri.yakd.daemonsets;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.DaemonSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;

import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class DaemonSetService implements Watchable<DaemonSet> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public DaemonSetService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<DaemonSet> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.apps().daemonSets().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return subscriber(kubernetesClient.apps().daemonSets().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.apps().daemonSets().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.apps().daemonSets().inNamespace(namespace).withName(name).delete();
  }

  public DaemonSet update(String name, String namespace, DaemonSet daemonSet) {
    return kubernetesClient.apps().daemonSets().inNamespace(namespace)
      .resource(new DaemonSetBuilder(daemonSet).editMetadata().withName(name).endMetadata().build()).update();
  }

  public DaemonSet restart(String name, String namespace) {
    final var toPatch = new DaemonSetBuilder()
      .withNewSpec().withNewTemplate()
      .withNewMetadata().addToAnnotations("yakd.marcnuri.com/restartedAt", Instant.now().toString()).endMetadata()
      .endTemplate().endSpec()
      .build();
    return kubernetesClient.apps().daemonSets().inNamespace(namespace).withName(name)
      .patch(PatchContext.of(PatchType.JSON_MERGE), toPatch);
  }
}
