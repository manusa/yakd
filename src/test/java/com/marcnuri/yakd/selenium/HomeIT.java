/*
 * Copyright 2024 Marc Nuri
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
package com.marcnuri.yakd.selenium;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class HomeIT {

  @TestHTTPResource
  URL url;
  WebDriver driver;

  @BeforeEach
  void loadHomePage() {
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
}
