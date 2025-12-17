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
import {Details, ownerReferencesUids, uid} from '../metadata';
import {ContainerList} from '../containers';
import {api, selectors} from './';
import {PodsList} from '../pods';
import {ReplicasField} from '../replicasets';
import {ReplicationControllersList} from '../replicationcontrollers';
import {Card, Form, Icon, Link} from '../components';
import {ResourceDetailPage} from '../dashboard';

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
    .filter(replicationController =>
      ownerReferencesUids(replicationController).includes(
        uid(stateProps.deploymentConfigs[ownProps.params.uid])
      )
    )
    .map(replicationController => uid(replicationController))
});

export const DeploymentConfigsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({deploymentConfig, replicationControllersUids}) => (
    <ResourceDetailPage
      kind='DeploymentConfigs'
      path='deploymentconfigs'
      resource={deploymentConfig}
      isReadyFunction={selectors.isReady}
      deleteFunction={api.deleteDc}
      actions={
        <Link
          className='ml-2'
          size={Link.sizes.small}
          variant={Link.variants.outline}
          onClick={() => api.restart(deploymentConfig)}
          title='Restart'
        >
          <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2' />
          Restart
        </Link>
      }
      body={
        <Form>
          <Details resource={deploymentConfig} />
          <ReplicasField
            resource={deploymentConfig}
            replicas={selectors.specReplicas(deploymentConfig)}
            updateReplicas={api.updateReplicas}
          />
          <Form.Field label='Strategy'>
            {selectors.specStrategyType(deploymentConfig)}
          </Form.Field>
        </Form>
      }
    >
      <ContainerList
        title='Containers'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        containers={selectors.containers(deploymentConfig)}
      />
      <ReplicationControllersList
        title='Replication Controller'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(deploymentConfig)}
      />
      <PodsList
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUids={replicationControllersUids}
      />
    </ResourceDetailPage>
  ))
);
