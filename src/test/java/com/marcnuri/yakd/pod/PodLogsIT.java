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
 * Created on 2024-12-21, 12:00
 */
package com.marcnuri.yakd.pod;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class PodLogsIT {

  private static final String POD_UID = "test-pod-uid-logs";
  private static final String POD_NAME = "test-pod";
  private static final String POD_NAMESPACE = "default";

  @KubernetesTestServer
  KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;
  WebDriver driver;
  Wait<WebDriver> wait;

  @BeforeEach
  void setUp() {
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    // Create a mock pod with two containers
    kubernetes.getClient().pods().inNamespace(POD_NAMESPACE).resource(new PodBuilder()
        .withNewMetadata()
          .withName(POD_NAME)
          .withNamespace(POD_NAMESPACE)
          .withUid(POD_UID)
        .endMetadata()
        .withNewSpec()
          .withContainers(
            new ContainerBuilder()
              .withName("container-1")
              .withImage("busybox")
              .build(),
            new ContainerBuilder()
              .withName("container-2")
              .withImage("busybox")
              .build())
        .endSpec()
        .withNewStatus()
          .withPhase("Running")
        .endStatus()
      .build()).createOr(NonDeletingOperation::update);

    // Mock log endpoints for both containers with different messages
    kubernetes.expect().get()
      .withPath("/api/v1/namespaces/" + POD_NAMESPACE + "/pods/" + POD_NAME + "/log?pretty=true&container=container-1&timestamps=true&follow=true")
      .andReturn(200, "Hello from container 1 logs!")
      .always();
    kubernetes.expect().get()
      .withPath("/api/v1/namespaces/" + POD_NAMESPACE + "/pods/" + POD_NAME + "/log?pretty=true&container=container-2&timestamps=true&follow=true")
      .andReturn(200, "Hello from container 2 logs!")
      .always();

    // Load the pods list page and wait for the pod logs link to appear
    driver.get(url.toString() + "pods");
    wait.until(d -> !d.findElements(By.cssSelector("[data-testid='pod-list__logs-link']")).isEmpty());

    // Navigate to the logs page by clicking the logs link
    driver.findElement(By.cssSelector("[data-testid='pod-list__logs-link']")).click();
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page")).isDisplayed());
  }

  @Test
  void displaysLogsPageWithPodName() {
    assertThat(driver.findElement(By.cssSelector(".dashboard-page")).getText())
      .contains("Logs")
      .contains(POD_NAME);
  }

  @Test
  void displaysFirstContainerLogs() {
    // Wait for log content from container-1 to appear
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page")).getText().contains("Hello from container 1 logs!"));

    assertThat(driver.findElement(By.cssSelector(".pods-logs-page")).getText())
      .contains("Hello from container 1 logs!");
  }

  @Test
  void containerDropdownDisplaysFirstContainerName() {
    final var dropdown = driver.findElement(By.cssSelector("[data-testid='container-dropdown']"));
    assertThat(dropdown.getText()).contains("container-1");
  }

  @Test
  void switchingContainerDisplaysCorrectLogs() {
    // Wait for initial logs from container-1
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page")).getText().contains("Hello from container 1 logs!"));

    // Click on the container dropdown to open it
    driver.findElement(By.cssSelector("[data-testid='container-dropdown']")).click();

    // Wait for dropdown to open and click on container-2
    wait.until(d -> !d.findElements(By.xpath("//*[contains(text(), 'container-2')]")).isEmpty());
    driver.findElement(By.xpath("//*[contains(text(), 'container-2')]")).click();

    // Wait for logs from container-2 to appear
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page")).getText().contains("Hello from container 2 logs!"));

    assertThat(driver.findElement(By.cssSelector(".pods-logs-page")).getText())
      .contains("Hello from container 2 logs!");
  }
}