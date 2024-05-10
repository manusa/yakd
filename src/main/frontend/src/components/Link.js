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
import React from 'react';
import {Link as OriginalRouterLink} from 'react-router-dom';
import {uid} from '../metadata';
import {
  Icon,
  EndpointsIcon,
  ClusterRoleIcon,
  ClusterRoleBindingIcon,
  ConfigMapIcon,
  CronJobIcon,
  CustomResourceDefinitionIcon,
  DaemonSetIcon,
  DeploymentIcon,
  DeploymentConfigIcon,
  HorizontalPodAutoscalerIcon,
  IngressIcon,
  JobIcon,
  NamespaceIcon,
  NodeIcon,
  PersistentVolumeIcon,
  PersistentVolumeClaimIcon,
  PodIcon,
  ReplicaSetIcon,
  RoleIcon,
  RouteIcon,
  SecretIcon,
  ServiceIcon,
  ServiceAccountIcon,
  StatefulSetIcon,
  RoleBindingIcon
} from './';

const variants = {
  none: '',
  default: 'text-blue-600 hover:text-blue-800 hover:underline',
  danger: 'text-red-600 hover:text-red-800 hover:underline',
  outline:
    'text-blue-600 border-blue-600 border rounded py-1 px-3 hover:bg-blue-600 hover:text-white',
  outlineDanger:
    'text-red-600 border-red-600 border rounded py-1 px-3 hover:bg-red-600 hover:text-white'
};

const sizes = {
  normal: '',
  small: 'text-sm font-normal'
};

export const Link = ({
  className,
  children,
  href = '#',
  variant = variants.default,
  size = sizes.normal,
  disabled = false,
  ...props
}) => {
  let disabledClasses = '';
  if (disabled) {
    props['aria-disabled'] = true;
    disabledClasses = 'pointer-events-none opacity-50';
  }
  return (
    <a
      className={`${variant} ${size} ${disabledClasses} ${className ?? ''}`}
      href={href}
      {...props}
    >
      {children}
    </a>
  );
};

Link.variants = variants;
Link.sizes = sizes;

Link.RouterLink = ({
  className,
  children,
  href = '#',
  variant = variants.default,
  size = sizes.normal,
  ...props
}) => (
  <OriginalRouterLink
    className={`${variant} ${size} ${className ?? ''}`}
    {...props}
  >
    {children}
  </OriginalRouterLink>
);

Link.ResourceLink = ({
  className,
  Icon: IconComponent,
  iconClassName,
  children,
  ...props
}) => (
  <Link.RouterLink className={`flex ${className ?? ''}`} {...props}>
    {IconComponent && (
      <IconComponent className={`w-5 mr-1 ${iconClassName ?? ''}`} />
    )}
    <div className='flex-1'>{children}</div>
  </Link.RouterLink>
);

Link.ClusterRole = ({...props}) => (
  <Link.ResourceLink Icon={ClusterRoleIcon} {...props} />
);
Link.ClusterRoleBinding = ({...props}) => (
  <Link.ResourceLink Icon={ClusterRoleBindingIcon} {...props} />
);
Link.ConfigMap = ({...props}) => (
  <Link.ResourceLink Icon={ConfigMapIcon} {...props} />
);
Link.CronJob = ({...props}) => (
  <Link.ResourceLink Icon={CronJobIcon} {...props} />
);
Link.CustomResourceDefinition = ({...props}) => (
  <Link.ResourceLink Icon={CustomResourceDefinitionIcon} {...props} />
);
Link.DaemonSet = ({...props}) => (
  <Link.ResourceLink Icon={DaemonSetIcon} {...props} />
);
Link.DeploymentConfig = ({...props}) => (
  <Link.ResourceLink Icon={DeploymentConfigIcon} {...props} />
);
Link.Deployment = ({...props}) => (
  <Link.ResourceLink Icon={DeploymentIcon} {...props} />
);
Link.Endpoints = ({...props}) => (
  <Link.ResourceLink Icon={EndpointsIcon} {...props} />
);
Link.HorizontalPodAutoscaler = ({...props}) => (
  <Link.ResourceLink Icon={HorizontalPodAutoscalerIcon} {...props} />
);
Link.Ingress = ({...props}) => (
  <Link.ResourceLink Icon={IngressIcon} {...props} />
);
Link.Job = ({...props}) => <Link.ResourceLink Icon={JobIcon} {...props} />;
Link.Namespace = ({...props}) => (
  <Link.ResourceLink Icon={NamespaceIcon} {...props} />
);
Link.Node = ({...props}) => <Link.ResourceLink Icon={NodeIcon} {...props} />;
Link.PersistentVolume = ({...props}) => (
  <Link.ResourceLink Icon={PersistentVolumeIcon} {...props} />
);
Link.PersistentVolumeClaim = ({...props}) => (
  <Link.ResourceLink Icon={PersistentVolumeClaimIcon} {...props} />
);
Link.Pod = ({...props}) => <Link.ResourceLink Icon={PodIcon} {...props} />;
Link.ReplicationController = ({...props}) => (
  <Link.ResourceLink Icon={ReplicaSetIcon} {...props} />
);
Link.Role = ({...props}) => <Link.ResourceLink Icon={RoleIcon} {...props} />;
Link.RoleBinding = ({...props}) => (
  <Link.ResourceLink Icon={RoleBindingIcon} {...props} />
);
Link.Route = ({...props}) => <Link.ResourceLink Icon={RouteIcon} {...props} />;
Link.Secret = ({...props}) => (
  <Link.ResourceLink Icon={SecretIcon} {...props} />
);
Link.Service = ({...props}) => (
  <Link.ResourceLink Icon={ServiceIcon} {...props} />
);
Link.ServiceAccount = ({...props}) => (
  <Link.ResourceLink Icon={ServiceAccountIcon} {...props} />
);
Link.StatefulSet = ({...props}) => (
  <Link.ResourceLink Icon={StatefulSetIcon} {...props} />
);

Link.EditLink = ({path, resource, ...props}) => (
  <Link.RouterLink
    size={Link.sizes.small}
    variant={Link.variants.outline}
    to={`/${path}/${uid(resource)}/edit`}
    title='Edit'
    {...props}
  >
    <Icon icon='fa-pen' className='mr-2' />
    Edit
  </Link.RouterLink>
);
