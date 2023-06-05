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
package com.marcnuri.yakc.quickstarts.dashboard.daemonsets;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.DaemonSet;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.DaemonSetSpec;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.PodTemplateSpec;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Instant;

@Singleton
public class DaemonSetService implements Watchable<DaemonSet> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public DaemonSetService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<DaemonSet>> watch() throws IOException {
    final AppsV1Api apps = kubernetesClient.create(AppsV1Api.class);
    try {
      apps.listDaemonSetForAllNamespaces(new AppsV1Api.ListDaemonSetForAllNamespaces().limit(1)).get();
      return apps.listDaemonSetForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return apps.listNamespacedDaemonSet(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public Status delete(String name, String namespace) throws IOException {
    return kubernetesClient.create(AppsV1Api.class).deleteNamespacedDaemonSet(name, namespace).get();
  }

  public DaemonSet update(String name, String namespace, DaemonSet daemonSet) throws IOException {
    return kubernetesClient.create(AppsV1Api.class).replaceNamespacedDaemonSet(name, namespace, daemonSet).get();
  }

  public DaemonSet restart(String name, String namespace) throws IOException {
    final DaemonSet toPatch = emptyDaemonSet();
    toPatch.getSpec().setTemplate(PodTemplateSpec.builder()
      .metadata(ObjectMeta.builder()
        .putInAnnotations("yakc.marcnuri.com/restartedAt", Instant.now().toString())
        .build())
      .build());
    return kubernetesClient.create(AppsV1Api.class)
      .patchNamespacedDaemonSet(name, namespace, toPatch)
      .get();
  }

  private static DaemonSet emptyDaemonSet() {
    final DaemonSet ret = new DaemonSet();
    ret.setSpec(new DaemonSetSpec());
    return ret;
  }
}
