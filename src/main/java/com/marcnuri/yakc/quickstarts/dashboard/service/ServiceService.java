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
 * Created on 2020-09-23, 19:18
 */
package com.marcnuri.yakc.quickstarts.dashboard.service;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ListNamespacedService;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ListServiceForAllNamespaces;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.Service;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.executeRaw;
import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class ServiceService implements Watchable<Service> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ServiceService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Service>> watch() throws IOException {
    final CoreV1Api api = kubernetesClient.create(CoreV1Api.class);
    return tryWithFallback(
      () -> {
        api.listServiceForAllNamespaces(new ListServiceForAllNamespaces().limit(1)).get();
        return api.listServiceForAllNamespaces().watch();
      },
      () -> {
        final String ns = kubernetesClient.getConfiguration().getNamespace();
        api.listNamespacedService(ns, new ListNamespacedService().limit(1)).get();
        return api.listNamespacedService(ns).watch();
      }
    );
  }

  public List<Service> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(CoreV1Api.class).listServiceForAllNamespaces().get().getItems(),
      () -> kubernetesClient.create(CoreV1Api.class)
        .listNamespacedService(kubernetesClient.getConfiguration().getNamespace()).get().getItems()
    );
  }

  public void deleteService(String name, String namespace) throws IOException {
    executeRaw(kubernetesClient.create(CoreV1Api.class).deleteNamespacedService(name, namespace)).call();
  }

  public Service updateService(String name, String namespace, Service service) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNamespacedService(name, namespace, service).get();
  }
}
