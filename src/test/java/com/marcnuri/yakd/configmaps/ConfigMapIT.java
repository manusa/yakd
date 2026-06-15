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
package com.marcnuri.yakd.configmaps;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import com.marcnuri.yakd.selenium.SeleniumTestResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("ConfigMap")
public class ConfigMapIT extends AbstractResourceIT<ConfigMap> {

  private static final String DETAIL_MARKER = "config-map-detail-marker";

  @ConfigProperty(name = SeleniumTestResource.DOWNLOAD_DIRECTORY_PROPERTY)
  String downloadDirectory;

  public ConfigMapIT() {
    super("configmaps");
  }

  @Override
  protected ConfigMap resource(String name) {
    return new ConfigMapBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .addToData("detail-key", DETAIL_MARKER)
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
    @DisplayName("the list renders a row for the seeded config map")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded config map present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the config map's data value")
    void detailRendersDataValue() {
      openDetail(seededUid(name));

      // The data value can only come from the seeded resource's data map, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(DETAIL_MARKER);
      assertThat(pageContains(DETAIL_MARKER))
        .as("config map detail page contents")
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
    @DisplayName("clicking delete removes the config map's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("config map row still present after delete")
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
        .as("mutation-marker label on the persisted config map")
        .isEqualTo("after-edit");
    }
  }

  @Nested
  @DisplayName("when deleting from the detail page action menu")
  class DeleteFromDetail {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-delete-detail");
      openDetail(seededUid(name));
      // The name only renders once the detail page has loaded the resource, so it gates the action.
      awaitPageContains(name);
    }

    @Test
    @DisplayName("clicking delete removes the config map from the backend")
    void deleteRemovesResource() {
      deleteFromDetail();

      await(() -> !exists(name));
      assertThat(exists(name))
        .as("config map still present in the backend after detail-page delete")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when downloading from the detail page action menu")
  class DownloadFromDetail {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-download");
      openDetail(seededUid(name));
      awaitPageContains(name);
    }

    @Test
    @DisplayName("clicking download writes the config map YAML to a file")
    void downloadWritesYaml() throws Exception {
      downloadFromDetail();

      final Path downloaded = Path.of(downloadDirectory, name + ".yaml");
      await(() -> Files.exists(downloaded));
      assertThat(Files.readString(downloaded))
        .as("downloaded config map YAML contents")
        .contains(DETAIL_MARKER);
    }
  }
}
