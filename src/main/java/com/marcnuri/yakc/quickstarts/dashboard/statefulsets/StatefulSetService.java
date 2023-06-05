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
package com.marcnuri.yakc.quickstarts.dashboard.statefulsets;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api;
import com.marcnuri.yakc.api.apps.v1.AppsV1Api.ListStatefulSetForAllNamespaces;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.StatefulSet;
import com.marcnuri.yakc.model.io.k8s.api.apps.v1.StatefulSetSpec;
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
public class StatefulSetService implements Watchable<StatefulSet> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public StatefulSetService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<StatefulSet>> watch() throws IOException {
    final AppsV1Api apps = kubernetesClient.create(AppsV1Api.class);
    try {
      apps.listStatefulSetForAllNamespaces(new ListStatefulSetForAllNamespaces().limit(1)).get();
      return apps.listStatefulSetForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return apps.listNamespacedStatefulSet(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public Status deleteStatefulSet(String name, String namespace) throws IOException {
    return kubernetesClient.create(AppsV1Api.class).deleteNamespacedStatefulSet(name, namespace).get();
  }

  public StatefulSet updateStatefulSet(String name, String namespace, StatefulSet statefulset) throws IOException {
    return kubernetesClient.create(AppsV1Api.class).replaceNamespacedStatefulSet(name, namespace, statefulset).get();
  }

  public StatefulSet restart(String name, String namespace) throws IOException {
    final StatefulSet toPatch = emptyStatefulSet();
    toPatch.getSpec().setTemplate(PodTemplateSpec.builder()
      .metadata(ObjectMeta.builder()
        .putInAnnotations("yakc.marcnuri.com/restartedAt", Instant.now().toString())
        .build())
      .build());
    return kubernetesClient.create(AppsV1Api.class)
      .patchNamespacedStatefulSet(name, namespace, toPatch)
      .get();
  }

  public StatefulSet updateReplicas(String name, String namespace, Integer replicas) throws IOException {
    final StatefulSet toPatch = emptyStatefulSet();
    toPatch.getSpec().setReplicas(replicas);
    return kubernetesClient.create(AppsV1Api.class)
      .patchNamespacedStatefulSet(name, namespace, toPatch).get();
  }

  private static StatefulSet emptyStatefulSet() {
    final StatefulSet ret = new StatefulSet();
    ret.setSpec(new StatefulSetSpec());
    return ret;
  }
}
