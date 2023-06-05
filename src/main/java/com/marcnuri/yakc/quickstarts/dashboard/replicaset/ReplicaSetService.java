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
package com.marcnuri.yakc.quickstarts.dashboard.replicaset;


import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api.ListReplicaSetForAllNamespaces;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.ReplicaSet;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.executeRaw;

@Singleton
public class ReplicaSetService implements Watchable<ReplicaSet> {

  private final AppsV1Api apps;
  private final String namespace;

  @Inject
  public ReplicaSetService(KubernetesClient kubernetesClient) {
    apps = kubernetesClient.create(AppsV1Api.class);
    namespace = kubernetesClient.getConfiguration().getNamespace();
  }

  @Override
  public Observable<WatchEvent<ReplicaSet>> watch() throws IOException {
    try {
      apps.listReplicaSetForAllNamespaces(new ListReplicaSetForAllNamespaces().limit(1)).get();
      return apps.listReplicaSetForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return apps.listNamespacedReplicaSet(namespace).watch();
    }
  }

  public void deleteReplicaSet(String name, String namespace) throws IOException {
    executeRaw(apps.deleteNamespacedReplicaSet(name, namespace)).call();
  }

}
