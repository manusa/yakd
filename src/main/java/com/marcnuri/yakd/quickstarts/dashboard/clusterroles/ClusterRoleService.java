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
 * Created on 2020-11-07, 18:40
 */
package com.marcnuri.yakd.quickstarts.dashboard.clusterroles;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.toMulti;

@Singleton
public class ClusterRoleService implements Watchable<ClusterRole> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ClusterRoleService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
    return Optional.of(() -> kubernetesClient.supports(ClusterRole.class));
  }

  @Override
  public Multi<WatchEvent<ClusterRole>> watch() {
    return toMulti(kubernetesClient.rbac().clusterRoles());
  }

  public List<ClusterRole> get() {
    return kubernetesClient.rbac().clusterRoles().list().getItems();
  }

  public void delete(String name) {
    kubernetesClient.rbac().clusterRoles().withName(name).delete();
  }

  public ClusterRole update(String name, ClusterRole clusterRole) {
    return kubernetesClient.rbac().clusterRoles()
      .resource(new ClusterRoleBuilder(clusterRole).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
