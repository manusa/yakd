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
import {connect} from 'react-redux';
import {withParams} from '../router';
import metadata from '../metadata';
import cnt from '../containers';
import dc from './';
import pods from '../pods';
import rs from '../replicasets';
import rc from '../replicationcontrollers';
import Card from '../components/Card';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';
import Link from '../components/Link';
import Icon from '../components/Icon';

const DeploymentConfigsDetailPage = ({deploymentConfig, replicationControllersUids}) => (
  <ResourceDetailPage
    kind='DeploymentConfigs'
    path='deploymentconfigs'
    resource={deploymentConfig}
    isReadyFunction={dc.selectors.isReady}
    deleteFunction={dc.api.delete}
    actions={
      <Link
        className='ml-2'
        size={Link.sizes.small}
        variant={Link.variants.outline}
        onClick={() => dc.api.restart(deploymentConfig)}
        title='Restart'
      >
        <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2'/>
        Restart
      </Link>
    }
    body={
      <Form>
        <metadata.Details resource={deploymentConfig} />
        <rs.ReplicasField
          resource={deploymentConfig}
          replicas={dc.selectors.specReplicas(deploymentConfig)}
          updateReplicas={dc.api.updateReplicas}
        />
        <Form.Field label='Strategy'>{dc.selectors.specStrategyType(deploymentConfig)}</Form.Field>
      </Form>
    }
  >
    <cnt.ContainerList
      title='Containers'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      containers={dc.selectors.containers(deploymentConfig)} />
    <rc.List
      title='Replication Controller'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUid={metadata.selectors.uid(deploymentConfig)} />
    <pods.List
      title='Pods'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUids={replicationControllersUids} />
  </ResourceDetailPage>
);

const mapStateToProps = ({deploymentConfigs, replicationControllers}) => ({
  deploymentConfigs,
  replicationControllers
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  deploymentConfig: stateProps.deploymentConfigs[ownProps.params.uid],
  replicationControllersUids: Object.values(stateProps.replicationControllers)
    .filter(replicationController => metadata.selectors.ownerReferencesUids(replicationController)
      .includes(metadata.selectors.uid(stateProps.deploymentConfigs[ownProps.params.uid])))
    .map(replicationController => metadata.selectors.uid(replicationController))
});

export default withParams(connect(mapStateToProps, null, mergeProps)(DeploymentConfigsDetailPage));
