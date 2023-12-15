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
 * Created on 2023-12-15, 09:18
 */
package com.marcnuri.yakd;

import io.fabric8.kubernetes.api.model.APIGroupBuilder;
import io.fabric8.kubernetes.api.model.APIGroupListBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@WithKubernetesTestServer
class ApiResourceTest {

  @KubernetesTestServer
  KubernetesServer mockServer;

  @Test
  @DisplayName("GET /api/v1/apis/groups - Should return a list of api group names")
  void getApiGroups() {
    mockServer.expect().get().withPath("/apis").andReturn(200, new APIGroupListBuilder()
        .addToGroups(new APIGroupBuilder().withName("api.registration.k8s.io").build())
        .build())
      .once();
    given()
      .when().get("/api/v1/apis/groups")
      .then()
      .statusCode(200)
      .body("[0]", equalTo("api.registration.k8s.io"));
  }
}
