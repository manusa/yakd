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
 * Created on 2021-12-12, 15:37
 */
package com.marcnuri.yakc.quickstarts.dashboard.watch;

import com.marcnuri.yakc.model.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiAvailability {

  private static final int CHECK_INTERVAL_SECONDS = 30;

  private final Map<Class<?>, Availability> availability;

  public ApiAvailability() {
    this.availability = new ConcurrentHashMap<>();
  }

  private void check(Watchable<? extends Model> watchable) {
      final var wAvailability = availability.get(watchable.getClass());
      if (wAvailability == null ||
        Duration.between(wAvailability.lastCheck, LocalDateTime.now()).toSeconds() > CHECK_INTERVAL_SECONDS) {
        try {
          watchable.getAvailabilityCheckFunction().orElseThrow().call();
          availability.put(watchable.getClass(), new Availability(LocalDateTime.now(), true));
        } catch(Exception e) {
          availability.put(watchable.getClass(), new Availability(LocalDateTime.now(), false));
        }
      }
  }

  boolean isAvailable(Watchable<? extends Model> watchable) {
    if (watchable.getAvailabilityCheckFunction().isEmpty()) {
      return true;
    }
    check(watchable);
    return availability.get(watchable.getClass()).available;
  }

  private static final class Availability {
    private final LocalDateTime lastCheck;
    private final boolean available;

    Availability(LocalDateTime lastCheck, boolean available) {
      this.lastCheck = lastCheck;
      this.available = available;
    }
  }
}
