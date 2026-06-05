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

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Deployment")
public class DeploymentIT {

  private static final String NAMESPACE = "default";
  private static final String DEPLOYMENT_NAME = "mutation-it-deployment";
  private static final String ADDED_DEPLOYMENT_NAME = "watch-added-deployment";
  private static final String SEEDED_IMAGE = "busybox";
  private static final String MODIFIED_IMAGE = "nginx:1.25";
  private static final By ROW = By.cssSelector("[data-testid='resource-list__row']");

  @KubernetesTestServer
  KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;
  WebDriver driver;
  Wait<WebDriver> wait;

  @BeforeEach
  void setUp() {
    wait = new FluentWait<>(driver)
      .withTimeout(Duration.ofSeconds(10))
      .pollingEvery(Duration.ofMillis(100))
      .ignoring(NoSuchElementException.class)
      .ignoring(StaleElementReferenceException.class);
    kubernetes.getClient().apps().deployments().inNamespace(NAMESPACE)
      .resource(deployment()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().apps().deployments().inNamespace(NAMESPACE).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static Deployment deployment() {
    return deployment(DEPLOYMENT_NAME);
  }

  private static Deployment deployment(String name) {
    return deployment(name, NAMESPACE);
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

  private boolean listHasRow(String deploymentName) {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(deploymentName));
  }

  private boolean listHasDeploymentRow() {
    return listHasRow(DEPLOYMENT_NAME);
  }

  private boolean deploymentRowShowsImage(String image) {
    return driver.findElements(ROW).stream()
      .filter(row -> row.getText().contains(DEPLOYMENT_NAME))
      .anyMatch(row -> row.getText().contains(image));
  }

  private String backendMarker() {
    final Deployment deployment = kubernetes.getClient().apps().deployments()
      .inNamespace(NAMESPACE).withName(DEPLOYMENT_NAME).get();
    if (deployment == null || deployment.getMetadata().getLabels() == null) {
      return null;
    }
    return deployment.getMetadata().getLabels().get("mutation-marker");
  }

  private Integer backendReplicas() {
    final Deployment deployment = kubernetes.getClient().apps().deployments()
      .inNamespace(NAMESPACE).withName(DEPLOYMENT_NAME).get();
    if (deployment == null || deployment.getSpec() == null) {
      return null;
    }
    return deployment.getSpec().getReplicas();
  }

  private String backendRestartedAt() {
    final Deployment deployment = kubernetes.getClient().apps().deployments()
      .inNamespace(NAMESPACE).withName(DEPLOYMENT_NAME).get();
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
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "deployments");
      wait.until(d -> listHasDeploymentRow());
    }

    @Test
    @DisplayName("clicking delete removes the deployment's row from the list")
    void deleteRemovesRow() {
      driver.findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasDeploymentRow());
      assertThat(listHasDeploymentRow())
        .as("deployment row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    @BeforeEach
    void navigate() {
      // The MockServer assigns its own uid on create, so resolve the stored uid
      // rather than relying on a client-provided one; the edit page keys off it.
      final Deployment seeded = kubernetes.getClient().apps().deployments()
        .inNamespace(NAMESPACE).withName(DEPLOYMENT_NAME).get();
      assertThat(seeded).as("seeded deployment available before editing").isNotNull();
      driver.navigate().to(url.toString() + "deployments/" + seeded.getMetadata().getUid() + "/edit");
      wait.until(d -> aceValue((JavascriptExecutor) d).contains("before-edit"));
    }

    @Test
    @DisplayName("saving the edited YAML persists the change to the backend")
    void savePersistsChange() {
      ((JavascriptExecutor) driver).executeScript(
        "const ed = document.querySelector('.ace_editor').env.editor;"
          + "ed.setValue(ed.getValue().replace('before-edit', 'after-edit'), 1);");

      driver.findElement(By.cssSelector("[data-testid='resource-edit__save']")).click();

      wait.until(d -> "after-edit".equals(backendMarker()));
      assertThat(backendMarker())
        .as("mutation-marker label on the persisted deployment")
        .isEqualTo("after-edit");
    }

    private String aceValue(JavascriptExecutor js) {
      final Object value = js.executeScript(
        "const e = document.querySelector('.ace_editor');"
          + "return e && e.env && e.env.editor ? e.env.editor.getValue() : '';");
      return value == null ? "" : value.toString();
    }
  }

  @Nested
  @DisplayName("when scaling from the detail page")
  class Scale {

    @BeforeEach
    void navigate() {
      final Deployment seeded = kubernetes.getClient().apps().deployments()
        .inNamespace(NAMESPACE).withName(DEPLOYMENT_NAME).get();
      assertThat(seeded).as("seeded deployment available before scaling").isNotNull();
      driver.navigate().to(url.toString() + "deployments/" + seeded.getMetadata().getUid());
      // The replicas control operates on the resource once the watch has loaded it
      // into the store; the name only renders after that, so it is a safe gate.
      wait.until(d -> d.getPageSource().contains(DEPLOYMENT_NAME));
    }

    @Test
    @DisplayName("incrementing the replicas persists the new replica count to the backend")
    void incrementPersistsReplicas() {
      driver.findElement(By.cssSelector("[data-testid='replicas-field__increment']")).click();

      wait.until(d -> Integer.valueOf(2).equals(backendReplicas()));
      assertThat(backendReplicas())
        .as("spec.replicas on the persisted deployment")
        .isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("when restarting from the list page")
  class Restart {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "deployments");
      wait.until(d -> listHasDeploymentRow());
    }

    @Test
    @DisplayName("clicking restart records a rollout restart on the backend")
    void restartRecordsRestartedAt() {
      driver.findElement(By.cssSelector("[data-testid='deployment-list__restart']")).click();

      wait.until(d -> backendRestartedAt() != null);
      assertThat(backendRestartedAt())
        .as("kubectl.kubernetes.io/restartedAt annotation on the persisted deployment")
        .isNotNull();
    }
  }

  @Nested
  @DisplayName("when the server pushes live watch events")
  class WatchLiveUpdates {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "deployments");
      // Waiting for the seeded row guarantees the watch SSE stream is connected and
      // its initial sync has been replayed before the test mutates the cluster, so any
      // later change can only reach the list through a live watch event.
      wait.until(d -> listHasDeploymentRow());
    }

    @Test
    @DisplayName("a deployment created server-side appears in the list without a refresh")
    void createdDeploymentAppears() {
      kubernetes.getClient().apps().deployments().inNamespace(NAMESPACE)
        .resource(deployment(ADDED_DEPLOYMENT_NAME)).create();

      wait.until(d -> listHasRow(ADDED_DEPLOYMENT_NAME));
      assertThat(listHasRow(ADDED_DEPLOYMENT_NAME))
        .as("row for the server-side-created deployment present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a deployment deleted server-side disappears from the list without a refresh")
    void deletedDeploymentDisappears() {
      kubernetes.getClient().apps().deployments().inNamespace(NAMESPACE)
        .withName(DEPLOYMENT_NAME).delete();

      wait.until(d -> !listHasDeploymentRow());
      assertThat(listHasDeploymentRow())
        .as("row for the server-side-deleted deployment still present in the list")
        .isFalse();
    }

    @Test
    @DisplayName("a deployment modified server-side updates its row in place without a refresh")
    void modifiedDeploymentUpdatesRow() {
      kubernetes.getClient().apps().deployments().inNamespace(NAMESPACE)
        .withName(DEPLOYMENT_NAME)
        .edit(d -> new DeploymentBuilder(d)
          .editSpec().editTemplate().editSpec()
            .editFirstContainer().withImage(MODIFIED_IMAGE).endContainer()
          .endSpec().endTemplate().endSpec()
          .build());

      wait.until(d -> deploymentRowShowsImage(MODIFIED_IMAGE));
      assertThat(deploymentRowShowsImage(MODIFIED_IMAGE))
        .as("deployment row reflects the server-side image change")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded deployment")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "deployments");

      wait.until(d -> listHasDeploymentRow());
      assertThat(listHasDeploymentRow())
        .as("row for the seeded deployment present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded deployment's container image")
    void listRowShowsImage() {
      driver.navigate().to(url.toString() + "deployments");

      wait.until(d -> deploymentRowShowsImage(SEEDED_IMAGE));
      assertThat(deploymentRowShowsImage(SEEDED_IMAGE))
        .as("seeded container image shown in the deployment's row")
        .isTrue();
    }

    @Test
    @DisplayName("the side bar does not expose the OpenShift navigation items")
    void sideBarHasNoOpenShiftNav() {
      driver.navigate().to(url.toString() + "deployments");

      // Gate on the page being fully loaded (watch synced) so the absence below
      // cannot pass vacuously against a not-yet-rendered side bar; also guards
      // that OpenShiftIT's /apis expectations stay on its own dedicated profile.
      wait.until(d -> listHasDeploymentRow());
      assertThat(driver.findElements(By.cssSelector("[data-testid='side-bar__nav-routes']")))
        .as("Routes side bar navigation item in vanilla Kubernetes mode")
        .isEmpty();
    }

    @Test
    @DisplayName("the detail page renders the deployment's name")
    void detailRendersName() {
      final Deployment seeded = kubernetes.getClient().apps().deployments()
        .inNamespace(NAMESPACE).withName(DEPLOYMENT_NAME).get();
      assertThat(seeded).as("seeded deployment available before navigating").isNotNull();

      driver.navigate().to(url.toString() + "deployments/" + seeded.getMetadata().getUid());

      wait.until(d -> d.getPageSource().contains(DEPLOYMENT_NAME));
      assertThat(driver.getPageSource())
        .as("detail page contents")
        .contains(DEPLOYMENT_NAME);
    }
  }

  @Nested
  @DisplayName("when filtering the list by namespace")
  class FilterByNamespace {

    private static final String OTHER_NAMESPACE = "filter-other";
    private static final String OTHER_DEPLOYMENT_NAME = "filter-other-deployment";
    private static final By NAMESPACE_ITEM = By.cssSelector("[data-testid='filter-bar__namespace-item']");

    @BeforeEach
    void seedOtherNamespaceAndFilter() {
      // The FilterBar lists Namespace resources streamed through the watch, so both
      // namespaces must exist as actual resources for their dropdown items to render.
      kubernetes.getClient().namespaces().resource(new NamespaceBuilder()
        .withNewMetadata().withName(NAMESPACE).endMetadata().build()).createOr(NonDeletingOperation::update);
      kubernetes.getClient().namespaces().resource(new NamespaceBuilder()
        .withNewMetadata().withName(OTHER_NAMESPACE).endMetadata().build()).createOr(NonDeletingOperation::update);
      kubernetes.getClient().apps().deployments().inNamespace(OTHER_NAMESPACE)
        .resource(deployment(OTHER_DEPLOYMENT_NAME, OTHER_NAMESPACE)).createOr(NonDeletingOperation::update);
      driver.navigate().to(url.toString() + "deployments");
      wait.until(d -> listHasDeploymentRow() && listHasRow(OTHER_DEPLOYMENT_NAME));
      driver.findElement(By.cssSelector("[data-testid='filter-bar__namespace'] button")).click();
      // Item texts are only visible once the panel is open, so matching by text also
      // waits for the dropdown to be effectively expanded.
      wait.until(d -> d.findElements(NAMESPACE_ITEM).stream()
        .anyMatch(item -> item.getText().equals(NAMESPACE)));
      driver.findElements(NAMESPACE_ITEM).stream()
        .filter(item -> item.getText().equals(NAMESPACE))
        .findFirst().orElseThrow().click();
    }

    @AfterEach
    void deleteOtherNamespaceResources() {
      kubernetes.getClient().apps().deployments().inNamespace(OTHER_NAMESPACE).delete();
      // Delete the seeded Namespace resources too: the mock server outlives this
      // class, so a leaked namespace would show up in every later FilterBar.
      kubernetes.getClient().namespaces().withName(OTHER_NAMESPACE).delete();
      kubernetes.getClient().namespaces().withName(NAMESPACE).delete();
    }

    @Test
    @DisplayName("selecting a namespace hides deployments from other namespaces")
    void hidesOtherNamespaceRows() {
      wait.until(d -> !listHasRow(OTHER_DEPLOYMENT_NAME));
      assertThat(listHasRow(OTHER_DEPLOYMENT_NAME))
        .as("row for the other-namespace deployment still present after filtering")
        .isFalse();
    }

    @Test
    @DisplayName("selecting a namespace keeps deployments in that namespace visible")
    void keepsSelectedNamespaceRows() {
      wait.until(d -> !listHasRow(OTHER_DEPLOYMENT_NAME));
      assertThat(listHasDeploymentRow())
        .as("row for the selected-namespace deployment present after filtering")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when searching cluster resources globally")
  class GlobalSearch {

    private static final By SEARCH_INPUT = By.cssSelector("[data-testid='search__input']");

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "search");
      wait.until(d -> d.findElement(SEARCH_INPUT).isDisplayed());
    }

    @Test
    @DisplayName("a query matching the seeded deployment shows its row in the results")
    void matchingQueryShowsRow() {
      driver.findElement(SEARCH_INPUT).sendKeys(DEPLOYMENT_NAME);

      wait.until(d -> listHasDeploymentRow());
      assertThat(listHasDeploymentRow())
        .as("row for the seeded deployment present in the search results")
        .isTrue();
    }

    @Test
    @DisplayName("narrowing the query to a non-matching term hides the results")
    void nonMatchingQueryHidesRows() {
      driver.findElement(SEARCH_INPUT).sendKeys(DEPLOYMENT_NAME);
      // Gate on the matching row first so its later absence proves the query
      // narrowed the results rather than the store never having synced.
      wait.until(d -> listHasDeploymentRow());

      driver.findElement(SEARCH_INPUT).sendKeys("-no-match");

      wait.until(d -> !listHasDeploymentRow());
      assertThat(listHasDeploymentRow())
        .as("deployment row still present after narrowing the query to a non-matching term")
        .isFalse();
    }
  }
}
