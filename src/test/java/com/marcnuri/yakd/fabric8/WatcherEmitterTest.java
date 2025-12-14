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
 * Created on 2025-12-13
 */
package com.marcnuri.yakd.fabric8;

import com.marcnuri.yakd.watch.WatchEvent;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.Watchable;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class WatcherEmitterTest {

  @Inject
  KubernetesClient kubernetesClient;

  @Nested
  @DisplayName("WatcherEmitter integration with Kubernetes API")
  class IntegrationTests {

    @Test
    @DisplayName("should emit ADDED event when resource is created")
    void shouldEmitAddedEvent() {
      // Given
      var watchable = kubernetesClient.configMaps().inNamespace(kubernetesClient.getNamespace());
      var watcherEmitter = new WatcherEmitter<>(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<ConfigMap>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      // When
      kubernetesClient.configMaps()
        .resource(new ConfigMapBuilder()
          .withNewMetadata().withName("watcher-emitter-test-added").endMetadata()
          .addToData("key", "value")
          .build())
        .create();

      // Then
      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> e.type() == Watcher.Action.ADDED &&
            "watcher-emitter-test-added".equals(e.object().getMetadata().getName())));

      var addedEvent = subscriber.getItems().stream()
        .filter(e -> e.type() == Watcher.Action.ADDED &&
          "watcher-emitter-test-added".equals(e.object().getMetadata().getName()))
        .findFirst()
        .orElseThrow();
      assertThat(addedEvent.object().getData()).containsEntry("key", "value");

      // Cleanup
      subscriber.cancel();
    }

    @Test
    @DisplayName("should emit MODIFIED event when resource is updated")
    void shouldEmitModifiedEvent() {
      // Given
      var created = kubernetesClient.configMaps()
        .resource(new ConfigMapBuilder()
          .withNewMetadata().withName("watcher-emitter-test-modified").endMetadata()
          .addToData("key", "original")
          .build())
        .create();

      var watchable = kubernetesClient.configMaps().inNamespace(kubernetesClient.getNamespace());
      var watcherEmitter = new WatcherEmitter<>(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<ConfigMap>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      // When
      kubernetesClient.configMaps()
        .resource(new ConfigMapBuilder(created)
          .addToData("key", "updated")
          .build())
        .update();

      // Then
      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> e.type() == Watcher.Action.MODIFIED &&
            "watcher-emitter-test-modified".equals(e.object().getMetadata().getName()) &&
            "updated".equals(e.object().getData().get("key"))));

      // Cleanup
      subscriber.cancel();
    }

    @Test
    @DisplayName("should emit DELETED event when resource is deleted")
    void shouldEmitDeletedEvent() {
      // Given
      kubernetesClient.configMaps()
        .resource(new ConfigMapBuilder()
          .withNewMetadata().withName("watcher-emitter-test-deleted").endMetadata()
          .build())
        .create();

      var watchable = kubernetesClient.configMaps().inNamespace(kubernetesClient.getNamespace());
      var watcherEmitter = new WatcherEmitter<>(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<ConfigMap>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      // When
      kubernetesClient.configMaps().withName("watcher-emitter-test-deleted").delete();

      // Then
      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> e.type() == Watcher.Action.DELETED &&
            "watcher-emitter-test-deleted".equals(e.object().getMetadata().getName())));

      // Cleanup
      subscriber.cancel();
    }

    @Test
    @DisplayName("should emit multiple events in sequence")
    void shouldEmitMultipleEvents() {
      // Given
      var watchable = kubernetesClient.configMaps().inNamespace(kubernetesClient.getNamespace());
      var watcherEmitter = new WatcherEmitter<>(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<ConfigMap>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      // When - create multiple resources
      kubernetesClient.configMaps()
        .resource(new ConfigMapBuilder()
          .withNewMetadata().withName("watcher-emitter-test-multi-1").endMetadata()
          .build())
        .create();
      kubernetesClient.configMaps()
        .resource(new ConfigMapBuilder()
          .withNewMetadata().withName("watcher-emitter-test-multi-2").endMetadata()
          .build())
        .create();

      // Then
      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .filter(e -> e.object().getMetadata().getName().startsWith("watcher-emitter-test-multi-"))
          .count() >= 2);

      assertThat(subscriber.getItems())
        .extracting(e -> e.object().getMetadata().getName())
        .contains("watcher-emitter-test-multi-1", "watcher-emitter-test-multi-2");

      // Cleanup
      subscriber.cancel();
    }

    @Test
    @DisplayName("should close watch and stop emitting when cancelled")
    void shouldStopOnCancel() {
      // Given
      var testWatchable = new TestWatchable();
      var watcherEmitter = new WatcherEmitter<>(testWatchable);
      var subscriber = AssertSubscriber.<WatchEvent<Pod>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> testWatchable.capturedWatcher.get() != null);

      // When
      subscriber.cancel();

      // Then - verify watch was closed (state-based check, no waiting)
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(testWatchable.watchClosed::get);

      // After watch is closed, emitting events should have no effect
      final int countBeforeEmit = subscriber.getItems().size();
      testWatchable.capturedWatcher.get().eventReceived(Watcher.Action.ADDED, null);

      // Verify no new events were added (immediate state check)
      assertThat(subscriber.getItems()).hasSize(countBeforeEmit);
    }
  }

  @Nested
  @DisplayName("WatchEventEmitter error handling")
  class ErrorHandlingTests {

    @Test
    @DisplayName("should complete the emitter when onClose is called without exception")
    void onCloseShouldCompleteEmitter() {
      // Given
      var testWatchable = new TestWatchable();
      var watcherEmitter = new WatcherEmitter<>(testWatchable);
      var subscriber = AssertSubscriber.<WatchEvent<Pod>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> testWatchable.capturedWatcher.get() != null);

      // When
      testWatchable.capturedWatcher.get().onClose();

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .untilAsserted(subscriber::assertCompleted);
    }

    @Test
    @DisplayName("should fail the emitter when onClose is called with WatcherException")
    void onCloseWithExceptionShouldFailEmitter() {
      // Given
      var testWatchable = new TestWatchable();
      var watcherEmitter = new WatcherEmitter<>(testWatchable);
      var subscriber = AssertSubscriber.<WatchEvent<Pod>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> testWatchable.capturedWatcher.get() != null);

      // When
      var exception = new WatcherException("Connection lost");
      testWatchable.capturedWatcher.get().onClose(exception);

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .untilAsserted(() -> subscriber.assertFailedWith(WatcherException.class, "Connection lost"));
    }

    @Test
    @DisplayName("should close watch when emitter is cancelled")
    void shouldCloseWatchOnCancel() {
      // Given
      var testWatchable = new TestWatchable();
      var watcherEmitter = new WatcherEmitter<>(testWatchable);
      var subscriber = AssertSubscriber.<WatchEvent<Pod>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(watcherEmitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> testWatchable.capturedWatcher.get() != null);

      // When
      subscriber.cancel();

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(testWatchable.watchClosed::get);
    }
  }

  /**
   * Test implementation of Watchable for testing error handling scenarios
   * that cannot be easily triggered with the Kubernetes mock server.
   */
  static class TestWatchable implements Watchable<Pod> {
    final AtomicReference<Watcher<Pod>> capturedWatcher = new AtomicReference<>();
    final AtomicBoolean watchClosed = new AtomicBoolean(false);

    @Override
    public Watch watch(ListOptions listOptions, Watcher<Pod> watcher) {
      capturedWatcher.set(watcher);
      return () -> watchClosed.set(true);
    }

    @Override
    public Watch watch(String resourceVersion, Watcher<Pod> watcher) {
      capturedWatcher.set(watcher);
      return () -> watchClosed.set(true);
    }

    @Override
    public Watch watch(Watcher<Pod> watcher) {
      capturedWatcher.set(watcher);
      return () -> watchClosed.set(true);
    }
  }
}
