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
package com.marcnuri.yakc.quickstarts.dashboard.roles;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.rbacauthorization.v1.RbacAuthorizationV1Api;
import com.marcnuri.yakc.api.rbacauthorization.v1.RbacAuthorizationV1Api.ListNamespacedRole;
import com.marcnuri.yakc.api.rbacauthorization.v1.RbacAuthorizationV1Api.ListRoleForAllNamespaces;
import com.marcnuri.yakc.api.rbacauthorization.v1beta1.RbacAuthorizationV1beta1Api;
import com.marcnuri.yakc.model.io.k8s.api.rbac.v1.Role;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.clusterroles.ClusterRoleService;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class RoleService implements Watchable<Role> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public RoleService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Role>> watch() throws IOException {
    final String ns = kubernetesClient.getConfiguration().getNamespace();
    return tryWithFallback(
      () -> {
        kubernetesClient.create(RbacAuthorizationV1Api.class)
          .listRoleForAllNamespaces(new ListRoleForAllNamespaces().limit(1)).get();
        return kubernetesClient.create(RbacAuthorizationV1Api.class).listRoleForAllNamespaces().watch();
      },
      () -> {
        kubernetesClient.create(RbacAuthorizationV1beta1Api.class)
          .listRoleForAllNamespaces(new RbacAuthorizationV1beta1Api.ListRoleForAllNamespaces().limit(1)).get();
        return kubernetesClient.create(RbacAuthorizationV1beta1Api.class).listRoleForAllNamespaces().watch()
          .map(RoleService::to);
      },
      () -> {
        kubernetesClient.create(RbacAuthorizationV1Api.class)
          .listNamespacedRole(ns, new ListNamespacedRole().limit(1)).get();
        return kubernetesClient.create(RbacAuthorizationV1Api.class).listNamespacedRole(ns).watch();
      },
      () -> {
        kubernetesClient.create(RbacAuthorizationV1beta1Api.class)
          .listNamespacedRole(ns, new RbacAuthorizationV1beta1Api.ListNamespacedRole().limit(1)).get();
        return kubernetesClient.create(RbacAuthorizationV1beta1Api.class).listNamespacedRole(ns).watch()
          .map(RoleService::to);
      }
    );
  }

  public Status delete(String name, String namespace) throws IOException {
    return kubernetesClient.create(RbacAuthorizationV1Api.class).deleteNamespacedRole(name, namespace).get();
  }

  public Role update(String name, String namespace, Role role) throws IOException {
    return kubernetesClient.create(RbacAuthorizationV1Api.class)
      .replaceNamespacedRole(name, namespace, role).get();
  }

  static WatchEvent<Role> to(WatchEvent<com.marcnuri.yakc.model.io.k8s.api.rbac.v1beta1.Role> from) {
    return new WatchEvent<>(from.getType(), to(from.getObject()));
  }

  static Role to(com.marcnuri.yakc.model.io.k8s.api.rbac.v1beta1.Role from) {
    return Role.builder()
      .apiVersion(from.getApiVersion())
      .kind(from.getKind())
      .metadata(from.getMetadata())
      .rules(from.getRules().stream().map(ClusterRoleService::to).collect(Collectors.toList()))
      .build();
  }

}
