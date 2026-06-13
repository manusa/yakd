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
package com.marcnuri.yakd.endpoints;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Endpoint")
public class EndpointIT extends AbstractResourceIT<Endpoints> {

  private static final String ADDRESS_IP = "10.40.50.60";
  private static final String NO_TARGET_REF_IP = "10.40.50.61";

  public EndpointIT() {
    super("endpoints");
  }

  @Override
  protected Endpoints resource(String name) {
    return new EndpointsBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("default")
      .endMetadata()
      .addNewSubset()
        .addNewAddress()
          .withIp(ADDRESS_IP)
          // Carries a targetRef so the detail page's Target component renders the resource link;
          // the absent-targetRef path is covered by the NoTargetRef group below (see #208).
          .withNewTargetRef()
            .withKind("Pod").withName("it-target-pod").withUid("it-target-pod-uid")
          .endTargetRef()
        .endAddress()
        .addNewPort().withName("http").withPort(8080).withProtocol("TCP").endPort()
      .endSubset()
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
    @DisplayName("the list renders a row for the seeded endpoint")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded endpoint present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the endpoint's subset address")
    void detailRendersAddress() {
      openDetail(seededUid(name));

      // The address IP can only come from the seeded resource's subsets, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(ADDRESS_IP);
      assertThat(pageContains(ADDRESS_IP))
        .as("endpoint detail page contents")
        .isTrue();
    }
  }

  // No EditAndSave group: the Endpoints detail page is rendered with editable=false
  // (the resource exposes no update endpoint), so there is no YAML editor to exercise.
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
    @DisplayName("clicking delete removes the endpoint's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("endpoint row still present after delete")
        .isFalse();
    }
  }

  // Regression coverage for #208: a subset address is not required to carry a targetRef (it is
  // absent for manually-managed Endpoints, addresses not backed by a Pod, and notReadyAddresses).
  // The detail page's Target component must render such an address instead of crashing. This
  // resource shape differs from resource(String), so it is seeded directly and cleaned up here
  // rather than through the base seed()/AfterEach (which only tracks the resource(String) shape).
  @Nested
  @DisplayName("when a subset address has no targetRef")
  class NoTargetRef {

    private Endpoints endpoint;

    @BeforeEach
    void seedResource() {
      endpoint = new EndpointsBuilder()
        .withNewMetadata()
          .withName("endpoints-no-target-ref-" + UUID.randomUUID().toString().substring(0, 8))
          .withNamespace("default")
        .endMetadata()
        .addNewSubset()
          .addNewAddress().withIp(NO_TARGET_REF_IP).endAddress()
          .addNewPort().withName("http").withPort(8080).withProtocol("TCP").endPort()
        .endSubset()
        .build();
      kubernetes.getClient().resource(endpoint).create();
    }

    @AfterEach
    void deleteResource() {
      kubernetes.getClient().resource(endpoint).delete();
    }

    @Test
    @DisplayName("the detail page renders the address instead of crashing")
    void detailRendersAddressWithoutTargetRef() {
      final String uid = kubernetes.getClient().resource(endpoint).get().getMetadata().getUid();
      openDetail(uid);

      // The IP can only come from the seeded subset address, so its presence proves the detail
      // page rendered the address even though it has no targetRef (before the fix the render threw).
      awaitPageContains(NO_TARGET_REF_IP);
      assertThat(pageContains(NO_TARGET_REF_IP))
        .as("endpoint detail page renders the address that has no targetRef")
        .isTrue();
    }
  }
}
