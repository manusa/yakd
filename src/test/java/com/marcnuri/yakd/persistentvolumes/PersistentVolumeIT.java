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
package com.marcnuri.yakd.persistentvolumes;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
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
@DisplayName("PersistentVolume")
public class PersistentVolumeIT {

  private static final String PV_NAME = "it-persistent-volume";
  private static final String ADDED_PV_NAME = "watch-added-persistent-volume";
  private static final String DETAIL_MARKER = "persistent-volume-detail-marker";
  private static final String MODIFIED_PHASE = "Released";
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
    kubernetes.getClient().persistentVolumes()
      .resource(persistentVolume(PV_NAME)).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().persistentVolumes().withName(PV_NAME).delete();
    kubernetes.getClient().persistentVolumes().withName(ADDED_PV_NAME).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static PersistentVolume persistentVolume(String pvName) {
    return new PersistentVolumeBuilder()
      .withNewMetadata()
        .withName(pvName)
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .withNewSpec()
        .addToCapacity("storage", new Quantity("1Gi"))
        .withAccessModes("ReadWriteOnce")
        .withStorageClassName("it-storage-class")
        .withPersistentVolumeReclaimPolicy("Retain")
        .withNewHostPath().withPath("/tmp/" + pvName).endHostPath()
      .endSpec()
      .withNewStatus().withPhase("Available").endStatus()
      .build();
  }

  private boolean listHasRow(String pvName) {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(pvName));
  }

  private boolean persistentVolumeRowShowsPhase(String phase) {
    return driver.findElements(ROW).stream()
      .filter(row -> row.getText().contains(PV_NAME))
      .anyMatch(row -> row.getText().contains(phase));
  }

  private String seededUid() {
    final PersistentVolume seeded = kubernetes.getClient().persistentVolumes().withName(PV_NAME).get();
    assertThat(seeded).as("seeded persistent volume available").isNotNull();
    return seeded.getMetadata().getUid();
  }

  private String backendMarker() {
    final PersistentVolume persistentVolume = kubernetes.getClient().persistentVolumes()
      .withName(PV_NAME).get();
    if (persistentVolume == null || persistentVolume.getMetadata().getLabels() == null) {
      return null;
    }
    return persistentVolume.getMetadata().getLabels().get("mutation-marker");
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded persistent volume")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "persistentvolumes");

      wait.until(d -> listHasRow(PV_NAME));
      assertThat(listHasRow(PV_NAME))
        .as("row for the seeded persistent volume present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the persistent volume's label")
    void detailRendersLabel() {
      driver.navigate().to(url.toString() + "persistentvolumes/" + seededUid());

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      wait.until(d -> d.getPageSource().contains(DETAIL_MARKER));
      assertThat(driver.getPageSource())
        .as("persistent volume detail page contents")
        .contains(DETAIL_MARKER);
    }
  }

  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "persistentvolumes");
      wait.until(d -> listHasRow(PV_NAME));
    }

    @Test
    @DisplayName("clicking delete removes the persistent volume's row from the list")
    void deleteRemovesRow() {
      driver.findElements(ROW).stream()
        .filter(row -> row.getText().contains(PV_NAME))
        .findFirst().orElseThrow()
        .findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasRow(PV_NAME));
      assertThat(listHasRow(PV_NAME))
        .as("persistent volume row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "persistentvolumes/" + seededUid() + "/edit");
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
        .as("mutation-marker label on the persisted persistent volume")
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
  @DisplayName("when the server pushes live watch events")
  class WatchLiveUpdates {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "persistentvolumes");
      // Waiting for the seeded row guarantees the watch stream is connected and its
      // initial sync replayed before the test mutates the cluster, so any later change
      // can only reach the list through a live watch event.
      wait.until(d -> listHasRow(PV_NAME));
    }

    @Test
    @DisplayName("a persistent volume created server-side appears in the list without a refresh")
    void createdPersistentVolumeAppears() {
      kubernetes.getClient().persistentVolumes().resource(persistentVolume(ADDED_PV_NAME)).create();

      wait.until(d -> listHasRow(ADDED_PV_NAME));
      assertThat(listHasRow(ADDED_PV_NAME))
        .as("row for the server-side-created persistent volume present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a persistent volume deleted server-side disappears from the list without a refresh")
    void deletedPersistentVolumeDisappears() {
      kubernetes.getClient().persistentVolumes().withName(PV_NAME).delete();

      wait.until(d -> !listHasRow(PV_NAME));
      assertThat(listHasRow(PV_NAME))
        .as("row for the server-side-deleted persistent volume still present in the list")
        .isFalse();
    }

    @Test
    @DisplayName("a persistent volume modified server-side updates its row in place without a refresh")
    void modifiedPersistentVolumeUpdatesRow() {
      kubernetes.getClient().persistentVolumes().withName(PV_NAME)
        .edit(pv -> new PersistentVolumeBuilder(pv)
          .editOrNewStatus().withPhase(MODIFIED_PHASE).endStatus()
          .build());

      wait.until(d -> persistentVolumeRowShowsPhase(MODIFIED_PHASE));
      assertThat(persistentVolumeRowShowsPhase(MODIFIED_PHASE))
        .as("persistent volume row reflects the server-side phase change")
        .isTrue();
    }
  }
}
