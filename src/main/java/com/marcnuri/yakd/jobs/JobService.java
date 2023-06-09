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
package com.marcnuri.yakd.jobs;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class JobService implements Watchable<Job> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public JobService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Subscriber<Job> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.batch().v1().jobs().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return subscriber(kubernetesClient.batch().v1().jobs().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.batch().v1().jobs()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.batch().v1().jobs().inNamespace(namespace).withName(name).delete();
  }

  public Job update(String name, String namespace, Job job) {
    return kubernetesClient.batch().v1().jobs().inNamespace(namespace)
      .resource(new JobBuilder(job).editMetadata().withName(name).endMetadata().build())
      .update();
  }

}
