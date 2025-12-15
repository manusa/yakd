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
 * Created on 2025-12-14
 */
package com.marcnuri.yakd.pod;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StandardStream enum")
class StandardStreamTest {

  @Test
  @DisplayName("STDIN should have code 0")
  void stdinShouldHaveCode0() {
    assertThat(PodExecEndpoint.StandardStream.STDIN.getStandardStreamCode()).isZero();
  }

  @Test
  @DisplayName("STDOUT should have code 1")
  void stdoutShouldHaveCode1() {
    assertThat(PodExecEndpoint.StandardStream.STDOUT.getStandardStreamCode()).isEqualTo(1);
  }

  @Test
  @DisplayName("STDERR should have code 2")
  void stderrShouldHaveCode2() {
    assertThat(PodExecEndpoint.StandardStream.STDERR.getStandardStreamCode()).isEqualTo(2);
  }
}
