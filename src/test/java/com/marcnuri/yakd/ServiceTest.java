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
 * Created on 2023-12-16, 13:16
 */
package com.marcnuri.yakd;


import io.fabric8.kubernetes.api.model.ServiceBuilder;
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

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;

@QuarkusTest
@WithKubernetesTestServer
class ServiceTest {

  @Inject
  KubernetesClient kubernetesClient;
  @TestHTTPResource
  URL url;

  @Test
  @DisplayName("DELETE /api/v1/services/{namespace}/{name} - Should delete the service")
  void delete() throws Exception {
    kubernetesClient.services().resource(new ServiceBuilder().withNewMetadata().withName("to-delete").endMetadata().build())
      .create();
    final var noResourceFuture = kubernetesClient.services().withName("to-delete").informOnCondition(List::isEmpty);
    when()
      .delete("/api/v1/services/" + kubernetesClient.getConfiguration().getNamespace() + "/to-delete")
      .then()
      .statusCode(204);
    noResourceFuture.get(10, TimeUnit.SECONDS);
  }

  @Test
  @DisplayName("PUT /api/v1/services/{namespace}/{name} - Should update the service")
  void update() {
    final var service = kubernetesClient.services().resource(new ServiceBuilder().withNewMetadata().withName("to-update").endMetadata().build())
      .create();
    given()
      .contentType("application/json")
      .body(new ServiceBuilder(service).withNewMetadata().addToAnnotations("updated", "true").endMetadata().build())
      .when()
      .put("/api/v1/services/" + kubernetesClient.getConfiguration().getNamespace() + "/to-update")
      .then()
      .statusCode(200)
      .body(
        "metadata.resourceVersion", matchesRegex("[2-9]"),
        "metadata.annotations.updated", is("true"));
  }

  @Test
  void watch() throws URISyntaxException {
    try (var watch = WatcherUtil.watch(url, "/api/v1/watch")) {
      // Given
      kubernetesClient.services().resource(new ServiceBuilder().withNewMetadata().withName("to-watch").endMetadata().build())
        .create();
      // When
      Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> watch.events().stream().anyMatch(watchEvent -> Watcher.Action.ADDED.equals(watchEvent.type())));
      // Then
      assertThat(watch.events())
        .extracting("object.kind", "object.metadata.name")
        .contains(tuple("Service", "to-watch"));
    }
  }
}
