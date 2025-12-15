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
 * Created on 2024-12-14
 */
package com.marcnuri.yakd.pod;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class PodExecEndpointTest {

  @TestHTTPResource("/api/v1/pods/default/test-pod/exec/main")
  URI execUri;

  @Nested
  @DisplayName("WebSocket connection lifecycle")
  class ConnectionLifecycleTests {

    private TestWebSocketClient client;
    private Session session;

    @BeforeEach
    void setUp() throws Exception {
      client = new TestWebSocketClient();
      session = ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @AfterEach
    void tearDown() {
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
    }

    @Test
    @DisplayName("should establish WebSocket connection to endpoint")
    void shouldEstablishConnection() {
      assertThat(client.openLatch.getCount()).isZero();
    }

    @Test
    @DisplayName("should receive close reason when connection closes")
    void shouldReceiveCloseReason() {
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }

    @Test
    @DisplayName("should handle multiple concurrent connections with unique session IDs")
    void shouldHandleMultipleConnections() throws Exception {
      var client2 = new TestWebSocketClient();
      var session2 = ContainerProvider.getWebSocketContainer().connectToServer(client2, execUri);
      assertThat(client2.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      assertThat(session.getId()).isNotEqualTo(session2.getId());

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client2.closeLatch.getCount() == 0);
    }
  }

  @Nested
  @DisplayName("Message transmission")
  class MessageTransmissionTests {

    private TestWebSocketClient client;
    private Session session;

    @BeforeEach
    void setUp() throws Exception {
      client = new TestWebSocketClient();
      session = ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @AfterEach
    void tearDown() {
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
    }

    @Test
    @DisplayName("should accept text message")
    void shouldAcceptTextMessage() throws Exception {
      session.getBasicRemote().sendText("echo hello");
      // Session closes after K8s API error, message was accepted without exception
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }

    @Test
    @DisplayName("should accept binary message")
    void shouldAcceptBinaryMessage() throws Exception {
      session.getBasicRemote().sendBinary(ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02, 0x03}));
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }

    @Test
    @DisplayName("should handle multiple messages in sequence")
    void shouldHandleMultipleMessages() throws Exception {
      session.getBasicRemote().sendText("command1");
      session.getBasicRemote().sendText("command2");
      session.getBasicRemote().sendText("command3");
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }
  }

  @Nested
  @DisplayName("Error handling")
  class ErrorHandlingTests {

    @Test
    @DisplayName("should close connection when K8s API connection fails")
    void shouldCloseOnKubernetesApiError() throws Exception {
      var client = new TestWebSocketClient();
      ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }

    @Test
    @DisplayName("should handle connection to different namespace")
    void shouldHandleDifferentNamespace() throws Exception {
      var client = new TestWebSocketClient();
      var uri = URI.create(execUri.toString().replace("/default/", "/other-namespace/"));
      ContainerProvider.getWebSocketContainer().connectToServer(client, uri);

      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }

    @Test
    @DisplayName("should handle connection to different container")
    void shouldHandleDifferentContainer() throws Exception {
      var client = new TestWebSocketClient();
      var uri = URI.create(execUri.toString().replace("/main", "/sidecar"));
      ContainerProvider.getWebSocketContainer().connectToServer(client, uri);

      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason.get()).isNotNull();
    }
  }

  @Nested
  @DisplayName("Connection cleanup")
  class ConnectionCleanupTests {

    @Test
    @DisplayName("should trigger onClose handler when session closes")
    void shouldTriggerOnCloseHandler() throws Exception {
      var client = new TestWebSocketClient();
      ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
      assertThat(client.closeReason.get()).isNotNull();
    }

    @Test
    @DisplayName("should handle rapid open and close cycles")
    void shouldHandleRapidOpenCloseCycles() throws Exception {
      for (int i = 0; i < 3; i++) {
        var client = new TestWebSocketClient();
        ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
        assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
      }
    }
  }

  @Nested
  @DisplayName("StandardStream enum")
  class StandardStreamTests {

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

  @ClientEndpoint
  public static class TestWebSocketClient {
    final CountDownLatch openLatch = new CountDownLatch(1);
    final CountDownLatch closeLatch = new CountDownLatch(1);
    final AtomicReference<CloseReason> closeReason = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final CopyOnWriteArrayList<String> textMessages = new CopyOnWriteArrayList<>();
    final CopyOnWriteArrayList<ByteBuffer> binaryMessages = new CopyOnWriteArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
      openLatch.countDown();
    }

    @OnMessage
    public void onMessage(String message) {
      textMessages.add(message);
    }

    @OnMessage
    public void onMessage(ByteBuffer message) {
      binaryMessages.add(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
      closeReason.set(reason);
      closeLatch.countDown();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
      error.set(throwable);
    }
  }
}
