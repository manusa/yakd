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
 * Created on 2020-09-04, 7:19
 */
package com.marcnuri.yakc.quickstarts.dashboard;

import com.marcnuri.yakc.quickstarts.dashboard.apis.ApisResource;
import com.marcnuri.yakc.quickstarts.dashboard.clusterrolebindings.ClusterRoleBindingResource;
import com.marcnuri.yakc.quickstarts.dashboard.clusterroles.ClusterRoleResource;
import com.marcnuri.yakc.quickstarts.dashboard.configmaps.ConfigMapResource;
import com.marcnuri.yakc.quickstarts.dashboard.cronjobs.CronJobResource;
import com.marcnuri.yakc.quickstarts.dashboard.customresourcedefinitions.CustomResourceDefinitionResource;
import com.marcnuri.yakc.quickstarts.dashboard.customresources.CustomResourceResource;
import com.marcnuri.yakc.quickstarts.dashboard.daemonsets.DaemonSetResource;
import com.marcnuri.yakc.quickstarts.dashboard.deployment.DeploymentResource;
import com.marcnuri.yakc.quickstarts.dashboard.deploymentconfigs.DeploymentConfigResource;
import com.marcnuri.yakc.quickstarts.dashboard.events.EventResource;
import com.marcnuri.yakc.quickstarts.dashboard.horizontalpodautoscalers.HorizontalPodAutoscalerResource;
import com.marcnuri.yakc.quickstarts.dashboard.ingresses.IngressResource;
import com.marcnuri.yakc.quickstarts.dashboard.jobs.JobResource;
import com.marcnuri.yakc.quickstarts.dashboard.namespaces.NamespaceResource;
import com.marcnuri.yakc.quickstarts.dashboard.node.NodeResource;
import com.marcnuri.yakc.quickstarts.dashboard.persistentvolumeclaims.PersistentVolumeClaimResource;
import com.marcnuri.yakc.quickstarts.dashboard.persistentvolumes.PersistentVolumeResource;
import com.marcnuri.yakc.quickstarts.dashboard.pod.PodResource;
import com.marcnuri.yakc.quickstarts.dashboard.replicaset.ReplicaSetResource;
import com.marcnuri.yakc.quickstarts.dashboard.replicationcontrollers.ReplicationControllerResource;
import com.marcnuri.yakc.quickstarts.dashboard.roles.RoleResource;
import com.marcnuri.yakc.quickstarts.dashboard.routes.RouteResource;
import com.marcnuri.yakc.quickstarts.dashboard.secrets.SecretResource;
import com.marcnuri.yakc.quickstarts.dashboard.service.ServiceResource;
import com.marcnuri.yakc.quickstarts.dashboard.statefulsets.StatefulSetResource;
import com.marcnuri.yakc.quickstarts.dashboard.watch.WatchResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Path;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class ApiResource {

  @Inject ApisResource apisResource;
  @Inject ClusterRoleBindingResource clusterRoleBindingResource;
  @Inject ClusterRoleResource clusterRoleResource;
  @Inject ConfigMapResource configMapResource;
  @Inject CronJobResource cronJobResource;
  @Inject CustomResourceDefinitionResource customResourceDefinitionResource;
  @Inject CustomResourceResource customResourceResource;
  @Inject DaemonSetResource daemonSetResource;
  @Inject DeploymentConfigResource deploymentConfigResource;
  @Inject DeploymentResource deploymentResource;
  @Inject EventResource eventResource;
  @Inject HorizontalPodAutoscalerResource horizontalPodAutoscalerResource;
  @Inject IngressResource ingressResource;
  @Inject JobResource jobResource;
  @Inject NamespaceResource namespaceResource;
  @Inject NodeResource nodeResource;
  @Inject PersistentVolumeClaimResource persistentVolumeClaimResource;
  @Inject PersistentVolumeResource persistentVolumeResource;
  @Inject PodResource podResource;
  @Inject ReplicaSetResource replicaSetResource;
  @Inject ReplicationControllerResource replicationControllerResource;
  @Inject RoleResource roleResource;
  @Inject RouteResource routeResource;
  @Inject SecretResource secretResource;
  @Inject ServiceResource serviceResource;
  @Inject StatefulSetResource statefulSetResource;
  @Inject WatchResource watchResource;

  @Path("/apis")
  public ApisResource getApisResource() {
    return apisResource;
  }

  @Path("/clusterrolebindings")
  public ClusterRoleBindingResource getClusterRoleBindingResource() {
    return clusterRoleBindingResource;
  }

  @Path("/clusterroles")
  public ClusterRoleResource getClusterRoleResource() {
    return clusterRoleResource;
  }

  @Path("/configmaps")
  public ConfigMapResource getConfigMapResource() {
    return configMapResource;
  }

  @Path("/cronjobs")
  public CronJobResource getCronJobResource() {
    return cronJobResource;
  }

  @Path("/customresourcedefinitions")
  public CustomResourceDefinitionResource getCustomResourceDefinitionResource() {
    return customResourceDefinitionResource;
  }

  @Path("/customresources")
  public CustomResourceResource getCustomResourceResource() {
    return customResourceResource;
  }

  @Path("/daemonsets")
  public DaemonSetResource getDaemonSetResource() {
    return daemonSetResource;
  }

  @Path("/deploymentconfigs")
  public DeploymentConfigResource getDeploymentConfigResource() {
    return deploymentConfigResource;
  }

  @Path("/deployments")
  public DeploymentResource getDeploymentResource() {
    return deploymentResource;
  }

  @Path("/events")
  public EventResource getEventResource() {
    return eventResource;
  }

  @Path("/horizontalpodautoscalers")
  public HorizontalPodAutoscalerResource getHorizontalPodAutoscalerResource() {
    return horizontalPodAutoscalerResource;
  }

  @Path("/ingresses")
  public IngressResource getIngressResource() {
    return ingressResource;
  }

  @Path("/jobs")
  public JobResource getJobResource() {
    return jobResource;
  }

  @Path("/namespaces")
  public NamespaceResource getNamespaceResource() {
    return namespaceResource;
  }

  @Path("/nodes")
  public NodeResource getNodeResource() {
    return nodeResource;
  }

  @Path("/persistentvolumeclaims")
  public PersistentVolumeClaimResource getPersistentVolumeClaimResource() {
    return persistentVolumeClaimResource;
  }

  @Path("/persistentvolumes")
  public PersistentVolumeResource getPersistentVolumeResource() {
    return persistentVolumeResource;
  }

  @Path("/pods")
  public PodResource getPodResource() {
    return podResource;
  }

  @Path("/replicasets")
  public ReplicaSetResource getReplicaSetResource() {
    return replicaSetResource;
  }

  @Path("/replicationcontrollers")
  public ReplicationControllerResource getReplicationControllerResource() {
    return replicationControllerResource;
  }

  @Path("/roles")
  public RoleResource getRoleResource() {
    return roleResource;
  }

  @Path("/routes")
  public RouteResource getRouteResource() {
    return routeResource;
  }

  @Path("/secrets")
  public SecretResource getSecretResource() {
    return secretResource;
  }

  @Path("/services")
  public ServiceResource getServiceResource() {
    return serviceResource;
  }

  @Path("/statefulsets")
  public StatefulSetResource getStatefulSetResource() {
    return statefulSetResource;
  }

  @Path("/watch")
  public WatchResource getWatchResource() {
    return watchResource;
  }
}
