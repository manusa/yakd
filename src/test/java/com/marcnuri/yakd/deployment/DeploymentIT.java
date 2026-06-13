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
package com.marcnuri.yakd.deployment;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Deployment")
public class DeploymentIT extends AbstractResourceIT<Deployment> {

  private static final String SEEDED_IMAGE = "busybox";
  private static final String MODIFIED_IMAGE = "nginx:1.25";

  public DeploymentIT() {
    super("deployments");
  }

  @Override
  protected Deployment resource(String name) {
    return deployment(name, "default");
  }

  private static Deployment deployment(String name, String namespace) {
    return new DeploymentBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace(namespace)
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .withReplicas(1)
        .withNewSelector().addToMatchLabels("app", name).endSelector()
        .withNewTemplate()
          .withNewMetadata().addToLabels("app", name).endMetadata()
          .withNewSpec()
            .addNewContainer().withName("container-1").withImage(SEEDED_IMAGE).endContainer()
          .endSpec()
        .endTemplate()
      .endSpec()
      .build();
  }

  private Integer backendReplicas(String name) {
    final Deployment deployment = kubernetes.getClient().apps().deployments()
      .inNamespace("default").withName(name).get();
    if (deployment == null || deployment.getSpec() == null) {
      return null;
    }
    return deployment.getSpec().getReplicas();
  }

  private String backendRestartedAt(String name) {
    final Deployment deployment = kubernetes.getClient().apps().deployments()
      .inNamespace("default").withName(name).get();
    if (deployment == null || deployment.getSpec() == null
      || deployment.getSpec().getTemplate() == null
      || deployment.getSpec().getTemplate().getMetadata() == null
      || deployment.getSpec().getTemplate().getMetadata().getAnnotations() == null) {
      return null;
    }
    return deployment.getSpec().getTemplate().getMetadata().getAnnotations()
      .get("kubectl.kubernetes.io/restartedAt");
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    private String name;

    @BeforeEach
    void seedResource() {
      name = seed("to-view");
    }

    @Test
    @DisplayName("the list renders a row for the seeded deployment")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded deployment present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded deployment's container image")
    void listRowShowsImage() {
      openList();

      await(() -> rowShows(name, SEEDED_IMAGE));
      assertThat(rowShows(name, SEEDED_IMAGE))
        .as("seeded container image shown in the deployment's row")
        .isTrue();
    }

    @Test
    @DisplayName("the side bar does not expose the OpenShift navigation items")
    void sideBarHasNoOpenShiftNav() {
      openList();

      // Gate on the page being fully loaded (watch synced) so the absence below
      // cannot pass vacuously against a not-yet-rendered side bar.
      awaitRow(name);
      assertThat(driver.findElements(By.cssSelector("[data-testid='side-bar__nav-routes']")))
        .as("Routes side bar navigation item in vanilla Kubernetes mode")
        .isEmpty();
    }

    @Test
    @DisplayName("the detail page renders the deployment's name")
    void detailRendersName() {
      openDetail(seededUid(name));

      awaitPageContains(name);
      assertThat(pageContains(name))
        .as("detail page contents")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-delete");
      openList();
      awaitRow(name);
    }

    @Test
    @DisplayName("clicking delete removes the deployment's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("deployment row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-edit");
      openEditor(seededUid(name));
      awaitEditorContains("before-edit");
    }

    @Test
    @DisplayName("saving the edited YAML persists the change to the backend")
    void savePersistsChange() {
      editorReplace("before-edit", "after-edit");
      save();

      await(() -> "after-edit".equals(label(name, "mutation-marker")));
      assertThat(label(name, "mutation-marker"))
        .as("mutation-marker label on the persisted deployment")
        .isEqualTo("after-edit");
    }
  }

  @Nested
  @DisplayName("when scaling from the detail page")
  class Scale {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-scale");
      openDetail(seededUid(name));
      // The replicas control operates on the resource once the watch has loaded it
      // into the store; the name only renders after that, so it is a safe gate.
      awaitPageContains(name);
    }

    @Test
    @DisplayName("incrementing the replicas persists the new replica count to the backend")
    void incrementPersistsReplicas() {
      driver.findElement(By.cssSelector("[data-testid='replicas-field__increment']")).click();

      await(() -> Integer.valueOf(2).equals(backendReplicas(name)));
      assertThat(backendReplicas(name))
        .as("spec.replicas on the persisted deployment")
        .isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("when restarting from the list page")
  class Restart {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-restart");
      openList();
      awaitRow(name);
    }

    @Test
    @DisplayName("clicking restart records a rollout restart on the backend")
    void restartRecordsRestartedAt() {
      driver.findElement(By.cssSelector("[data-testid='deployment-list__restart']")).click();

      await(() -> backendRestartedAt(name) != null);
      assertThat(backendRestartedAt(name))
        .as("kubectl.kubernetes.io/restartedAt annotation on the persisted deployment")
        .isNotNull();
    }
  }

  @Nested
  @DisplayName("when the server pushes live watch events")
  class WatchLiveUpdates {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-watch");
      openList();
      // Waiting for the seeded row guarantees the watch SSE stream is connected and its initial
      // sync has been replayed before the test mutates the cluster, so any later change can only
      // reach the list through a live watch event.
      awaitRow(name);
    }

    @Test
    @DisplayName("a deployment created server-side appears in the list without a refresh")
    void createdDeploymentAppears() {
      final String added = seed("watch-added");

      awaitRow(added);
      assertThat(hasRow(added))
        .as("row for the server-side-created deployment present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a deployment deleted server-side disappears from the list without a refresh")
    void deletedDeploymentDisappears() {
      kubernetes.getClient().apps().deployments().inNamespace("default").withName(name).delete();

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("row for the server-side-deleted deployment still present in the list")
        .isFalse();
    }

    @Test
    @DisplayName("a deployment modified server-side updates its row in place without a refresh")
    void modifiedDeploymentUpdatesRow() {
      kubernetes.getClient().apps().deployments().inNamespace("default").withName(name)
        .edit(d -> new DeploymentBuilder(d)
          .editSpec().editTemplate().editSpec()
            .editFirstContainer().withImage(MODIFIED_IMAGE).endContainer()
          .endSpec().endTemplate().endSpec()
          .build());

      await(() -> rowShows(name, MODIFIED_IMAGE));
      assertThat(rowShows(name, MODIFIED_IMAGE))
        .as("deployment row reflects the server-side image change")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when filtering the list by namespace")
  class FilterByNamespace {

    private static final String OTHER_NAMESPACE = "filter-other";
    private static final String OTHER_DEPLOYMENT_NAME = "filter-other-deployment";
    private static final By NAMESPACE_ITEM = By.cssSelector("[data-testid='filter-bar__namespace-item']");

    private String name;

    @BeforeEach
    void seedOtherNamespaceAndFilter() {
      name = seed("filter-selected");
      // The FilterBar lists Namespace resources streamed through the watch, so both
      // namespaces must exist as actual resources for their dropdown items to render.
      kubernetes.getClient().namespaces().resource(new NamespaceBuilder()
        .withNewMetadata().withName("default").endMetadata().build()).createOr(NonDeletingOperation::update);
      kubernetes.getClient().namespaces().resource(new NamespaceBuilder()
        .withNewMetadata().withName(OTHER_NAMESPACE).endMetadata().build()).createOr(NonDeletingOperation::update);
      kubernetes.getClient().resource(deployment(OTHER_DEPLOYMENT_NAME, OTHER_NAMESPACE))
        .createOr(NonDeletingOperation::update);
      openList();
      await(() -> hasRow(name) && hasRow(OTHER_DEPLOYMENT_NAME));
      driver.findElement(By.cssSelector("[data-testid='filter-bar__namespace'] button")).click();
      // Item texts are only visible once the panel is open, so matching by text also
      // waits for the dropdown to be effectively expanded.
      await(() -> driver.findElements(NAMESPACE_ITEM).stream()
        .anyMatch(item -> item.getText().equals("default")));
      driver.findElements(NAMESPACE_ITEM).stream()
        .filter(item -> item.getText().equals("default"))
        .findFirst().orElseThrow().click();
    }

    @AfterEach
    void deleteOtherNamespaceResources() {
      kubernetes.getClient().resource(deployment(OTHER_DEPLOYMENT_NAME, OTHER_NAMESPACE)).delete();
      // Delete the seeded Namespace resources too: the mock server outlives this
      // class, so a leaked namespace would show up in every later FilterBar.
      kubernetes.getClient().namespaces().withName(OTHER_NAMESPACE).delete();
      kubernetes.getClient().namespaces().withName("default").delete();
    }

    @Test
    @DisplayName("selecting a namespace hides deployments from other namespaces")
    void hidesOtherNamespaceRows() {
      awaitNoRow(OTHER_DEPLOYMENT_NAME);
      assertThat(hasRow(OTHER_DEPLOYMENT_NAME))
        .as("row for the other-namespace deployment still present after filtering")
        .isFalse();
    }

    @Test
    @DisplayName("selecting a namespace keeps deployments in that namespace visible")
    void keepsSelectedNamespaceRows() {
      awaitNoRow(OTHER_DEPLOYMENT_NAME);
      assertThat(hasRow(name))
        .as("row for the selected-namespace deployment present after filtering")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when searching cluster resources globally")
  class GlobalSearch {

    private static final By SEARCH_INPUT = By.cssSelector("[data-testid='search__input']");

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-search");
      open("search");
      await(() -> driver.findElement(SEARCH_INPUT).isDisplayed());
    }

    @Test
    @DisplayName("a query matching the seeded deployment shows its row in the results")
    void matchingQueryShowsRow() {
      driver.findElement(SEARCH_INPUT).sendKeys(name);

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded deployment present in the search results")
        .isTrue();
    }

    @Test
    @DisplayName("narrowing the query to a non-matching term hides the results")
    void nonMatchingQueryHidesRows() {
      driver.findElement(SEARCH_INPUT).sendKeys(name);
      // Gate on the matching row first so its later absence proves the query
      // narrowed the results rather than the store never having synced.
      awaitRow(name);

      driver.findElement(SEARCH_INPUT).sendKeys("-no-match");

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("deployment row still present after narrowing the query to a non-matching term")
        .isFalse();
    }
  }
}
