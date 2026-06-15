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
package com.marcnuri.yakd.customresources;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import com.marcnuri.yakd.selenium.ResourceUi;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// A namespaced custom resource instance: its list is embedded (fetched on demand, not watched) in
// the owning CRD's detail page, and editing opens the shared ResourceEditModal. Cluster-scoped
// instance listing is covered by ClusterScopedResourceIT; this exercises the namespaced scope plus
// the instance delete and edit-save mutation paths. Driven via ResourceUi by composition.
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("CustomResource (namespaced)")
public class CustomResourceIT {

  private static final String GROUP = "yakd.example.com";
  private static final String VERSION = "v1";
  private static final String PLURAL = "gadgets";
  private static final String KIND = "Gadget";
  private static final String CRD_NAME = PLURAL + "." + GROUP;
  private static final String NAMESPACE = "default";

  @KubernetesTestServer
  KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;
  WebDriver driver;
  ResourceUi ui;

  private final String instanceName = "gadget-" + UUID.randomUUID().toString().substring(0, 8);
  private String crdUid;

  @BeforeEach
  void setUp() {
    ui = ResourceUi.openTab(driver, url);
    kubernetes.getClient().resource(customResourceDefinition()).createOr(NonDeletingOperation::update);
    crdUid = kubernetes.getClient().apiextensions().v1().customResourceDefinitions()
      .withName(CRD_NAME).get().getMetadata().getUid();
    kubernetes.getClient().genericKubernetesResources(namespacedContext())
      .inNamespace(NAMESPACE).resource(instance()).createOr(NonDeletingOperation::update);
    // The CRD detail page hosts the (on-demand) custom resource list; wait for the instance row so
    // every test starts from a page that has fetched the namespaced instance through the backend.
    ui.openDetail("customresourcedefinitions", crdUid);
    ui.await(() -> ui.hasRow(instanceName));
  }

  @AfterEach
  void cleanUp() {
    try {
      kubernetes.getClient().genericKubernetesResources(namespacedContext())
        .inNamespace(NAMESPACE).withName(instanceName).delete();
      kubernetes.getClient().apiextensions().v1().customResourceDefinitions().withName(CRD_NAME).delete();
    } finally {
      ui.closeTab();
    }
  }

  private static ResourceDefinitionContext namespacedContext() {
    return new ResourceDefinitionContext.Builder()
      .withNamespaced(true).withGroup(GROUP).withVersion(VERSION).withPlural(PLURAL).build();
  }

  private static CustomResourceDefinition customResourceDefinition() {
    return new CustomResourceDefinitionBuilder()
      .withNewMetadata().withName(CRD_NAME).endMetadata()
      .withNewSpec()
        .withGroup(GROUP)
        .withScope("Namespaced")
        .withNewNames().withKind(KIND).withSingular("gadget").withPlural(PLURAL).endNames()
        .addNewVersion().withName(VERSION).withServed(true).withStorage(true).endVersion()
      .endSpec()
      .build();
  }

  private GenericKubernetesResource instance() {
    return new GenericKubernetesResourceBuilder()
      .withApiVersion(GROUP + "/" + VERSION)
      .withKind(KIND)
      .withNewMetadata()
        .withName(instanceName)
        .withNamespace(NAMESPACE)
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .build();
  }

  private GenericKubernetesResource backendInstance() {
    return kubernetes.getClient().genericKubernetesResources(namespacedContext())
      .inNamespace(NAMESPACE).withName(instanceName).get();
  }

  private String backendMutationMarker() {
    final GenericKubernetesResource stored = backendInstance();
    if (stored == null || stored.getMetadata().getLabels() == null) {
      return null;
    }
    return stored.getMetadata().getLabels().get("mutation-marker");
  }

  @Nested
  @DisplayName("when viewing the owning CRD's detail page")
  class ListAndMutate {

    @Test
    @DisplayName("the detail page lists the seeded namespaced custom resource instance")
    void listRendersNamespacedInstance() {
      assertThat(ui.hasRow(instanceName))
        .as("row for the seeded namespaced custom resource instance present in the detail page")
        .isTrue();
    }

    @Test
    @DisplayName("clicking delete removes the custom resource instance from the backend")
    void deleteRemovesInstance() {
      ui.deleteRow(instanceName);

      ui.await(() -> backendInstance() == null);
      assertThat(backendInstance())
        .as("custom resource instance still present in the backend after delete")
        .isNull();
    }

    @Test
    @DisplayName("editing and saving persists the change to the backend instance")
    void editPersistsChange() {
      // The instance name renders as a link that opens the edit modal for that instance.
      driver.findElement(By.linkText(instanceName)).click();
      ui.await(() -> ui.editorContains("before-edit"));

      ui.editorReplace("before-edit", "after-edit");
      ui.save();

      ui.await(() -> "after-edit".equals(backendMutationMarker()));
      assertThat(backendMutationMarker())
        .as("mutation-marker label on the persisted custom resource instance")
        .isEqualTo("after-edit");
    }

    @Test
    @DisplayName("editing and cancelling discards the change without persisting")
    void cancelDiscardsChange() {
      driver.findElement(By.linkText(instanceName)).click();
      ui.await(() -> ui.editorContains("before-edit"));

      ui.editorReplace("before-edit", "after-edit-cancelled");
      driver.findElement(By.cssSelector("[data-testid='resource-edit__cancel']")).click();
      // Cancel unmounts the modal; waiting for it to close proves Cancel fired before we assert
      // that nothing was persisted.
      ui.await(() -> driver.findElements(By.cssSelector("[data-testid='resource-edit__cancel']")).isEmpty());

      assertThat(backendMutationMarker())
        .as("mutation-marker label unchanged after cancelling the edit")
        .isEqualTo("before-edit");
    }
  }
}
