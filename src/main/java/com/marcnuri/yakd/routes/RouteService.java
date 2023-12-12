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
 * Created on 2020-11-21, 9:32
 */
package com.marcnuri.yakd.routes;

import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.function.Supplier;

import static com.marcnuri.yakd.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class RouteService implements Watchable<Route> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public RouteService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
    return Optional.of(() -> kubernetesClient.supports(Route.class));
  }

  @Override
  public Subscriber<Route> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.resources(Route.class).inAnyNamespace().list(LIMIT_1);
        return subscriber(kubernetesClient.resources(Route.class).inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.resources(Route.class).inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.resources(Route.class).inNamespace(namespace).withName(name).delete();
  }

  public Route update(String name, String namespace, Route route) {
    return kubernetesClient.resources(Route.class).inNamespace(namespace)
      .resource(new RouteBuilder(route).editMetadata().withName(name).endMetadata().build()).update();
  }
}
