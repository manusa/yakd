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
package com.marcnuri.yakd.replicationcontrollers;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("ReplicationController")
public class ReplicationControllerIT extends AbstractResourceIT<ReplicationController> {

  private static final String DETAIL_MARKER = "replication-controller-detail-marker";

  public ReplicationControllerIT() {
    super("replicationcontrollers");
  }

  @Override
  protected ReplicationController resource(String name) {
    return new ReplicationControllerBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .withNewSpec()
        .withReplicas(1)
        .addToSelector("app", name)
        .withNewTemplate()
          .withNewMetadata().addToLabels("app", name).endMetadata()
          .withNewSpec()
            .addNewContainer().withName("container-1").withImage("busybox").endContainer()
          .endSpec()
        .endTemplate()
      .endSpec()
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
    @DisplayName("the list renders a row for the seeded replication controller")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded replication controller present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the replication controller's label")
    void detailRendersLabel() {
      openDetail(seededUid(name));

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(DETAIL_MARKER);
      assertThat(pageContains(DETAIL_MARKER))
        .as("replication controller detail page contents")
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
    @DisplayName("clicking delete removes the replication controller's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("replication controller row still present after delete")
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
        .as("mutation-marker label on the persisted replication controller")
        .isEqualTo("after-edit");
    }
  }
}
