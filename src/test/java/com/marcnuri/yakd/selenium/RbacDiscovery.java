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
 */
package com.marcnuri.yakd.selenium;

import io.fabric8.kubernetes.api.model.APIResourceListBuilder;
import io.quarkus.test.kubernetes.client.KubernetesServer;

/**
 * Seeds the discovery response the application needs before it will subscribe to the cluster-scoped
 * RBAC watchers (ClusterRole, ClusterRoleBinding). Those watchers only subscribe when the cluster's
 * discovery reports the resource as supported, and the CRUD mock does not advertise them by default.
 *
 * <p>Because this expectation is always-on and the mock server outlives a test class, the RBAC ITs
 * run on the dedicated {@link RbacIntegrationTestProfile} so it cannot leak into vanilla-mode classes.
 */
public final class RbacDiscovery {

  private RbacDiscovery() {
  }

  public static void advertise(KubernetesServer kubernetes) {
    kubernetes.expect().get().withPath("/apis/rbac.authorization.k8s.io/v1")
      .andReturn(200, new APIResourceListBuilder()
        .withGroupVersion("rbac.authorization.k8s.io/v1")
        .addNewResource().withName("clusterroles").withKind("ClusterRole").withNamespaced(false).endResource()
        .addNewResource().withName("clusterrolebindings").withKind("ClusterRoleBinding").withNamespaced(false).endResource()
        .addNewResource().withName("roles").withKind("Role").withNamespaced(true).endResource()
        .addNewResource().withName("rolebindings").withKind("RoleBinding").withNamespaced(true).endResource()
        .build())
      .always();
  }
}
