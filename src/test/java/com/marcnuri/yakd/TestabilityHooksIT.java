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
 * Created on 2026-05-31, 12:00
 */
package com.marcnuri.yakd;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("The shared list/detail/edit testability hooks")
public class TestabilityHooksIT {

  private static final String DEPLOYMENT_UID = "test-deployment-uid-hooks";
  private static final String DEPLOYMENT_NAME = "test-deployment-hooks";
  private static final String DEPLOYMENT_NAMESPACE = "default";

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

    kubernetes.getClient().apps().deployments().inNamespace(DEPLOYMENT_NAMESPACE).resource(new DeploymentBuilder()
        .withNewMetadata()
          .withName(DEPLOYMENT_NAME)
          .withNamespace(DEPLOYMENT_NAMESPACE)
          .withUid(DEPLOYMENT_UID)
        .endMetadata()
        .withNewSpec()
          .withReplicas(1)
          .withNewTemplate()
            .withNewSpec()
              .withContainers(
                new ContainerBuilder()
                  .withName("container-1")
                  .withImage("busybox")
                  .build())
            .endSpec()
          .endTemplate()
        .endSpec()
        .withNewStatus()
          .withReplicas(1)
          .withReadyReplicas(1)
        .endStatus()
      .build()).createOr(NonDeletingOperation::update);

    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  @Nested
  @DisplayName("when the resource list page is loaded")
  class ListPage {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "deployments");
      wait.until(d -> !d.findElements(By.cssSelector("[data-testid='resource-list__row']")).isEmpty());
    }

    @Test
    @DisplayName("the shared list container exposes the resource-list hook")
    void listContainerExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='resource-list']")))
        .as("resource-list container hook")
        .isNotEmpty();
    }

    @Test
    @DisplayName("each row exposes the resource-list__row hook")
    void rowExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='resource-list__row']")))
        .as("resource-list__row hook")
        .isNotEmpty();
    }

    @Test
    @DisplayName("the delete action exposes the resource-list__delete hook")
    void deleteActionExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='resource-list__delete']")))
        .as("resource-list__delete hook")
        .isNotEmpty();
    }
  }

  @Nested
  @DisplayName("when the resource detail page is loaded")
  class DetailPage {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "deployments/" + DEPLOYMENT_UID);
      wait.until(d -> !d.findElements(By.cssSelector("[data-testid='popup-menu__trigger']")).isEmpty());
    }

    @Test
    @DisplayName("the action menu exposes the popup-menu__trigger hook")
    void menuTriggerExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='popup-menu__trigger']")))
        .as("popup-menu__trigger hook")
        .isNotEmpty();
    }

    @Test
    @DisplayName("the action menu exposes the resource-detail__delete hook")
    void deleteItemExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='resource-detail__delete']")))
        .as("resource-detail__delete hook")
        .isNotEmpty();
    }

    @Test
    @DisplayName("the action menu exposes the resource-detail__download hook")
    void downloadItemExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='resource-detail__download']")))
        .as("resource-detail__download hook")
        .isNotEmpty();
    }
  }

  @Nested
  @DisplayName("when the resource edit page is loaded")
  class EditPage {

    @BeforeEach
    void navigate() {
      driver.navigate().to(url.toString() + "deployments/" + DEPLOYMENT_UID + "/edit");
      wait.until(d -> !d.findElements(By.cssSelector("[data-testid='resource-edit__save']")).isEmpty());
    }

    @Test
    @DisplayName("the edit page exposes the resource-edit__save hook")
    void saveActionExposesHook() {
      assertThat(driver.findElements(By.cssSelector("[data-testid='resource-edit__save']")))
        .as("resource-edit__save hook")
        .isNotEmpty();
    }
  }
}
