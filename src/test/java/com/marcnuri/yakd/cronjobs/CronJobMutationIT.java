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

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("CronJob mutation paths")
public class CronJobMutationIT extends AbstractResourceIT<CronJob> {

  private static final By TOGGLE = By.cssSelector("[data-testid='cronjob-detail__suspend-toggle']");

  public CronJobMutationIT() {
    super("cronjobs");
  }

  @Override
  protected CronJob resource(String name) {
    return new CronJobBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
      .endMetadata()
      .withNewSpec()
        .withSchedule("*/1 * * * *")
        .withSuspend(false)
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

  // The toggle renders fa-pause-circle for an undefined resource too (specSuspend(undefined) ===
  // false), so the name only renders once the resource is loaded and is the safe gate before
  // trusting the toggle state.
  private void navigateToDetail(String name) {
    openDetail(seededUid(name));
    awaitPageContains(name);
  }

  // The toggle renders fa-pause-circle while running and fa-play-circle while suspended,
  // so its icon class is a reliable gate for the loaded state before clicking.
  private boolean toggleShows(String iconClass) {
    return driver.findElements(TOGGLE).stream()
      .anyMatch(e -> e.getAttribute("class").contains(iconClass));
  }

  private Boolean backendSuspended(String name) {
    final CronJob cronJob = kubernetes.getClient().batch().v1().cronjobs()
      .inNamespace("default").withName(name).get();
    if (cronJob == null || cronJob.getSpec() == null) {
      return null;
    }
    return cronJob.getSpec().getSuspend();
  }

  @Nested
  @DisplayName("when suspending a running cronjob")
  class Suspend {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-suspend");
      navigateToDetail(name);
      await(() -> toggleShows("fa-pause-circle"));
    }

    @Test
    @DisplayName("clicking the toggle suspends the cronjob in the backend")
    void toggleSuspends() {
      driver.findElement(TOGGLE).click();

      await(() -> Boolean.TRUE.equals(backendSuspended(name)));
      assertThat(backendSuspended(name))
        .as("spec.suspend on the persisted cronjob")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when resuming a suspended cronjob")
  class Resume {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-resume");
      kubernetes.getClient().batch().v1().cronjobs().inNamespace("default").withName(name)
        .edit(cronJob -> new CronJobBuilder(cronJob).editSpec().withSuspend(true).endSpec().build());
      navigateToDetail(name);
      await(() -> toggleShows("fa-play-circle"));
    }

    @Test
    @DisplayName("clicking the toggle resumes the cronjob in the backend")
    void toggleResumes() {
      driver.findElement(TOGGLE).click();

      await(() -> Boolean.FALSE.equals(backendSuspended(name)));
      assertThat(backendSuspended(name))
        .as("spec.suspend on the persisted cronjob")
        .isFalse();
    }
  }
}
