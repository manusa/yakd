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
package com.marcnuri.yakd.quickstarts.dashboard.watch;

import com.marcnuri.yakd.quickstarts.dashboard.clusterrolebindings.ClusterRoleBindingService;
import com.marcnuri.yakd.quickstarts.dashboard.clusterroles.ClusterRoleService;
import com.marcnuri.yakd.quickstarts.dashboard.configmaps.ConfigMapService;
import com.marcnuri.yakd.quickstarts.dashboard.cronjobs.CronJobService;
import com.marcnuri.yakd.quickstarts.dashboard.customresourcedefinitions.CustomResourceDefinitionService;
import com.marcnuri.yakd.quickstarts.dashboard.daemonsets.DaemonSetService;
import com.marcnuri.yakd.quickstarts.dashboard.deployment.DeploymentService;
import com.marcnuri.yakd.quickstarts.dashboard.deploymentconfigs.DeploymentConfigService;
import com.marcnuri.yakd.quickstarts.dashboard.events.EventService;
import com.marcnuri.yakd.quickstarts.dashboard.horizontalpodautoscalers.HorizontalPodAutoscalerService;
import com.marcnuri.yakd.quickstarts.dashboard.jobs.JobService;
import com.marcnuri.yakd.quickstarts.dashboard.namespaces.NamespaceService;
import com.marcnuri.yakd.quickstarts.dashboard.node.NodeService;
import com.marcnuri.yakd.quickstarts.dashboard.openshiftconfig.ClusterVersionService;
import com.marcnuri.yakd.quickstarts.dashboard.pod.PodService;
import com.marcnuri.yakd.quickstarts.dashboard.replicaset.ReplicaSetService;
import com.marcnuri.yakd.quickstarts.dashboard.replicationcontrollers.ReplicationControllerService;
import com.marcnuri.yakd.quickstarts.dashboard.roles.RoleService;
import com.marcnuri.yakd.quickstarts.dashboard.routes.RouteService;
import com.marcnuri.yakd.quickstarts.dashboard.secrets.SecretService;
import com.marcnuri.yakd.quickstarts.dashboard.service.ServiceService;
import com.marcnuri.yakd.quickstarts.dashboard.statefulsets.StatefulSetService;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.marcnuri.yakd.quickstarts.dashboard.KubernetesDashboardConfiguration.WATCH_EXECUTOR_SERVICE;

@Singleton
public class WatchService {

  private final List<Watchable<?>> watchables;
  private ScheduledExecutorService executorService;

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
    return Multi.createFrom().emitter(new SelfHealingEmitter(executorService, watchables));
  }
}
