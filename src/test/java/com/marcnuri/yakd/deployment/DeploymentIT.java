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
    return new DeploymentBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace(NAMESPACE)
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
}
