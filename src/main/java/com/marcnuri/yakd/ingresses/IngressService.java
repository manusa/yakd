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
package com.marcnuri.yakd.ingresses;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class IngressService implements Watchable<Ingress> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public IngressService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<Ingress> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.network().v1().ingresses().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return subscriber(kubernetesClient.network().v1().ingresses().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.network().v1().ingresses()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public List<Ingress> get() {
    final String namespace = kubernetesClient.getConfiguration().getNamespace();
    return tryInOrder(
      () -> kubernetesClient.network().v1().ingresses().inAnyNamespace().list().getItems(),
      () -> kubernetesClient.network().v1().ingresses().inNamespace(namespace).list().getItems()
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(name).delete();
  }

  public Ingress updateIngress(String name, String namespace, Ingress ingress) {
    return kubernetesClient.network().v1().ingresses().inNamespace(namespace)
      .resource(new IngressBuilder(ingress).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
