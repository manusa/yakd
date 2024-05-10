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
 * Created on 2024-05-09, 19:30
 */
package com.marcnuri.yakd.rolebindings;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class RoleBindingService implements Watchable<RoleBinding> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public RoleBindingService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<RoleBinding> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.rbac().roleBindings().inAnyNamespace().list(LIMIT_1);
        return subscriber(kubernetesClient.rbac().roleBindings().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.rbac().roleBindings().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.rbac().roleBindings().inNamespace(namespace).withName(name).delete();
  }

  public RoleBinding update(String name, String namespace, RoleBinding roleBinding) {
    return kubernetesClient.rbac().roleBindings().inNamespace(namespace)
      .resource(new RoleBindingBuilder(roleBinding).editMetadata().withName(name).endMetadata().build()).update();
  }
}
