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
 * Created on 2020-10-11, 8:32
 */
package com.marcnuri.yakc.quickstarts.dashboard.namespaces;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.Namespace;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.justWithNoComplete;
import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class NamespaceService implements Watchable<Namespace> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public NamespaceService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Namespace>> watch() throws IOException {
    final CoreV1Api core = kubernetesClient.create(CoreV1Api.class);
    final String configNamespace = kubernetesClient.getConfiguration().getNamespace();
    return tryWithFallback(
      () -> {
        core.listNamespace(new CoreV1Api.ListNamespace().limit(1)).get();
        return core.listNamespace().watch();
      },
      () -> {
        if (configNamespace != null) {
          core.listNamespace(new CoreV1Api.ListNamespace().limit(1).fieldSelector("metadata.name=" + configNamespace)).get();
          return core.listNamespace(new CoreV1Api.ListNamespace().fieldSelector("metadata.name=" + configNamespace)).watch();
        }
        return Observable.empty();
      },
      () -> {
        if (configNamespace != null) {
          return justWithNoComplete(new WatchEvent<>(WatchEvent.Type.ADDED, core.readNamespace(configNamespace).get()));
        }
        return Observable.empty();
      }
    );
  }

  public List<Namespace> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(CoreV1Api.class).listNamespace().get().getItems(),
      () -> {
        final String configNamespace = kubernetesClient.getConfiguration().getNamespace();
        if (configNamespace != null) {
          return Collections.singletonList(kubernetesClient.create(CoreV1Api.class).readNamespace(configNamespace).get());
        }
        return Collections.emptyList();
      }
    );
  }

  public Namespace deleteNamespace(String name) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).deleteNamespace(name).get(Namespace.class);
  }
}
