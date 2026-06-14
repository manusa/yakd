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
package com.marcnuri.yakd.replicaset;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import com.marcnuri.yakd.selenium.ResourceUi;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// A ReplicaSet has no list/detail route of its own: its list is only rendered, filtered by owner
// uid, inside the owning Deployment's detail page. So this drives that page directly via ResourceUi
// by composition rather than extending the single-type AbstractResourceIT.
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("ReplicaSet")
public class ReplicaSetIT {

  @KubernetesTestServer
  KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;
  WebDriver driver;
  ResourceUi ui;

  private final String suffix = UUID.randomUUID().toString().substring(0, 8);
  private final String deploymentName = "replicaset-it-deploy-" + suffix;
  private final String replicaSetName = "replicaset-it-rs-" + suffix;
  private String deploymentUid;

  @BeforeEach
  void setUp() {
    ui = ResourceUi.openTab(driver, url);
    kubernetes.getClient().resource(deployment()).createOr(NonDeletingOperation::update);
    deploymentUid = kubernetes.getClient().apps().deployments().inNamespace("default")
      .withName(deploymentName).get().getMetadata().getUid();
    kubernetes.getClient().resource(replicaSet(deploymentUid)).createOr(NonDeletingOperation::update);
  }

  @AfterEach
  void cleanUp() {
    try {
      kubernetes.getClient().apps().replicaSets().inNamespace("default").withName(replicaSetName).delete();
      kubernetes.getClient().apps().deployments().inNamespace("default").withName(deploymentName).delete();
    } finally {
      ui.closeTab();
    }
  }

  private Deployment deployment() {
    return new DeploymentBuilder()
      .withNewMetadata().withName(deploymentName).withNamespace("default").endMetadata()
      .withNewSpec()
        .withReplicas(1)
        .withNewSelector().addToMatchLabels("app", deploymentName).endSelector()
        .withNewTemplate()
          .withNewMetadata().addToLabels("app", deploymentName).endMetadata()
          .withNewSpec()
            .addNewContainer().withName("container-1").withImage("busybox").endContainer()
          .endSpec()
        .endTemplate()
      .endSpec()
      .build();
  }

  private ReplicaSet replicaSet(String ownerUid) {
    return new ReplicaSetBuilder()
      .withNewMetadata()
        .withName(replicaSetName)
        .withNamespace("default")
        .withOwnerReferences(new OwnerReferenceBuilder()
          .withApiVersion("apps/v1").withKind("Deployment")
          .withName(deploymentName).withUid(ownerUid).withController(true).build())
      .endMetadata()
      .withNewSpec()
        .withReplicas(1)
        .withNewSelector().addToMatchLabels("app", deploymentName).endSelector()
      .endSpec()
      .build();
  }

  private ReplicaSet backendReplicaSet() {
    return kubernetes.getClient().apps().replicaSets().inNamespace("default").withName(replicaSetName).get();
  }

  private void openOwningDeploymentDetail() {
    ui.openDetail("deployments", deploymentUid);
    // The replica-sets card only renders once the deployment is loaded from the watch; the
    // deployment name renders after that, so it is a safe gate before trusting the embedded list.
    ui.await(() -> ui.pageContains(deploymentName));
  }

  @Nested
  @DisplayName("when viewing the owning deployment's detail page")
  class ListAndDelete {

    @Test
    @DisplayName("the replica sets card renders a row for the owned replica set")
    void listRendersOwnedReplicaSet() {
      openOwningDeploymentDetail();

      ui.await(() -> ui.hasRow(replicaSetName));
      assertThat(ui.hasRow(replicaSetName))
        .as("row for the owned replica set present in the deployment detail page")
        .isTrue();
    }

    @Test
    @DisplayName("clicking delete removes the replica set from the backend")
    void deleteRemovesReplicaSet() {
      openOwningDeploymentDetail();
      ui.await(() -> ui.hasRow(replicaSetName));

      ui.deleteRow(replicaSetName);

      ui.await(() -> backendReplicaSet() == null);
      assertThat(backendReplicaSet())
        .as("replica set still present in the backend after delete")
        .isNull();
    }
  }
}
