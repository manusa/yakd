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
 */
package com.marcnuri.yakd.watch;

import com.marcnuri.yakd.clusterrolebindings.ClusterRoleBindingService;
import com.marcnuri.yakd.clusterroles.ClusterRoleService;
import com.marcnuri.yakd.configmaps.ConfigMapService;
import com.marcnuri.yakd.cronjobs.CronJobService;
import com.marcnuri.yakd.customresourcedefinitions.CustomResourceDefinitionService;
import com.marcnuri.yakd.events.EventService;
import com.marcnuri.yakd.daemonsets.DaemonSetService;
import com.marcnuri.yakd.deployment.DeploymentService;
import com.marcnuri.yakd.deploymentconfigs.DeploymentConfigService;
import com.marcnuri.yakd.horizontalpodautoscalers.HorizontalPodAutoscalerService;
import com.marcnuri.yakd.ingresses.IngressService;
import com.marcnuri.yakd.jobs.JobService;
import com.marcnuri.yakd.namespaces.NamespaceService;
import com.marcnuri.yakd.node.NodeService;
import com.marcnuri.yakd.openshiftconfig.ClusterVersionService;
import com.marcnuri.yakd.pod.PodService;
import com.marcnuri.yakd.replicaset.ReplicaSetService;
import com.marcnuri.yakd.replicationcontrollers.ReplicationControllerService;
import com.marcnuri.yakd.roles.RoleService;
import com.marcnuri.yakd.routes.RouteService;
import com.marcnuri.yakd.secrets.SecretService;
import com.marcnuri.yakd.service.ServiceService;
import com.marcnuri.yakd.statefulsets.StatefulSetService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.BackPressureStrategy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.marcnuri.yakd.KubernetesDashboardConfiguration.WATCH_EXECUTOR_SERVICE;

@Singleton
public class WatchService {

  private final List<Watchable<?>> watchables;
  private final ScheduledExecutorService executorService;

  @SuppressWarnings("java:S107")
  @Inject
  public WatchService(
    @Named(WATCH_EXECUTOR_SERVICE) ScheduledExecutorService executorService,
    ClusterRoleBindingService clusterRoleBindingService,
    ClusterRoleService clusterRoleService,
    ClusterVersionService clusterVersionService,
    ConfigMapService configMapService,
    CronJobService cronJobService,
    CustomResourceDefinitionService customResourceDefinitionService,
    DaemonSetService daemonSetService,
    DeploymentConfigService deploymentConfigService,
    DeploymentService deploymentService,
    EventService eventService,
    HorizontalPodAutoscalerService horizontalPodAutoscalerService,
    IngressService ingressService,
    JobService jobService,
    NamespaceService namespaceService,
    NodeService nodeService,
    PodService podService,
    ReplicaSetService replicaSetService,
    ReplicationControllerService replicationControllerService,
    RoleService roleService,
    RouteService routeService,
    SecretService secretService,
    ServiceService serviceService,
    StatefulSetService statefulSetService
  ) {
    this.executorService = executorService;
    this.watchables = Arrays.asList(
      clusterRoleBindingService,
      clusterRoleService,
      clusterVersionService,
      configMapService,
      cronJobService,
      customResourceDefinitionService,
      daemonSetService,
      deploymentConfigService,
      deploymentService,
      eventService,
      horizontalPodAutoscalerService,
      ingressService,
      jobService,
      namespaceService,
      nodeService,
      podService,
      replicaSetService,
      replicationControllerService,
      roleService,
      routeService,
      secretService,
      serviceService,
      statefulSetService
    );
  }

  public Multi<WatchEvent<?>> newWatch() {
    // No backpressure to reduce memory footprint.
    // Downstream client should handle every event or provide its own buffering
    return Multi.createFrom().emitter(new SelfHealingWatchableEmitter(executorService, watchables), BackPressureStrategy.ERROR);
  }
}
