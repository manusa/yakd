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
import React, {useLayoutEffect, useRef} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import * as cr from '../clusterroles';
import * as cm from '../configmaps';
import * as cj from '../cronjobs';
import crd from '../customresourcedefinitions';
import d from '../deployments';
import dc from '../deploymentconfigs';
import * as ds from '../daemonsets';
import * as ep from '../endpoints';
import hpa from '../horizontalpodautoscalers';
import i from '../ingresses';
import j from '../jobs';
import ns from '../namespaces';
import p from '../pods';
import pvc from '../persistentvolumeclaims';
import pv from '../persistentvolumes';
import s from '../secrets';
import ss from '../statefulsets';
import svc from '../services';
import * as sa from '../serviceaccounts';
import redux from '../redux';
import rs from '../replicasets';
import rc from '../replicationcontrollers';
import roles from '../roles';
import routes from '../routes';
import {Card, FilterBar} from '../components';
import DashboardPage from '../components/DashboardPage';
import Textfield from '../components/Textfield';

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
      <d.List
        {...commonProps}
        title='Deployments'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <dc.List
        {...commonProps}
        title='DeploymentConfigs'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ds.List
        {...commonProps}
        title='DaemonSets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <cj.List
        {...commonProps}
        title='CronJobs'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <j.List
        {...commonProps}
        title='Jobs'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ss.List
        {...commonProps}
        title='StatefulSets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <rs.List
        {...commonProps}
        title='ReplicaSets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <rc.List
        {...commonProps}
        title='ReplicationControllers'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <hpa.List
        {...commonProps}
        title='HorizontalPodAutoscalers'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <svc.List
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
      <i.List
        {...commonProps}
        title='Ingresses'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <routes.List
        {...commonProps}
        title='Routes'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <ns.List
        {...commonProps}
        title='Namespaces'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <cm.List
        {...commonProps}
        title='ConfigMaps'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <s.List
        {...commonProps}
        title='Secrets'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <sa.List
        {...commonProps}
        title='ServiceAccounts'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <pv.List
        {...commonProps}
        title='PersistentVolumes'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <pvc.List
        {...commonProps}
        title='PersistentVolumeClaims'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <crd.List
        {...commonProps}
        title='CustomResourceDefinitions'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <cr.List
        {...commonProps}
        title='ClusterRoles'
        nameLike={query}
        namespace={selectedNamespace}
      />
      <roles.List
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
      setQuery: redux.actions.setQuery
    },
    dispatch
  );

export const SearchPage = connect(
  mapStateToProps,
  mapDispatchToProps
)(({selectedNamespace, query, setQuery}) => {
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
          onChange={({target: {value}}) => setQuery(value)}
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
