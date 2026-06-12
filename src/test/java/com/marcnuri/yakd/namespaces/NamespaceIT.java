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
package com.marcnuri.yakd.namespaces;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
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
@DisplayName("Namespace")
public class NamespaceIT {

  private static final String NAMESPACE_NAME = "it-namespace";
  private static final String DETAIL_MARKER = "namespace-detail-marker";
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
    kubernetes.getClient().namespaces()
      .resource(namespace()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().namespaces().withName(NAMESPACE_NAME).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static Namespace namespace() {
    return new NamespaceBuilder()
      .withNewMetadata()
        .withName(NAMESPACE_NAME)
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .build();
  }

  private boolean listHasNamespaceRow() {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(NAMESPACE_NAME));
  }

  private String seededUid() {
    final Namespace seeded = kubernetes.getClient().namespaces().withName(NAMESPACE_NAME).get();
    assertThat(seeded).as("seeded namespace available").isNotNull();
    return seeded.getMetadata().getUid();
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded namespace")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "namespaces");

      wait.until(d -> listHasNamespaceRow());
      assertThat(listHasNamespaceRow())
        .as("row for the seeded namespace present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the namespace's label")
    void detailRendersLabel() {
      driver.navigate().to(url.toString() + "namespaces/" + seededUid());

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      wait.until(d -> d.getPageSource().contains(DETAIL_MARKER));
      assertThat(driver.getPageSource())
        .as("namespace detail page contents")
        .contains(DETAIL_MARKER);
    }
  }

  // No EditAndSave group: Namespace has no edit route and its detail page is rendered
  // without a YAML editor, so only list/detail and delete are exercised here.
  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "namespaces");
      wait.until(d -> listHasNamespaceRow());
    }

    @Test
    @DisplayName("clicking delete removes the namespace's row from the list")
    void deleteRemovesRow() {
      driver.findElements(ROW).stream()
        .filter(row -> row.getText().contains(NAMESPACE_NAME))
        .findFirst().orElseThrow()
        .findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasNamespaceRow());
      assertThat(listHasNamespaceRow())
        .as("namespace row still present after delete")
        .isFalse();
    }
  }
}
