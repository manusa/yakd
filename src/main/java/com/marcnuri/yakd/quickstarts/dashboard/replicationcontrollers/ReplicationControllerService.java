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
 * Created on 2020-11-19, 19:27
 */
package com.marcnuri.yakd.quickstarts.dashboard.replicationcontrollers;

import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class ReplicationControllerService implements Watchable<ReplicationController> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ReplicationControllerService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<ReplicationController>> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.replicationControllers().inAnyNamespace().list(LIMIT_1);
        return observable(kubernetesClient.replicationControllers().inAnyNamespace());
      },
      () -> observable(kubernetesClient.replicationControllers()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.replicationControllers().inNamespace(namespace).withName(name).delete();
  }

  public ReplicationController update(String name, String namespace, ReplicationController replicationController) {
    return kubernetesClient.replicationControllers().inNamespace(namespace)
      .resource(new ReplicationControllerBuilder(replicationController).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
