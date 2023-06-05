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
import React, {useEffect} from 'react';
import {connect} from 'react-redux';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import apis from './apis';
import crb from './clusterrolebindings';
import cRoles from './clusterroles';
import configMaps from './configmaps';
import cj from './cronjobs';
import crd from './customresourcedefinitions';
import dc from './deploymentconfigs';
import ds from './daemonsets';
import deployments from './deployments';
import hpa from './horizontalpodautoscalers';
import ingresses from './ingresses';
import jobs from './jobs';
import nodes from './nodes';
import ns from './namespaces';
import pvc from './persistentvolumeclaims';
import pv from './persistentvolumes';
import pods from './pods';
import rc from './replicationcontrollers';
import redux from './redux';
import roles from './roles';
import routes from './routes';
import search from './search';
import secrets from './secrets';
import services from './services';
import sts from './statefulsets';
import watch from './watch';
import Home from './Home';

let eventSource;
let pollResourcesTimeout;

const pollResources = dispatch => {
  const dispatchedPoll = async () => {
    const handleResourceList = kind => async resources => {
      await dispatch(redux.actions.crudSetAll({kind, resources}));
      dispatch(redux.actions.setResourceLoaded({kind, loaded: true}));
    };
    try {
      await Promise.all([
        apis.api.listGroups().then(apiGroups => dispatch(redux.actions.apiGroupsSet(apiGroups))),
        ingresses.api.list().then(handleResourceList('Ingress')),
        pvc.api.list().then(handleResourceList('PersistentVolumeClaim')),
        pv.api.list().then(handleResourceList('PersistentVolume'))
      ]);
    } catch (e) {
      dispatch(redux.actions.setError('Error when polling resources (retrying)'));
    }
    if (eventSource.readyState === EventSource.CLOSED) {
      console.error('EventSource connection was lost, reconnecting');
      redux.actions.setOffline(true);
      eventSource.close();
      eventSource = watch.api.startEventSource({dispatch});
    }
    pollResourcesTimeout = setTimeout(dispatchedPoll, 3000)
  };
  return dispatchedPoll;
};

const onMount = ({dispatch}) => {
  eventSource = watch.api.startEventSource({dispatch});
  pollResources(dispatch)();
};

const onUnmount = () => {
  clearTimeout(pollResourcesTimeout);
  if (eventSource.close) {
    eventSource.close();
  }
};

const App = ({dispatch}) => {
  useEffect(() => {
    onMount({dispatch});
    return onUnmount;
  }, []); // eslint-disable-line react-hooks/exhaustive-deps
  return (
      <Router>
        <Routes>
          <Route exact path='/' element={<Home/>} />
          <Route exact path='/clusterrolebindings' element={<crb.ClusterRoleBindingsPage/>} />
          <Route exact path='/clusterrolebindings/:uidOrName' element={<crb.ClusterRoleBindingsDetailPage/>} />
          <Route exact path='/clusterrolebindings/:uid/edit' element={<crb.ClusterRoleBindingsEditPage/>} />
          <Route exact path='/clusterroles' element={<cRoles.ClusterRolesPage/>} />
          <Route exact path='/clusterroles/:uidOrName' element={<cRoles.ClusterRolesDetailPage/>} />
          <Route exact path='/clusterroles/:uid/edit' element={<cRoles.ClusterRolesEditPage/>} />
          <Route exact path='/configmaps' element={<configMaps.ConfigMapsPage/>} />
          <Route exact path='/configmaps/:uid' element={<configMaps.ConfigMapsDetailPage/>} />
          <Route exact path='/configmaps/:uid/edit' element={<configMaps.ConfigMapsEditPage/>} />
          <Route exact path='/cronjobs' element={<cj.CronJobsPage/>} />
          <Route exact path='/cronjobs/:uid' element={<cj.CronJobsDetailPage/>} />
          <Route exact path='/cronjobs/:uid/edit' element={<cj.CronJobsEditPage/>} />
          <Route exact path='/customresourcedefinitions' element={<crd.CustomResourceDefinitionsPage/>} />
          <Route exact path='/customresourcedefinitions/:uid' element={<crd.CustomResourceDefinitionsDetailPage/>} />
          <Route exact path='/customresourcedefinitions/:uid/edit' element={<crd.CustomResourceDefinitionsEditPage/>} />
          <Route exact path='/daemonsets' element={<ds.DaemonSetsPage/>} />
          <Route exact path='/daemonsets/:uid' element={<ds.DaemonSetsDetailPage/>} />
          <Route exact path='/daemonsets/:uid/edit' element={<ds.DaemonSetsEditPage/>} />
          <Route exact path='/deploymentconfigs' element={<dc.DeploymentConfigsPage/>} />
          <Route exact path='/deploymentconfigs/:uid' element={<dc.DeploymentConfigsDetailPage/>} />
          <Route exact path='/deploymentconfigs/:uid/edit' element={<dc.DeploymentConfigsEditPage/>} />
          <Route exact path='/deployments' element={<deployments.DeploymentsPage/>} />
          <Route exact path='/deployments/:uid' element={<deployments.DeploymentsDetailPage/>} />
          <Route exact path='/deployments/:uid/edit' element={<deployments.DeploymentsEditPage/>} />
          <Route exact path='/horizontalpodautoscalers' element={<hpa.HorizontalPodAutoscalersPage/>} />
          <Route exact path='/horizontalpodautoscalers/:uid' element={<hpa.HorizontalPodAutoscalersDetailPage/>} />
          <Route exact path='/horizontalpodautoscalers/:uid/edit' element={<hpa.HorizontalPodAutoscalersEditPage/>} />
          <Route exact path='/ingresses' element={<ingresses.IngressesPage/>} />
          <Route exact path='/ingresses/:uid' element={<ingresses.IngressesDetailPage/>} />
          <Route exact path='/ingresses/:uid/edit' element={<ingresses.IngressEditPage/>} />
          <Route exact path='/jobs' element={<jobs.JobsPage/>} />
          <Route exact path='/jobs/:uid' element={<jobs.JobsDetailPage/>} />
          <Route exact path='/jobs/:uid/edit' element={<jobs.JobsEditPage/>} />
          <Route exact path='/namespaces' element={<ns.NamespacesPage/>} />
          <Route exact path='/namespaces/:uidOrName' element={<ns.NamespacesDetailPage/>} />
          <Route exact path='/nodes' element={<nodes.NodesPage/>} />
          <Route exact path='/nodes/:name' element={<nodes.NodesDetailPage/>} />
          <Route exact path='/nodes/:uid/edit' element={<nodes.NodesEditPage/>} />
          <Route exact path='/persistentvolumeclaims' element={<pvc.PersistentVolumeClaimsPage/>} />
          <Route exact path='/persistentvolumeclaims/:uid' element={<pvc.PersistentVolumeClaimsDetailPage/>} />
          <Route exact path='/persistentvolumeclaims/:uid/edit' element={<pvc.PersistentVolumeClaimsEditPage/>} />
          <Route exact path='/persistentvolumes' element={<pv.PersistentVolumesPage/>} />
          <Route exact path='/persistentvolumes/:uid' element={<pv.PersistentVolumesDetailPage/>} />
          <Route exact path='/persistentvolumes/:uid/edit' element={<pv.PersistentVolumesEditPage/>} />
          <Route exact path='/pods' element={<pods.PodsPage/>} />
          <Route exact path='/pods/:uid' element={<pods.PodsDetailPage/>} />
          <Route exact path='/pods/:uid/edit' element={<pods.PodsEditPage/>} />
          <Route exact path='/pods/:uid/exec' element={<pods.PodsExecPage/>} />
          <Route exact path='/pods/:uid/logs' element={<pods.PodsLogsPage/>} />
          <Route exact path='/replicationcontrollers' element={<rc.ReplicationControllersPage/>} />
          <Route exact path='/replicationcontrollers/:uid' element={<rc.ReplicationControllersDetailPage/>} />
          <Route exact path='/replicationcontrollers/:uid/edit' element={<rc.ReplicationControllersEditPage/>} />
          <Route exact path='/roles' element={<roles.RolesPage/>} />
          <Route exact path='/roles/:uid' element={<roles.RolesDetailPage/>} />
          <Route exact path='/roles/:uid/edit' element={<roles.RolesEditPage/>} />
          <Route exact path='/routes' element={<routes.RoutesPage/>} />
          <Route exact path='/routes/:uid' element={<routes.RoutesDetailPage/>} />
          <Route exact path='/routes/:uid/edit' element={<routes.RoutesEditPage/>} />
          <Route exact path='/search' element={<search.SearchPage/>} />
          <Route exact path='/secrets' element={<secrets.SecretsPage/>} />
          <Route exact path='/secrets/:uid' element={<secrets.SecretsDetailPage/>} />
          <Route exact path='/secrets/:uid/edit' element={<secrets.SecretsEditPage/>} />
          <Route exact path='/services' element={<services.ServicesPage/>} />
          <Route exact path='/services/:uid' element={<services.ServicesDetailPage/>} />
          <Route exact path='/services/:uid/edit' element={<services.ServicesEditPage/>} />
          <Route exact path='/statefulsets' element={<sts.StatefulSetsPage/>} />
          <Route exact path='/statefulsets/:uid' element={<sts.StatefulSetsDetailPage/>} />
          <Route exact path='/statefulsets/:uid/edit' element={<sts.StatefulSetsEditPage/>} />
          {/*<Route element={<Error404Page/>} />*/}
        </Routes>
      </Router>
  );
}

const mapStateToProps = () => ({});

const mapDispatchToProps = dispatch => ({dispatch});

export default connect(mapStateToProps, mapDispatchToProps)(App);
