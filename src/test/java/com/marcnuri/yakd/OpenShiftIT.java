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
package com.marcnuri.yakd;

import com.marcnuri.yakd.selenium.OpenShiftIntegrationTestProfile;
import io.fabric8.kubernetes.api.model.APIGroupBuilder;
import io.fabric8.kubernetes.api.model.APIGroupListBuilder;
import io.fabric8.kubernetes.api.model.APIResourceListBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
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
@TestProfile(OpenShiftIntegrationTestProfile.class)
@DisplayName("OpenShift mode")
public class OpenShiftIT {

  private static final String NAMESPACE = "default";
  private static final String ROUTE_NAME = "openshift-it-route";
  private static final String ROUTE_HOST = "openshift-it.example.com";
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
    // The frontend flips to OpenShift mode when the backend reports an
    // *.openshift.io API group (served from the cluster's /apis discovery).
    kubernetes.expect().get().withPath("/apis").andReturn(200, new APIGroupListBuilder()
        .addToGroups(new APIGroupBuilder().withName("route.openshift.io").build())
        .addToGroups(new APIGroupBuilder().withName("apps.openshift.io").build())
        .build())
      .always();
    // The backend only subscribes its Route/DeploymentConfig watchers when the
    // cluster's version discovery reports the resource as supported.
    kubernetes.expect().get().withPath("/apis/route.openshift.io/v1").andReturn(200, new APIResourceListBuilder()
        .withGroupVersion("route.openshift.io/v1")
        .addNewResource().withName("routes").withKind("Route").withNamespaced(true).endResource()
        .build())
      .always();
    kubernetes.expect().get().withPath("/apis/apps.openshift.io/v1").andReturn(200, new APIResourceListBuilder()
        .withGroupVersion("apps.openshift.io/v1")
        .addNewResource().withName("deploymentconfigs").withKind("DeploymentConfig").withNamespaced(true).endResource()
        .build())
      .always();
    kubernetes.getClient().resources(Route.class).inNamespace(NAMESPACE)
      .resource(route()).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().resources(Route.class).inNamespace(NAMESPACE).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static Route route() {
    return new RouteBuilder()
      .withNewMetadata()
        .withName(ROUTE_NAME)
        .withNamespace(NAMESPACE)
      .endMetadata()
      .withNewSpec()
        .withHost(ROUTE_HOST)
        .withPath("/openshift-it")
        .withNewTo().withKind("Service").withName("openshift-it-service").endTo()
      .endSpec()
      .build();
  }

  private boolean listHasRow(String text) {
    return driver.findElements(ROW).stream()
      .anyMatch(row -> row.getText().contains(text));
  }

  @Nested
  @DisplayName("when rendering the side bar navigation")
  class SideBarNav {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString());
    }

    @Test
    @DisplayName("the side bar exposes the Routes navigation item")
    void exposesRoutesNavItem() {
      // Conditional rendering is the behavior under test: the item only exists in
      // the DOM at all when the frontend has detected OpenShift mode.
      wait.until(d -> !d.findElements(By.cssSelector("[data-testid='side-bar__nav-routes']")).isEmpty());
      assertThat(driver.findElements(By.cssSelector("[data-testid='side-bar__nav-routes']")))
        .as("Routes side bar navigation item")
        .isNotEmpty();
    }

    @Test
    @DisplayName("the side bar exposes the Deployment Configs navigation item")
    void exposesDeploymentConfigsNavItem() {
      wait.until(d -> !d.findElements(By.cssSelector("[data-testid='side-bar__nav-deploymentconfigs']")).isEmpty());
      assertThat(driver.findElements(By.cssSelector("[data-testid='side-bar__nav-deploymentconfigs']")))
        .as("Deployment Configs side bar navigation item")
        .isNotEmpty();
    }
  }

  @Nested
  @DisplayName("when viewing the Route list and detail pages")
  class RouteListAndDetail {

    @Test
    @DisplayName("the list renders a row for the seeded route")
    void listRendersSeededRow() {
      driver.navigate().to(url.toString() + "routes");

      wait.until(d -> listHasRow(ROUTE_NAME));
      assertThat(listHasRow(ROUTE_NAME))
        .as("row for the seeded route present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the route's host")
    void listRowShowsHost() {
      driver.navigate().to(url.toString() + "routes");

      wait.until(d -> listHasRow(ROUTE_HOST));
      assertThat(listHasRow(ROUTE_HOST))
        .as("seeded host shown in the route's row")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the route's host")
    void detailRendersHost() {
      final Route seeded = kubernetes.getClient().resources(Route.class)
        .inNamespace(NAMESPACE).withName(ROUTE_NAME).get();
      assertThat(seeded).as("seeded route available before navigating").isNotNull();

      driver.navigate().to(url.toString() + "routes/" + seeded.getMetadata().getUid());

      wait.until(d -> d.getPageSource().contains(ROUTE_HOST));
      assertThat(driver.getPageSource())
        .as("route detail page contents")
        .contains(ROUTE_HOST);
    }
  }
}
