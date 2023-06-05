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
package com.marcnuri.yakc.quickstarts.dashboard.replicationcontrollers;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.ReplicationController;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.executeRaw;

@Singleton
public class ReplicationControllerService implements Watchable<ReplicationController> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ReplicationControllerService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<ReplicationController>> watch() throws IOException {
    final CoreV1Api core = kubernetesClient.create(CoreV1Api.class);
    try {
      core.listReplicationControllerForAllNamespaces(new CoreV1Api.ListReplicationControllerForAllNamespaces().limit(1)).get();
      return core.listReplicationControllerForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return core.listNamespacedReplicationController(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public void delete(String name, String namespace) throws IOException {
    executeRaw(kubernetesClient.create(CoreV1Api.class).deleteNamespacedReplicationController(name, namespace)).call();
  }

  public ReplicationController update(String name, String namespace, ReplicationController replicationController) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNamespacedReplicationController(name, namespace, replicationController).get();
  }
}
