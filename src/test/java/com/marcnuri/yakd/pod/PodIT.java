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
package com.marcnuri.yakd.pod;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Pod")
public class PodIT extends AbstractResourceIT<Pod> {

  private static final String SEEDED_PHASE = "Running";
  private static final String MODIFIED_PHASE = "Succeeded";

  public PodIT() {
    super("pods");
  }

  @Override
  protected Pod resource(String name) {
    return new PodBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewSpec()
        .addNewContainer().withName("container-1").withImage("busybox").endContainer()
      .endSpec()
      .withNewStatus().withPhase(SEEDED_PHASE).endStatus()
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
    @DisplayName("the list renders a row for the seeded pod")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded pod present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the list row shows the seeded pod's phase")
    void listRowShowsPhase() {
      openList();

      await(() -> rowShows(name, SEEDED_PHASE));
      assertThat(rowShows(name, SEEDED_PHASE))
        .as("seeded phase shown in the pod's row")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the pod's name")
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
    @DisplayName("clicking delete removes the pod's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("pod row still present after delete")
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
        .as("mutation-marker label on the persisted pod")
        .isEqualTo("after-edit");
    }
  }

  @Nested
  @DisplayName("when viewing metrics on the detail page")
  class Metrics {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-measure");
      // The detail page polls /pods/<ns>/<name>/metrics, which the backend proxies to the
      // metrics.k8s.io API; seed a PodMetrics there so the page can render the usage.
      kubernetes.expect().get()
        .withPath("/apis/metrics.k8s.io/v1beta1/namespaces/default/pods/" + name)
        .andReturn(200, new PodMetricsBuilder()
          .withNewMetadata().withName(name).withNamespace("default").endMetadata()
          .addNewContainer()
            .withName("container-1")
            .addToUsage("cpu", new Quantity("123m"))
            .addToUsage("memory", new Quantity("321Mi"))
          .endContainer()
          .build())
        .always();
      openDetail(seededUid(name));
      awaitPageContains(name);
    }

    @Test
    @DisplayName("the detail page renders the summed container CPU usage")
    void rendersUsedCpu() {
      // 123m summed and formatted to three decimals by the UI.
      awaitPageContains("0.123");
      assertThat(pageContains("0.123"))
        .as("used CPU rendered from the seeded pod metrics")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the summed container memory usage")
    void rendersUsedMemory() {
      // 321Mi rendered human-readable by the UI.
      awaitPageContains("321 MiB");
      assertThat(pageContains("321 MiB"))
        .as("used memory rendered from the seeded pod metrics")
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
    @DisplayName("a pod created server-side appears in the list without a refresh")
    void createdPodAppears() {
      final String added = seed("watch-added");

      awaitRow(added);
      assertThat(hasRow(added))
        .as("row for the server-side-created pod present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a pod deleted server-side disappears from the list without a refresh")
    void deletedPodDisappears() {
      kubernetes.getClient().pods().inNamespace("default").withName(name).delete();

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("row for the server-side-deleted pod still present in the list")
        .isFalse();
    }

    @Test
    @DisplayName("a pod modified server-side updates its row in place without a refresh")
    void modifiedPodUpdatesRow() {
      kubernetes.getClient().pods().inNamespace("default").withName(name)
        .edit(pod -> new PodBuilder(pod)
          .editStatus().withPhase(MODIFIED_PHASE).endStatus().build());

      await(() -> rowShows(name, MODIFIED_PHASE));
      assertThat(rowShows(name, MODIFIED_PHASE))
        .as("pod row reflects the server-side phase change")
        .isTrue();
    }
  }
}
