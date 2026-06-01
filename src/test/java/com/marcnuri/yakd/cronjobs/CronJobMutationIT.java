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
package com.marcnuri.yakd.cronjobs;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
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
@DisplayName("CronJob mutation paths")
public class CronJobMutationIT {

  private static final String NAMESPACE = "default";
  private static final String CRONJOB_NAME = "mutation-it-cronjob";
  private static final By TOGGLE = By.cssSelector("[data-testid='cronjob-detail__suspend-toggle']");

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
    kubernetes.getClient().batch().v1().cronjobs().inNamespace(NAMESPACE)
      .resource(cronJob(false)).createOr(NonDeletingOperation::update);
    driver.switchTo().newWindow(WindowType.TAB);
  }

  @AfterEach
  void tearDown() {
    kubernetes.getClient().batch().v1().cronjobs().inNamespace(NAMESPACE).delete();
    driver.close();
    driver.switchTo().window(driver.getWindowHandles().iterator().next());
  }

  private static CronJob cronJob(boolean suspend) {
    return new CronJobBuilder()
      .withNewMetadata()
        .withName(CRONJOB_NAME)
        .withNamespace(NAMESPACE)
      .endMetadata()
      .withNewSpec()
        .withSchedule("*/1 * * * *")
        .withSuspend(suspend)
        .withNewJobTemplate()
          .withNewSpec()
            .withNewTemplate()
              .withNewSpec()
                .addNewContainer().withName("container-1").withImage("busybox").endContainer()
                .withRestartPolicy("OnFailure")
              .endSpec()
            .endTemplate()
          .endSpec()
        .endJobTemplate()
      .endSpec()
      .build();
  }

  // Navigate to the detail page of the seeded CronJob and wait until the watch has
  // loaded it into the store. The toggle renders fa-pause-circle for an undefined
  // resource too (specSuspend(undefined) === false), so the name only renders once
  // the resource is loaded and is the safe gate before trusting the toggle state.
  private void navigateToDetail() {
    final CronJob seeded = kubernetes.getClient().batch().v1().cronjobs()
      .inNamespace(NAMESPACE).withName(CRONJOB_NAME).get();
    assertThat(seeded).as("seeded cronjob available before navigating").isNotNull();
    driver.navigate().to(url.toString() + "cronjobs/" + seeded.getMetadata().getUid());
    wait.until(d -> d.getPageSource().contains(CRONJOB_NAME));
  }

  // The toggle renders fa-pause-circle while running and fa-play-circle while suspended,
  // so its icon class is a reliable gate for the loaded state before clicking.
  private boolean toggleShows(String iconClass) {
    return driver.findElements(TOGGLE).stream()
      .anyMatch(e -> e.getAttribute("class").contains(iconClass));
  }

  private Boolean backendSuspended() {
    final CronJob cronJob = kubernetes.getClient().batch().v1().cronjobs()
      .inNamespace(NAMESPACE).withName(CRONJOB_NAME).get();
    if (cronJob == null || cronJob.getSpec() == null) {
      return null;
    }
    return cronJob.getSpec().getSuspend();
  }

  @Nested
  @DisplayName("when suspending a running cronjob")
  class Suspend {

    @BeforeEach
    void navigate() {
      navigateToDetail();
      wait.until(d -> toggleShows("fa-pause-circle"));
    }

    @Test
    @DisplayName("clicking the toggle suspends the cronjob in the backend")
    void toggleSuspends() {
      driver.findElement(TOGGLE).click();

      wait.until(d -> Boolean.TRUE.equals(backendSuspended()));
      assertThat(backendSuspended())
        .as("spec.suspend on the persisted cronjob")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when resuming a suspended cronjob")
  class Resume {

    @BeforeEach
    void navigate() {
      kubernetes.getClient().batch().v1().cronjobs().inNamespace(NAMESPACE).withName(CRONJOB_NAME)
        .edit(cronJob -> new CronJobBuilder(cronJob).editSpec().withSuspend(true).endSpec().build());
      navigateToDetail();
      wait.until(d -> toggleShows("fa-play-circle"));
    }

    @Test
    @DisplayName("clicking the toggle resumes the cronjob in the backend")
    void toggleResumes() {
      driver.findElement(TOGGLE).click();

      wait.until(d -> Boolean.FALSE.equals(backendSuspended()));
      assertThat(backendSuspended())
        .as("spec.suspend on the persisted cronjob")
        .isFalse();
    }
  }
}
