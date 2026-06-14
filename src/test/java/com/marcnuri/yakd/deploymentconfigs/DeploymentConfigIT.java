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
package com.marcnuri.yakd.deploymentconfigs;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.OpenShiftDiscovery;
import com.marcnuri.yakd.selenium.OpenShiftIntegrationTestProfile;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(OpenShiftIntegrationTestProfile.class)
@DisplayName("DeploymentConfig")
public class DeploymentConfigIT extends AbstractResourceIT<DeploymentConfig> {

  private static final String SEEDED_IMAGE = "busybox";

  public DeploymentConfigIT() {
    super("deploymentconfigs");
  }

  @BeforeEach
  void advertiseOpenShiftDiscovery() {
    OpenShiftDiscovery.advertise(kubernetes);
  }

  @Override
  protected DeploymentConfig resource(String name) {
    return new DeploymentConfigBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .withReplicas(1)
        .addToSelector("app", name)
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
    final DeploymentConfig deploymentConfig = kubernetes.getClient().resources(DeploymentConfig.class)
      .inNamespace("default").withName(name).get();
    if (deploymentConfig == null || deploymentConfig.getSpec() == null) {
      return null;
    }
    return deploymentConfig.getSpec().getReplicas();
  }

  private String backendRestartedAt(String name) {
    final DeploymentConfig deploymentConfig = kubernetes.getClient().resources(DeploymentConfig.class)
      .inNamespace("default").withName(name).get();
    if (deploymentConfig == null || deploymentConfig.getSpec() == null
      || deploymentConfig.getSpec().getTemplate() == null
      || deploymentConfig.getSpec().getTemplate().getMetadata() == null
      || deploymentConfig.getSpec().getTemplate().getMetadata().getAnnotations() == null) {
      return null;
    }
    return deploymentConfig.getSpec().getTemplate().getMetadata().getAnnotations()
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
    @DisplayName("the list renders a row for the seeded deployment config")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded deployment config present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded deployment config's container image")
    void listRowShowsImage() {
      openList();

      await(() -> rowShows(name, SEEDED_IMAGE));
      assertThat(rowShows(name, SEEDED_IMAGE))
        .as("seeded container image shown in the deployment config's row")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the deployment config's name")
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
    @DisplayName("clicking delete removes the deployment config's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("deployment config row still present after delete")
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
        .as("mutation-marker label on the persisted deployment config")
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
        .as("spec.replicas on the persisted deployment config")
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
      driver.findElement(By.cssSelector("[data-testid='deploymentconfig-list__restart']")).click();

      await(() -> backendRestartedAt(name) != null);
      assertThat(backendRestartedAt(name))
        .as("yakd.marcnuri.com/restartedAt annotation on the persisted deployment config")
        .isNotNull();
    }
  }
}
