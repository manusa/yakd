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
package com.marcnuri.yakd.horizontalpodautoscalers;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerBuilder;
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
@DisplayName("HorizontalPodAutoscaler")
public class HorizontalPodAutoscalerIT {

  private static final String NAMESPACE = "default";
  private static final String HPA_NAME = "it-horizontal-pod-autoscaler";
  private static final String DETAIL_MARKER = "horizontal-pod-autoscaler-detail-marker";
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
    kubernetes.getClient().autoscaling().v1().horizontalPodAutoscalers().inNamespace(NAMESPACE)
      .resource(horizontalPodAutoscaler()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().autoscaling().v1().horizontalPodAutoscalers().inNamespace(NAMESPACE).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static HorizontalPodAutoscaler horizontalPodAutoscaler() {
    return new HorizontalPodAutoscalerBuilder()
      .withNewMetadata()
        .withName(HPA_NAME)
        .withNamespace(NAMESPACE)
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .withNewSpec()
        .withNewScaleTargetRef()
          .withApiVersion("apps/v1").withKind("Deployment").withName("it-target")
        .endScaleTargetRef()
        .withMaxReplicas(5)
      .endSpec()
      .build();
  }

  private boolean listHasHorizontalPodAutoscalerRow() {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(HPA_NAME));
  }

  private String seededUid() {
    final HorizontalPodAutoscaler seeded = kubernetes.getClient().autoscaling().v1()
      .horizontalPodAutoscalers().inNamespace(NAMESPACE).withName(HPA_NAME).get();
    assertThat(seeded).as("seeded horizontal pod autoscaler available").isNotNull();
    return seeded.getMetadata().getUid();
  }

  private String backendMarker() {
    final HorizontalPodAutoscaler horizontalPodAutoscaler = kubernetes.getClient().autoscaling().v1()
      .horizontalPodAutoscalers().inNamespace(NAMESPACE).withName(HPA_NAME).get();
    if (horizontalPodAutoscaler == null || horizontalPodAutoscaler.getMetadata().getLabels() == null) {
      return null;
    }
    return horizontalPodAutoscaler.getMetadata().getLabels().get("mutation-marker");
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded horizontal pod autoscaler")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "horizontalpodautoscalers");

      wait.until(d -> listHasHorizontalPodAutoscalerRow());
      assertThat(listHasHorizontalPodAutoscalerRow())
        .as("row for the seeded horizontal pod autoscaler present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the horizontal pod autoscaler's label")
    void detailRendersLabel() {
      driver.navigate().to(url.toString() + "horizontalpodautoscalers/" + seededUid());

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      wait.until(d -> d.getPageSource().contains(DETAIL_MARKER));
      assertThat(driver.getPageSource())
        .as("horizontal pod autoscaler detail page contents")
        .contains(DETAIL_MARKER);
    }
  }

  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "horizontalpodautoscalers");
      wait.until(d -> listHasHorizontalPodAutoscalerRow());
    }

    @Test
    @DisplayName("clicking delete removes the horizontal pod autoscaler's row from the list")
    void deleteRemovesRow() {
      driver.findElements(ROW).stream()
        .filter(row -> row.getText().contains(HPA_NAME))
        .findFirst().orElseThrow()
        .findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasHorizontalPodAutoscalerRow());
      assertThat(listHasHorizontalPodAutoscalerRow())
        .as("horizontal pod autoscaler row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "horizontalpodautoscalers/" + seededUid() + "/edit");
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
        .as("mutation-marker label on the persisted horizontal pod autoscaler")
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
