/*
 * Copyright 2020 Marc Nuri
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import React from 'react';
import {DeploymentIcon} from './DeploymentIcon';
import {IngressIcon} from './IngressIcon';
export {ClusterRoleIcon} from './ClusterRoleIcon';
export {ClusterRoleBindingIcon} from './ClusterRoleBindingIcon';
export {ConfigMapIcon} from './ConfigMapIcon';
export {CronJobIcon} from './CronJobIcon';
export {CustomResourceDefinitionIcon} from './CustomResourceDefinitionIcon';
export {DaemonSetIcon} from './DaemonSetIcon';
export {DeploymentIcon} from './DeploymentIcon';
export {EndpointsIcon} from './EndpointsIcon';
export {HorizontalPodAutoscalerIcon} from './HorizontalPodAutoscalerIcon';
export {IngressIcon} from './IngressIcon';
export {JobIcon} from './JobIcon';
export {KubernetesIcon} from './KubernetesIcon';
export {MinikubeIcon} from './MinikubeIcon';
export {NamespaceIcon} from './NamespaceIcon';
export {NodeIcon} from './NodeIcon';
export {OpenShiftIcon} from './OpenShiftIcon';
export {PersistentVolumeIcon} from './PersistentVolumeIcon';
export {PersistentVolumeClaimIcon} from './PersistentVolumeClaimIcon';
export {PodIcon} from './PodIcon';
export {ReplicaSetIcon} from './ReplicaSetIcon';
export {RoleIcon} from './RoleIcon';
export {SecretIcon} from './SecretIcon';
export {ServiceIcon} from './ServiceIcon';
export {ServiceAccountIcon} from './ServiceAccountIcon';
export {StatefulSetIcon} from './StatefulSetIcon';
export {YAKDLogo} from './YAKDLogo';

export const DeploymentConfigIcon = ({...props}) => (
  <DeploymentIcon kubernetesColor='#db212e' {...props} />
);
export const RouteIcon = ({...props}) => (
  <IngressIcon kubernetesColor='#db212e' {...props} />
);
