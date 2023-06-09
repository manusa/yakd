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
package com.marcnuri.yakd.service;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class ServiceService implements Watchable<Service> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ServiceService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<Service> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.services().inAnyNamespace().list(LIMIT_1);
        return subscriber(kubernetesClient.services().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.services().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public List<Service> get() {
    return tryInOrder(
      () -> kubernetesClient.services().inAnyNamespace().list().getItems(),
      () -> kubernetesClient.services().inNamespace(kubernetesClient.getConfiguration().getNamespace())
        .list().getItems()
    );
  }

  public void deleteService(String name, String namespace) {
    kubernetesClient.services().inNamespace(namespace).withName(name).delete();
  }

  public Service updateService(String name, String namespace, Service service) {
    return kubernetesClient.services().inNamespace(namespace)
      .resource(new ServiceBuilder(service).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
