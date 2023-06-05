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
 * Created on 2020-09-04, 15:57
 */
package com.marcnuri.yakc.quickstarts.dashboard.node;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.Node;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class NodeService implements Watchable<Node> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public NodeService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public boolean isRetrySubscription() {
    return false;
  }

  @Override
  public Observable<WatchEvent<Node>> watch() throws IOException {
    final CoreV1Api core = kubernetesClient.create(CoreV1Api.class);
    return core.listNode().watch();
  }

  public Node update(String name, Node node) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNode(name, node).get();
  }
}
