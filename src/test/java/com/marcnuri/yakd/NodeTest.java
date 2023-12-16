/*
 * Copyright 2023 Marc Nuri
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
 * Created on 2023-12-16, 06:15
 */
package com.marcnuri.yakd;


import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.api.model.WatchEvent;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.sse.SseEventSource;
import org.awaitility.Awaitility;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@WithKubernetesTestServer
class NodeTest {

  @Inject
  KubernetesClient kubernetesClient;
  @TestHTTPResource
  URL url;

  @Test
  @DisplayName("PUT /api/v1/nodes/{name} - Should update the node")
  void update() {
    final var node = kubernetesClient.nodes().resource(new NodeBuilder().withNewMetadata().withName("to-rename").endMetadata().build())
      .create();
    given()
      .contentType("application/json")
      .body(new NodeBuilder(node).withNewMetadata().addToAnnotations("updated", "true").endMetadata().build())
      .when()
      .put("/api/v1/nodes/to-rename")
      .then()
      .statusCode(200)
      .body(
        "metadata.resourceVersion", is("2"),
        "metadata.annotations.updated", is("true"));
  }

  @Test
  void watch() throws Exception {
    final ConcurrentLinkedQueue<WatchEvent> messagesReceived = new ConcurrentLinkedQueue<>();
    final var uri = UriBuilder.fromUri(url.toURI()).replacePath("/api/v1/watch").build();
    try (var wsCli = ClientBuilder.newClient(); var sseSource = SseEventSource.target(wsCli.target(uri)).build()) {
      sseSource.register(event -> messagesReceived.add(event.readData(WatchEvent.class)));
      sseSource.open();
      kubernetesClient.nodes().resource(new NodeBuilder().withNewMetadata().withName("to-watch").endMetadata().build())
        .create();
      Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> messagesReceived.stream().anyMatch(watchEvent -> "ADDED".equals(watchEvent.getType())));
    }
    assertThat(messagesReceived, hasItem(
      HasPropertyWithValue.<Object>hasPropertyAtPath("object.metadata.name", is("to-watch"))
    ));
  }
}
