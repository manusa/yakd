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
package com.marcnuri.yakd.endpoints;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsBuilder;
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
@DisplayName("Endpoint")
public class EndpointIT {

  private static final String NAMESPACE = "default";
  private static final String ENDPOINT_NAME = "it-endpoint";
  private static final String ADDRESS_IP = "10.40.50.60";
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
    kubernetes.getClient().endpoints().inNamespace(NAMESPACE)
      .resource(endpoint()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().endpoints().inNamespace(NAMESPACE).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static Endpoints endpoint() {
    return new EndpointsBuilder()
      .withNewMetadata()
        .withName(ENDPOINT_NAME)
        .withNamespace(NAMESPACE)
      .endMetadata()
      .addNewSubset()
        .addNewAddress()
          .withIp(ADDRESS_IP)
          // The detail page's Target component dereferences address.targetRef.kind without a
          // null guard, so the address must carry a targetRef or the detail render would throw.
          .withNewTargetRef()
            .withKind("Pod").withName("it-target-pod").withUid("it-target-pod-uid")
          .endTargetRef()
        .endAddress()
        .addNewPort().withName("http").withPort(8080).withProtocol("TCP").endPort()
      .endSubset()
      .build();
  }

  private boolean listHasEndpointRow() {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(ENDPOINT_NAME));
  }

  private String seededUid() {
    final Endpoints seeded = kubernetes.getClient().endpoints()
      .inNamespace(NAMESPACE).withName(ENDPOINT_NAME).get();
    assertThat(seeded).as("seeded endpoint available").isNotNull();
    return seeded.getMetadata().getUid();
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded endpoint")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "endpoints");

      wait.until(d -> listHasEndpointRow());
      assertThat(listHasEndpointRow())
        .as("row for the seeded endpoint present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the endpoint's subset address")
    void detailRendersAddress() {
      driver.navigate().to(url.toString() + "endpoints/" + seededUid());

      // The address IP can only come from the seeded resource's subsets, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      wait.until(d -> d.getPageSource().contains(ADDRESS_IP));
      assertThat(driver.getPageSource())
        .as("endpoint detail page contents")
        .contains(ADDRESS_IP);
    }
  }

  // No EditAndSave group: the Endpoints detail page is rendered with editable=false
  // (the resource exposes no update endpoint), so there is no YAML editor to exercise.
  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "endpoints");
      wait.until(d -> listHasEndpointRow());
    }

    @Test
    @DisplayName("clicking delete removes the endpoint's row from the list")
    void deleteRemovesRow() {
      driver.findElements(ROW).stream()
        .filter(row -> row.getText().contains(ENDPOINT_NAME))
        .findFirst().orElseThrow()
        .findElement(By.cssSelector("[data-testid='resource-list__delete']")).click();

      wait.until(d -> !listHasEndpointRow());
      assertThat(listHasEndpointRow())
        .as("endpoint row still present after delete")
        .isFalse();
    }
  }
}
