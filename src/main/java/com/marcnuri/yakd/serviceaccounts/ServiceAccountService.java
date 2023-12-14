/*
 * Copyright 2023 Marc Nuri
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
 * Created on 2023-12-14, 13:15
 */
package com.marcnuri.yakd.serviceaccounts;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class ServiceAccountService implements Watchable<ServiceAccount> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ServiceAccountService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<ServiceAccount> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.serviceAccounts().inAnyNamespace().list(LIMIT_1);
        return subscriber(kubernetesClient.serviceAccounts().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.serviceAccounts().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.serviceAccounts().inNamespace(namespace).withName(name).delete();
  }

  public ServiceAccount update(String name, String namespace, ServiceAccount serviceAccount) {
    return kubernetesClient.serviceAccounts().inNamespace(namespace)
      .resource(new ServiceAccountBuilder(serviceAccount).editMetadata().withName(name).endMetadata().build()).update();
  }
}
