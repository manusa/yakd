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
package com.marcnuri.yakd.secrets;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Secret")
public class SecretIT {

  private static final String NAMESPACE = "default";
  private static final String SECRET_NAME = "it-secret";
  private static final String DECODED_VALUE = "secret-detail-marker";
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
    kubernetes.getClient().secrets().inNamespace(NAMESPACE)
      .resource(secret()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().secrets().inNamespace(NAMESPACE).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static Secret secret() {
    return new SecretBuilder()
      .withNewMetadata()
        .withName(SECRET_NAME)
        .withNamespace(NAMESPACE)
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withType("Opaque")
      .addToData("detail-key",
        Base64.getEncoder().encodeToString(DECODED_VALUE.getBytes(StandardCharsets.UTF_8)))
      .build();
  }

  private boolean listHasSecretRow() {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(SECRET_NAME));
  }

  private String seededUid() {
    final Secret seeded = kubernetes.getClient().secrets()
      .inNamespace(NAMESPACE).withName(SECRET_NAME).get();
    assertThat(seeded).as("seeded secret available").isNotNull();
    return seeded.getMetadata().getUid();
  }

  private String backendMarker() {
    final Secret secret = kubernetes.getClient().secrets()
      .inNamespace(NAMESPACE).withName(SECRET_NAME).get();
    if (secret == null || secret.getMetadata().getLabels() == null) {
      return null;
    }
    return secret.getMetadata().getLabels().get("mutation-marker");
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded secret")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "secrets");

      wait.until(d -> listHasSecretRow());
      assertThat(listHasSecretRow())
        .as("row for the seeded secret present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the secret's base64-decoded data value")
    void detailRendersDecodedValue() {
      driver.navigate().to(url.toString() + "secrets/" + seededUid());

      // The page only ever shows the atob-decoded form, so the cleartext can only
      // appear if the detail page decoded the seeded resource's base64 data value.
      wait.until(d -> d.getPageSource().contains(DECODED_VALUE));
      assertThat(driver.getPageSource())
        .as("secret detail page contents")
        .contains(DECODED_VALUE);
    }
  }

  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "secrets");
      wait.until(d -> listHasSecretRow());
    }

    @Test
    @DisplayName("clicking delete removes the secret's row from the list")
    void deleteRemovesRow() {
      driver.findElements(ROW).stream()
        .filter(row -> row.getText().contains(SECRET_NAME))
        .findFirst().orElseThrow()
        .findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasSecretRow());
      assertThat(listHasSecretRow())
        .as("secret row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "secrets/" + seededUid() + "/edit");
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
        .as("mutation-marker label on the persisted secret")
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
