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
package com.marcnuri.yakd.clusterroles;

import com.marcnuri.yakd.selenium.RbacIntegrationTestProfile;
import io.fabric8.kubernetes.api.model.APIResourceListBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
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
@TestProfile(RbacIntegrationTestProfile.class)
@DisplayName("ClusterRole")
public class ClusterRoleIT {

  private static final String CLUSTER_ROLE_NAME = "it-cluster-role";
  private static final String DETAIL_MARKER = "cluster-role-detail-marker";
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
    // The ClusterRole watcher only subscribes when the cluster's discovery reports
    // clusterroles as supported; the CRUD mock does not advertise them by default.
    kubernetes.expect().get().withPath("/apis/rbac.authorization.k8s.io/v1")
      .andReturn(200, new APIResourceListBuilder()
        .withGroupVersion("rbac.authorization.k8s.io/v1")
        .addNewResource().withName("clusterroles").withKind("ClusterRole").withNamespaced(false).endResource()
        .addNewResource().withName("clusterrolebindings").withKind("ClusterRoleBinding").withNamespaced(false).endResource()
        .addNewResource().withName("roles").withKind("Role").withNamespaced(true).endResource()
        .addNewResource().withName("rolebindings").withKind("RoleBinding").withNamespaced(true).endResource()
        .build())
      .always();
    kubernetes.getClient().rbac().clusterRoles()
      .resource(clusterRole()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().rbac().clusterRoles().withName(CLUSTER_ROLE_NAME).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static ClusterRole clusterRole() {
    return new ClusterRoleBuilder()
      .withNewMetadata()
        .withName(CLUSTER_ROLE_NAME)
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .addNewRule()
        .withApiGroups("").withResources("pods").withVerbs("get", "list", "watch")
      .endRule()
      .build();
  }

  private boolean listHasClusterRoleRow() {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(CLUSTER_ROLE_NAME));
  }

  private String seededUid() {
    final ClusterRole seeded = kubernetes.getClient().rbac().clusterRoles()
      .withName(CLUSTER_ROLE_NAME).get();
    assertThat(seeded).as("seeded cluster role available").isNotNull();
    return seeded.getMetadata().getUid();
  }

  private String backendMarker() {
    final ClusterRole clusterRole = kubernetes.getClient().rbac().clusterRoles()
      .withName(CLUSTER_ROLE_NAME).get();
    if (clusterRole == null || clusterRole.getMetadata().getLabels() == null) {
      return null;
    }
    return clusterRole.getMetadata().getLabels().get("mutation-marker");
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded cluster role")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "clusterroles");

      wait.until(d -> listHasClusterRoleRow());
      assertThat(listHasClusterRoleRow())
        .as("row for the seeded cluster role present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the cluster role's label")
    void detailRendersLabel() {
      driver.navigate().to(url.toString() + "clusterroles/" + seededUid());

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      wait.until(d -> d.getPageSource().contains(DETAIL_MARKER));
      assertThat(driver.getPageSource())
        .as("cluster role detail page contents")
        .contains(DETAIL_MARKER);
    }
  }

  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "clusterroles");
      wait.until(d -> listHasClusterRoleRow());
    }

    @Test
    @DisplayName("clicking delete removes the cluster role's row from the list")
    void deleteRemovesRow() {
      driver.findElements(ROW).stream()
        .filter(row -> row.getText().contains(CLUSTER_ROLE_NAME))
        .findFirst().orElseThrow()
        .findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasClusterRoleRow());
      assertThat(listHasClusterRoleRow())
        .as("cluster role row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "clusterroles/" + seededUid() + "/edit");
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
        .as("mutation-marker label on the persisted cluster role")
        .isEqualTo("after-edit");
    }

    private String aceValue(JavascriptExecutor js) {
      final Object value = js.executeScript(
        "const e = document.querySelector('.ace_editor');"
          + "return e && e.env && e.env.editor ? e.env.editor.getValue() : '';");
      return value == null ? "" : value.toString();
    }
  }
}
