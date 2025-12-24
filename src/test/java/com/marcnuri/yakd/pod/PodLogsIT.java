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
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class PodLogsIT {

  private static final String POD_UID = "test-pod-uid-logs";
  private static final String POD_NAME = "test-pod-logs";
  private static final String POD_NAMESPACE = "default";

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
    driver.switchTo().newWindow(WindowType.TAB);
    driver.navigate().to(url.toString() + "pods");
    wait.until(d -> !d.findElements(By.cssSelector("[data-testid='pod-list__logs-link']")).isEmpty());

    // Navigate to the logs page by clicking the logs link
    driver.findElement(By.cssSelector("[data-testid='pod-list__logs-link']")).click();
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page [data-testid='pod-logs__content']")).isDisplayed());
  }

  @AfterEach
  void tearDown() {
    // Close the current tab and switch back to the original
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  @Test
  void displaysLogsPageWithPodName() {
    assertThat(driver.findElement(By.cssSelector(".dashboard-page")).getText())
      .contains("Logs")
      .contains(POD_NAME);
  }

  @Test
  void displaysFirstContainerLogs() {
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page [data-testid='pod-logs__content']")).getText().contains("Hello from container 1 logs!"));
    assertThat(driver.findElement(By.cssSelector(".pods-logs-page [data-testid='pod-logs__content']")).getText())
      .contains("Hello from container 1 logs!");
  }

  @Test
  void containerDropdownDisplaysFirstContainerName() {
    final var dropdown = driver.findElement(By.cssSelector("[data-testid='container-dropdown']"));
    assertThat(dropdown.getText()).contains("container-1");
  }

  @Test
  void switchingContainerDisplaysCorrectLogs() {
    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page [data-testid='pod-logs__content']")).getText().contains("Hello from container 1 logs!"));

    driver.findElement(By.cssSelector("[data-testid='container-dropdown']")).click();
    wait.until(d -> !d.findElements(By.xpath("//*[contains(text(), 'container-2')]")).isEmpty());
    driver.findElement(By.xpath("//*[contains(text(), 'container-2')]")).click();

    wait.until(d -> d.findElement(By.cssSelector(".pods-logs-page [data-testid='pod-logs__content']")).getText().contains("Hello from container 2 logs!"));
    assertThat(driver.findElement(By.cssSelector(".pods-logs-page [data-testid='pod-logs__content']")).getText())
      .contains("Hello from container 2 logs!");
  }
}
