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
package com.marcnuri.yakd.namespaces;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;
import static com.marcnuri.yakd.watch.Subscriber.empty;
import static com.marcnuri.yakd.watch.Subscriber.items;

@Singleton
public class NamespaceService implements Watchable<Namespace> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public NamespaceService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<Namespace> watch() {
    final var configNamespace = kubernetesClient.getConfiguration().getNamespace();
    return tryInOrder(
      () -> {
        kubernetesClient.namespaces().list(LIMIT_1);
        return subscriber(kubernetesClient.namespaces());
      },
      () -> {
        if (configNamespace != null) {
          kubernetesClient.namespaces().withField("metadata.name", configNamespace).list(LIMIT_1);
          return subscriber(kubernetesClient.namespaces().withField("metadata.name", configNamespace));
        }
        return empty();
      },
      () -> {
        if (configNamespace != null) {
          return items(kubernetesClient.namespaces().withName(configNamespace).get());
        }
        return empty();
      }
    );
  }

  public List<Namespace> get() {
    return tryInOrder(
      () -> kubernetesClient.namespaces().list().getItems(),
      () -> {
        final var configNamespace = kubernetesClient.getConfiguration().getNamespace();
        if (configNamespace != null) {
          return Collections.singletonList(kubernetesClient.namespaces().withName(configNamespace).get());
        }
        return Collections.emptyList();
      }
    );
  }

  public void deleteNamespace(String name) {
    kubernetesClient.namespaces().withName(name).delete();
  }
}
