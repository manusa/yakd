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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SeleniumTestResource implements QuarkusTestResourceConfigurableLifecycleManager<WithSelenium> {

  // Exposes the browser's download directory to tests (e.g. the detail-page Download action) as a
  // config property so they can read the file Chrome writes there.
  public static final String DOWNLOAD_DIRECTORY_PROPERTY = "yakd.it.download-directory";

  private boolean headless;
  private ChromeDriverService chromeDriverService;
  private ChromeDriver chromeDriver;
  private Path downloadDirectory;

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
    try {
      downloadDirectory = Files.createTempDirectory("yakd-it-downloads");
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to create the browser download directory", exception);
    }
    final ChromeOptions chromeOptions = new ChromeOptions();
    if (headless) {
      chromeOptions.addArguments("--headless=new");
    }
    final Map<String, Object> preferences = new HashMap<>();
    preferences.put("download.default_directory", downloadDirectory.toString());
    preferences.put("download.prompt_for_download", false);
    chromeOptions.setExperimentalOption("prefs", preferences);
    chromeDriver = new ChromeDriver(chromeDriverService, chromeOptions);
    return Map.of(DOWNLOAD_DIRECTORY_PROPERTY, downloadDirectory.toString());
  }

  @Override
  public void stop() {
    if (chromeDriver != null) {
      chromeDriver.close();
    }
    if (chromeDriverService != null) {
      chromeDriverService.stop();
    }
    if (downloadDirectory != null) {
      // Best-effort cleanup of the temp download directory and anything the browser wrote there.
      try (Stream<Path> paths = Files.walk(downloadDirectory)) {
        paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      } catch (IOException ignored) {
        // Test-only temp directory; leave it for the OS to reap if cleanup fails.
      }
    }
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(chromeDriver, new TestInjector.MatchesType(WebDriver.class));
  }
}
