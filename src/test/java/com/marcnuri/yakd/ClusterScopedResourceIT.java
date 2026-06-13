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
package com.marcnuri.yakd;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import com.marcnuri.yakd.selenium.ResourceUi;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

// Two distinct resource types are exercised in one class, so this reuses the ResourceUi plumbing
// by composition rather than extending the single-type AbstractResourceIT.
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Cluster-scoped resources")
public class ClusterScopedResourceIT {

  @KubernetesTestServer
  KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;
  WebDriver driver;
  ResourceUi ui;

  @BeforeEach
  void openTab() {
    ui = ResourceUi.openTab(driver, url);
  }

  @AfterEach
  void closeTab() {
    ui.closeTab();
  }

  @Nested
  @DisplayName("when viewing the Node list and detail pages")
  class NodeListAndDetail {

    private static final String NODE_NAME = "cluster-it-node";
    private static final String KUBELET_VERSION = "v1.33.0-cluster-it";

    @BeforeEach
    void seed() {
      kubernetes.getClient().nodes().resource(node()).createOr(NonDeletingOperation::update);
    }

    @AfterEach
    void deleteNode() {
      kubernetes.getClient().nodes().withName(NODE_NAME).delete();
    }

    private static Node node() {
      return new NodeBuilder()
        .withNewMetadata()
          .withName(NODE_NAME)
          .addToLabels("node-role.kubernetes.io/control-plane", "")
        .endMetadata()
        .withNewStatus()
          .addToAllocatable("cpu", new Quantity("4"))
          .addToAllocatable("memory", new Quantity("16Gi"))
          .addToAllocatable("pods", new Quantity("110"))
          .addNewCondition().withType("Ready").withStatus("True").endCondition()
          .withNewNodeInfo()
            .withOperatingSystem("linux")
            .withArchitecture("arm64")
            .withKernelVersion("6.6.0")
            .withContainerRuntimeVersion("containerd://1.7.0")
            .withKubeletVersion(KUBELET_VERSION)
          .endNodeInfo()
        .endStatus()
        .build();
    }

    @Test
    @DisplayName("the list renders a row for the seeded node")
    void listRendersSeededRow() {
      ui.openList("nodes");

      ui.await(() -> ui.hasRow(NODE_NAME));
      assertThat(ui.hasRow(NODE_NAME))
        .as("row for the seeded node present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the name-keyed detail page renders the node's kubelet version")
    void detailRendersKubeletVersion() {
      ui.openDetail("nodes", NODE_NAME);

      // The kubelet version can only come from the seeded node's status, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      ui.await(() -> ui.pageContains(KUBELET_VERSION));
      assertThat(ui.pageContains(KUBELET_VERSION))
        .as("node detail page contents")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when viewing the CustomResourceDefinition list and detail pages")
  class CustomResourceDefinitionListAndDetail {

    private static final String GROUP = "yakd.example.com";
    private static final String VERSION = "v1";
    private static final String PLURAL = "widgets";
    private static final String CRD_NAME = PLURAL + "." + GROUP;
    private static final String CUSTOM_RESOURCE_NAME = "cluster-it-widget";

    @BeforeEach
    void seed() {
      kubernetes.getClient().apiextensions().v1().customResourceDefinitions()
        .resource(customResourceDefinition()).createOr(NonDeletingOperation::update);
      kubernetes.getClient().genericKubernetesResources(widgetContext())
        .resource(new GenericKubernetesResourceBuilder()
          .withApiVersion(GROUP + "/" + VERSION)
          .withKind("Widget")
          .withNewMetadata().withName(CUSTOM_RESOURCE_NAME).endMetadata()
          .build())
        .createOr(NonDeletingOperation::update);
    }

    @AfterEach
    void deleteCustomResources() {
      kubernetes.getClient().genericKubernetesResources(widgetContext())
        .withName(CUSTOM_RESOURCE_NAME).delete();
      kubernetes.getClient().apiextensions().v1().customResourceDefinitions()
        .withName(CRD_NAME).delete();
    }

    private static ResourceDefinitionContext widgetContext() {
      return new ResourceDefinitionContext.Builder()
        .withNamespaced(false).withGroup(GROUP).withVersion(VERSION).withPlural(PLURAL).build();
    }

    private static CustomResourceDefinition customResourceDefinition() {
      return new CustomResourceDefinitionBuilder()
        .withNewMetadata()
          .withName(CRD_NAME)
        .endMetadata()
        .withNewSpec()
          .withGroup(GROUP)
          .withScope("Cluster")
          .withNewNames().withKind("Widget").withSingular("widget").withPlural(PLURAL).endNames()
          .addNewVersion().withName(VERSION).withServed(true).withStorage(true).endVersion()
        .endSpec()
        .build();
    }

    private String customResourceDefinitionUid() {
      final CustomResourceDefinition crd = kubernetes.getClient().apiextensions().v1()
        .customResourceDefinitions().withName(CRD_NAME).get();
      assertThat(crd).as("seeded custom resource definition available").isNotNull();
      return crd.getMetadata().getUid();
    }

    @Test
    @DisplayName("the list renders a row for the seeded custom resource definition")
    void listRendersSeededRow() {
      ui.openList("customresourcedefinitions");

      ui.await(() -> ui.hasRow(CRD_NAME));
      assertThat(ui.hasRow(CRD_NAME))
        .as("row for the seeded custom resource definition present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the custom resource definition's kind")
    void detailRendersKind() {
      ui.openDetail("customresourcedefinitions", customResourceDefinitionUid());

      // Case-sensitive match: 'Widget' can only come from the CRD's spec.names.kind
      // (the CRD name only contains the lowercase plural form).
      ui.await(() -> ui.pageContains("Widget"));
      assertThat(ui.pageContains("Widget"))
        .as("custom resource definition detail page contents")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page lists the seeded custom resource instance")
    void detailListsCustomResourceInstance() {
      ui.openDetail("customresourcedefinitions", customResourceDefinitionUid());

      // Custom resource instances are not watched: the embedded list is fetched on
      // demand through the backend's dynamic-client REST endpoint, so this row
      // proves that path end-to-end.
      ui.await(() -> ui.hasRow(CUSTOM_RESOURCE_NAME));
      assertThat(ui.hasRow(CUSTOM_RESOURCE_NAME))
        .as("row for the seeded custom resource instance present in the detail page")
        .isTrue();
    }
  }
}
