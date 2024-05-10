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
import React, {useRef, useLayoutEffect} from 'react';
import {connect} from 'react-redux';
import {useMatch} from 'react-router-dom';
import * as apis from '../apis';
import {useUiSidebar} from '../redux';
import * as crd from '../customresourcedefinitions';
import {
  Icon,
  Link,
  EndpointsIcon,
  ClusterRoleBindingIcon,
  ClusterRoleIcon,
  ConfigMapIcon,
  CronJobIcon,
  CustomResourceDefinitionIcon,
  DaemonSetIcon,
  DeploymentConfigIcon,
  DeploymentIcon,
  HorizontalPodAutoscalerIcon,
  IngressIcon,
  JobIcon,
  KubernetesIcon,
  NamespaceIcon,
  NodeIcon,
  PersistentVolumeClaimIcon,
  PersistentVolumeIcon,
  PodIcon,
  ReplicaSetIcon,
  RoleIcon,
  RouteIcon,
  SecretIcon,
  ServiceAccountIcon,
  ServiceIcon,
  StatefulSetIcon,
  YAKDLogo
} from '../components';

import './SideBar.css';

const RoutedLink = ({to, staticContext, className = '', ...props}) => {
  const match = useMatch(`${to}/*`);
  return (
    <Link.RouterLink
      variant={Link.variants.none}
      to={to}
      className={`${className}
      flex items-center border-l-4 px-4 py-2 lg:py-3
      ${
        match
          ? 'bg-gray-600/25 text-gray-100 border-gray-100'
          : 'border-transparent text-gray-500 hover:bg-gray-600/25 hover:text-gray-100'
      }`}
      {...props}
    />
  );
};

const NavGroup = ({expandedItems, toggleItem, label, icon, children}) => {
  const expanded = expandedItems.includes(label);
  const onClick = () => toggleItem(label);
  return (
    <div className={`border-t last:border-b border-black`}>
      <div
        className='flex items-center border-l-4 border-transparent px-4 py-2 lg:py-3 text-gray-300 hover:bg-gray-600/25 cursor-pointer'
        onClick={onClick}
      >
        <Icon className='side-bar__nav-group-icon' icon={icon} />
        <span className='flex-1'>{label}</span>
        <Icon
          className=''
          icon={expanded ? 'fa-chevron-down' : 'fa-chevron-right'}
        />
      </div>
      <div
        className={`
        overflow-hidden transform origin-top transition-transform duration-75
        ${expanded ? 'scale-y-100' : 'h-0 scale-y-0'}
      `}
      >
        {children}
      </div>
    </div>
  );
};

const K8sNavItem = ({to, Icon: ComponentIcon, children}) => (
  <RoutedLink to={to}>
    <ComponentIcon className='side-bar__nav-item-icon' />
    {children}
  </RoutedLink>
);

const IconNavItem = ({to, icon, children}) => (
  <RoutedLink to={to}>
    <Icon className='side-bar__nav-item-icon' icon={icon} />
    {children}
  </RoutedLink>
);

const ExtNavItem = ({href, children}) => (
  <Link
    variant={Link.variants.none}
    href={href}
    className={`flex items-center border-l-4 px-4 py-2 lg:py-3
      border-transparent text-gray-500 hover:bg-gray-600/25 hover:text-gray-100`}
  >
    {children}
  </Link>
);

const NavSection = ({
  currentScrollTop,
  scroll,
  expandedItems,
  toggleItem,
  isOpenShift,
  crdGroups
}) => {
  const ref = useRef(null);
  useLayoutEffect(() => {
    ref.current.scroll(0, currentScrollTop);
  }, [ref, currentScrollTop]);
  return (
    <nav
      ref={ref}
      onScroll={({target: {scrollTop}}) => scroll({scrollTop})}
      className='mt-4 lg:mt-6 flex-1 h-1 overflow-x-hidden overflow-y-auto custom-scroll-dark'
    >
      <IconNavItem to='/search' icon='fa-search'>
        Search
      </IconNavItem>
      <K8sNavItem to='/' Icon={KubernetesIcon}>
        Home
      </K8sNavItem>
      <K8sNavItem to='/nodes' Icon={NodeIcon}>
        Nodes
      </K8sNavItem>
      <K8sNavItem to='/namespaces' Icon={NamespaceIcon}>
        Namespaces
      </K8sNavItem>
      <div>
        <NavGroup
          expandedItems={expandedItems}
          toggleItem={toggleItem}
          label='Workloads'
          icon='fa-cubes'
        >
          <K8sNavItem to='/pods' Icon={PodIcon}>
            Pods
          </K8sNavItem>
          <K8sNavItem to='/deployments' Icon={DeploymentIcon}>
            Deployments
          </K8sNavItem>
          {isOpenShift && (
            <K8sNavItem to='/deploymentconfigs' Icon={DeploymentConfigIcon}>
              Deployment Configs
            </K8sNavItem>
          )}
          <K8sNavItem to='/statefulsets' Icon={StatefulSetIcon}>
            StatefulSets
          </K8sNavItem>
          <K8sNavItem to='/cronjobs' Icon={CronJobIcon}>
            CronJobs
          </K8sNavItem>
          <K8sNavItem to='/jobs' Icon={JobIcon}>
            Jobs
          </K8sNavItem>
          <K8sNavItem to='/daemonsets' Icon={DaemonSetIcon}>
            DaemonSets
          </K8sNavItem>
          <K8sNavItem to='/replicationcontrollers' Icon={ReplicaSetIcon}>
            Replication Controllers
          </K8sNavItem>
          <K8sNavItem
            to='/horizontalpodautoscalers'
            Icon={HorizontalPodAutoscalerIcon}
          >
            Horizontal Pod Autoscalers
          </K8sNavItem>
        </NavGroup>
        <NavGroup
          expandedItems={expandedItems}
          toggleItem={toggleItem}
          label='Network'
          icon='fa-network-wired'
        >
          <K8sNavItem to='/services' Icon={ServiceIcon}>
            Services
          </K8sNavItem>
          <K8sNavItem to='/endpoints' Icon={EndpointsIcon}>
            Endpoints
          </K8sNavItem>
          <K8sNavItem to='/ingresses' Icon={IngressIcon}>
            Ingresses
          </K8sNavItem>
          {isOpenShift && (
            <K8sNavItem to='/routes' Icon={RouteIcon}>
              Routes
            </K8sNavItem>
          )}
        </NavGroup>
        <NavGroup
          expandedItems={expandedItems}
          toggleItem={toggleItem}
          label='Configuration'
          icon='fa-list'
        >
          <K8sNavItem to='/configmaps' Icon={ConfigMapIcon}>
            ConfigMaps
          </K8sNavItem>
          <K8sNavItem to='/secrets' Icon={SecretIcon}>
            Secrets
          </K8sNavItem>
        </NavGroup>
        <NavGroup
          expandedItems={expandedItems}
          toggleItem={toggleItem}
          label='Storage'
          icon='fa-database'
        >
          <K8sNavItem to='/persistentvolumes' Icon={PersistentVolumeIcon}>
            PersistentVolumes
          </K8sNavItem>
          <K8sNavItem
            to='/persistentvolumeclaims'
            Icon={PersistentVolumeClaimIcon}
          >
            PersistentVolume Claims
          </K8sNavItem>
        </NavGroup>
        <NavGroup
          expandedItems={expandedItems}
          toggleItem={toggleItem}
          label='Access Control'
          icon='fa-shield-alt'
        >
          <K8sNavItem to='/serviceaccounts' Icon={ServiceAccountIcon}>
            ServiceAccounts
          </K8sNavItem>
          <K8sNavItem to='/clusterroles' Icon={ClusterRoleIcon}>
            ClusterRoles
          </K8sNavItem>
          <K8sNavItem to='/clusterrolebindings' Icon={ClusterRoleBindingIcon}>
            ClusterRoleBindings
          </K8sNavItem>
          <K8sNavItem to='/roles' Icon={RoleIcon}>
            Roles
          </K8sNavItem>
          <K8sNavItem to='/secrets' Icon={SecretIcon}>
            Secrets
          </K8sNavItem>
        </NavGroup>
        <NavGroup
          expandedItems={expandedItems}
          toggleItem={toggleItem}
          label='Custom Resources'
          icon='fa-puzzle-piece'
        >
          <K8sNavItem
            to='/customresourcedefinitions'
            Icon={CustomResourceDefinitionIcon}
          >
            Definitions
          </K8sNavItem>
          {crdGroups.map(g => (
            <RoutedLink
              key={g}
              to={`/customresourcedefinitions?group=${g}`}
              className='pl-8 text-sm truncate max-w-full'
            >
              {g}
            </RoutedLink>
          ))}
        </NavGroup>
      </div>
      <h2 className='mt-6 mb-2 px-4 text-gray-100 text-xl'>About</h2>
      <ExtNavItem href='https://github.com/manusa/yakd'>
        YAKD - Yet Another Kubernetes Dashboard
      </ExtNavItem>
    </nav>
  );
};

const mapStateToProps = ({apiGroups, customResourceDefinitions}) => ({
  isOpenShift: apis.selectors.isOpenShift(apiGroups),
  crdGroups: crd.selectors.groups(customResourceDefinitions)
});

export const SideBar = connect(mapStateToProps)(({
  sideBarOpen,
  isOpenShift,
  crdGroups
}) => {
  const {
    sidebarExpandedItems,
    sidebarScrollTop,
    sidebarScroll,
    sidebarToggleItem
  } = useUiSidebar();
  return (
    <div
      className={`${
        sideBarOpen ? 'translate-x-0 ease-out' : '-translate-x-full ease-in'
      }
          side-bar
          fixed z-30 inset-y-0 left-0 w-64 flex flex-col
          transition duration-300 transform bg-gray-900 lg:translate-x-0 lg:static lg:inset-0`}
    >
      <div className='flex items-center justify-center mt-8'>
        <Link.RouterLink
          to='/'
          variant={Link.variants.none}
          className='flex flex-col items-center'
        >
          <YAKDLogo kubernetesColor='#FFFFFF' className='block h-8 lg:h-12' />
          <div className='text-white text-lg lg:text-xl mt-2 mx-2 font-semibold'>
            Kubernetes Dashboard
          </div>
        </Link.RouterLink>
      </div>
      <NavSection
        currentScrollTop={sidebarScrollTop}
        scroll={sidebarScroll}
        expandedItems={sidebarExpandedItems}
        toggleItem={sidebarToggleItem}
        isOpenShift={isOpenShift}
        crdGroups={crdGroups}
      />
    </div>
  );
});
