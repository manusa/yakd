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
package com.marcnuri.yakc.quickstarts.dashboard.pod;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ExecMessage;
import com.marcnuri.yakc.apiextensions.ExtendedCoreV1Api;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/api/v1/pods/{namespace}/{name}/exec/{container}")
@Singleton
public class PodExecEndpoint {

  private final Logger log = LoggerFactory.getLogger(PodExecEndpoint.class);

  private final KubernetesClient kubernetesClient;
  private final Map<String, WebSocket> activeSessions;

  @Inject
  public PodExecEndpoint(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
    this.activeSessions = new ConcurrentHashMap<>();
  }

  @OnOpen
  public void onOpen(
    Session session, @PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("container") String container) {

    activeSessions.put(
      session.getId(),
      kubernetesClient.create(ExtendedCoreV1Api.class)
        .execInNamespacedPod(name, namespace, container, Collections.singletonList("/bin/sh"), true, true, true, true)
        .exec(new PodExecWebSocketListener(session))
    );
  }

  @OnMessage
  public void onMessage(Session session, String text) {
    byte[] commandBytes = text.getBytes(StandardCharsets.UTF_8);
    byte[] toSend = new byte[commandBytes.length + 1];
    toSend[0] = (byte) ExecMessage.StandardStream.STDIN.getStandardStreamCode();
    System.arraycopy(commandBytes, 0, toSend, 1, commandBytes.length);
    activeSessions.get(session.getId()).send(ByteString.of(toSend));
  }

  @OnMessage
  public void onMessage(Session session, ByteBuffer byteBuffer) {
    activeSessions.get(session.getId()).send(ByteString.of(byteBuffer));
  }

  @OnError
  public void onError(Throwable ex) {
    log.error("WebSocket error {}", ex.getMessage());
  }

  @OnClose
  public void onClose(Session session, CloseReason reason) {
    activeSessions.get(session.getId()).close(reason.getCloseCode().getCode(), reason.getReasonPhrase());
    activeSessions.remove(session.getId());
  }

  private static final class PodExecWebSocketListener extends WebSocketListener {
    private final Session session;

    public PodExecWebSocketListener(Session session) {
      this.session = session;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
      session.getAsyncRemote().sendText(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
      session.getAsyncRemote().sendBinary(bytes.asByteBuffer());
    }
  }

}
