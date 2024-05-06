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
import pods from '../pods';
import {ReplicasField, ReplicaSetsList} from '../replicasets';
import {Card, Form, Icon, Link, ResourceDetailPage} from '../components';

const mapStateToProps = ({deployments, replicaSets}) => ({
  deployments,
  replicaSets
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  deployment: stateProps.deployments[ownProps.params.uid],
  replicaSetsUids: Object.values(stateProps.replicaSets)
    .filter(replicaSet =>
      ownerReferencesUids(replicaSet).includes(
        uid(stateProps.deployments[ownProps.params.uid])
      )
    )
    .map(replicaSet => uid(replicaSet))
});

export const DeploymentsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({deployment, replicaSetsUids}) => (
    <ResourceDetailPage
      kind='Deployments'
      path='deployments'
      resource={deployment}
      isReadyFunction={selectors.isReady}
      deleteFunction={api.deleteDeployment}
      actions={
        <Link
          className='ml-2'
          size={Link.sizes.small}
          variant={Link.variants.outline}
          onClick={() => api.restart(deployment)}
          title='Restart'
        >
          <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2' />
          Restart
        </Link>
      }
      body={
        <Form>
          <Details resource={deployment} />
          <ReplicasField
            resource={deployment}
            replicas={selectors.specReplicas(deployment)}
            updateReplicas={api.updateReplicas}
          />
          <Form.Field label='Strategy'>
            {selectors.specStrategyType(deployment)}
          </Form.Field>
        </Form>
      }
    >
      <ContainerList
        title='Containers'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        containers={selectors.containers(deployment)}
      />
      <ReplicaSetsList
        title='Replica Sets'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(deployment)}
      />
      <pods.List
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUids={replicaSetsUids}
      />
    </ResourceDetailPage>
  ))
);
