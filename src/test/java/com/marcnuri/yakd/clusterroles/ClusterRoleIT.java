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
package com.marcnuri.yakd.clusterroles;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.RbacDiscovery;
import com.marcnuri.yakd.selenium.RbacIntegrationTestProfile;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(RbacIntegrationTestProfile.class)
@DisplayName("ClusterRole")
public class ClusterRoleIT extends AbstractResourceIT<ClusterRole> {

  private static final String DETAIL_MARKER = "cluster-role-detail-marker";

  public ClusterRoleIT() {
    super("clusterroles");
  }

  @BeforeEach
  void advertiseRbacDiscovery() {
    // The ClusterRole watcher only subscribes when the cluster's discovery reports clusterroles
    // as supported; the CRUD mock does not advertise them by default.
    RbacDiscovery.advertise(kubernetes);
  }

  @Override
  protected ClusterRole resource(String name) {
    return new ClusterRoleBuilder()
      .withNewMetadata()
        .withName(name)
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .addNewRule()
        .withApiGroups("").withResources("pods").withVerbs("get", "list", "watch")
      .endRule()
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
    @DisplayName("the list renders a row for the seeded cluster role")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded cluster role present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the cluster role's label")
    void detailRendersLabel() {
      openDetail(seededUid(name));

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(DETAIL_MARKER);
      assertThat(pageContains(DETAIL_MARKER))
        .as("cluster role detail page contents")
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
    @DisplayName("clicking delete removes the cluster role's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("cluster role row still present after delete")
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
        .as("mutation-marker label on the persisted cluster role")
        .isEqualTo("after-edit");
    }
  }
}
