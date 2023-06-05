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
package com.marcnuri.yakc.quickstarts.dashboard.clusterroles;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.rbacauthorization.v1.RbacAuthorizationV1Api;
import com.marcnuri.yakc.api.rbacauthorization.v1.RbacAuthorizationV1Api.ListClusterRole;
import com.marcnuri.yakc.api.rbacauthorization.v1beta1.RbacAuthorizationV1beta1Api;
import com.marcnuri.yakc.model.io.k8s.api.rbac.v1.AggregationRule;
import com.marcnuri.yakc.model.io.k8s.api.rbac.v1.ClusterRole;
import com.marcnuri.yakc.model.io.k8s.api.rbac.v1.PolicyRule;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class ClusterRoleService implements Watchable<ClusterRole> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public ClusterRoleService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<ClusterRole>> watch() throws IOException {
    return tryWithFallback(
      () -> {
        kubernetesClient.create(RbacAuthorizationV1Api.class).listClusterRole(new ListClusterRole().limit(1)).get();
        return kubernetesClient.create(RbacAuthorizationV1Api.class).listClusterRole().watch();
      },
      () -> {
        kubernetesClient.create(RbacAuthorizationV1beta1Api.class)
          .listClusterRole(new RbacAuthorizationV1beta1Api.ListClusterRole().limit(1)).get();
        return kubernetesClient.create(RbacAuthorizationV1beta1Api.class).listClusterRole().watch()
          .map(ClusterRoleService::to);
      }
    );
  }

  public List<ClusterRole> get() throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(RbacAuthorizationV1Api.class).listClusterRole().get().getItems(),
      () -> kubernetesClient.create(RbacAuthorizationV1beta1Api.class).listClusterRole().stream()
        .map(ClusterRoleService::to).collect(Collectors.toList())
    );
  }

  public Status delete(String name) throws IOException {
    return kubernetesClient.create(RbacAuthorizationV1Api.class).deleteClusterRole(name).get();
  }

  public ClusterRole update(String name, ClusterRole clusterRole) throws IOException {
    return kubernetesClient.create(RbacAuthorizationV1Api.class).replaceClusterRole(name, clusterRole).get();
  }

  static WatchEvent<ClusterRole> to(WatchEvent<com.marcnuri.yakc.model.io.k8s.api.rbac.v1beta1.ClusterRole> from) {
    return new WatchEvent<>(from.getType(), to(from.getObject()));
  }

  static ClusterRole to(com.marcnuri.yakc.model.io.k8s.api.rbac.v1beta1.ClusterRole from) {
    return ClusterRole.builder()
      .apiVersion(from.getApiVersion())
      .kind(from.getKind())
      .metadata(from.getMetadata())
      .aggregationRule(to(from.getAggregationRule()))
      .rules(from.getRules().stream().map(ClusterRoleService::to).collect(Collectors.toList()))
      .build();
  }

  static AggregationRule to(com.marcnuri.yakc.model.io.k8s.api.rbac.v1beta1.AggregationRule from) {
    return AggregationRule.builder()
      .clusterRoleSelectors(from.getClusterRoleSelectors())
      .build();
  }

  public static PolicyRule to(com.marcnuri.yakc.model.io.k8s.api.rbac.v1beta1.PolicyRule from) {
    return PolicyRule.builder()
      .apiGroups(from.getApiGroups())
      .nonResourceURLs(from.getNonResourceURLs())
      .resourceNames(from.getResourceNames())
      .resources(from.getResources())
      .verbs(from.getVerbs())
    .build();
  }
}
