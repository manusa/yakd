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
 * Created on 2023-12-17, 08:23
 */
package com.marcnuri.yakd;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;

@QuarkusTest
@WithKubernetesTestServer
class CronJobsTest {

  @Inject
  KubernetesClient kubernetesClient;
  @TestHTTPResource
  URL url;

  @Test
  @DisplayName("DELETE /api/v1/cronjobs/{namespace}/{name} - Should delete the Cron Job")
  void delete() {
    kubernetesClient.batch().v1().cronjobs()
      .resource(new CronJobBuilder().withNewMetadata().withName("to-delete").endMetadata().build())
      .create();
    when()
      .delete("/api/v1/cronjobs/" + kubernetesClient.getConfiguration().getNamespace() + "/to-delete")
      .then()
      .statusCode(204);
    assertThat(kubernetesClient.batch().v1().cronjobs().withName("to-delete").get())
      .isNull();
  }

  @Test
  @DisplayName("PUT /api/v1/cronjobs/{namespace}/{name} - Should update the Cron Job")
  void update() {
    final var cronJob = kubernetesClient.batch().v1().cronjobs()
      .resource(new CronJobBuilder().withNewMetadata().withName("to-update").endMetadata().build())
      .create();
    given()
      .contentType("application/json")
      .body(new CronJobBuilder(cronJob).withNewMetadata().addToAnnotations("updated", "true").endMetadata().build())
      .when()
      .put("/api/v1/cronjobs/" + kubernetesClient.getConfiguration().getNamespace() + "/to-update")
      .then()
      .statusCode(200)
      .body(
        "metadata.resourceVersion", matchesRegex("[2-9]"),
        "metadata.annotations.updated", is("true"));
  }

  @Test
  @DisplayName("PUT /api/v1/cronjobs/{namespace}/{name}/suspend/true - Should suspend the Cron Job")
  void suspend() {
    kubernetesClient.batch().v1().cronjobs()
      .resource(new CronJobBuilder().withNewMetadata().withName("to-suspend").endMetadata().build())
      .create();
    when()
      .put("/api/v1/cronjobs/" + kubernetesClient.getConfiguration().getNamespace() + "/to-suspend/spec/suspend/true")
      .then()
      .statusCode(204);
    assertThat(kubernetesClient.batch().v1().cronjobs().inNamespace(kubernetesClient.getConfiguration().getNamespace())
      .withName("to-suspend").get())
      .hasFieldOrPropertyWithValue("spec.suspend", true);
  }

  @Test
  @DisplayName("PUT /api/v1/cronjobs/{namespace}/{name}/trigger - Should create a Job")
  void trigger() {
    kubernetesClient.batch().v1().cronjobs()
      .resource(new CronJobBuilder()
        .withNewMetadata().withName("to-trigger").endMetadata()
        .withNewSpec().withNewJobTemplate().withNewSpec().endSpec().endJobTemplate().endSpec()
        .build())
      .create();
    when()
      .put("/api/v1/cronjobs/" + kubernetesClient.getConfiguration().getNamespace() + "/to-trigger/trigger")
      .then()
      .statusCode(204);
    assertThat(kubernetesClient.batch().v1().jobs().inNamespace(kubernetesClient.getConfiguration().getNamespace()).list().getItems())
      .singleElement()
      .extracting(Job::getMetadata)
      .hasFieldOrPropertyWithValue("annotations", Collections.singletonMap("cronjob.kubernetes.io/instantiate", "manual"))
      .extracting(ObjectMeta::getName).asString().startsWith("to-trigger-manual-");
  }

  @Test
  void watch() throws URISyntaxException {
    try (var watch = WatcherUtil.watch(url, "/api/v1/watch")) {
      // Given
      kubernetesClient.batch().v1().cronjobs()
        .resource(new CronJobBuilder().withNewMetadata().withName("to-watch").endMetadata().build())
        .create();
      // When
      Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> watch.events().stream()
          .anyMatch(watchEvent -> ((Map<String, ?>)watchEvent.object().get("metadata")).get("name").equals("to-watch")));
      // Then
      assertThat(watch.events())
        .extracting("object.kind", "object.metadata.name")
        .contains(tuple("CronJob", "to-watch"));
    }
  }
}
