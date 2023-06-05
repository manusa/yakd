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
 * Created on 2020-10-25, 7:22
 */
package com.marcnuri.yakc.quickstarts.dashboard.secrets;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ListNamespacedSecret;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ListSecretForAllNamespaces;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.Secret;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class SecretService implements Watchable<Secret> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public SecretService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Secret>> watch() throws IOException {
    final CoreV1Api api = kubernetesClient.create(CoreV1Api.class);
    return tryWithFallback(
      () -> {
        api.listSecretForAllNamespaces(new ListSecretForAllNamespaces().limit(1)).get();
        return api.listSecretForAllNamespaces().watch();
      },
      () -> {
        final String ns = kubernetesClient.getConfiguration().getNamespace();
        api.listNamespacedSecret(ns, new ListNamespacedSecret().limit(1)).get();
        return api.listNamespacedSecret(ns).watch();
      }
    );
  }

  public List<Secret> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(CoreV1Api.class).listSecretForAllNamespaces().get().getItems(),
      () -> kubernetesClient.create(CoreV1Api.class)
        .listNamespacedSecret(kubernetesClient.getConfiguration().getNamespace()).get().getItems()
    );
  }

  public Status deleteSecret(String name, String namespace) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).deleteNamespacedSecret(name, namespace).get();
  }

  public Secret updateSecret(String name, String namespace, Secret secret) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNamespacedSecret(name, namespace, secret).get();
  }
}
