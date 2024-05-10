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
import {combineReducers, createStore} from 'redux';
import * as apis from '../apis';
import {reducer as reduxReducer, uiReducer} from './';

const storeEnhancer = () => {
  if (
    process.env.NODE_ENV === 'development' &&
    window.__REDUX_DEVTOOLS_EXTENSION__
  ) {
    return window.__REDUX_DEVTOOLS_EXTENSION__();
  }
};

const appReducer = combineReducers({
  apiGroups: apis.apiGroupsReducer,
  clusterRoleBindings: reduxReducer('ClusterRoleBinding'),
  clusterRoles: reduxReducer('ClusterRole'),
  clusterVersions: reduxReducer('ClusterVersion'),
  configMaps: reduxReducer('ConfigMap'),
  cronJobs: reduxReducer('CronJob'),
  customResourceDefinitions: reduxReducer('CustomResourceDefinition'),
  daemonSets: reduxReducer('DaemonSet'),
  deploymentConfigs: reduxReducer('DeploymentConfig'),
  deployments: reduxReducer('Deployment'),
  endpoints: reduxReducer('Endpoints'),
  events: reduxReducer('Event'),
  horizontalPodAutoscalers: reduxReducer('HorizontalPodAutoscaler'),
  ingresses: reduxReducer('Ingress'),
  jobs: reduxReducer('Job'),
  namespaces: reduxReducer('Namespace'),
  nodes: reduxReducer('Node'),
  persistentVolumeClaims: reduxReducer('PersistentVolumeClaim'),
  persistentVolumes: reduxReducer('PersistentVolume'),
  pods: reduxReducer('Pod'),
  replicaSets: reduxReducer('ReplicaSet'),
  replicationControllers: reduxReducer('ReplicationController'),
  roleBindings: reduxReducer('RoleBinding'),
  roles: reduxReducer('Role'),
  routes: reduxReducer('Route'),
  secrets: reduxReducer('Secret'),
  services: reduxReducer('Service'),
  serviceAccounts: reduxReducer('ServiceAccount'),
  statefulSets: reduxReducer('StatefulSet'),
  ui: uiReducer
});

// noinspection JSDeprecatedSymbols
export const store = createStore(appReducer, storeEnhancer());
