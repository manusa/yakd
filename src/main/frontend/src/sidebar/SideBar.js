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
import {bindActionCreators} from 'redux';
import {useMatch} from 'react-router-dom';
import apis from "../apis";
import i from '../components/icons';
import redux from '../redux';
import crd from '../customresourcedefinitions';
import Icon from '../components/Icon';
import Link from '../components/Link';

import './SideBar.css';

const RoutedLink = ({to, staticContext, className = '', ...props}) => {
  const match = useMatch(`${to}/*`);
  return   <Link.RouterLink
    variant={Link.variants.none}
    to={to}
    className={`${className}
      flex items-center border-l-4 px-4 py-2 lg:py-3
      ${match ?
      'bg-gray-600 bg-opacity-25 text-gray-100 border-gray-100' :
      'border-transparent text-gray-500 hover:bg-gray-600 hover:bg-opacity-25 hover:text-gray-100'}`}
    {...props}
  />
}

const NavGroup = ({expandedItems, toggleItem, label, icon, children}) => {
  const expanded = expandedItems.includes(label);
  const onClick = () => toggleItem(label);
  return (
    <div className={`border-t last:border-b border-black`}>
      <div
        className='flex items-center border-l-4 border-transparent px-4 py-2 lg:py-3 text-gray-300 hover:bg-gray-600 hover:bg-opacity-25 cursor-pointer'
        onClick={onClick}
      >
        <Icon className='side-bar__nav-group-icon' icon={icon} />
        <span className='flex-1'>{label}</span>
        <Icon className='' icon={expanded ? 'fa-chevron-down' : 'fa-chevron-right'} />
      </div>
      <div className={`
        overflow-hidden transform origin-top transition-transform duration-75
        ${expanded ? 'scale-y-100' : 'h-0 scale-y-0'}
      `}>
        {children}
      </div>
    </div>
  );
};

const K8sNavItem = ({to, Icon: ComponentIcon, children}) => (
  <RoutedLink to={to}>
    <ComponentIcon className='side-bar__nav-item-icon'/>
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
      border-transparent text-gray-500 hover:bg-gray-600 hover:bg-opacity-25 hover:text-gray-100`}
  >{children}</Link>
);

const NavSection = ({currentScrollTop, scroll, expandedItems, toggleItem, isOpenShift, crdGroups}) => {
  const ref = useRef(null);
  useLayoutEffect(() => {
    ref.current.scroll(0, currentScrollTop);
  }, [ref, currentScrollTop]);
  return (
    <nav
      ref={ref} onScroll={({target: {scrollTop}}) => scroll({scrollTop})}
      className='mt-4 lg:mt-6 flex-1 h-1 overflow-x-hidden overflow-y-auto custom-scroll-dark'
    >
      <IconNavItem to='/search' icon='fa-search'>Search</IconNavItem>
      <K8sNavItem to='/' Icon={i.Kubernetes}>Home</K8sNavItem>
      <K8sNavItem to='/nodes' Icon={i.Node}>Nodes</K8sNavItem>
      <K8sNavItem to='/namespaces' Icon={i.Namespace}>Namespaces</K8sNavItem>
      <div>
        <NavGroup expandedItems={expandedItems} toggleItem={toggleItem}
                  label='Workloads' icon='fa-cubes'>
          <K8sNavItem to='/pods' Icon={i.Pod}>Pods</K8sNavItem>
          <K8sNavItem to='/deployments' Icon={i.Deployment}>Deployments</K8sNavItem>
          {isOpenShift && <K8sNavItem to='/deploymentconfigs' Icon={i.DeploymentConfig}>Deployment Configs</K8sNavItem>}
          <K8sNavItem to='/statefulsets' Icon={i.StatefulSet}>StatefulSets</K8sNavItem>
          <K8sNavItem to='/cronjobs' Icon={i.CronJob}>CronJobs</K8sNavItem>
          <K8sNavItem to='/jobs' Icon={i.Job}>Jobs</K8sNavItem>
          <K8sNavItem to='/daemonsets' Icon={i.DaemonSet}>DaemonSets</K8sNavItem>
          <K8sNavItem to='/replicationcontrollers' Icon={i.ReplicaSet}>Replication Controllers</K8sNavItem>
          <K8sNavItem to='/horizontalpodautoscalers' Icon={i.HorizontalPodAutoscaler}>Horizontal Pod Autoscalers</K8sNavItem>
        </NavGroup>
        <NavGroup expandedItems={expandedItems} toggleItem={toggleItem}
                  label='Network' icon='fa-network-wired'>
          <K8sNavItem to='/services' Icon={i.Service}>Services</K8sNavItem>
          <K8sNavItem to='/ingresses' Icon={i.Ingress}>Ingresses</K8sNavItem>
          {isOpenShift && <K8sNavItem to='/routes' Icon={i.Route}>Routes</K8sNavItem>}
        </NavGroup>
        <NavGroup expandedItems={expandedItems} toggleItem={toggleItem}
                  label='Configuration' icon='fa-list'>
          <K8sNavItem to='/configmaps' Icon={i.ConfigMap}>ConfigMaps</K8sNavItem>
          <K8sNavItem to='/secrets' Icon={i.Secret}>Secrets</K8sNavItem>
        </NavGroup>
        <NavGroup expandedItems={expandedItems} toggleItem={toggleItem}
                  label='Storage' icon='fa-database'>
          <K8sNavItem to='/persistentvolumes' Icon={i.PersistentVolume}>PersistentVolumes</K8sNavItem>
          <K8sNavItem to='/persistentvolumeclaims' Icon={i.PersistentVolumeClaim}>PersistentVolume Claims</K8sNavItem>
        </NavGroup>
        <NavGroup expandedItems={expandedItems} toggleItem={toggleItem}
                  label='Access Control' icon='fa-shield-alt'>
          <K8sNavItem to='/clusterroles' Icon={i.ClusterRole}>ClusterRoles</K8sNavItem>
          <K8sNavItem to='/clusterrolebindings' Icon={i.ClusterRoleBinding}>ClusterRoleBindings</K8sNavItem>
          <K8sNavItem to='/roles' Icon={i.Role}>Roles</K8sNavItem>
          <K8sNavItem to='/secrets' Icon={i.Secret}>Secrets</K8sNavItem>
        </NavGroup>
        <NavGroup expandedItems={expandedItems} toggleItem={toggleItem}
                  label='Custom Resources' icon='fa-puzzle-piece'>
          <K8sNavItem to='/customresourcedefinitions' Icon={i.CustomResourceDefinition}>Definitions</K8sNavItem>
          {crdGroups.map(g =>
            <RoutedLink
              key={g} to={`/customresourcedefinitions?group=${g}`}
              className='pl-8 text-sm truncate max-w-full'
            >{g}</RoutedLink>
          )}
        </NavGroup>
      </div>
      <h2 className='mt-6 mb-2 px-4 text-gray-100 text-xl'>About</h2>
      <ExtNavItem href='https://github.com/manusa/yakc/tree/master/quickstarts/quarkus-dashboard'>
        Quarkus Kubernetes Dashboard</ExtNavItem>
      <ExtNavItem href='https://github.com/manusa/yakc'>YAKC</ExtNavItem>
    </nav>
  );
}

const SideBar = ({sideBarOpen, scroll, currentScrollTop, expandedItems, toggleItem, isOpenShift, crdGroups}) => {
  return (
    <div
      className={`${sideBarOpen ? 'translate-x-0 ease-out' : '-translate-x-full ease-in'}
          side-bar
          fixed z-30 inset-y-0 left-0 w-64 flex flex-col
          transition duration-300 transform bg-gray-900 lg:translate-x-0 lg:static lg:inset-0`}
    >
      <div className='flex items-center justify-center mt-8'>
        <Link.RouterLink
          to='/' variant={Link.variants.none} className='flex flex-col items-center'
        >
          <i.YAKCLogo kubernetesColor='#FFFFFF' className='block h-8 lg:h-12' />
          <div className='text-white text-lg lg:text-xl mt-2 mx-2 font-semibold'>
            Kubernetes Dashboard
          </div>
        </Link.RouterLink>
      </div>
      <NavSection
        currentScrollTop={currentScrollTop} scroll={scroll}
        expandedItems={expandedItems} toggleItem={toggleItem}
        isOpenShift={isOpenShift}
        crdGroups={crdGroups}
      />
    </div>
  );
};

const mapStateToProps = ({
  apiGroups, sidebar: {expandedItems, scroll: {scrollTop: currentScrollTop}}, customResourceDefinitions
}) => ({
  expandedItems, currentScrollTop,
  isOpenShift: apis.selectors.isOpenShift(apiGroups),
  crdGroups: crd.selectors.groups(customResourceDefinitions)
});

const mapDispatchToProps = dispatch =>  bindActionCreators({
  scroll: redux.actions.sideBarScroll,
  toggleItem: redux.actions.sideBarToggleItem
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SideBar);
