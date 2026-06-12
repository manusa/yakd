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
 * Created on 2026-06-12
 */
package com.marcnuri.yakd.fabric8;

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientUtilTest {

  @Test
  @DisplayName("tryInOrder - returns the first supplier's value when it succeeds")
  void returnsFirstSuccessfulValue() {
    final Supplier<String> first = () -> "first";
    final Supplier<String> second = () -> "second";
    assertThat(tryInOrder(first, second)).isEqualTo("first");
  }

  @Test
  @DisplayName("tryInOrder - falls back to the next supplier when the previous one is denied")
  void fallsBackOnKubernetesClientException() {
    final Supplier<String> denied = () -> {
      throw new KubernetesClientException("Forbidden");
    };
    final Supplier<String> fallback = () -> "fallback";
    assertThat(tryInOrder(denied, fallback)).isEqualTo("fallback");
  }

  @Test
  @DisplayName("tryInOrder - throws the last exception when every supplier is denied")
  void throwsLastExceptionWhenAllDenied() {
    final Supplier<String> first = () -> {
      throw new KubernetesClientException("first failure");
    };
    final Supplier<String> last = () -> {
      throw new KubernetesClientException("last failure");
    };
    assertThatThrownBy(() -> tryInOrder(first, last))
      .isInstanceOf(KubernetesClientException.class)
      .hasMessage("last failure");
  }
}
