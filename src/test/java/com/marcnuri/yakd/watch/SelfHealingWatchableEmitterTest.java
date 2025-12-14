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
package com.marcnuri.yakd.watch;

import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class SelfHealingWatchableEmitterTest {

  private ScheduledExecutorService executorService;

  @BeforeEach
  void setUp() {
    executorService = Executors.newScheduledThreadPool(4);
  }

  @AfterEach
  void tearDown() {
    executorService.shutdownNow();
  }

  @Nested
  @DisplayName("SelfHealingWatchableEmitter.accept()")
  class AcceptTests {

    @Test
    @DisplayName("should subscribe to all watchables")
    void acceptShouldSubscribeToAllWatchables() {
      // Given
      var watchable1 = new TestWatchable();
      var watchable2 = new TestWatchable();
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable1, watchable2));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      // When
      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable1.subscribeCount.get() > 0 && watchable2.subscribeCount.get() > 0);
      assertThat(watchable1.subscribeCount.get()).isGreaterThanOrEqualTo(1);
      assertThat(watchable2.subscribeCount.get()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should emit events from watchables")
    void acceptShouldEmitEvents() {
      // Given
      var watchable = new TestWatchable();
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      // When
      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() > 0);

      watchable.emitEvent("test-event");

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> !subscriber.getItems().isEmpty());
      assertThat(subscriber.getItems().get(0).type()).isEqualTo(Watcher.Action.ADDED);
      assertThat(subscriber.getItems().get(0).object()).isEqualTo("test-event");
    }
  }

  @Nested
  @DisplayName("Self-healing behavior")
  class SelfHealingTests {

    @Test
    @DisplayName("should emit ERROR event with RequestRestartError on watcher close")
    void shouldEmitErrorOnWatcherClose() {
      // Given
      var watchable = new TestWatchable();
      watchable.selfHealingDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() > 0);

      // When
      watchable.triggerClose(new WatcherException("Test exception"));

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> subscriber.getItems().stream().anyMatch(e -> e.type() == Watcher.Action.ERROR));

      var errorEvent = subscriber.getItems().stream()
        .filter(e -> e.type() == Watcher.Action.ERROR)
        .findFirst()
        .orElseThrow();
      assertThat(errorEvent.object()).isInstanceOf(RequestRestartError.class);
    }

    @Test
    @DisplayName("should resubscribe after watcher close with exception when retrySubscription is true")
    void shouldResubscribeOnError() {
      // Given
      var watchable = new TestWatchable();
      watchable.selfHealingDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() == 1);

      // When
      watchable.triggerClose(new WatcherException("Test exception"));

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() >= 2);
    }

    @Test
    @DisplayName("should resubscribe after watcher close without exception when retrySubscription is true")
    void shouldResubscribeOnCloseWithoutException() {
      // Given
      var watchable = new TestWatchable();
      watchable.selfHealingDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() == 1);

      // When
      watchable.triggerClose(null);

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() >= 2);
    }

    @Test
    @DisplayName("should not resubscribe when retrySubscription is false")
    void shouldNotResubscribeWhenRetryFalse() {
      // Given
      var watchable = new TestWatchable();
      watchable.retrySubscription = false;
      watchable.selfHealingDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() == 1);

      // When - triggerClose invokes handlers synchronously
      watchable.triggerClose(new WatcherException("Test exception"));

      // Then - When retrySubscription is false:
      // 1. No ERROR event should be emitted (per implementation)
      // 2. No resubscription should occur
      // Since handlers run synchronously on triggerClose, we can assert immediately
      assertThat(subscriber.getItems().stream().noneMatch(e -> e.type() == Watcher.Action.ERROR))
        .as("No ERROR event should be emitted when retrySubscription is false")
        .isTrue();
      assertThat(watchable.subscribeCount.get())
        .as("subscribeCount should still be 1 (no resubscription)")
        .isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("Availability check")
  class AvailabilityCheckTests {

    @Test
    @DisplayName("should skip subscription when availability check returns false")
    void shouldSkipSubscriptionWhenNotAvailable() {
      // Given
      var watchable = new TestWatchable();
      watchable.availabilityCheck = () -> false;
      watchable.retrySubscriptionDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      // When
      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      // Then - should retry after delay
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.availabilityCheckCount.get() >= 2);
      assertThat(watchable.subscribeCount.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("should subscribe when availability check becomes true")
    void shouldSubscribeWhenBecomesAvailable() {
      // Given
      var watchable = new TestWatchable();
      watchable.availabilityCheck = () -> watchable.availabilityCheckCount.get() > 1;
      watchable.retrySubscriptionDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      // When
      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() >= 1);
    }
  }

  @Nested
  @DisplayName("Termination handling")
  class TerminationTests {

    @Test
    @DisplayName("should close all active watches on termination")
    void shouldCloseWatchesOnTermination() {
      // Given
      var watchable1 = new TestWatchable();
      var watchable2 = new TestWatchable();
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable1, watchable2));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable1.subscribeCount.get() > 0 && watchable2.subscribeCount.get() > 0);

      // When
      subscriber.cancel();

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable1.closeCount.get() > 0 && watchable2.closeCount.get() > 0);
    }

    @Test
    @DisplayName("should not resubscribe after emitter is cancelled")
    void shouldNotResubscribeAfterCancel() {
      // Given
      var watchable = new TestWatchable();
      watchable.selfHealingDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() == 1);

      final int subscribeCountBefore = watchable.subscribeCount.get();

      // When
      subscriber.cancel();

      // Then - wait for the watch to be closed (termination cleanup), then verify no resubscription
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.closeCount.get() > 0);

      // State-based check: subscribeCount should be unchanged (no resubscription occurred)
      assertThat(watchable.subscribeCount.get()).isEqualTo(subscribeCountBefore);
    }
  }

  @Nested
  @DisplayName("Duplicate subscription handling")
  class DuplicateSubscriptionTests {

    @Test
    @DisplayName("should cancel previous subscription when resubscribing same watchable")
    void shouldCancelPreviousSubscription() {
      // Given
      var watchable = new TestWatchable();
      watchable.selfHealingDelay = Duration.ofMillis(50);
      var emitter = new SelfHealingWatchableEmitter(executorService, List.of(watchable));
      var subscriber = AssertSubscriber.<WatchEvent<?>>create(Long.MAX_VALUE);

      Multi.createFrom().emitter(emitter).subscribe().withSubscriber(subscriber);

      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() == 1);

      // When - trigger close to force resubscription
      watchable.triggerClose(null);

      // Then
      Awaitility.await().atMost(2, TimeUnit.SECONDS)
        .until(() -> watchable.subscribeCount.get() >= 2);

      // Previous watch should have been closed before new subscription
      assertThat(watchable.closeCount.get()).isGreaterThanOrEqualTo(1);
    }
  }

  /**
   * Test implementation of Watchable for unit testing.
   */
  static class TestWatchable implements Watchable<String> {
    final AtomicInteger subscribeCount = new AtomicInteger(0);
    final AtomicInteger closeCount = new AtomicInteger(0);
    final AtomicInteger availabilityCheckCount = new AtomicInteger(0);
    final List<Consumer<WatcherException>> closeHandlers = new CopyOnWriteArrayList<>();
    final List<io.smallrye.mutiny.subscription.MultiEmitter<? super WatchEvent<String>>> emitters = new CopyOnWriteArrayList<>();

    boolean retrySubscription = true;
    Duration selfHealingDelay = Duration.ofSeconds(5);
    Duration retrySubscriptionDelay = Duration.ofSeconds(30);
    Supplier<Boolean> availabilityCheck = null;

    @Override
    public Subscriber<String> watch() {
      return (close, emitter) -> {
        subscribeCount.incrementAndGet();
        closeHandlers.add(close);
        emitters.add(emitter);
        return closeCount::incrementAndGet;
      };
    }

    @Override
    public String getType() {
      return "TestWatchable";
    }

    @Override
    public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
      if (availabilityCheck == null) {
        return Optional.empty();
      }
      return Optional.of(() -> {
        availabilityCheckCount.incrementAndGet();
        return availabilityCheck.get();
      });
    }

    @Override
    public boolean isRetrySubscription() {
      return retrySubscription;
    }

    @Override
    public Duration getSelfHealingDelay() {
      return selfHealingDelay;
    }

    @Override
    public Duration getRetrySubscriptionDelay() {
      return retrySubscriptionDelay;
    }

    void emitEvent(String event) {
      emitters.forEach(e -> e.emit(new WatchEvent<>(Watcher.Action.ADDED, event)));
    }

    void triggerClose(WatcherException exception) {
      closeHandlers.forEach(h -> h.accept(exception));
    }
  }
}
