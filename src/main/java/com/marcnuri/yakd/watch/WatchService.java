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
package com.marcnuri.yakd.watch;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.BackPressureStrategy;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.marcnuri.yakd.KubernetesDashboardConfiguration.WATCH_EXECUTOR_SERVICE;

@Singleton
public class WatchService {

  private final ScheduledExecutorService executorService;
  private final List<Watchable<?>> watchables;

  @SuppressWarnings("java:S107")
  @Inject
  public WatchService(
    @Named(WATCH_EXECUTOR_SERVICE) ScheduledExecutorService executorService,
    Instance<Watchable<?>> watchableHandlers
  ) {
    this.executorService = executorService;
    this.watchables = new ArrayList<>();
    watchableHandlers.forEach(watchables::add);
  }

  public Multi<WatchEvent<?>> newWatch() {
    // No backpressure to reduce memory footprint.
    // Downstream client should handle every event or provide its own buffering
    return Multi.createFrom().emitter(new SelfHealingWatchableEmitter(executorService, watchables), BackPressureStrategy.ERROR);
  }
}
