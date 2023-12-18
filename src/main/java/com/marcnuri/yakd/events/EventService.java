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
 * Created on 2020-09-05, 7:26
 */
package com.marcnuri.yakd.events;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class EventService implements Watchable<Event> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public EventService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<Event> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.v1().events().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return subscriber(kubernetesClient.v1().events().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.v1().events().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

}
