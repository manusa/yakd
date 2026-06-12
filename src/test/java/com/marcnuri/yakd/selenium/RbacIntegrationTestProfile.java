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

import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;

/**
 * Same configuration as {@link IntegrationTestProfile}, but a distinct profile class on
 * purpose: the cluster-scoped RBAC watchers (ClusterRole, ClusterRoleBinding) only subscribe
 * when the cluster's discovery reports the resource as supported, so these tests seed an
 * {@code /apis/rbac.authorization.k8s.io/v1} discovery expectation. Quarkus only restarts the
 * application and its test resources (including the Kubernetes mock server) when the test
 * profile changes, so that always-on discovery expectation cannot leak into the vanilla-mode
 * classes sharing the default integration profile.
 */
@WithSelenium
@WithKubernetesTestServer
public class RbacIntegrationTestProfile extends IntegrationTestProfile {
}
