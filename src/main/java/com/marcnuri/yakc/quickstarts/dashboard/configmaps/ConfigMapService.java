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
 * Created on 2020-10-19, 19:02
 */
package com.marcnuri.yakc.quickstarts.dashboard.configmaps;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ListConfigMapForAllNamespaces;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.ConfigMap;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class ConfigMapService implements Watchable<ConfigMap> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ConfigMapService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<ConfigMap> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(CoreV1Api.class).listConfigMapForAllNamespaces().get().getItems(),
      () -> kubernetesClient.create(CoreV1Api.class)
        .listNamespacedConfigMap(kubernetesClient.getConfiguration().getNamespace()).get().getItems()
    );
  }

  @Override
  public Observable<WatchEvent<ConfigMap>> watch() throws IOException {
    final CoreV1Api core = kubernetesClient.create(CoreV1Api.class);
    try {
      core.listConfigMapForAllNamespaces(new ListConfigMapForAllNamespaces().limit(1)).get();
      return core.listConfigMapForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return core.listNamespacedConfigMap(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public Status deleteConfigMap(String name, String namespace) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).deleteNamespacedConfigMap(name, namespace).get();
  }

  public ConfigMap updateConfigMap(String name, String namespace, ConfigMap configMap) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNamespacedConfigMap(name, namespace, configMap).get();
  }
}
