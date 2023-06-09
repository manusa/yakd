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
package com.marcnuri.yakd.quickstarts.dashboard.routes;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.function.Supplier;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class RouteService implements Watchable<Route> {

  private final OpenShiftClient openShiftClient;

  @Inject
  public RouteService(KubernetesClient kubernetesClient) {
    this.openShiftClient = kubernetesClient.adapt(OpenShiftClient.class);
  }

  @Override
  public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
    return Optional.of(() -> openShiftClient.supports(Route.class));
  }

  @Override
  public Observable<WatchEvent<Route>> watch() {
    return tryInOrder(
      () -> {
        openShiftClient.routes().inAnyNamespace().list(LIMIT_1);
        return observable(openShiftClient.routes().inAnyNamespace());
      },
      () -> observable(openShiftClient.routes().inNamespace(openShiftClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    openShiftClient.routes().inNamespace(namespace).withName(name).delete();
  }

  public Route update(String name, String namespace, Route route) {
    return openShiftClient.routes().inNamespace(namespace)
      .resource(new RouteBuilder(route).editMetadata().withName(name).endMetadata().build()).update();
  }
}
