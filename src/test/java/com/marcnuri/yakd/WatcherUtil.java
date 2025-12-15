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
 * Created on 2023-12-16, 07:10
 */
package com.marcnuri.yakd;

import com.marcnuri.yakd.watch.WatchEvent;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.sse.SseEventSource;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WatcherUtil {
  private WatcherUtil() {
  }

  public record Watch(Client client, SseEventSource eventSource, ConcurrentLinkedQueue<WatchEvent<Map<String, ?>>> events) implements AutoCloseable {
    @Override
    public void close() {
      eventSource.close();
      client.close();
    }
  }

  public static Watch watch(URL url, String path) throws URISyntaxException {
    final ConcurrentLinkedQueue<WatchEvent<Map<String, ?>>> messagesReceived = new ConcurrentLinkedQueue<>();
    final var uri = UriBuilder.fromUri(url.toURI()).replacePath(path).build();
    var wsCli = ClientBuilder.newClient();
    final var sseSource = SseEventSource.target(wsCli.target(uri)).build();
    sseSource.register(event -> messagesReceived.add(event.readData(WatchEvent.class)));
    sseSource.open();
    return new Watch(wsCli, sseSource, messagesReceived);
  }
}
