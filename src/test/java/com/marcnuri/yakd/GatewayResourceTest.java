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
 * Created on 2023-12-15, 08:53
 */
package com.marcnuri.yakd;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class GatewayResourceTest {

  @Test
  @DisplayName("GET /, should return index.html")
  void getFrontendRoot() {
    given()
      .when().get("/")
      .then()
      .statusCode(200)
      .body("html.head.title", equalTo("YAKD - Kubernetes Dashboard"));
  }

  @Test
  @DisplayName("GET /static.json, should return static.json")
  void getFrontendStaticFile() {
    given()
      .when().get("/static.json")
      .then()
      .statusCode(200)
      .body(containsString("some static resource"));
  }

  @Test
  @DisplayName("GET /nonexistent.json, should return index.html")
  void getFrontendStaticFileFallback() {
    given()
      .when().get("/nonexistent.json")
      .then()
      .statusCode(200)
      .body("html.head.title", equalTo("YAKD - Kubernetes Dashboard"));
    ;
  }
}
