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
 * Created on 2020-12-28, 18:01
 */
package com.marcnuri.yakd.quickstarts.dashboard.pod;

import com.marcnuri.yakc.api.ExecMessage;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.http.WebSocket;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/api/v1/pods/{namespace}/{name}/exec/{container}")
@Singleton
public class PodExecEndpoint {

  private final Logger LOG = LoggerFactory.getLogger(PodExecEndpoint.class);

  private final KubernetesClient kubernetesClient;
  private final Map<String, CompletableFuture<WebSocket>> activeSessions;

  @Inject
  public PodExecEndpoint(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
    this.activeSessions = new ConcurrentHashMap<>();
  }

  @OnOpen
  public void onOpen(
    Session session, @PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("container") String container
  ) throws URISyntaxException {
    final var uri = UriBuilder.fromUri(kubernetesClient.getMasterUrl().toURI())
      .path("api").path("v1").path("namespaces").path(namespace).path("pods").path(name).path("exec")
      .queryParam("container", container)
      .queryParam("command", "/bin/sh")
      .queryParam("stdin", true)
      .queryParam("stdout", true)
      .queryParam("stderr", true)
      .queryParam("tty", true)
      .build();
    final var webSocketFuture = kubernetesClient.getHttpClient().newWebSocketBuilder()
      .uri(uri)
      .subprotocol("v4.channel.k8s.io")
      .buildAsync(new PodExecWebSocketListener(session))
      .exceptionally(throwable -> {
        try {
          session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
        } catch (IOException ex) {
          LOG.error("Error closing session {}", session.getId(), ex);
        }
        return null;
      });
    activeSessions.put(session.getId(), webSocketFuture);
  }

  @OnMessage
  public void onMessage(Session session, String text) {
    final var ws = activeSessions.get(session.getId()).getNow(null);
    if (ws == null) {
      return;
    }
    byte[] commandBytes = text.getBytes(StandardCharsets.UTF_8);
    byte[] toSend = new byte[commandBytes.length + 1];
    toSend[0] = (byte) ExecMessage.StandardStream.STDIN.getStandardStreamCode();
    System.arraycopy(commandBytes, 0, toSend, 1, commandBytes.length);
    ws.send(ByteBuffer.wrap(toSend));
  }

  @OnMessage
  public void onMessage(Session session, ByteBuffer byteBuffer) {
    final var ws = activeSessions.get(session.getId()).getNow(null);
    if (ws == null) {
      return;
    }
    ws.send(byteBuffer);
  }

  @OnError
  public void onError(Throwable ex) {
    LOG.error("WebSocket error {}", ex.getMessage());
  }

  @OnClose
  public void onClose(Session session, CloseReason reason) {
    final var as = activeSessions.get(session.getId());
    if (as.isDone()) {
      as.getNow(null).sendClose(reason.getCloseCode().getCode(), reason.getReasonPhrase());
    } else {
      as.cancel(true);
    }
    activeSessions.remove(session.getId());
  }

  private record PodExecWebSocketListener(Session session) implements WebSocket.Listener {
    @Override
    public void onOpen(WebSocket webSocket) {
      WebSocket.Listener.super.onOpen(webSocket);
      webSocket.request();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
      session.getAsyncRemote().sendText(text);
      webSocket.request();
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteBuffer bytes) {
      session.getAsyncRemote().sendBinary(bytes);
      webSocket.request();
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(code), reason));
      } catch (IOException ignore) {
      }
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error, boolean connectionError) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, error.getMessage()));
      } catch (IOException ignore) {
      }
    }
  }

}
