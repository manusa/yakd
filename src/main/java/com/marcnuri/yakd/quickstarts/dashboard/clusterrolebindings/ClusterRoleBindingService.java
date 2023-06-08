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
 * Created on 2020-12-12, 9:06
 */
package com.marcnuri.yakd.quickstarts.dashboard.clusterrolebindings;

import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.fabric8.InformerOnSubscribe;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class ClusterRoleBindingService implements Watchable<ClusterRoleBinding> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ClusterRoleBindingService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
    return Optional.of(() -> kubernetesClient.supports(ClusterRoleBinding.class));
  }

  @Override
  public Observable<WatchEvent<ClusterRoleBinding>> watch() {
    return InformerOnSubscribe.observable(kubernetesClient.rbac().clusterRoleBindings()::inform);
  }

  @Override
  public boolean isRetrySubscription() {
    return false;
  }

  public void delete(String name) {
    kubernetesClient.rbac().clusterRoleBindings().withName(name).delete();
  }

  public ClusterRoleBinding update(String name, ClusterRoleBinding clusterRoleBinding) {
    return kubernetesClient.rbac().clusterRoleBindings()
      .resource(new ClusterRoleBindingBuilder(clusterRoleBinding).editMetadata().withName(name).endMetadata().build())
      .update();
  }

}
