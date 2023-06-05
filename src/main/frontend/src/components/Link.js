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
import metadata from '../metadata';
import Icon from './Icon';
import i from './icons';

const variants = ({
  none: '',
  default: 'text-blue-600 hover:text-blue-800 hover:underline',
  danger: 'text-red-600 hover:text-red-800 hover:underline',
  outline: 'text-blue-600 border-blue-600 border rounded py-1 px-3 hover:bg-blue-600 hover:text-white',
  outlineDanger: 'text-red-600 border-red-600 border rounded py-1 px-3 hover:bg-red-600 hover:text-white'
});

const sizes = ({
  normal: '',
  small: 'text-sm font-normal'
});

const Link = ({
  className, children, href = '#',
  variant = variants.default, size = sizes.normal,
  ...props
}) => (
  <a
    className={`${variant} ${size} ${className ?? ''}`}
    href={href}
    {...props}
  >{children}</a>
);

Link.variants = variants;
Link.sizes = sizes;

Link.RouterLink = ({
  className, children, href = '#',
  variant = variants.default, size = sizes.normal,
  ...props
}) => (
  <OriginalRouterLink
    className={`${variant} ${size} ${className ?? ''}`}
    {...props}
  >{children}</OriginalRouterLink>
);

Link.ResourceLink = ({className, Icon: IconComponent, iconClassName, children, ...props}) => (
  <Link.RouterLink
    className={`flex ${className ?? ''}`}
    {...props}
  >
    {IconComponent && <IconComponent className={`w-5 mr-1 ${iconClassName ?? ''}`} />}
    <div className='flex-1'>{children}</div>
  </Link.RouterLink>
);

Link.ClusterRole = ({...props}) => <Link.ResourceLink Icon={i.ClusterRole} {...props} />;
Link.ClusterRoleBinding = ({...props}) => <Link.ResourceLink Icon={i.ClusterRoleBinding} {...props} />;
Link.ConfigMap = ({...props}) => <Link.ResourceLink Icon={i.ConfigMap} {...props} />;
Link.CronJob = ({...props}) => <Link.ResourceLink Icon={i.CronJob} {...props} />;
Link.CustomResourceDefinition = ({...props}) => <Link.ResourceLink Icon={i.CustomResourceDefinition} {...props} />;
Link.DaemonSet = ({...props}) => <Link.ResourceLink Icon={i.DaemonSet} {...props} />;
Link.DeploymentConfig = ({...props}) => <Link.ResourceLink Icon={i.DeploymentConfig} {...props} />;
Link.Deployment = ({...props}) => <Link.ResourceLink Icon={i.Deployment} {...props} />;
Link.HorizontalPodAutoscaler = ({...props}) => <Link.ResourceLink Icon={i.HorizontalPodAutoscaler} {...props} />;
Link.Ingress = ({...props}) => <Link.ResourceLink Icon={i.Ingress} {...props} />;
Link.Job = ({...props}) => <Link.ResourceLink Icon={i.Job} {...props} />;
Link.Namespace = ({...props}) => <Link.ResourceLink Icon={i.Namespace} {...props} />;
Link.Node = ({...props}) => <Link.ResourceLink Icon={i.Node} {...props} />;
Link.PersistentVolume = ({...props}) => <Link.ResourceLink Icon={i.PersistentVolume} {...props} />;
Link.PersistentVolumeClaim = ({...props}) => <Link.ResourceLink Icon={i.PersistentVolumeClaim} {...props} />;
Link.Pod = ({...props}) => <Link.ResourceLink Icon={i.Pod} {...props} />;
Link.ReplicationController = ({...props}) => <Link.ResourceLink Icon={i.ReplicaSet} {...props} />;
Link.Role = ({...props}) => <Link.ResourceLink Icon={i.Role} {...props} />;
Link.Route = ({...props}) => <Link.ResourceLink Icon={i.Route} {...props} />;
Link.Secret = ({...props}) => <Link.ResourceLink Icon={i.Secret} {...props} />;
Link.Service = ({...props}) => <Link.ResourceLink Icon={i.Service} {...props} />;
Link.StatefulSet = ({...props}) => <Link.ResourceLink Icon={i.StatefulSet} {...props} />;

Link.EditLink = ({path, resource, ...props}) => (
  <Link.RouterLink
    size={Link.sizes.small}
    variant={Link.variants.outline}
    to={`/${path}/${metadata.selectors.uid(resource)}/edit`}
    title='Edit'
    {...props}
  >
    <Icon icon='fa-pen' className='mr-2'/>Edit
  </Link.RouterLink>
);

export default Link;
