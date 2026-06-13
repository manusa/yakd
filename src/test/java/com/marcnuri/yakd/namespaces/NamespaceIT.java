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
package com.marcnuri.yakd.namespaces;

import com.marcnuri.yakd.selenium.AbstractResourceIT;
import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("Namespace")
public class NamespaceIT extends AbstractResourceIT<Namespace> {

  private static final String DETAIL_MARKER = "namespace-detail-marker";

  public NamespaceIT() {
    super("namespaces");
  }

  @Override
  protected Namespace resource(String name) {
    return new NamespaceBuilder()
      .withNewMetadata()
        .withName(name)
        .addToLabels("detail-marker", DETAIL_MARKER)
      .endMetadata()
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
    @DisplayName("the list renders a row for the seeded namespace")
    void listRendersSeededRow() {
      openList();

      awaitRow(name);
      assertThat(hasRow(name))
        .as("row for the seeded namespace present in the list")
        .isTrue();
    }

    @Test
    @DisplayName("the detail page renders the namespace's label")
    void detailRendersLabel() {
      openDetail(seededUid(name));

      // The label value can only come from the seeded resource's metadata, so its
      // presence proves the detail page rendered the resource loaded via the watch.
      awaitPageContains(DETAIL_MARKER);
      assertThat(pageContains(DETAIL_MARKER))
        .as("namespace detail page contents")
        .isTrue();
    }
  }

  // No EditAndSave group: Namespace has no edit route and its detail page is rendered
  // without a YAML editor, so only list/detail and delete are exercised here.
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
    @DisplayName("clicking delete removes the namespace's row from the list")
    void deleteRemovesRow() {
      deleteRow(name);

      awaitNoRow(name);
      assertThat(hasRow(name))
        .as("namespace row still present after delete")
        .isFalse();
    }
  }
}
