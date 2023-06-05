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
import d from './';
import pods from '../pods';
import rs from '../replicasets';
import Card from '../components/Card';
import Form from '../components/Form';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceDetailPage from '../components/ResourceDetailPage';

const DeploymentsDetailPage = ({deployment, replicaSetsUids}) => (
  <ResourceDetailPage
    kind='Deployments'
    path='deployments'
    resource={deployment}
    isReadyFunction={d.selectors.isReady}
    deleteFunction={d.api.delete}
    actions={
      <Link
        className='ml-2'
        size={Link.sizes.small}
        variant={Link.variants.outline}
        onClick={() => d.api.restart(deployment)}
        title='Restart'
      >
        <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2'/>
        Restart
      </Link>
    }
    body={
      <Form>
        <metadata.Details resource={deployment} />
        <rs.ReplicasField
          resource={deployment}
          replicas={d.selectors.specReplicas(deployment)}
          updateReplicas={d.api.updateReplicas}
        />
        <Form.Field label='Strategy'>{d.selectors.specStrategyType(deployment)}</Form.Field>
      </Form>
    }
  >
    <cnt.ContainerList
      title='Containers'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      containers={d.selectors.containers(deployment)} />
    <rs.List
      title='Replica Sets'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUid={metadata.selectors.uid(deployment)} />
    <pods.List
      title='Pods'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUids={replicaSetsUids} />
  </ResourceDetailPage>
);

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
    .filter(replicaSet => metadata.selectors.ownerReferencesUids(replicaSet)
      .includes(metadata.selectors.uid(stateProps.deployments[ownProps.params.uid])))
    .map(replicaSet => metadata.selectors.uid(replicaSet))
});

export default withParams(connect(mapStateToProps, null, mergeProps)(DeploymentsDetailPage));
