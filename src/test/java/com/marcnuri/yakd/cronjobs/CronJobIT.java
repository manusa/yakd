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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("CronJob")
public class CronJobIT extends AbstractResourceIT<CronJob> {

  private static final String SEEDED_SCHEDULE = "*/1 * * * *";
  private static final String MODIFIED_SCHEDULE = "*/5 * * * *";

  public CronJobIT() {
    super("cronjobs");
  }

  @Override
  protected CronJob resource(String name) {
    return new CronJobBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .withSchedule(SEEDED_SCHEDULE)
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

  private boolean triggeredJobExists(String name) {
    return kubernetes.getClient().batch().v1().jobs().inNamespace("default").list().getItems().stream()
      .anyMatch(job -> job.getMetadata().getName().startsWith(name + "-manual-"));
  }

  @Nested
  @DisplayName("when viewing the list and detail pages")
  class ListAndDetail {

    private String name;

    @BeforeEach
    void seedResource() {
      name = seed("to-view");
    }

    @Test
    @DisplayName("the list renders a row for the seeded cron job")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded cron job present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded cron job's schedule")
    void listRowShowsSchedule() {
      openList();

      await(() -> rowShows(name, SEEDED_SCHEDULE));
      assertThat(rowShows(name, SEEDED_SCHEDULE))
        .as("seeded schedule shown in the cron job's row")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the cron job's name")
    void detailRendersName() {
      openDetail(seededUid(name));

      awaitPageContains(name);
      assertThat(pageContains(name))
        .as("detail page contents")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when deleting from the list page")
  class DeleteFromList {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-delete");
      openList();
      awaitRow(name);
    }

    @Test
    @DisplayName("clicking delete removes the cron job's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("cron job row still present after delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-edit");
      openEditor(seededUid(name));
      awaitEditorContains("before-edit");
    }

    @Test
    @DisplayName("saving the edited YAML persists the change to the backend")
    void savePersistsChange() {
      editorReplace("before-edit", "after-edit");
      save();

      await(() -> "after-edit".equals(label(name, "mutation-marker")));
      assertThat(label(name, "mutation-marker"))
        .as("mutation-marker label on the persisted cron job")
        .isEqualTo("after-edit");
    }
  }

  @Nested
  @DisplayName("when manually triggering from the detail page")
  class Trigger {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-trigger");
      openDetail(seededUid(name));
      // The trigger action operates on the resource once the watch has loaded it into the
      // store; the name only renders after that, so it is a safe gate before clicking.
      awaitPageContains(name);
    }

    @AfterEach
    void deleteTriggeredJobs() {
      // The manually triggered Job is created by the app, not tracked by the base seed()
      // bookkeeping, so delete it here lest it leak into later Job/Pod list assertions.
      kubernetes.getClient().batch().v1().jobs().inNamespace("default").list().getItems().stream()
        .filter(job -> job.getMetadata().getName().startsWith(name + "-manual-"))
        .forEach(job -> kubernetes.getClient().batch().v1().jobs().inNamespace("default")
          .withName(job.getMetadata().getName()).delete());
    }

    @Test
    @DisplayName("clicking trigger creates a manual job on the backend")
    void triggerCreatesJob() {
      driver.findElement(By.cssSelector("[data-testid='cronjob-detail__trigger']")).click();

      await(() -> triggeredJobExists(name));
      assertThat(triggeredJobExists(name))
        .as("manual job created for the triggered cron job")
        .isTrue();
    }
  }

  @Nested
  @DisplayName("when the server pushes live watch events")
  class WatchLiveUpdates {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-watch");
      openList();
      // Waiting for the seeded row guarantees the watch stream is connected and its initial sync
      // has replayed before the test mutates the cluster, so any later change can only reach the
      // list through a live watch event.
      awaitRow(name);
    }

    @Test
    @DisplayName("a cron job created server-side appears in the list without a refresh")
    void createdCronJobAppears() {
      final String added = seed("watch-added");

      awaitRow(added);
      assertThat(hasRow(added))
        .as("row for the server-side-created cron job present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a cron job deleted server-side disappears from the list without a refresh")
    void deletedCronJobDisappears() {
      kubernetes.getClient().batch().v1().cronjobs().inNamespace("default").withName(name).delete();

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("row for the server-side-deleted cron job still present in the list")
        .isFalse();
    }

    @Test
    @DisplayName("a cron job modified server-side updates its row in place without a refresh")
    void modifiedCronJobUpdatesRow() {
      kubernetes.getClient().batch().v1().cronjobs().inNamespace("default").withName(name)
        .edit(cronJob -> new CronJobBuilder(cronJob)
          .editSpec().withSchedule(MODIFIED_SCHEDULE).endSpec().build());

      await(() -> rowShows(name, MODIFIED_SCHEDULE));
      assertThat(rowShows(name, MODIFIED_SCHEDULE))
        .as("cron job row reflects the server-side schedule change")
        .isTrue();
    }
  }
}
