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
 * Created on 2021-01-01, 18:34
 */
package com.marcnuri.yakc.quickstarts.dashboard.jobs;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.batch.v1.BatchV1Api;
import com.marcnuri.yakc.model.io.k8s.api.batch.v1.Job;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class JobService implements Watchable<Job> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public JobService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Job>> watch() throws IOException {
    final BatchV1Api batch = kubernetesClient.create(BatchV1Api.class);
    try {
      batch.listJobForAllNamespaces(new BatchV1Api.ListJobForAllNamespaces().limit(1)).get();
      return batch.listJobForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return batch.listNamespacedJob(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public Job delete(String name, String namespace) throws IOException {
    return kubernetesClient.create(BatchV1Api.class).deleteNamespacedJob(name, namespace).get(Job.class);
  }

  public Job update(String name, String namespace, Job job) throws IOException {
    return kubernetesClient.create(BatchV1Api.class).replaceNamespacedJob(name, namespace, job).get();
  }

}
