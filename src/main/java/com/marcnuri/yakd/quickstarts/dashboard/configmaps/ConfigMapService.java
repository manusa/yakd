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
package com.marcnuri.yakd.quickstarts.dashboard.configmaps;

import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.fabric8.InformerOnSubscribe;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.reactivex.Observable;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static com.marcnuri.yakd.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class ConfigMapService implements Watchable<ConfigMap> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ConfigMapService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<ConfigMap> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.configMaps().inAnyNamespace().list().getItems(),
      () -> kubernetesClient.configMaps().inNamespace(kubernetesClient.getConfiguration().getNamespace()).list().getItems()
    );
  }

  @Override
  public Observable<WatchEvent<ConfigMap>> watch() {
    try {
      kubernetesClient.configMaps().inAnyNamespace()
        .list(new ListOptionsBuilder().withLimit(1L).build());
      return InformerOnSubscribe.observable(kubernetesClient.configMaps().inAnyNamespace()::inform);
    } catch (KubernetesClientException ex) {
      return InformerOnSubscribe.observable(kubernetesClient.configMaps().inNamespace(kubernetesClient.getConfiguration().getNamespace())::inform);
    }
  }

  public void deleteConfigMap(String name, String namespace) {
    kubernetesClient.configMaps().inNamespace(namespace).withName(name).delete();
  }

  public ConfigMap updateConfigMap(String name, String namespace, ConfigMap configMap) {
    return kubernetesClient.configMaps().inNamespace(namespace)
      .resource(new ConfigMapBuilder(configMap).editMetadata().withName(name).endMetadata().build()).update();
  }
}
