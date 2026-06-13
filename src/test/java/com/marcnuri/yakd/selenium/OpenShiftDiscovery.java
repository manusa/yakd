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

import io.fabric8.kubernetes.api.model.APIGroupBuilder;
import io.fabric8.kubernetes.api.model.APIGroupListBuilder;
import io.fabric8.kubernetes.api.model.APIResourceListBuilder;
import io.quarkus.test.kubernetes.client.KubernetesServer;

/**
 * Seeds the discovery responses that flip the application into OpenShift mode and let it subscribe
 * the Route/DeploymentConfig watchers. Because these expectations are always-on and change app-wide
 * behavior, ITs that use them must run on the dedicated {@link OpenShiftIntegrationTestProfile} so
 * they cannot leak into vanilla-Kubernetes classes sharing the default profile.
 */
public final class OpenShiftDiscovery {

  private OpenShiftDiscovery() {
  }

  public static void advertise(KubernetesServer kubernetes) {
    // The frontend flips to OpenShift mode when the backend reports an *.openshift.io API group
    // (served from the cluster's /apis discovery).
    kubernetes.expect().get().withPath("/apis").andReturn(200, new APIGroupListBuilder()
        .addToGroups(new APIGroupBuilder().withName("route.openshift.io").build())
        .addToGroups(new APIGroupBuilder().withName("apps.openshift.io").build())
        .build())
      .always();
    // The backend only subscribes its Route/DeploymentConfig watchers when the cluster's version
    // discovery reports the resource as supported.
    kubernetes.expect().get().withPath("/apis/route.openshift.io/v1").andReturn(200, new APIResourceListBuilder()
        .withGroupVersion("route.openshift.io/v1")
        .addNewResource().withName("routes").withKind("Route").withNamespaced(true).endResource()
        .build())
      .always();
    kubernetes.expect().get().withPath("/apis/apps.openshift.io/v1").andReturn(200, new APIResourceListBuilder()
        .withGroupVersion("apps.openshift.io/v1")
        .addNewResource().withName("deploymentconfigs").withKind("DeploymentConfig").withNamespaced(true).endResource()
        .build())
      .always();
  }
}
