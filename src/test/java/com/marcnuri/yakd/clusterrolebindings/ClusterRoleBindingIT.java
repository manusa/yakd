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
package com.marcnuri.yakd.clusterrolebindings;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.RbacDiscovery;
import com.marcnuri.yakd.selenium.RbacIntegrationTestProfile;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(RbacIntegrationTestProfile.class)
@DisplayName("ClusterRoleBinding")
public class ClusterRoleBindingIT extends AbstractResourceIT<ClusterRoleBinding> {

  private static final String DETAIL_MARKER = "cluster-role-binding-detail-marker";

  public ClusterRoleBindingIT() {
    super("clusterrolebindings");
  }

  @BeforeEach
  void advertiseRbacDiscovery() {
    // The ClusterRoleBinding watcher only subscribes when the cluster's discovery reports
    // clusterrolebindings as supported; the CRUD mock does not advertise them by default.
    RbacDiscovery.advertise(kubernetes);
  }

  @Override
  protected ClusterRoleBinding resource(String name) {
    return new ClusterRoleBindingBuilder()
      .withNewMetadata()
        .withName(name)
        .addToLabels("mutation-marker", "before-edit")
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
      .withNewRoleRef()
        .withApiGroup("rbac.authorization.k8s.io").withKind("ClusterRole").withName("it-cluster-role")
      .endRoleRef()
      .addNewSubject()
        .withApiGroup("rbac.authorization.k8s.io").withKind("User").withName("it-user")
      .endSubject()
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
    @DisplayName("the list renders a row for the seeded cluster role binding")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded cluster role binding present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the cluster role binding's label")
    void detailRendersLabel() {
      openDetail(seededUid(name));

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(DETAIL_MARKER);
      assertThat(pageContains(DETAIL_MARKER))
        .as("cluster role binding detail page contents")
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
    @DisplayName("clicking delete removes the cluster role binding's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("cluster role binding row still present after delete")
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
        .as("mutation-marker label on the persisted cluster role binding")
        .isEqualTo("after-edit");
    }
  }
}
