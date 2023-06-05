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
 * Created on 2021-01-09, 17:43
 */
package com.marcnuri.yakc.quickstarts.dashboard.horizontalpodautoscalers;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.autoscaling.v1.AutoscalingV1Api;
import com.marcnuri.yakc.model.io.k8s.api.autoscaling.v1.HorizontalPodAutoscaler;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

@Singleton
public class HorizontalPodAutoscalerService implements Watchable<HorizontalPodAutoscaler> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public HorizontalPodAutoscalerService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public boolean isRetrySubscription() {
    return false;
  }

  @Override
  public Observable<WatchEvent<HorizontalPodAutoscaler>> watch() throws IOException {
    final AutoscalingV1Api autoscaling = kubernetesClient.create(AutoscalingV1Api.class);
    return tryWithFallback(
      () -> {
        autoscaling.listHorizontalPodAutoscalerForAllNamespaces(new AutoscalingV1Api.ListHorizontalPodAutoscalerForAllNamespaces().limit(1))
          .get();
        return autoscaling.listHorizontalPodAutoscalerForAllNamespaces().watch();
      },
      () -> autoscaling.listNamespacedHorizontalPodAutoscaler(kubernetesClient.getConfiguration().getNamespace()).watch()
    );
  }

  public Status delete(String name, String namespace) throws IOException {
    return kubernetesClient.create(AutoscalingV1Api.class).deleteNamespacedHorizontalPodAutoscaler(name, namespace).get();
  }

  public HorizontalPodAutoscaler update(String name, String namespace, HorizontalPodAutoscaler horizontalPodAutoscaler) throws IOException {
    return kubernetesClient.create(AutoscalingV1Api.class)
      .replaceNamespacedHorizontalPodAutoscaler(name, namespace, horizontalPodAutoscaler).get();
  }
}
