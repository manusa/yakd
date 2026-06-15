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
package com.marcnuri.yakd.node;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Node detail/list pages are name-keyed; this exercises edit-save, watch and the detail dials.
// List/detail rendering is already covered by ClusterScopedResourceIT.
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Node")
public class NodeIT extends AbstractResourceIT<Node> {

  public NodeIT() {
    super("nodes");
  }

  @Override
  protected Node resource(String name) {
    return new NodeBuilder()
      .withNewMetadata()
        .withName(name)
        .addToLabels("mutation-marker", "before-edit")
      .endMetadata()
      .withNewStatus()
        .addToAllocatable("cpu", new Quantity("8"))
        .addToAllocatable("memory", new Quantity("16384Mi"))
        .addToAllocatable("pods", new Quantity("110"))
        .addNewCondition().withType("Ready").withStatus("True").endCondition()
        .withNewNodeInfo()
          .withOperatingSystem("linux")
          .withArchitecture("arm64")
          .withKernelVersion("6.6.0")
          .withContainerRuntimeVersion("containerd://1.7.0")
          .withKubeletVersion("v1.33.0-node-it")
        .endNodeInfo()
      .endStatus()
      .build();
  }

  @Nested
  @DisplayName("when editing and saving the YAML")
  class EditAndSave {

    private String name;

    @BeforeEach
    void navigate() {
      name = seed("to-edit");
      // The editor route is uid-keyed (state.nodes[uid]), unlike the name-keyed detail page.
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
        .as("mutation-marker label on the persisted node")
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
    @DisplayName("a node created server-side appears in the list without a refresh")
    void createdNodeAppears() {
      final String added = seed("watch-added");

      awaitRow(added);
      assertThat(hasRow(added))
        .as("row for the server-side-created node present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("a node deleted server-side disappears from the list without a refresh")
    void deletedNodeDisappears() {
      kubernetes.getClient().nodes().withName(name).delete();

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("row for the server-side-deleted node still present in the list")
        .isFalse();
    }
  }

  @Nested
  @DisplayName("when viewing the resource dials on the detail page")
  class Dials {

    private String name;
    private String podName;

    @BeforeEach
    void seedNodeAndScheduledPod() {
      name = seed("to-measure");
      podName = name + "-pod";
      // A pod scheduled on the node carrying resource requests: the CPU/Memory dials sum these
      // requests against the node's allocatable capacity.
      kubernetes.getClient().pods().inNamespace("default").resource(new PodBuilder()
        .withNewMetadata().withName(podName).withNamespace("default").endMetadata()
        .withNewSpec()
          .withNodeName(name)
          .addNewContainer()
            .withName("container-1").withImage("busybox")
            .withNewResources()
              .addToRequests("cpu", new Quantity("250m"))
              .addToRequests("memory", new Quantity("321Mi"))
            .endResources()
          .endContainer()
        .endSpec()
        .build()).createOr(NonDeletingOperation::update);
      open("nodes/" + name);
      // The node name renders once the detail page has loaded the node from the watch, so it is a
      // safe gate before trusting the computed dials.
      awaitPageContains(name);
    }

    @AfterEach
    void deletePod() {
      kubernetes.getClient().pods().inNamespace("default").withName(podName).delete();
    }

    @Test
    @DisplayName("the CPU dial renders the summed requested CPU from scheduled pods")
    void cpuDialShowsRequested() {
      // 250m requested by the scheduled pod, formatted to three decimals by the UI.
      awaitPageContains("0.250");
      assertThat(pageContains("0.250"))
        .as("requested CPU rendered in the node's CPU dial")
        .isTrue();
    }

    @Test
    @DisplayName("the Memory dial renders the node's allocatable memory")
    void memoryDialShowsAllocatable() {
      // 16384Mi allocatable on the node, rendered human-readable by the UI.
      awaitPageContains("16.000 GiB");
      assertThat(pageContains("16.000 GiB"))
        .as("allocatable memory rendered in the node's Memory dial")
        .isTrue();
    }

    @Test
    @DisplayName("the Memory dial renders the summed requested memory from scheduled pods")
    void memoryDialShowsRequested() {
      // 321Mi requested by the scheduled pod, rendered human-readable by the UI.
      awaitPageContains("321 MiB");
      assertThat(pageContains("321 MiB"))
        .as("requested memory rendered in the node's Memory dial")
        .isTrue();
    }
  }
}
