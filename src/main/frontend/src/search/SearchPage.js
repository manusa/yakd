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
import React, {useEffect, useLayoutEffect, useRef} from 'react';
import {bindActionCreators} from 'redux';
import {useSearchParams} from 'react-router-dom';
import {connect} from 'react-redux';
import {ClusterRolesList} from '../clusterroles';
import {ConfigMapsList} from '../configmaps';
import {CronJobsList} from '../cronjobs';
import {CustomResourceDefinitionsList} from '../customresourcedefinitions';
import {DeploymentsList} from '../deployments';
import {DeploymentConfigsList} from '../deploymentconfigs';
import {DaemonSetsList} from '../daemonsets';
import * as ep from '../endpoints';
import {HorizontalPodAutoscalersList} from '../horizontalpodautoscalers';
import {IngressesList} from '../ingresses';
import {JobsList} from '../jobs';
import {NamespacesList} from '../namespaces';
import p from '../pods';
import {PersistentVolumeClaimsList} from '../persistentvolumeclaims';
import {PersistentVolumesList} from '../persistentvolumes';
import {SecretsList} from '../secrets';
import {StatefulSetsList} from '../statefulsets';
import {ServicesList} from '../services';
import {ServiceAccountsList} from '../serviceaccounts';
import {setQuery} from '../redux';
import {ReplicaSetsList} from '../replicasets';
import {ReplicationControllersList} from '../replicationcontrollers';
import {RolesList} from '../roles';
import {RoutesList} from '../routes';
import {Card, FilterBar, Textfield} from '../components';
import {DashboardPage} from '../dashboard';

const Instructions = () => (
  <Card>
    <Card.Body className='text-gray-800'>
      Type at least 3 characters into the Search box to start the query.
    </Card.Body>
  </Card>
);

const Results = ({query, selectedNamespace}) => {
  const commonProps = {
    className: 'mt-2',
    titleVariant: Card.titleVariants.small,
    hideWhenNoResults: true
  };
  return (
    <>
      <p.List
        {...commonProps}
        title='Pods'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <DeploymentsList
        {...commonProps}
        title='Deployments'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <DeploymentConfigsList
        {...commonProps}
        title='DeploymentConfigs'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <DaemonSetsList
        {...commonProps}
        title='DaemonSets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <CronJobsList
        {...commonProps}
        title='CronJobs'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <JobsList
        {...commonProps}
        title='Jobs'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <StatefulSetsList
        {...commonProps}
        title='StatefulSets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ReplicaSetsList
        {...commonProps}
        title='ReplicaSets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ReplicationControllersList
        {...commonProps}
        title='ReplicationControllers'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <HorizontalPodAutoscalersList
        {...commonProps}
        title='HorizontalPodAutoscalers'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ServicesList
        {...commonProps}
        title='Services'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ep.List
        {...commonProps}
        title='Endpoints'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <IngressesList
        {...commonProps}
        title='Ingresses'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <RoutesList
        {...commonProps}
        title='Routes'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <NamespacesList
        {...commonProps}
        title='Namespaces'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ConfigMapsList
        {...commonProps}
        title='ConfigMaps'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <SecretsList
        {...commonProps}
        title='Secrets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ServiceAccountsList
        {...commonProps}
        title='ServiceAccounts'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <PersistentVolumesList
        {...commonProps}
        title='PersistentVolumes'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <PersistentVolumeClaimsList
        {...commonProps}
        title='PersistentVolumeClaims'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <CustomResourceDefinitionsList
        {...commonProps}
        title='CustomResourceDefinitions'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ClusterRolesList
        {...commonProps}
        title='ClusterRoles'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <RolesList
        {...commonProps}
        title='Roles'
        nameLike={query}
        namespace={selectedNamespace}
      />
    </>
  );
};

const mapStateToProps = ({ui: {selectedNamespace, query}}) => ({
  selectedNamespace,
  query
});

const mapDispatchToProps = dispatch =>
  bindActionCreators(
    {
      setQuery
    },
    dispatch
  );

export const SearchPage = connect(
  mapStateToProps,
  mapDispatchToProps
)(({selectedNamespace, query, setQuery}) => {
  const [searchParams, setSearchParams] = useSearchParams();
  useEffect(() => {
    const q = searchParams.get('q');
    if (typeof q === 'string' && q !== query) {
      setQuery(q);
    }
  }, [searchParams, query, setQuery]);
  const inputRef = useRef(null);
  useLayoutEffect(() => {
    inputRef.current.focus();
  }, [inputRef]);
  return (
    <DashboardPage title='Query cluster resources'>
      <div className='flex mb-4'>
        <Textfield
          className='flex-1  mr-2'
          inputRef={inputRef}
          placeholder='Search'
          icon='fa-search'
          value={query}
          onChange={({target: {value: q}}) =>
            setSearchParams(`?${new URLSearchParams({q})}`)
          }
        />
        <FilterBar />
      </div>
      {query.length > 2 ? (
        <Results query={query} selectedNamespace={selectedNamespace} />
      ) : (
        <Instructions />
      )}
    </DashboardPage>
  );
});
