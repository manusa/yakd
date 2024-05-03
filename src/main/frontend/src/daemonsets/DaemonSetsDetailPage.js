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
import {api, selectors} from './';
import pods from '../pods';
import {Card, Form, Icon, Link} from '../components';
import ResourceDetailPage from '../components/ResourceDetailPage';

const mapStateToProps = ({daemonSets}) => ({
  daemonSets
});

const mergeProps = ({daemonSets}, dispatchProps, {params: {uid}}) => ({
  daemonSet: daemonSets[uid]
});

export const DaemonSetsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({daemonSet}) => (
    <ResourceDetailPage
      kind='DaemonSets'
      path='daemonsets'
      resource={daemonSet}
      isReadyFunction={selectors.isReady}
      deleteFunction={api.deleteDs}
      actions={
        <Link
          className='ml-2'
          size={Link.sizes.small}
          variant={Link.variants.outline}
          onClick={() => api.restart(daemonSet)}
          title='Restart'
        >
          <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2' />
          Restart
        </Link>
      }
      body={
        <Form>
          <Details resource={daemonSet} />
          <Form.Field label='Update Strategy'>
            {selectors.specUpdateStrategyType(daemonSet)}
          </Form.Field>
        </Form>
      }
    >
      <ContainerList
        title='Containers'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        containers={selectors.containers(daemonSet)}
      />
      <pods.List
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(daemonSet)}
      />
    </ResourceDetailPage>
  ))
);
