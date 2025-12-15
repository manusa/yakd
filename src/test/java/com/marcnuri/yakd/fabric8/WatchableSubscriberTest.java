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
 * Created on 2025-12-13
 */
package com.marcnuri.yakd.fabric8;

import com.marcnuri.yakd.watch.WatchEvent;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class WatchableSubscriberTest {

  @Inject
  KubernetesClient kubernetesClient;

  @Nested
  @DisplayName("WatchableSubscriber integration with Kubernetes API")
  class IntegrationTests {

    @Test
    @DisplayName("should emit ADDED event with identity mapper")
    void shouldEmitAddedEventWithIdentityMapper() {
      // Given
      var watchable = kubernetesClient.secrets().inNamespace(kubernetesClient.getNamespace());
      var watchableSubscriber = WatchableSubscriber.subscriber(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<Secret>>create(Long.MAX_VALUE);
      var closeException = new AtomicReference<WatcherException>();
      Consumer<WatcherException> closeHandler = closeException::set;

      Multi.createFrom().<WatchEvent<Secret>>emitter(emitter -> {
        try (var ignored = watchableSubscriber.subscribe(closeHandler, emitter)) {
          // When
          kubernetesClient.secrets()
            .resource(new SecretBuilder()
              .withNewMetadata().withName("watchable-subscriber-test-added").endMetadata()
              .addToStringData("key", "value")
              .build())
            .create();

          // Then
          Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until(() -> !subscriber.getItems().isEmpty() &&
              subscriber.getItems().stream()
                .anyMatch(e -> e.type() == Watcher.Action.ADDED &&
                  "watchable-subscriber-test-added".equals(e.object().getMetadata().getName())));
        } catch (Exception e) {
          emitter.fail(e);
        }
      }).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> "watchable-subscriber-test-added".equals(e.object().getMetadata().getName())));

      var addedEvent = subscriber.getItems().stream()
        .filter(e -> e.type() == Watcher.Action.ADDED &&
          "watchable-subscriber-test-added".equals(e.object().getMetadata().getName()))
        .findFirst()
        .orElseThrow();
      assertThat(addedEvent.object()).isInstanceOf(Secret.class);
    }

    @Test
    @DisplayName("should emit mapped event with custom mapper")
    void shouldEmitMappedEventWithCustomMapper() {
      // Given
      var watchable = kubernetesClient.secrets().inNamespace(kubernetesClient.getNamespace());
      var watchableSubscriber = WatchableSubscriber.subscriber(watchable, secret -> secret.getMetadata().getName());
      var subscriber = AssertSubscriber.<WatchEvent<String>>create(Long.MAX_VALUE);
      var closeException = new AtomicReference<WatcherException>();
      Consumer<WatcherException> closeHandler = closeException::set;

      Multi.createFrom().<WatchEvent<String>>emitter(emitter -> {
        try (var ignored = watchableSubscriber.subscribe(closeHandler, emitter)) {
          // When
          kubernetesClient.secrets()
            .resource(new SecretBuilder()
              .withNewMetadata().withName("watchable-subscriber-test-mapped").endMetadata()
              .build())
            .create();

          // Then
          Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until(() -> !subscriber.getItems().isEmpty() &&
              subscriber.getItems().stream()
                .anyMatch(e -> "watchable-subscriber-test-mapped".equals(e.object())));
        } catch (Exception e) {
          emitter.fail(e);
        }
      }).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> "watchable-subscriber-test-mapped".equals(e.object())));

      var addedEvent = subscriber.getItems().stream()
        .filter(e -> "watchable-subscriber-test-mapped".equals(e.object()))
        .findFirst()
        .orElseThrow();
      assertThat(addedEvent.type()).isEqualTo(Watcher.Action.ADDED);
      assertThat(addedEvent.object()).isEqualTo("watchable-subscriber-test-mapped");
    }

    @Test
    @DisplayName("should emit MODIFIED event when resource is updated")
    void shouldEmitModifiedEvent() {
      // Given
      var created = kubernetesClient.secrets()
        .resource(new SecretBuilder()
          .withNewMetadata().withName("watchable-subscriber-test-modified").endMetadata()
          .addToStringData("key", "original")
          .build())
        .create();

      var watchable = kubernetesClient.secrets().inNamespace(kubernetesClient.getNamespace());
      var watchableSubscriber = WatchableSubscriber.subscriber(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<Secret>>create(Long.MAX_VALUE);
      var closeException = new AtomicReference<WatcherException>();
      Consumer<WatcherException> closeHandler = closeException::set;

      Multi.createFrom().<WatchEvent<Secret>>emitter(emitter -> {
        try (var ignored = watchableSubscriber.subscribe(closeHandler, emitter)) {
          // When
          kubernetesClient.secrets()
            .resource(new SecretBuilder(created)
              .addToStringData("key", "updated")
              .build())
            .update();

          // Then
          Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until(() -> subscriber.getItems().stream()
              .anyMatch(e -> e.type() == Watcher.Action.MODIFIED &&
                "watchable-subscriber-test-modified".equals(e.object().getMetadata().getName())));
        } catch (Exception e) {
          emitter.fail(e);
        }
      }).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> e.type() == Watcher.Action.MODIFIED &&
            "watchable-subscriber-test-modified".equals(e.object().getMetadata().getName())));
    }

    @Test
    @DisplayName("should emit DELETED event when resource is deleted")
    void shouldEmitDeletedEvent() {
      // Given
      kubernetesClient.secrets()
        .resource(new SecretBuilder()
          .withNewMetadata().withName("watchable-subscriber-test-deleted").endMetadata()
          .build())
        .create();

      var watchable = kubernetesClient.secrets().inNamespace(kubernetesClient.getNamespace());
      var watchableSubscriber = WatchableSubscriber.subscriber(watchable);
      var subscriber = AssertSubscriber.<WatchEvent<Secret>>create(Long.MAX_VALUE);
      var closeException = new AtomicReference<WatcherException>();
      Consumer<WatcherException> closeHandler = closeException::set;

      Multi.createFrom().<WatchEvent<Secret>>emitter(emitter -> {
        try (var ignored = watchableSubscriber.subscribe(closeHandler, emitter)) {
          // When
          kubernetesClient.secrets().withName("watchable-subscriber-test-deleted").delete();

          // Then
          Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until(() -> subscriber.getItems().stream()
              .anyMatch(e -> e.type() == Watcher.Action.DELETED &&
                "watchable-subscriber-test-deleted".equals(e.object().getMetadata().getName())));
        } catch (Exception e) {
          emitter.fail(e);
        }
      }).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream()
          .anyMatch(e -> e.type() == Watcher.Action.DELETED &&
            "watchable-subscriber-test-deleted".equals(e.object().getMetadata().getName())));
    }
  }

  @Nested
  @DisplayName("Static factory methods")
  class FactoryMethodTests {

    @Test
    @DisplayName("subscriber(watchable) should create subscriber with identity mapper")
    void subscriberShouldCreateWithIdentityMapper() {
      // Given
      var watchable = kubernetesClient.secrets().inNamespace(kubernetesClient.getNamespace());

      // When
      var subscriber = WatchableSubscriber.subscriber(watchable);

      // Then
      assertThat(subscriber).isInstanceOf(WatchableSubscriber.class);
    }

    @Test
    @DisplayName("subscriber(watchable, mapper) should create subscriber with custom mapper")
    void subscriberWithMapperShouldCreateWithCustomMapper() {
      // Given
      var watchable = kubernetesClient.secrets().inNamespace(kubernetesClient.getNamespace());

      // When
      var subscriber = WatchableSubscriber.subscriber(watchable, secret -> secret.getMetadata().getName());

      // Then
      assertThat(subscriber).isInstanceOf(WatchableSubscriber.class);
    }
  }
}
