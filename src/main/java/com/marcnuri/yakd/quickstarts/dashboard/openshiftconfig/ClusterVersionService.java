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
 * Created on 2021-01-02, 10:01
 */
package com.marcnuri.yakd.quickstarts.dashboard.openshiftconfig;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.config.v1.ClusterVersion;
import io.fabric8.openshift.client.OpenShiftClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;

@Singleton
public class ClusterVersionService implements Watchable<ClusterVersion> {

  private final OpenShiftClient openShiftClient;

  @Inject
  public ClusterVersionService(KubernetesClient kubernetesClient) {
    this.openShiftClient = kubernetesClient.adapt(OpenShiftClient.class);
  }

  @Override
  public Optional<Supplier<Boolean>> getAvailabilityCheckFunction() {
    return Optional.of(() -> openShiftClient.supports(ClusterVersion.class));
  }

  @Override
  public boolean isRetrySubscription() {
    return false;
  }

  @Override
  public Observable<WatchEvent<ClusterVersion>> watch() throws IOException {
    return observable(openShiftClient.config().clusterVersions());
  }
}
