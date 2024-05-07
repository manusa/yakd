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
import {Details, uid} from '../metadata';
import {ContainerList} from '../containers';
import pods from '../pods';
import {ReplicasField} from '../replicasets';
import {Card, Form, Icon, Link} from '../components';
import {ResourceDetailPage} from '../dashboard';
import {api, selectors} from './';

const mapStateToProps = ({statefulSets}) => ({
  statefulSets
});

const mergeProps = ({statefulSets}, dispatchProps, ownProps) => ({
  statefulSet: statefulSets[ownProps.params.uid]
});

export const StatefulSetsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({statefulSet}) => (
    <ResourceDetailPage
      kind='StatefulSets'
      path='statefulsets'
      resource={statefulSet}
      isReadyFunction={selectors.isReady}
      deleteFunction={api.deleteSts}
      actions={
        <Link
          className='ml-2'
          size={Link.sizes.small}
          variant={Link.variants.outline}
          onClick={() => api.restart(statefulSet)}
          title='Restart'
        >
          <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2' />
          Restart
        </Link>
      }
      body={
        <Form>
          <Details resource={statefulSet} />
          <ReplicasField
            resource={statefulSet}
            replicas={selectors.specReplicas(statefulSet)}
            updateReplicas={api.updateReplicas}
          />
        </Form>
      }
    >
      <ContainerList
        title='Containers'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        containers={selectors.containers(statefulSet)}
      />
      <pods.List
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(statefulSet)}
      />
    </ResourceDetailPage>
  ))
);
