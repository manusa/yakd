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
 */
package com.marcnuri.yakd.selenium;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base for the per-resource Selenium integration tests. It owns everything that is invariant
 * across resources — the injected {@link WebDriver}/{@link KubernetesServer}/URL, the
 * {@link ResourceUi} plumbing, the browser-tab lifecycle, and the generic backend helpers
 * (seed/uid/label) — but deliberately declares <strong>no</strong> {@code @Nested} groups or
 * {@code @Test} methods: each subclass keeps its full, visible spec so the file reads on its own.
 *
 * <p>The single resource-specific contract is {@link #resource(String)}, a builder for the type
 * under test. Seeding, fetching and deleting are scope-agnostic via {@code client.resource(obj)},
 * which keys off the object's own metadata, so the same code serves both namespaced and
 * cluster-scoped resources.
 *
 * <p><strong>Naming &amp; isolation.</strong> The constructor derives a per-class random base name;
 * {@link #seed(String)} appends a behavior suffix (e.g. {@code "to-delete"}), so every test seeds a
 * distinctly-named resource and no two tests in a class share backend state. This keeps the tests
 * mutually independent and is a prerequisite for a possible future experiment running them
 * concurrently within a class — note that actually enabling {@code @Execution(CONCURRENT)} would
 * additionally require a WebDriver per test/thread, as the one injected here is shared and Selenium
 * drivers are not thread-safe.
 *
 * @param <T> the Kubernetes resource type under test
 */
public abstract class AbstractResourceIT<T extends HasMetadata> {

  @KubernetesTestServer
  protected KubernetesServer kubernetes;

  @TestHTTPResource
  URL url;

  // Exposed to subclasses so resource-specific groups (e.g. scale carets, the cronjob suspend
  // toggle, sidebar nav) can drive bespoke elements the shared delegates do not cover.
  protected WebDriver driver;

  protected ResourceUi ui;

  private final String route;
  private final String base;
  private final List<T> seeded = new ArrayList<>();

  protected AbstractResourceIT(String route) {
    this.route = route;
    this.base = route + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  /** Builds a resource of the type under test carrying the given name. */
  protected abstract T resource(String name);

  // --- lifecycle ---

  @BeforeEach
  void openTab() {
    ui = ResourceUi.openTab(driver, url);
  }

  @AfterEach
  void closeTabAndCleanup() {
    try {
      // The mock server outlives the test class, so delete everything this test seeded. Do this
      // first (and in a finally) so a failure to close the tab cannot leak resources.
      seeded.forEach(resource -> kubernetes.getClient().resource(resource).delete());
      seeded.clear();
    } finally {
      if (ui != null) {
        ui.closeTab();
      }
    }
  }

  // --- backend helpers (scope-agnostic via client.resource(obj)) ---

  /** Seeds a uniquely-named resource ({@code <base>-<suffix>}) and returns its name. */
  protected String seed(String suffix) {
    final T resource = resource(base + "-" + suffix);
    kubernetes.getClient().resource(resource).createOr(NonDeletingOperation::update);
    seeded.add(resource);
    return resource.getMetadata().getName();
  }

  /** The server-assigned uid of a previously {@link #seed(String) seeded} resource. */
  protected String seededUid(String name) {
    final HasMetadata stored = fetch(name);
    assertThat(stored).as("seeded %s available", route).isNotNull();
    return stored.getMetadata().getUid();
  }

  /**
   * The current value of a metadata label on a previously {@link #seed(String) seeded} resource,
   * or {@code null}.
   */
  protected String label(String name, String key) {
    final HasMetadata stored = fetch(name);
    if (stored == null || stored.getMetadata().getLabels() == null) {
      return null;
    }
    return stored.getMetadata().getLabels().get(key);
  }

  // Re-reads the server copy of a resource this test seeded via seed(...); resolves only names
  // returned by seed(), so seededUid/label do not see resources created by raw client calls.
  private HasMetadata fetch(String name) {
    return seeded.stream()
      .filter(resource -> name.equals(resource.getMetadata().getName()))
      .findFirst()
      .map(resource -> kubernetes.getClient().resource(resource).get())
      .orElse(null);
  }

  // --- UI delegates (route pre-bound so the subclass spec stays terse) ---

  /** Navigates to an arbitrary application path (e.g. {@code ""} for the dashboard, {@code "search"}). */
  protected void open(String path) {
    ui.open(path);
  }

  protected void openList() {
    ui.openList(route);
  }

  protected void openDetail(String uid) {
    ui.openDetail(route, uid);
  }

  protected void openEditor(String uid) {
    ui.openEditor(route, uid);
  }

  protected boolean hasRow(String name) {
    return ui.hasRow(name);
  }

  protected boolean rowShows(String name, String text) {
    return ui.rowShows(name, text);
  }

  protected void deleteRow(String name) {
    ui.deleteRow(name);
  }

  protected void awaitRow(String name) {
    ui.await(() -> ui.hasRow(name));
  }

  protected void awaitNoRow(String name) {
    ui.await(() -> !ui.hasRow(name));
  }

  protected boolean pageContains(String text) {
    return ui.pageContains(text);
  }

  protected void awaitPageContains(String text) {
    ui.await(() -> ui.pageContains(text));
  }

  protected void awaitEditorContains(String text) {
    ui.await(() -> ui.editorContains(text));
  }

  /** Polls an arbitrary backend/UI condition until it holds or the timeout elapses. */
  protected void await(BooleanSupplier condition) {
    ui.await(condition);
  }

  protected void editorReplace(String from, String to) {
    ui.editorReplace(from, to);
  }

  protected void save() {
    ui.save();
  }
}
