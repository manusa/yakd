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
 * Created on 2020-09-06, 8:36
 */
package com.marcnuri.yakd.quickstarts.dashboard.pod;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Loggable;
import io.reactivex.Observable;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class PodService implements Watchable<Pod> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public PodService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Pod>> watch() throws IOException {
    return tryInOrder(
      () -> {
        kubernetesClient.pods().inAnyNamespace().list(LIMIT_1);
        return observable(kubernetesClient.pods().inAnyNamespace());
      },
      () -> observable(kubernetesClient.pods().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public Pod getPod(String name, String namespace) {
    return kubernetesClient.pods().inNamespace(namespace).withName(name).get();
  }

  public PodMetrics getPodMetrics(String name, String namespace) {
    return kubernetesClient.top().pods().inNamespace(namespace).withName(name).metric();
  }

  public void deletePod(String name, String namespace) {
    kubernetesClient.pods().inNamespace(namespace).withName(name).delete();
  }

  public Pod updatePod(String name, String namespace, Pod pod) {
    return kubernetesClient.pods().inNamespace(namespace)
      .resource(new PodBuilder(pod).editMetadata().withName(name).endMetadata().build())
      .update();
  }

  public Multi<String> getPodContainerLog(String container, String name, String namespace) {
    final var loggable = kubernetesClient.pods().inNamespace(namespace).withName(name).inContainer(container)
      .usingTimestamps().withPrettyOutput();
    return Multi.createFrom().emitter(new LogReader(loggable));
  }

  private static final class LogReader implements Consumer<MultiEmitter<? super String>> {

    private final Loggable loggable;

    LogReader(Loggable loggable) {
      this.loggable = loggable;
    }

    @Override
    public void accept(MultiEmitter<? super String> emitter) {
      try (
        final var watch = loggable.watchLog();
        final var is = watch.getOutput();
        final var isr = new InputStreamReader(is);
        final var br = new BufferedReader(isr);
      ) {
        String line;
        while ((line = br.readLine()) != null && !emitter.isCancelled()) {
          emitter.emit(line);
        }
        emitter.complete();
      } catch (Exception ex) {
        emitter.fail(ex);
      }
    }
  }
}
