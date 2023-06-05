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
package com.marcnuri.yakc.quickstarts.dashboard.watch;

import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.model.Model;
import com.marcnuri.yakc.quickstarts.dashboard.clusterrolebindings.ClusterRoleBindingService;
import com.marcnuri.yakc.quickstarts.dashboard.clusterroles.ClusterRoleService;
import com.marcnuri.yakc.quickstarts.dashboard.configmaps.ConfigMapService;
import com.marcnuri.yakc.quickstarts.dashboard.cronjobs.CronJobService;
import com.marcnuri.yakc.quickstarts.dashboard.customresourcedefinitions.CustomResourceDefinitionService;
import com.marcnuri.yakc.quickstarts.dashboard.daemonsets.DaemonSetService;
import com.marcnuri.yakc.quickstarts.dashboard.deployment.DeploymentService;
import com.marcnuri.yakc.quickstarts.dashboard.deploymentconfigs.DeploymentConfigService;
import com.marcnuri.yakc.quickstarts.dashboard.events.EventService;
import com.marcnuri.yakc.quickstarts.dashboard.horizontalpodautoscalers.HorizontalPodAutoscalerService;
import com.marcnuri.yakc.quickstarts.dashboard.jobs.JobService;
import com.marcnuri.yakc.quickstarts.dashboard.namespaces.NamespaceService;
import com.marcnuri.yakc.quickstarts.dashboard.node.NodeService;
import com.marcnuri.yakc.quickstarts.dashboard.openshiftconfig.ClusterVersionService;
import com.marcnuri.yakc.quickstarts.dashboard.pod.PodService;
import com.marcnuri.yakc.quickstarts.dashboard.replicaset.ReplicaSetService;
import com.marcnuri.yakc.quickstarts.dashboard.replicationcontrollers.ReplicationControllerService;
import com.marcnuri.yakc.quickstarts.dashboard.roles.RoleService;
import com.marcnuri.yakc.quickstarts.dashboard.routes.RouteService;
import com.marcnuri.yakc.quickstarts.dashboard.secrets.SecretService;
import com.marcnuri.yakc.quickstarts.dashboard.service.ServiceService;
import com.marcnuri.yakc.quickstarts.dashboard.statefulsets.StatefulSetService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.BackPressureStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class WatchService {

  private final List<Watchable<? extends Model>> watchables;

  @SuppressWarnings("java:S107")
  @Inject
  public WatchService(
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

  @SuppressWarnings({"unchecked", "rawtypes", "java:S1452"})
  public Multi<WatchEvent<? extends Model>> newWatch() {
    final SelfHealingWatchableConsumer consumer = new SelfHealingWatchableConsumer(watchables);
    return Multi.createFrom().emitter((Consumer)consumer, BackPressureStrategy.BUFFER)
      .onCompletion().call(consumer::dispose)
      .onCancellation().call(consumer::dispose)
      .onFailure().call(consumer::dispose);
  }
}
