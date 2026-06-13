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
package com.marcnuri.yakd.persistentvolumes;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("PersistentVolume")
public class PersistentVolumeIT extends AbstractResourceIT<PersistentVolume> {

  private static final String DETAIL_MARKER = "persistent-volume-detail-marker";
  private static final String MODIFIED_PHASE = "Released";

  public PersistentVolumeIT() {
    super("persistentvolumes");
  }

  @Override
  protected PersistentVolume resource(String name) {
    return new PersistentVolumeBuilder()
      .withNewMetadata()
        .withName(name)
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .withNewSpec()
        .addToCapacity("storage", new Quantity("1Gi"))
        .withAccessModes("ReadWriteOnce")
        .withStorageClassName("it-storage-class")
        .withPersistentVolumeReclaimPolicy("Retain")
        .withNewHostPath().withPath("/tmp/" + name).endHostPath()
      .endSpec()
      .withNewStatus().withPhase("Available").endStatus()
      .build();
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
    @DisplayName("the list renders a row for the seeded persistent volume")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded persistent volume present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the persistent volume's label")
    void detailRendersLabel() {
      openDetail(seededUid(name));

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(DETAIL_MARKER);
      assertThat(pageContains(DETAIL_MARKER))
        .as("persistent volume detail page contents")
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
    @DisplayName("clicking delete removes the persistent volume's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("persistent volume row still present after delete")
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
        .as("mutation-marker label on the persisted persistent volume")
        .isEqualTo("after-edit");
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
      // Waiting for the seeded row guarantees the watch stream is connected and its
      // initial sync replayed before the test mutates the cluster, so any later change
      // can only reach the list through a live watch event.
      awaitRow(name);
    }

    @Test
    @DisplayName("a persistent volume created server-side appears in the list without a refresh")
    void createdPersistentVolumeAppears() {
      final String added = seed("watch-added");

      awaitRow(added);
      assertThat(hasRow(added))
        .as("row for the server-side-created persistent volume present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a persistent volume deleted server-side disappears from the list without a refresh")
    void deletedPersistentVolumeDisappears() {
      kubernetes.getClient().persistentVolumes().withName(name).delete();

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("row for the server-side-deleted persistent volume still present in the list")
        .isFalse();
    }

    @Test
    @DisplayName("a persistent volume modified server-side updates its row in place without a refresh")
    void modifiedPersistentVolumeUpdatesRow() {
      kubernetes.getClient().persistentVolumes().withName(name)
        .edit(pv -> new PersistentVolumeBuilder(pv)
          .editOrNewStatus().withPhase(MODIFIED_PHASE).endStatus()
          .build());

      await(() -> rowShows(name, MODIFIED_PHASE));
      assertThat(rowShows(name, MODIFIED_PHASE))
        .as("persistent volume row reflects the server-side phase change")
        .isTrue();
    }
  }
}
