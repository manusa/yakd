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
 * Created on 2024-06-26, 17:20
 */
package com.marcnuri.yakd;

import com.marcnuri.yakd.selenium.IntegrationTestProfile;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.api.model.MicroTime;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class HomeIT {

  @KubernetesTestServer
  KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;
  WebDriver driver;

  @BeforeEach
  void loadHomePage() {
    // Add a few events to display something in the dashboard
    kubernetes.getClient().v1().events().resource(new EventBuilder()
        .withNewMetadata().withName("event-1").endMetadata()
        .withInvolvedObject(new ObjectReferenceBuilder()
          .withKind("Pod")
          .withName("a-pod-1")
          .build())
        .withReason("Started")
        .withMessage("Started container")
        .withEventTime(new MicroTime("2015-10-21 16:29"))
      .build()).createOr(NonDeletingOperation::update);
    driver.get(url.toString());
    final Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(1));
    wait.until(d -> d.findElement(By.cssSelector(".dashboard-page")).isDisplayed());
  }

  @Test
  void hasDocumentTitle() {
    assertThat(driver.getTitle())
      .isEqualTo("YAKD - Kubernetes Dashboard");
  }

  @Test
  void hasFooter() {
    assertThat(driver.findElement(By.cssSelector(".dashboard-page footer")).getText())
      .matches("Copyright © \\d{4} - Marc Nuri - Licensed under the Apache License 2.0");
  }

  @Test
  void hasEventList() {
    final By selector = By.cssSelector("[data-testid=list__events] [data-testid=list__events-row]");
    final Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    wait.until(d -> d.findElement(selector).isDisplayed());
    assertThat(driver.findElement(selector).getText())
      .matches("^Pod\\na-pod-1\\nStarted Started container\\n.+");
  }
}
