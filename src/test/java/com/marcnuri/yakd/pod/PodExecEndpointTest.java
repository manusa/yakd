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

import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.mockwebserver.internal.WebSocketMessage;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
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

  @KubernetesTestServer
  KubernetesServer kubernetes;

  private TestWebSocketClient client;

  @BeforeEach
  void setUp() {
    for (var container : new String[]{"main", "sidecar"}) {
      kubernetes.expect()
        .get()
        .withPath(
          "/api/v1/namespaces/default/pods/test-pod/exec?container=" + container + "&command=%2Fbin%2Fsh&stdin=true&stdout=true&stderr=true&tty=true"
        )
        .andUpgradeToWebSocket()
        .open()
        .waitFor(200L)
        .andEmit(new WebSocketMessage(0L, "\u0003" + Serialization.asJson(new StatusBuilder().withStatus("Success").build()), false, true))
        .done()
        .always();
    }
    client = new TestWebSocketClient();
  }

  @Nested
  @DisplayName("WebSocket connection lifecycle")
  class ConnectionLifecycleTests {
    private Session session;

    @BeforeEach
    void setUp() throws Exception {
      session = ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
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

    private Session session;

    @BeforeEach
    void setUp() throws Exception {
      session = ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
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
      var uri = URI.create(execUri.toString().replace("/main", "/not-mocked"));
      ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason)
        .doesNotHaveNullValue()
        .hasValueMatching(reason -> reason.getCloseCode() == CloseReason.CloseCodes.UNEXPECTED_CONDITION);
    }

    @Test
    @DisplayName("should handle connection to different namespace")
    void shouldHandleDifferentNamespace() throws Exception {
      var uri = URI.create(execUri.toString().replace("/default/", "/other-namespace/"));
      ContainerProvider.getWebSocketContainer().connectToServer(client, uri);

      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeReason.get() != null);
      assertThat(client.closeReason)
        .doesNotHaveNullValue()
        .hasValueMatching(reason -> reason.getCloseCode() == CloseReason.CloseCodes.UNEXPECTED_CONDITION);
    }

    @Test
    @DisplayName("should handle connection to different container")
    void shouldHandleDifferentContainer() throws Exception {
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
      ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
      assertThat(client.closeReason)
        .doesNotHaveNullValue()
        .hasValueMatching(reason -> reason.getCloseCode() == CloseReason.CloseCodes.NORMAL_CLOSURE);
    }

    @Test
    @DisplayName("should handle rapid open and close cycles")
    void shouldHandleRapidOpenCloseCycles() throws Exception {
      for (int i = 0; i < 3; i++) {
        client = new TestWebSocketClient();
        ContainerProvider.getWebSocketContainer().connectToServer(client, execUri);
        assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
      }
    }
  }

  @Nested
  @DisplayName("Message reception from K8s API")
  class MessageReceptionTests {

    @Test
    @DisplayName("should forward text messages from K8s API to client")
    void shouldForwardTextMessagesFromKubernetesApi() throws Exception {
      // Set up a mock that emits a text WebSocket frame (String constructor = text frame)
      kubernetes.expect()
        .get()
        .withPath(
          "/api/v1/namespaces/text-ns/pods/test-pod/exec?container=main&command=%2Fbin%2Fsh&stdin=true&stdout=true&stderr=true&tty=true"
        )
        .andUpgradeToWebSocket()
        .open()
        .immediately()
        .andEmit(new WebSocketMessage(0L, "\u0001Hello from K8s", false, false))
        .waitFor(100L)
        .andEmit(new WebSocketMessage(0L, new byte[0], false, true))
        .done()
        .once();

      var uri = URI.create(execUri.toString().replace("/default/", "/text-ns/"));
      ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      // Wait for the text message to be received
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !client.textMessages.isEmpty());
      assertThat(client.textMessages).hasSize(1);
      assertThat(client.textMessages.get(0)).contains("Hello from K8s");

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
    }

    @Test
    @DisplayName("should forward binary messages from K8s API to client")
    void shouldForwardBinaryMessagesFromKubernetesApi() throws Exception {
      // Set up a mock that emits a binary message
      kubernetes.expect()
        .get()
        .withPath(
          "/api/v1/namespaces/binary-ns/pods/test-pod/exec?container=main&command=%2Fbin%2Fsh&stdin=true&stdout=true&stderr=true&tty=true"
        )
        .andUpgradeToWebSocket()
        .open()
        .immediately()
        .andEmit(new WebSocketMessage(0L, new byte[]{0x01, 'h', 'e', 'l', 'l', 'o'}, false, true))
        .done()
        .once();

      var uri = URI.create(execUri.toString().replace("/default/", "/binary-ns/"));
      ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      // Wait for the binary message to be received
      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !client.binaryMessages.isEmpty());
      assertThat(client.binaryMessages).hasSize(1);
      var received = client.binaryMessages.get(0);
      assertThat(received.get(0)).isEqualTo((byte) 0x01); // STDOUT prefix

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
    }
  }

  @Nested
  @DisplayName("Client to K8s message forwarding")
  class ClientToKubernetesMessageTests {

    @Test
    @DisplayName("should forward client text message to K8s WebSocket when connected")
    void shouldForwardClientTextMessageToKubernetesWebSocket() throws Exception {
      kubernetes.expect()
        .get()
        .withPath(
          "/api/v1/namespaces/stdin-ns/pods/test-pod/exec?container=main&command=%2Fbin%2Fsh&stdin=true&stdout=true&stderr=true&tty=true"
        )
        .andUpgradeToWebSocket()
        .open()
        .waitFor(10L).andEmit("OPEN")
        .expect("\u0000echo hello").andEmit("RECEIVED").always()
        .expectSentWebSocketMessage("\u0000echo hello").andEmit("RECEIVED").always()
        .done()
        .once();

      var uri = URI.create(execUri.toString().replace("/default/", "/stdin-ns/"));
      var session = ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();
      Awaitility.await()
        .pollInterval(Duration.ofMillis(50L))
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> !client.textMessages.isEmpty());

      // Send a text message from client - the endpoint prepends STDIN prefix and forwards to K8s
      // This exercises the onMessage(String) handler where text is converted to binary with STDIN prefix
      session.getBasicRemote().sendText("echo hello");

      Awaitility.await()
        .pollInterval(Duration.ofMillis(50L))
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> client.textMessages.size() > 1);
      assertThat(client.textMessages).contains("RECEIVED");
    }

  }

  @Nested
  @DisplayName("Client-initiated close")
  class ClientInitiatedCloseTests {

    @Test
    @DisplayName("should close K8s WebSocket when client closes session")
    void shouldCloseKubernetesWebSocketWhenClientCloses() throws Exception {
      // Set up a mock that stays open longer
      kubernetes.expect()
        .get()
        .withPath(
          "/api/v1/namespaces/close-ns/pods/test-pod/exec?container=main&command=%2Fbin%2Fsh&stdin=true&stdout=true&stderr=true&tty=true"
        )
        .andUpgradeToWebSocket()
        .open()
        .waitFor(5000L)
        .andEmit(new WebSocketMessage(0L, "\u0003Done", false, true))
        .done()
        .once();

      var uri = URI.create(execUri.toString().replace("/default/", "/close-ns/"));
      var session = ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      // Client initiates close
      session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Client closing"));

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
      assertThat(client.closeReason.get()).isNotNull();
      assertThat(client.closeReason.get().getCloseCode()).isEqualTo(CloseReason.CloseCodes.NORMAL_CLOSURE);
    }

    @Test
    @DisplayName("should cancel pending WebSocket when client closes before connection established")
    void shouldCancelPendingWebSocketOnEarlyClose() throws Exception {
      // Set up a mock with a delay before the WebSocket is ready
      kubernetes.expect()
        .get()
        .withPath(
          "/api/v1/namespaces/early-close-ns/pods/test-pod/exec?container=main&command=%2Fbin%2Fsh&stdin=true&stdout=true&stderr=true&tty=true"
        )
        .andUpgradeToWebSocket()
        .open()
        .waitFor(10000L)
        .andEmit(new WebSocketMessage(0L, "\u0003Done", false, true))
        .done()
        .once();

      var uri = URI.create(execUri.toString().replace("/default/", "/early-close-ns/"));
      var session = ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
      assertThat(client.openLatch.await(5, TimeUnit.SECONDS)).isTrue();

      // Close immediately before K8s WebSocket message arrives
      session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Early close"));

      Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> client.closeLatch.getCount() == 0);
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
