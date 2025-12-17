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
import {PodsList} from '../pods';
import {Card, Form} from '../components';
import {ResourceDetailPage} from '../dashboard';
import {api, selectors} from './';

const mapStateToProps = ({replicationControllers}) => ({
  replicationControllers
});

const mergeProps = (
  {replicationControllers},
  dispatchProps,
  {params: {uid}}
) => ({
  replicationController: replicationControllers[uid]
});

export const ReplicationControllersDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({replicationController}) => (
    <ResourceDetailPage
      kind='ReplicationControllers'
      path='replicationcontrollers'
      resource={replicationController}
      isReadyFunction={selectors.isReady}
      deleteFunction={api.deleteRc}
      body={
        <Form>
          <Details resource={replicationController} />
        </Form>
      }
    >
      <ContainerList
        title='Containers'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        containers={selectors.containers(replicationController)}
      />
      <PodsList
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(replicationController)}
      />
    </ResourceDetailPage>
  ))
);
