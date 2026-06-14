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
package com.marcnuri.yakd.customresourcedefinitions;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// List/detail rendering is covered by ClusterScopedResourceIT; this exercises the remaining
// mutation paths (delete, edit-save) and the live watch for the cluster-scoped CRD list.
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("CustomResourceDefinition")
public class CustomResourceDefinitionIT extends AbstractResourceIT<CustomResourceDefinition> {

  public CustomResourceDefinitionIT() {
    super("customresourcedefinitions");
  }

  @Override
  protected CustomResourceDefinition resource(String name) {
    return new CustomResourceDefinitionBuilder()
      .withNewMetadata()
        .withName(name)
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .withGroup("yakd.example.com")
        .withScope("Cluster")
        .withNewNames().withKind("Widget").withSingular("widget").withPlural("widgets").endNames()
        .addNewVersion().withName("v1").withServed(true).withStorage(true).endVersion()
      .endSpec()
      .build();
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
    @DisplayName("clicking delete removes the custom resource definition's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("custom resource definition row still present after delete")
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
        .as("mutation-marker label on the persisted custom resource definition")
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
      // Waiting for the seeded row guarantees the watch stream is connected and its initial sync
      // has replayed before the test mutates the cluster, so any later change can only reach the
      // list through a live watch event.
      awaitRow(name);
    }

    @Test
    @DisplayName("a custom resource definition created server-side appears in the list without a refresh")
    void createdCustomResourceDefinitionAppears() {
      final String added = seed("watch-added");

      awaitRow(added);
      assertThat(hasRow(added))
        .as("row for the server-side-created custom resource definition present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a custom resource definition deleted server-side disappears from the list without a refresh")
    void deletedCustomResourceDefinitionDisappears() {
      kubernetes.getClient().apiextensions().v1().customResourceDefinitions().withName(name).delete();

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("row for the server-side-deleted custom resource definition still present in the list")
        .isFalse();
    }
  }
}
