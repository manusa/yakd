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
package com.marcnuri.yakc.quickstarts.dashboard.cronjobs;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.ClientErrorException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.batch.v1.BatchV1Api;
import com.marcnuri.yakc.api.batch.v1beta1.BatchV1beta1Api;
import com.marcnuri.yakc.model.io.k8s.api.batch.v1.Job;
import com.marcnuri.yakc.model.io.k8s.api.batch.v1beta1.CronJob;
import com.marcnuri.yakc.model.io.k8s.api.batch.v1beta1.CronJobSpec;
import com.marcnuri.yakc.model.io.k8s.api.batch.v1beta1.JobTemplateSpec;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.OwnerReference;
import com.marcnuri.yakc.quickstarts.dashboard.watch.Watchable;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

@Singleton
public class CronJobService implements Watchable<CronJob> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public CronJobService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<CronJob>> watch() throws IOException {
    final BatchV1beta1Api batch = kubernetesClient.create(BatchV1beta1Api.class);
    try {
      batch.listCronJobForAllNamespaces(new BatchV1beta1Api.ListCronJobForAllNamespaces().limit(1)).get();
      return batch.listCronJobForAllNamespaces().watch();
    } catch (ClientErrorException ex) {
      return batch.listNamespacedCronJob(kubernetesClient.getConfiguration().getNamespace()).watch();
    }
  }

  public CronJob delete(String name, String namespace) throws IOException {
    return kubernetesClient.create(BatchV1beta1Api.class).deleteNamespacedCronJob(name, namespace).get(CronJob.class);
  }

  public CronJob update(String name, String namespace, CronJob cronJob) throws IOException {
    return kubernetesClient.create(BatchV1beta1Api.class).replaceNamespacedCronJob(name, namespace, cronJob).get();
  }

  public CronJob updateSuspend(String name, String namespace, boolean suspend) throws IOException {
    final CronJob toPatch = emptyCronJob();
    toPatch.getSpec().setSuspend(suspend);
    return kubernetesClient.create(BatchV1beta1Api.class).patchNamespacedCronJob(name, namespace, toPatch).get();
  }

  public Job trigger(String name, String namespace) throws IOException {
    final CronJob cronJob = kubernetesClient.create(BatchV1beta1Api.class).readNamespacedCronJob(name, namespace).get();
    final JobTemplateSpec jts = cronJob.getSpec().getJobTemplate();
    final String jobName = String.format("%s-manual-%s",
      name.length() > 38 ? name.substring(0, 38) : name,
      new Random().nextInt(999999)
    );
    return kubernetesClient.create(BatchV1Api.class).createNamespacedJob(namespace, Job.builder()
      .metadata(ObjectMeta.builder()
        .name(jobName).namespace(namespace)
        .labels(new HashMap<>(Optional.ofNullable(cronJob.getMetadata().getLabels()).orElse(Collections.emptyMap())))
        .putInAnnotations("cronjob.kubernetes.io/instantiate", "manual")
        .addToOwnerReferences(OwnerReference.builder()
          .kind(cronJob.getKind())
          .apiVersion(cronJob.getApiVersion())
          .controller(false)
          .name(cronJob.getMetadata().getName())
          .uid(cronJob.getMetadata().getUid())
          .build())
        .build())
      .spec(jts.getSpec())
      .build()).get();
  }

  private static CronJob emptyCronJob() {
    final CronJob ret = new CronJob();
    ret.setSpec(new CronJobSpec());
    return ret;
  }
}
