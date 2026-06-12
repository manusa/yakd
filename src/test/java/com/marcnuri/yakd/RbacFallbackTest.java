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

import com.marcnuri.yakd.secrets.SecretService;
import com.marcnuri.yakd.watch.WatchEvent;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class RbacFallbackTest {

  private static final String OTHER_NAMESPACE = "yakd-rbac-other";

  @Inject
  KubernetesClient kubernetesClient;
  @Inject
  SecretService secretService;
  @KubernetesTestServer
  KubernetesServer mockServer;

  String configuredNamespace;

  @BeforeEach
  void setUp() {
    configuredNamespace = kubernetesClient.getConfiguration().getNamespace();
  }

  @AfterEach
  void cleanUp() {
    kubernetesClient.secrets().inNamespace(configuredNamespace).delete();
    kubernetesClient.secrets().inNamespace(OTHER_NAMESPACE).delete();
    kubernetesClient.configMaps().inNamespace(configuredNamespace).delete();
    kubernetesClient.configMaps().inNamespace(OTHER_NAMESPACE).delete();
  }

  private static Status forbidden() {
    return new StatusBuilder().withStatus("Failure").withReason("Forbidden").withCode(403)
      .withMessage("cluster-wide access is denied").build();
  }

  @Nested
  @DisplayName("get() falls back to the configured namespace when cluster-wide access is denied")
  class GetFallback {

    @Test
    @DisplayName("Secret get() returns only the configured namespace's secrets")
    void secretGetFallsBack() {
      mockServer.expect().get().withPath("/api/v1/secrets").andReturn(403, forbidden()).once();
      kubernetesClient.secrets().inNamespace(configuredNamespace)
        .resource(new SecretBuilder().withNewMetadata().withName("rbac-configured").endMetadata().build()).create();
      kubernetesClient.secrets().inNamespace(OTHER_NAMESPACE)
        .resource(new SecretBuilder().withNewMetadata().withName("rbac-other").endMetadata().build()).create();
      final var names = when().get("/api/v1/secrets")
        .then().statusCode(200)
        .extract().jsonPath().getList("metadata.name", String.class);
      assertThat(names).contains("rbac-configured").doesNotContain("rbac-other");
    }

    @Test
    @DisplayName("ConfigMap get() returns only the configured namespace's config maps")
    void configMapGetFallsBack() {
      mockServer.expect().get().withPath("/api/v1/configmaps").andReturn(403, forbidden()).once();
      kubernetesClient.configMaps().inNamespace(configuredNamespace)
        .resource(new ConfigMapBuilder().withNewMetadata().withName("rbac-configured").endMetadata().build()).create();
      kubernetesClient.configMaps().inNamespace(OTHER_NAMESPACE)
        .resource(new ConfigMapBuilder().withNewMetadata().withName("rbac-other").endMetadata().build()).create();
      final var names = when().get("/api/v1/configmaps")
        .then().statusCode(200)
        .extract().jsonPath().getList("metadata.name", String.class);
      assertThat(names).contains("rbac-configured").doesNotContain("rbac-other");
    }
  }

  @Nested
  @DisplayName("watch() falls back to the configured namespace when cluster-wide access is denied")
  class WatchFallback {

    @Test
    @DisplayName("Secret watch() emits only the configured namespace's events")
    void secretWatchFallsBack() {
      mockServer.expect().get().withPath("/api/v1/secrets?limit=1").andReturn(403, forbidden()).once();
      final var subscriber = AssertSubscriber.<WatchEvent<Secret>>create(Long.MAX_VALUE);
      Multi.createFrom().<WatchEvent<Secret>>emitter(emitter -> {
        try (var ignored = secretService.watch().subscribe(e -> {}, emitter)) {
          // Seed the other namespace first: watch events on one connection are ordered, so a
          // cluster-wide leak (fallback not taken) would surface "rbac-watch-other" before the
          // awaited "rbac-watch-configured" event, making the negative assertion below meaningful.
          kubernetesClient.secrets().inNamespace(OTHER_NAMESPACE)
            .resource(new SecretBuilder().withNewMetadata().withName("rbac-watch-other").endMetadata().build()).create();
          kubernetesClient.secrets().inNamespace(configuredNamespace)
            .resource(new SecretBuilder().withNewMetadata().withName("rbac-watch-configured").endMetadata().build()).create();
          Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until(() -> hasEvent(subscriber, "rbac-watch-configured"));
        } catch (Exception e) {
          emitter.fail(e);
        }
      }).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> hasEvent(subscriber, "rbac-watch-configured"));

      assertThat(subscriber.getItems())
        .extracting(e -> e.object().getMetadata().getName())
        .doesNotContain("rbac-watch-other");
    }

    private static boolean hasEvent(AssertSubscriber<WatchEvent<Secret>> subscriber, String name) {
      return subscriber.getItems().stream()
        .anyMatch(e -> name.equals(e.object().getMetadata().getName()));
    }
  }
}
