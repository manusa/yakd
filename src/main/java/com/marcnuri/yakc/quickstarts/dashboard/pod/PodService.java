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
package com.marcnuri.yakc.quickstarts.dashboard.pod;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.KubernetesCall;
import com.marcnuri.yakc.api.KubernetesException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ListPodForAllNamespaces;
import com.marcnuri.yakc.api.core.v1.CoreV1Api.ReadNamespacedPodLog;
import com.marcnuri.yakc.api.metrics.v1beta1.MetricsV1beta1Api;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.Pod;
import com.marcnuri.yakc.model.io.k8s.metrics.pkg.apis.metrics.v1beta1.PodMetrics;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@Singleton
public class PodService implements Watchable<Pod> {

  private static final Logger LOG = LoggerFactory.getLogger(PodService.class);

  private final KubernetesClient kubernetesClient;

  @Inject
  public PodService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Pod>> watch() throws IOException {
    final CoreV1Api core = kubernetesClient.create(CoreV1Api.class);
    try {
      core.listPodForAllNamespaces(new ListPodForAllNamespaces().limit(1)).get();
      return core.listPodForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return core.listNamespacedPod(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public Pod getPod(String name, String namespace) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).readNamespacedPod(name, namespace).get();
  }

  public PodMetrics getPodMetrics(String name, String namespace) throws IOException {
    return kubernetesClient.create(MetricsV1beta1Api.class).readNamespacedPodMetrics(name, namespace).get();
  }

  public Pod deletePod(String name, String namespace) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).deleteNamespacedPod(name, namespace).get();
  }

  public Pod updatePod(String name, String namespace, Pod pod) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replaceNamespacedPod(name, namespace, pod).get();
  }

  public Observable<String> getPodContainerLog(String container, String name, String namespace) {
    final KubernetesCall<String> podLogCall = kubernetesClient.create(CoreV1Api.class)
      .readNamespacedPodLog(name, namespace, new ReadNamespacedPodLog()
        .follow(true).pretty("true").timestamps(true).container(container));
    return Observable.create(new LogReader(podLogCall))
      .doOnError(ex -> LOG.error("Error when reading Pod logs {} - {}", namespace, name, ex));
  }

  private static final class LogReader implements ObservableOnSubscribe<String> {

    private final KubernetesCall<String> podLogCall;

    LogReader(KubernetesCall<String> podLogCall) {
      this.podLogCall = podLogCall;
    }

    @Override
    public void subscribe(ObservableEmitter<String> emitter)
      throws Exception {
      try (
        final Response response = podLogCall.executeRaw();
        final InputStream is = Objects.requireNonNull(response.body()).byteStream();
        final InputStreamReader isr = new InputStreamReader(is);
        final BufferedReader br = new BufferedReader(isr);
      ) {
        if (!response.isSuccessful()) {
          emitter.tryOnError(KubernetesException.forResponse("Error reading logs", response));
        }
        String line;
        while ((line = br.readLine()) != null) {
          emitter.onNext(line);
        }
        emitter.onComplete();
      }
    }
  }
}
