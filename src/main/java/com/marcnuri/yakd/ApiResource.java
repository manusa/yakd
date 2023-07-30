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
package com.marcnuri.yakd;

import com.marcnuri.yakd.apis.ApisResource;
import com.marcnuri.yakd.clusterrolebindings.ClusterRoleBindingResource;
import com.marcnuri.yakd.clusterroles.ClusterRoleResource;
import com.marcnuri.yakd.cronjobs.CronJobResource;
import com.marcnuri.yakd.customresourcedefinitions.CustomResourceDefinitionResource;
import com.marcnuri.yakd.customresources.CustomResourceResource;
import com.marcnuri.yakd.daemonsets.DaemonSetResource;
import com.marcnuri.yakd.deployment.DeploymentResource;
import com.marcnuri.yakd.endpoints.EndpointResource;
import com.marcnuri.yakd.fabric8.GenericResourceService;
import com.marcnuri.yakd.horizontalpodautoscalers.HorizontalPodAutoscalerResource;
import com.marcnuri.yakd.jobs.JobResource;
import com.marcnuri.yakd.node.NodeResource;
import com.marcnuri.yakd.persistentvolumeclaims.PersistentVolumeClaimResource;
import com.marcnuri.yakd.persistentvolumes.PersistentVolumeResource;
import com.marcnuri.yakd.pod.PodResource;
import com.marcnuri.yakd.configmaps.ConfigMapResource;
import com.marcnuri.yakd.deploymentconfigs.DeploymentConfigResource;
import com.marcnuri.yakd.ingresses.IngressResource;
import com.marcnuri.yakd.namespaces.NamespaceResource;
import com.marcnuri.yakd.replicaset.ReplicaSetResource;
import com.marcnuri.yakd.replicationcontrollers.ReplicationControllerResource;
import com.marcnuri.yakd.roles.RoleResource;
import com.marcnuri.yakd.routes.RouteResource;
import com.marcnuri.yakd.secrets.SecretResource;
import com.marcnuri.yakd.service.ServiceResource;
import com.marcnuri.yakd.statefulsets.StatefulSetResource;
import com.marcnuri.yakd.watch.WatchResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.InputStream;

@Singleton
@RegisterForReflection // Quarkus doesn't generate constructors for JAX-RS Subresources
public class ApiResource {

  @Inject
  ApisResource apisResource;
  @Inject
  ClusterRoleBindingResource clusterRoleBindingResource;
  @Inject
  ClusterRoleResource clusterRoleResource;
  @Inject
  ConfigMapResource configMapResource;
  @Inject
  CronJobResource cronJobResource;
  @Inject
  CustomResourceDefinitionResource customResourceDefinitionResource;
  @Inject
  CustomResourceResource customResourceResource;
  @Inject
  DaemonSetResource daemonSetResource;
  @Inject
  DeploymentConfigResource deploymentConfigResource;
  @Inject
  DeploymentResource deploymentResource;
  @Inject
  EndpointResource endpointResource;
  @Inject
  HorizontalPodAutoscalerResource horizontalPodAutoscalerResource;
  @Inject
  GenericResourceService genericResourceService;
  @Inject
  IngressResource ingressResource;
  @Inject
  JobResource jobResource;
  @Inject NamespaceResource namespaceResource;
  @Inject
  NodeResource nodeResource;
  @Inject
  PersistentVolumeClaimResource persistentVolumeClaimResource;
  @Inject
  PersistentVolumeResource persistentVolumeResource;
  @Inject
  PodResource podResource;
  @Inject ReplicaSetResource replicaSetResource;
  @Inject
  ReplicationControllerResource replicationControllerResource;
  @Inject
  RoleResource roleResource;
  @Inject
  RouteResource routeResource;
  @Inject
  SecretResource secretResource;
  @Inject
  ServiceResource serviceResource;
  @Inject
  StatefulSetResource statefulSetResource;
  @Inject
  WatchResource watchResource;

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

  @Path("/endpoints")
  public EndpointResource getEndpontResource() {
    return endpointResource;
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

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/")
  public HasMetadata create(InputStream resource) {
    return genericResourceService.create(resource);
  }
}
