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
 * Created on 2020-11-08, 9:48
 */
package com.marcnuri.yakd.quickstarts.dashboard.roles;

import com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil;
import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.toMulti;

@Singleton
public class RoleService implements Watchable<Role> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public RoleService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Multi<WatchEvent<Role>> watch() {
    return ClientUtil.tryInOrder(
      () -> {
        kubernetesClient.rbac().roles().inAnyNamespace().list(LIMIT_1);
        return toMulti(kubernetesClient.rbac().roles().inAnyNamespace());
      },
      () -> toMulti(kubernetesClient.rbac().roles().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.rbac().roles().inNamespace(namespace).withName(name).delete();
  }

  public Role update(String name, String namespace, Role role) {
    return kubernetesClient.rbac().roles().inNamespace(namespace)
      .resource(new RoleBuilder(role).editMetadata().withName(name).endMetadata().build()).update();
  }

}
