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
 * Created on 2020-09-06, 8:36
 */
package com.marcnuri.yakd.quickstarts.dashboard.replicaset;


import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class ReplicaSetService implements Watchable<ReplicaSet> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ReplicaSetService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<ReplicaSet>> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.apps().replicaSets().inAnyNamespace().list(LIMIT_1);
        return observable(kubernetesClient.apps().replicaSets().inAnyNamespace());
      },
      () -> observable(kubernetesClient.apps().replicaSets()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void deleteReplicaSet(String name, String namespace) {
    kubernetesClient.apps().replicaSets().inNamespace(namespace).withName(name).delete();
  }

}
