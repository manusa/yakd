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
 * Created on 2024-12-17, 12:00
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class PodExecIT {

  private static final String POD_UID = "test-pod-uid-12345";
  private static final String POD_NAME = "test-pod";
  private static final String POD_NAMESPACE = "default";
  private static final String CONTAINER_NAME = "test-container";

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
      .ignoring(NoSuchElementException.class);

    // Create a mock pod with a container for exec functionality
    kubernetes.getClient().pods().inNamespace(POD_NAMESPACE).resource(new PodBuilder()
        .withNewMetadata()
          .withName(POD_NAME)
          .withNamespace(POD_NAMESPACE)
          .withUid(POD_UID)
        .endMetadata()
        .withNewSpec()
          .withContainers(new ContainerBuilder()
            .withName(CONTAINER_NAME)
            .withImage("busybox")
            .build())
        .endSpec()
        .withNewStatus()
          .withPhase("Running")
        .endStatus()
      .build()).createOr(NonDeletingOperation::update);

    // Load the pods list page and wait for the pod terminal link to appear
    driver.get(url.toString() + "pods");
    wait.until(d -> !d.findElements(By.cssSelector("[data-testid='pod-list__terminal-link']")).isEmpty());

    // Navigate to the terminal page by clicking the terminal link
    driver.findElement(By.cssSelector("[data-testid='pod-list__terminal-link']")).click();
    wait.until(d -> d.findElement(By.cssSelector(".dashboard-page")).getText().contains("Terminal"));
  }

  @Test
  void displaysTerminalPageWithPodName() {
    // Verify page title includes Terminal and pod name
    assertThat(driver.findElement(By.cssSelector(".dashboard-page")).getText())
      .contains("Terminal")
      .contains(POD_NAME);
  }

  @Test
  void terminalComponentRenders() {
    // Wait for xterm terminal to render inside the test container
    wait.until(d -> !d.findElements(By.cssSelector("[data-testid='pod-exec__terminal'] .xterm")).isEmpty());

    // Verify xterm container is present and displayed
    assertThat(driver.findElement(By.cssSelector("[data-testid='pod-exec__terminal'] .xterm")).isDisplayed())
      .isTrue();

    // Verify xterm creates a screen element (confirms terminal is initialized)
    wait.until(d -> !d.findElements(By.cssSelector("[data-testid='pod-exec__terminal'] .xterm-screen")).isEmpty());
    assertThat(driver.findElement(By.cssSelector("[data-testid='pod-exec__terminal'] .xterm-screen")).isDisplayed())
      .isTrue();

    // Verify xterm helper textarea is present (used for clipboard and input)
    // This confirms the terminal is fully initialized and interactive
    assertThat(driver.findElements(By.cssSelector("[data-testid='pod-exec__terminal'] .xterm-helper-textarea")))
      .isNotEmpty();
  }

  @Test
  void containerDropdownDisplaysContainerName() {
    // Verify the container dropdown shows the container name
    final var dropdown = driver.findElement(By.cssSelector("[data-testid='container-dropdown']"));
    assertThat(dropdown.getText()).contains(CONTAINER_NAME);
  }
}
