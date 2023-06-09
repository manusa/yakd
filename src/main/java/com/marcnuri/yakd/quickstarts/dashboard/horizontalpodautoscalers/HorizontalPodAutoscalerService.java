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
package com.marcnuri.yakd.quickstarts.dashboard.horizontalpodautoscalers;

import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

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
  public Observable<WatchEvent<HorizontalPodAutoscaler>> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.autoscaling().v1().horizontalPodAutoscalers().inAnyNamespace().list(LIMIT_1);
        return observable(kubernetesClient.autoscaling().v1().horizontalPodAutoscalers().inAnyNamespace());
      },
      () -> observable(kubernetesClient.autoscaling().v1().horizontalPodAutoscalers()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.autoscaling().v1().horizontalPodAutoscalers().inNamespace(namespace).withName(name).delete();
  }

  public HorizontalPodAutoscaler update(String name, String namespace, HorizontalPodAutoscaler horizontalPodAutoscaler) {
    return kubernetesClient.autoscaling().v1().horizontalPodAutoscalers().inNamespace(namespace)
      .resource(new HorizontalPodAutoscalerBuilder(horizontalPodAutoscaler).editMetadata().withName(name).endMetadata().build()).update();
  }
}
