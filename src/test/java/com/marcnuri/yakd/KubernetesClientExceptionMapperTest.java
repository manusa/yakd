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
 * Created on 2026-06-12
 */
package com.marcnuri.yakd;

import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class KubernetesClientExceptionMapperTest {

  private final KubernetesClientExceptionMapper mapper = new KubernetesClientExceptionMapper();

  @Inject
  KubernetesClient kubernetesClient;
  @KubernetesTestServer
  KubernetesServer mockServer;

  @Nested
  @DisplayName("toResponse - status code")
  class StatusCode {

    @Test
    @DisplayName("propagates an in-range Kubernetes status code")
    void inRange() {
      assertThat(mapper.toResponse(new KubernetesClientException("not found", 404, null)).getStatus())
        .isEqualTo(404);
    }

    @Test
    @DisplayName("clamps a below-range code to 500")
    void belowRange() {
      assertThat(mapper.toResponse(new KubernetesClientException("boom", 0, null)).getStatus())
        .isEqualTo(500);
    }

    @Test
    @DisplayName("clamps an above-range code to 500")
    void aboveRange() {
      assertThat(mapper.toResponse(new KubernetesClientException("boom", 600, null)).getStatus())
        .isEqualTo(500);
    }
  }

  @Nested
  @DisplayName("toResponse - YAKD-Exception-Type header")
  class ExceptionTypeHeader {

    @Test
    @DisplayName("uses the cause's class name when present")
    void fromCause() {
      assertThat(mapper.toResponse(new KubernetesClientException("io", new IOException("io")))
        .getHeaderString("YAKD-Exception-Type"))
        .isEqualTo(IOException.class.getName());
    }

    @Test
    @DisplayName("defaults to KubernetesClientException when there is no cause")
    void withoutCause() {
      assertThat(mapper.toResponse(new KubernetesClientException("plain", 404, null))
        .getHeaderString("YAKD-Exception-Type"))
        .isEqualTo(KubernetesClientException.class.getName());
    }
  }

  @Nested
  @DisplayName("toResponse - entity")
  class Entity {

    @Test
    @DisplayName("uses the status message when a status is present")
    void fromStatusMessage() {
      assertThat(mapper.toResponse(new KubernetesClientException(
        "fallback", 404, new StatusBuilder().withCode(404).withMessage("status says boom").build())).getEntity())
        .isEqualTo("status says boom");
    }

    @Test
    @DisplayName("falls back to the exception message when there is no status")
    void fromExceptionMessage() {
      assertThat(mapper.toResponse(new KubernetesClientException("plain message", 404, null)).getEntity())
        .isEqualTo("plain message");
    }

    @Test
    @DisplayName("is rendered as plain text")
    void plainText() {
      assertThat(mapper.toResponse(new KubernetesClientException("boom", 404, null)).getMediaType())
        .isEqualTo(MediaType.TEXT_PLAIN_TYPE);
    }
  }

  @Nested
  @DisplayName("end-to-end propagation through the JAX-RS pipeline")
  class EndToEnd {

    @AfterEach
    void cleanUp() {
      kubernetesClient.secrets().inNamespace(kubernetesClient.getConfiguration().getNamespace()).delete();
    }

    @Test
    @DisplayName("a downstream 404 surfaces as a 404 response")
    void propagates404() {
      final var namespace = kubernetesClient.getConfiguration().getNamespace();
      kubernetesClient.secrets().inNamespace(namespace)
        .resource(new SecretBuilder().withNewMetadata().withName("gone").endMetadata().build()).create();
      mockServer.expect().put().withPath("/api/v1/namespaces/" + namespace + "/secrets/gone")
        .andReturn(404, new StatusBuilder().withCode(404).withMessage("secrets \"gone\" not found").build())
        .once();
      given().contentType("application/json")
        .body(new SecretBuilder().withNewMetadata().withName("gone").endMetadata().build())
        .when().put("/api/v1/secrets/" + namespace + "/gone")
        .then().statusCode(404);
    }
  }
}
