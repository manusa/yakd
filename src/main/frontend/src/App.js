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
import {useDispatch} from 'react-redux';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import * as apis from './apis';
import {
  ClusterRoleBindingsPage,
  ClusterRoleBindingsDetailPage,
  ClusterRoleBindingsEditPage
} from './clusterrolebindings';
import {
  ClusterRolesPage,
  ClusterRolesDetailPage,
  ClusterRolesEditPage
} from './clusterroles';
import {
  ConfigMapsPage,
  ConfigMapsDetailPage,
  ConfigMapsEditPage
} from './configmaps';
import {CronJobsPage, CronJobsDetailPage, CronJobsEditPage} from './cronjobs';
import crd from './customresourcedefinitions';
import dc from './deploymentconfigs';
import {
  DaemonSetsPage,
  DaemonSetsDetailPage,
  DaemonSetsEditPage
} from './daemonsets';
import {
  DeploymentsPage,
  DeploymentsEditPage,
  DeploymentsDetailPage
} from './deployments';
import {EndpointsPage, EndpointsDetailPage} from './endpoints';
import {
  HorizontalPodAutoscalersPage,
  HorizontalPodAutoscalersDetailPage,
  HorizontalPodAutoscalersEditPage
} from './horizontalpodautoscalers';
import {
  IngressesPage,
  IngressesDetailPage,
  IngressesEditPage
} from './ingresses';
import {JobsPage, JobsEditPage, JobsDetailPage} from './jobs';
import {NodesPage, NodesDetailPage, NodesEditPage} from './nodes';
import {NamespacesPage, NamespacesDetailPage} from './namespaces';
import pvc from './persistentvolumeclaims';
import {
  PersistentVolumesPage,
  PersistentVolumesEditPage,
  PersistentVolumesDetailPage
} from './persistentvolumes';
import pods from './pods';
import rc from './replicationcontrollers';
import {apiGroupsSet, setError, setOffline} from './redux';
import roles from './roles';
import routes from './routes';
import {SearchPage} from './search';
import secrets from './secrets';
import services from './services';
import {
  ServiceAccountsPage,
  ServiceAccountsDetailPage,
  ServiceAccountsEditPage
} from './serviceaccounts';
import sts from './statefulsets';
import watch from './watch';
import {Home} from './Home';

let eventSource;
let pollResourcesTimeout;

const pollResources = dispatch => {
  const dispatchedPoll = async () => {
    try {
      await Promise.all([
        apis.api
          .listGroups()
          .then(apiGroups => dispatch(apiGroupsSet(apiGroups)))
      ]);
    } catch (e) {
      dispatch(setError('Error when polling resources (retrying)'));
    }
    if (eventSource.readyState === EventSource.CLOSED) {
      console.error('EventSource connection was lost, reconnecting');
      dispatch(setOffline(true));
      eventSource.close();
      eventSource = watch.api.startEventSource({dispatch});
    }
    pollResourcesTimeout = setTimeout(dispatchedPoll, 3000);
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

export const App = () => {
  const dispatch = useDispatch();
  useEffect(() => {
    onMount({dispatch});
    return onUnmount;
  }, []); // eslint-disable-line react-hooks/exhaustive-deps
  return (
    <Router>
      <Routes>
        <Route exact path='/' element={<Home />} />
        <Route
          exact
          path='/clusterrolebindings'
          element={<ClusterRoleBindingsPage />}
        />
        <Route
          exact
          path='/clusterrolebindings/:uidOrName'
          element={<ClusterRoleBindingsDetailPage />}
        />
        <Route
          exact
          path='/clusterrolebindings/:uid/edit'
          element={<ClusterRoleBindingsEditPage />}
        />
        <Route exact path='/clusterroles' element={<ClusterRolesPage />} />
        <Route
          exact
          path='/clusterroles/:uidOrName'
          element={<ClusterRolesDetailPage />}
        />
        <Route
          exact
          path='/clusterroles/:uid/edit'
          element={<ClusterRolesEditPage />}
        />
        <Route exact path='/configmaps' element={<ConfigMapsPage />} />
        <Route
          exact
          path='/configmaps/:uid'
          element={<ConfigMapsDetailPage />}
        />
        <Route
          exact
          path='/configmaps/:uid/edit'
          element={<ConfigMapsEditPage />}
        />
        <Route exact path='/cronjobs' element={<CronJobsPage />} />
        <Route exact path='/cronjobs/:uid' element={<CronJobsDetailPage />} />
        <Route
          exact
          path='/cronjobs/:uid/edit'
          element={<CronJobsEditPage />}
        />
        <Route
          exact
          path='/customresourcedefinitions'
          element={<crd.CustomResourceDefinitionsPage />}
        />
        <Route
          exact
          path='/customresourcedefinitions/:uid'
          element={<crd.CustomResourceDefinitionsDetailPage />}
        />
        <Route
          exact
          path='/customresourcedefinitions/:uid/edit'
          element={<crd.CustomResourceDefinitionsEditPage />}
        />
        <Route exact path='/daemonsets' element={<DaemonSetsPage />} />
        <Route
          exact
          path='/daemonsets/:uid'
          element={<DaemonSetsDetailPage />}
        />
        <Route
          exact
          path='/daemonsets/:uid/edit'
          element={<DaemonSetsEditPage />}
        />
        <Route
          exact
          path='/deploymentconfigs'
          element={<dc.DeploymentConfigsPage />}
        />
        <Route
          exact
          path='/deploymentconfigs/:uid'
          element={<dc.DeploymentConfigsDetailPage />}
        />
        <Route
          exact
          path='/deploymentconfigs/:uid/edit'
          element={<dc.DeploymentConfigsEditPage />}
        />
        <Route exact path='/deployments' element={<DeploymentsPage />} />
        <Route
          exact
          path='/deployments/:uid'
          element={<DeploymentsDetailPage />}
        />
        <Route
          exact
          path='/deployments/:uid/edit'
          element={<DeploymentsEditPage />}
        />
        <Route exact path='/endpoints' element={<EndpointsPage />} />
        <Route exact path='/endpoints/:uid' element={<EndpointsDetailPage />} />
        <Route
          exact
          path='/horizontalpodautoscalers'
          element={<HorizontalPodAutoscalersPage />}
        />
        <Route
          exact
          path='/horizontalpodautoscalers/:uid'
          element={<HorizontalPodAutoscalersDetailPage />}
        />
        <Route
          exact
          path='/horizontalpodautoscalers/:uid/edit'
          element={<HorizontalPodAutoscalersEditPage />}
        />
        <Route exact path='/ingresses' element={<IngressesPage />} />
        <Route exact path='/ingresses/:uid' element={<IngressesDetailPage />} />
        <Route
          exact
          path='/ingresses/:uid/edit'
          element={<IngressesEditPage />}
        />
        <Route exact path='/jobs' element={<JobsPage />} />
        <Route exact path='/jobs/:uid' element={<JobsDetailPage />} />
        <Route exact path='/jobs/:uid/edit' element={<JobsEditPage />} />
        <Route exact path='/namespaces' element={<NamespacesPage />} />
        <Route
          exact
          path='/namespaces/:uidOrName'
          element={<NamespacesDetailPage />}
        />
        <Route exact path='/nodes' element={<NodesPage />} />
        <Route exact path='/nodes/:name' element={<NodesDetailPage />} />
        <Route exact path='/nodes/:uid/edit' element={<NodesEditPage />} />
        <Route
          exact
          path='/persistentvolumeclaims'
          element={<pvc.PersistentVolumeClaimsPage />}
        />
        <Route
          exact
          path='/persistentvolumeclaims/:uid'
          element={<pvc.PersistentVolumeClaimsDetailPage />}
        />
        <Route
          exact
          path='/persistentvolumeclaims/:uid/edit'
          element={<pvc.PersistentVolumeClaimsEditPage />}
        />
        <Route
          exact
          path='/persistentvolumes'
          element={<PersistentVolumesPage />}
        />
        <Route
          exact
          path='/persistentvolumes/:uid'
          element={<PersistentVolumesDetailPage />}
        />
        <Route
          exact
          path='/persistentvolumes/:uid/edit'
          element={<PersistentVolumesEditPage />}
        />
        <Route exact path='/pods' element={<pods.PodsPage />} />
        <Route exact path='/pods/:uid' element={<pods.PodsDetailPage />} />
        <Route exact path='/pods/:uid/edit' element={<pods.PodsEditPage />} />
        <Route exact path='/pods/:uid/exec' element={<pods.PodsExecPage />} />
        <Route exact path='/pods/:uid/logs' element={<pods.PodsLogsPage />} />
        <Route
          exact
          path='/replicationcontrollers'
          element={<rc.ReplicationControllersPage />}
        />
        <Route
          exact
          path='/replicationcontrollers/:uid'
          element={<rc.ReplicationControllersDetailPage />}
        />
        <Route
          exact
          path='/replicationcontrollers/:uid/edit'
          element={<rc.ReplicationControllersEditPage />}
        />
        <Route exact path='/roles' element={<roles.RolesPage />} />
        <Route exact path='/roles/:uid' element={<roles.RolesDetailPage />} />
        <Route
          exact
          path='/roles/:uid/edit'
          element={<roles.RolesEditPage />}
        />
        <Route exact path='/routes' element={<routes.RoutesPage />} />
        <Route
          exact
          path='/routes/:uid'
          element={<routes.RoutesDetailPage />}
        />
        <Route
          exact
          path='/routes/:uid/edit'
          element={<routes.RoutesEditPage />}
        />
        <Route exact path='/search' element={<SearchPage />} />
        <Route exact path='/secrets' element={<secrets.SecretsPage />} />
        <Route
          exact
          path='/secrets/:uid'
          element={<secrets.SecretsDetailPage />}
        />
        <Route
          exact
          path='/secrets/:uid/edit'
          element={<secrets.SecretsEditPage />}
        />
        <Route exact path='/services' element={<services.ServicesPage />} />
        <Route
          exact
          path='/services/:uid'
          element={<services.ServicesDetailPage />}
        />
        <Route
          exact
          path='/services/:uid/edit'
          element={<services.ServicesEditPage />}
        />
        <Route
          exact
          path='/serviceaccounts'
          element={<ServiceAccountsPage />}
        />
        <Route
          exact
          path='/serviceaccounts/:uid'
          element={<ServiceAccountsDetailPage />}
        />
        <Route
          exact
          path='/serviceaccounts/:uid/edit'
          element={<ServiceAccountsEditPage />}
        />
        <Route exact path='/statefulsets' element={<sts.StatefulSetsPage />} />
        <Route
          exact
          path='/statefulsets/:uid'
          element={<sts.StatefulSetsDetailPage />}
        />
        <Route
          exact
          path='/statefulsets/:uid/edit'
          element={<sts.StatefulSetsEditPage />}
        />
        {/*<Route element={<Error404Page/>} />*/}
      </Routes>
    </Router>
  );
};
