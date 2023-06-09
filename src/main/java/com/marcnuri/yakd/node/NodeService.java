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
package com.marcnuri.yakd.node;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

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
  public Subscriber<Node> watch() {
    return subscriber(kubernetesClient.nodes());
  }

  public Node update(String name, Node node) {
    return kubernetesClient.nodes()
      .resource(new NodeBuilder(node).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
