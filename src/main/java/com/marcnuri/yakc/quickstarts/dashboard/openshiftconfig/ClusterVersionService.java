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
package com.marcnuri.yakc.quickstarts.dashboard.openshiftconfig;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.configopenshiftio.v1.ConfigOpenshiftIoV1Api;
import com.marcnuri.yakc.model.io.openshift.config.v1.ClusterVersion;
import com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.ClientFunction;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.executeRaw;

@Singleton
public class ClusterVersionService implements Watchable<ClusterVersion> {

  private final ConfigOpenshiftIoV1Api config;

  @Inject
  public ClusterVersionService(KubernetesClient kubernetesClient) {
    config = kubernetesClient.create(ConfigOpenshiftIoV1Api.class);
  }

  @Override
  public Optional<ClientFunction<?>> getAvailabilityCheckFunction() {
    return Optional.of(executeRaw(config.listClusterVersion(new ConfigOpenshiftIoV1Api.ListClusterVersion().limit(1))));
  }

  @Override
  public boolean isRetrySubscription() {
    return false;
  }

  @Override
  public Observable<WatchEvent<ClusterVersion>> watch() throws IOException {
    return config.listClusterVersion().watch();
  }
}
