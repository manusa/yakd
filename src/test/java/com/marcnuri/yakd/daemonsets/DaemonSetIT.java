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
package com.marcnuri.yakd.daemonsets;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.DaemonSetBuilder;
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
@DisplayName("DaemonSet")
public class DaemonSetIT extends AbstractResourceIT<DaemonSet> {

  private static final String SEEDED_IMAGE = "busybox";

  public DaemonSetIT() {
    super("daemonsets");
  }

  @Override
  protected DaemonSet resource(String name) {
    return new DaemonSetBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .withNewSelector().addToMatchLabels("app", name).endSelector()
        .withNewTemplate()
          .withNewMetadata().addToLabels("app", name).endMetadata()
          .withNewSpec()
            .addNewContainer().withName("container-1").withImage(SEEDED_IMAGE).endContainer()
          .endSpec()
        .endTemplate()
      .endSpec()
      .build();
  }

  private String backendRestartedAt(String name) {
    final DaemonSet daemonSet = kubernetes.getClient().apps().daemonSets()
      .inNamespace("default").withName(name).get();
    if (daemonSet == null || daemonSet.getSpec() == null
      || daemonSet.getSpec().getTemplate() == null
      || daemonSet.getSpec().getTemplate().getMetadata() == null
      || daemonSet.getSpec().getTemplate().getMetadata().getAnnotations() == null) {
      return null;
    }
    return daemonSet.getSpec().getTemplate().getMetadata().getAnnotations()
      .get("yakd.marcnuri.com/restartedAt");
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
    @DisplayName("the list renders a row for the seeded daemon set")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded daemon set present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded daemon set's container image")
    void listRowShowsImage() {
      openList();

      await(() -> rowShows(name, SEEDED_IMAGE));
      assertThat(rowShows(name, SEEDED_IMAGE))
        .as("seeded container image shown in the daemon set's row")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the daemon set's name")
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
    @DisplayName("clicking delete removes the daemon set's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("daemon set row still present after delete")
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
        .as("mutation-marker label on the persisted daemon set")
        .isEqualTo("after-edit");
    }
  }

  @Nested
  @DisplayName("when restarting from the list page")
  class Restart {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-restart");
      openList();
      awaitRow(name);
    }

    @Test
    @DisplayName("clicking restart records a rollout restart on the backend")
    void restartRecordsRestartedAt() {
      driver.findElement(By.cssSelector("[data-testid='daemonset-list__restart']")).click();

      await(() -> backendRestartedAt(name) != null);
      assertThat(backendRestartedAt(name))
        .as("yakd.marcnuri.com/restartedAt annotation on the persisted daemon set")
        .isNotNull();
    }
  }
}
