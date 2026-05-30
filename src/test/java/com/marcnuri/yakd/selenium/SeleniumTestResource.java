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
 * Created on 2024-06-26, 17:30
 */
package com.marcnuri.yakd.selenium;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class SeleniumTestResource implements QuarkusTestResourceConfigurableLifecycleManager<WithSelenium> {

  private boolean headless;
  private ChromeDriverService chromeDriverService;
  private ChromeDriver chromeDriver;

  @Override
  public void init(WithSelenium annotation) {
    headless = annotation.headless();
  }

  @Override
  public Map<String, String> start() {
    final ChromeDriverService.Builder serviceBuilder = new ChromeDriverService.Builder()
      .usingAnyFreePort();
    // Platforms without a Selenium Manager-resolvable driver (e.g. linux-arm64, where there is no
    // Chrome-for-Testing build) can supply an explicit driver via webdriver.chrome.driver/CHROME_DRIVER_BIN.
    final String driverPath = configured("webdriver.chrome.driver", "CHROME_DRIVER_BIN");
    if (driverPath != null) {
      serviceBuilder.usingDriverExecutable(new File(driverPath));
    }
    chromeDriverService = serviceBuilder.build();
    try {
      chromeDriverService.start();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to start ChromeDriverService", exception);
    }
    final ChromeOptions chromeOptions = new ChromeOptions();
    // Likewise, a non-default browser binary (e.g. Chromium) can be supplied via
    // webdriver.chrome.binary/CHROME_BIN; unset falls back to the auto-detected browser.
    final String browserBinary = configured("webdriver.chrome.binary", "CHROME_BIN");
    if (browserBinary != null) {
      chromeOptions.setBinary(browserBinary);
    }
    if (headless) {
      chromeOptions.addArguments("--headless=new");
    }
    chromeDriver = new ChromeDriver(chromeDriverService, chromeOptions);
    return Collections.emptyMap();
  }

  @Override
  public void stop() {
    if (chromeDriver != null) {
      chromeDriver.close();
    }
    if (chromeDriverService != null) {
      chromeDriverService.stop();
    }
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(chromeDriver, new TestInjector.MatchesType(WebDriver.class));
  }

  // Resolves an optional path from a system property first, then an environment variable.
  // Returns null when neither is set, preserving the default Selenium Manager-based behavior.
  private static String configured(String systemProperty, String environmentVariable) {
    String value = System.getProperty(systemProperty);
    if (value == null || value.isBlank()) {
      value = System.getenv(environmentVariable);
    }
    return value == null || value.isBlank() ? null : value;
  }
}
