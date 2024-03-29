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
 * Created on 2021-01-02, 17:30
 */
package com.marcnuri.yakd.cronjobs;

import com.marcnuri.yakd.fabric8.ClientUtil;
import com.marcnuri.yakd.watch.Subscriber;
import com.marcnuri.yakd.watch.Watchable;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobSpec;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobSpecBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobTemplateSpec;
import io.fabric8.kubernetes.api.model.batch.v1.JobTemplateSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

import static com.marcnuri.yakd.fabric8.ClientUtil.tryInOrder;
import static com.marcnuri.yakd.fabric8.WatchableSubscriber.subscriber;

@Singleton
public class CronJobService implements Watchable<CronJob> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public CronJobService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  static CronJob to(io.fabric8.kubernetes.api.model.batch.v1beta1.CronJob from) {
    return new CronJobBuilder()
      .withMetadata(from.getMetadata())
      .withSpec(to(from.getSpec()))
      .build();
  }

  static CronJobSpec to(io.fabric8.kubernetes.api.model.batch.v1beta1.CronJobSpec from) {
    return new CronJobSpecBuilder()
      .withConcurrencyPolicy(from.getConcurrencyPolicy())
      .withFailedJobsHistoryLimit(from.getFailedJobsHistoryLimit())
      .withJobTemplate(to(from.getJobTemplate()))
      .withSchedule(from.getSchedule())
      .withStartingDeadlineSeconds(from.getStartingDeadlineSeconds())
      .withSuccessfulJobsHistoryLimit(from.getSuccessfulJobsHistoryLimit())
      .withSuspend(from.getSuspend())
      .withTimeZone(from.getTimeZone())
      .withAdditionalProperties(from.getAdditionalProperties())
      .build();
  }

  static JobTemplateSpec to(io.fabric8.kubernetes.api.model.batch.v1beta1.JobTemplateSpec from) {
    return new JobTemplateSpecBuilder()
      .withMetadata(from.getMetadata())
      .withSpec(from.getSpec())
      .build();
  }

  @Override
  public Subscriber<CronJob> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.batch().v1().cronjobs().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return subscriber(kubernetesClient.batch().v1().cronjobs().inAnyNamespace());
      },
      () -> subscriber(kubernetesClient.batch().v1().cronjobs()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace())),
      () -> {
        kubernetesClient.batch().v1beta1().cronjobs().inAnyNamespace().list(ClientUtil.LIMIT_1);
        return subscriber(kubernetesClient.batch().v1beta1().cronjobs().inAnyNamespace(), CronJobService::to);
      },
      () -> subscriber(kubernetesClient.batch().v1beta1().cronjobs()
        .inNamespace(kubernetesClient.getConfiguration().getNamespace()), CronJobService::to)
    );
  }

  public void delete(String name, String namespace) {
    kubernetesClient.batch().v1().cronjobs().inNamespace(namespace).withName(name).delete();
  }

  public CronJob update(String name, String namespace, CronJob cronJob) {
    return kubernetesClient.batch().v1().cronjobs().inNamespace(namespace)
      .resource(new CronJobBuilder(cronJob).editMetadata().withName(name).endMetadata().build()).update();
  }

  public CronJob updateSuspend(String name, String namespace, boolean suspend) {
    final CronJob toPatch = new CronJobBuilder().editOrNewSpec().withSuspend(suspend).endSpec().build();
    return kubernetesClient.batch().v1().cronjobs().inNamespace(namespace).withName(name)
      .patch(PatchContext.of(PatchType.JSON_MERGE), toPatch);
  }

  public Job trigger(String name, String namespace) {
    final var cronJob = kubernetesClient.batch().v1().cronjobs().inNamespace(namespace).withName(name).get();
    final String jobName = String.format("%s-manual-%s",
      name.length() > 38 ? name.substring(0, 38) : name,
      new Random().nextInt(999999)
    );
    final Job job = new JobBuilder()
      .withMetadata(new ObjectMetaBuilder()
        .withName(jobName)
        .withLabels(new HashMap<>(Optional.ofNullable(cronJob.getMetadata().getLabels()).orElse(Collections.emptyMap())))
        .addToAnnotations("cronjob.kubernetes.io/instantiate", "manual")
        .addToOwnerReferences(new OwnerReferenceBuilder()
          .withKind(cronJob.getKind())
          .withApiVersion(cronJob.getApiVersion())
          .withController(false)
          .withName(cronJob.getMetadata().getName())
          .withUid(cronJob.getMetadata().getUid())
          .build())
        .build())
      .withSpec(cronJob.getSpec().getJobTemplate().getSpec())
      .build();
    return kubernetesClient.batch().v1().jobs().inNamespace(namespace).resource(job).create();
  }
}
