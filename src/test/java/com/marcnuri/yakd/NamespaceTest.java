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
 * Created on 2025-12-15, 10:00
 */
package com.marcnuri.yakd;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@QuarkusTest
@WithKubernetesTestServer
class NamespaceTest {

  @Inject
  KubernetesClient kubernetesClient;
  @TestHTTPResource
  URL url;

  @Test
  @DisplayName("GET /api/v1/namespaces - Should list namespaces")
  void list() {
    kubernetesClient.namespaces()
      .resource(new NamespaceBuilder().withNewMetadata().withName("to-list").endMetadata().build())
      .create();
    when()
      .get("/api/v1/namespaces")
      .then()
      .statusCode(200);
    assertThat(kubernetesClient.namespaces().list().getItems())
      .extracting("metadata.name")
      .contains("to-list");
  }

  @Test
  @DisplayName("DELETE /api/v1/namespaces/{name} - Should delete the namespace")
  void delete() throws Exception {
    kubernetesClient.namespaces()
      .resource(new NamespaceBuilder().withNewMetadata().withName("to-delete").endMetadata().build())
      .create();
    final var noResourceFuture = kubernetesClient.namespaces().withName("to-delete").informOnCondition(List::isEmpty);
    when()
      .delete("/api/v1/namespaces/to-delete")
      .then()
      .statusCode(204);
    noResourceFuture.get(10, TimeUnit.SECONDS);
  }

  @Test
  void watch() throws URISyntaxException {
    try (var watch = WatcherUtil.watch(url, "/api/v1/watch")) {
      // Given
      kubernetesClient.namespaces()
        .resource(new NamespaceBuilder().withNewMetadata().withName("to-watch").endMetadata().build())
        .create();
      // When
      Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> watch.events().stream().anyMatch(watchEvent -> Watcher.Action.ADDED.equals(watchEvent.type())));
      // Then
      assertThat(watch.events())
        .extracting("object.kind", "object.metadata.name")
        .contains(tuple("Namespace", "to-watch"));
    }
  }
}