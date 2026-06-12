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
package com.marcnuri.yakd.customresources;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class CustomResourceRoutingTest {

  private static final String GROUP = "yakd.example.com";
  private static final String VERSION = "v1";
  private static final String PLURAL = "widgets";
  private static final String NAMESPACE = "widget-namespace";
  private static final String BASE = "/api/v1/customresources/" + GROUP + "/" + VERSION + "/" + PLURAL;

  @Inject
  KubernetesClient kubernetesClient;

  @AfterEach
  void cleanUp() {
    for (final String name : new String[] {"to-delete", "to-update"}) {
      kubernetesClient.genericKubernetesResources(clusterContext()).withName(name).delete();
      kubernetesClient.genericKubernetesResources(namespacedContext()).inNamespace(NAMESPACE).withName(name).delete();
    }
  }

  @Nested
  @DisplayName("DELETE resolves to the correct scope")
  class Delete {

    @Test
    @DisplayName("namespaced path deletes the namespaced custom resource")
    void namespaced() {
      kubernetesClient.genericKubernetesResources(namespacedContext()).inNamespace(NAMESPACE)
        .resource(customResource("to-delete")).create();
      when().delete(BASE + "/namespaces/" + NAMESPACE + "/to-delete")
        .then().statusCode(204);
      assertThat(kubernetesClient.genericKubernetesResources(namespacedContext()).inNamespace(NAMESPACE)
        .withName("to-delete").get()).isNull();
    }

    @Test
    @DisplayName("cluster-scoped path deletes the cluster-scoped custom resource")
    void clusterScoped() {
      kubernetesClient.genericKubernetesResources(clusterContext())
        .resource(customResource("to-delete")).create();
      when().delete(BASE + "/to-delete")
        .then().statusCode(204);
      assertThat(kubernetesClient.genericKubernetesResources(clusterContext())
        .withName("to-delete").get()).isNull();
    }
  }

  @Nested
  @DisplayName("PUT resolves to the correct scope")
  class Update {

    @Test
    @DisplayName("namespaced path updates the namespaced custom resource")
    void namespaced() {
      final var created = kubernetesClient.genericKubernetesResources(namespacedContext()).inNamespace(NAMESPACE)
        .resource(customResource("to-update")).create();
      given().contentType("application/json")
        .body(new GenericKubernetesResourceBuilder(created)
          .editMetadata().addToAnnotations("updated", "true").endMetadata().build())
        .when().put(BASE + "/namespaces/" + NAMESPACE + "/to-update")
        .then().statusCode(200);
      assertThat(kubernetesClient.genericKubernetesResources(namespacedContext()).inNamespace(NAMESPACE)
        .withName("to-update").get())
        .extracting(r -> r.getMetadata().getAnnotations().get("updated"))
        .isEqualTo("true");
    }

    @Test
    @DisplayName("cluster-scoped path updates the cluster-scoped custom resource")
    void clusterScoped() {
      final var created = kubernetesClient.genericKubernetesResources(clusterContext())
        .resource(customResource("to-update")).create();
      given().contentType("application/json")
        .body(new GenericKubernetesResourceBuilder(created)
          .editMetadata().addToAnnotations("updated", "true").endMetadata().build())
        .when().put(BASE + "/to-update")
        .then().statusCode(200);
      assertThat(kubernetesClient.genericKubernetesResources(clusterContext())
        .withName("to-update").get())
        .extracting(r -> r.getMetadata().getAnnotations().get("updated"))
        .isEqualTo("true");
    }
  }

  private static GenericKubernetesResource customResource(String name) {
    return new GenericKubernetesResourceBuilder()
      .withApiVersion(GROUP + "/" + VERSION)
      .withKind("Widget")
      .withNewMetadata().withName(name).endMetadata()
      .build();
  }

  private static ResourceDefinitionContext clusterContext() {
    return new ResourceDefinitionContext.Builder()
      .withNamespaced(false).withGroup(GROUP).withVersion(VERSION).withPlural(PLURAL).build();
  }

  private static ResourceDefinitionContext namespacedContext() {
    return new ResourceDefinitionContext.Builder()
      .withNamespaced(true).withGroup(GROUP).withVersion(VERSION).withPlural(PLURAL).build();
  }
}
