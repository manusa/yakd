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
package com.marcnuri.yakd.statefulsets;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
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
@DisplayName("StatefulSet")
public class StatefulSetIT extends AbstractResourceIT<StatefulSet> {

  private static final String SEEDED_IMAGE = "busybox";

  public StatefulSetIT() {
    super("statefulsets");
  }

  @Override
  protected StatefulSet resource(String name) {
    return new StatefulSetBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .withReplicas(1)
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

  private Integer backendReplicas(String name) {
    final StatefulSet statefulSet = kubernetes.getClient().apps().statefulSets()
      .inNamespace("default").withName(name).get();
    if (statefulSet == null || statefulSet.getSpec() == null) {
      return null;
    }
    return statefulSet.getSpec().getReplicas();
  }

  private String backendRestartedAt(String name) {
    final StatefulSet statefulSet = kubernetes.getClient().apps().statefulSets()
      .inNamespace("default").withName(name).get();
    if (statefulSet == null || statefulSet.getSpec() == null
      || statefulSet.getSpec().getTemplate() == null
      || statefulSet.getSpec().getTemplate().getMetadata() == null
      || statefulSet.getSpec().getTemplate().getMetadata().getAnnotations() == null) {
      return null;
    }
    return statefulSet.getSpec().getTemplate().getMetadata().getAnnotations()
      .get("kubectl.kubernetes.io/restartedAt");
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
    @DisplayName("the list renders a row for the seeded stateful set")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded stateful set present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded stateful set's container image")
    void listRowShowsImage() {
      openList();

      await(() -> rowShows(name, SEEDED_IMAGE));
      assertThat(rowShows(name, SEEDED_IMAGE))
        .as("seeded container image shown in the stateful set's row")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the stateful set's name")
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
    @DisplayName("clicking delete removes the stateful set's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("stateful set row still present after delete")
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
        .as("mutation-marker label on the persisted stateful set")
        .isEqualTo("after-edit");
    }
  }

  @Nested
  @DisplayName("when scaling from the detail page")
  class Scale {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-scale");
      openDetail(seededUid(name));
      // The replicas control operates on the resource once the watch has loaded it
      // into the store; the name only renders after that, so it is a safe gate.
      awaitPageContains(name);
    }

    @Test
    @DisplayName("incrementing the replicas persists the new replica count to the backend")
    void incrementPersistsReplicas() {
      driver.findElement(By.cssSelector("[data-testid='replicas-field__increment']")).click();

      await(() -> Integer.valueOf(2).equals(backendReplicas(name)));
      assertThat(backendReplicas(name))
        .as("spec.replicas on the persisted stateful set")
        .isEqualTo(2);
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
      driver.findElement(By.cssSelector("[data-testid='statefulset-list__restart']")).click();

      await(() -> backendRestartedAt(name) != null);
      assertThat(backendRestartedAt(name))
        .as("kubectl.kubernetes.io/restartedAt annotation on the persisted stateful set")
        .isNotNull();
    }
  }
}
