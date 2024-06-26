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
 * Created on 2024-06-26, 17:30
 */
package com.marcnuri.yakd.selenium;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

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
    chromeDriverService = new ChromeDriverService.Builder()
      .usingAnyFreePort()
      .build();
    try {
      chromeDriverService.start();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to start ChromeDriverService", exception);
    }
    final ChromeOptions chromeOptions = new ChromeOptions();
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
}
