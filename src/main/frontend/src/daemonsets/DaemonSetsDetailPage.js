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
import ds from './';
import pods from '../pods';
import Card from '../components/Card';
import Form from '../components/Form';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceDetailPage from '../components/ResourceDetailPage';

const DaemonSetsDetailPage = ({daemonSet}) => (
  <ResourceDetailPage
    kind='DaemonSets'
    path='daemonsets'
    resource={daemonSet}
    isReadyFunction={ds.selectors.isReady}
    deleteFunction={ds.api.delete}
    actions={
      <Link
        className='ml-2'
        size={Link.sizes.small}
        variant={Link.variants.outline}
        onClick={() => ds.api.restart(daemonSet)}
        title='Restart'
      >
        <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2'/>
        Restart
      </Link>
    }
    body={
      <Form>
        <metadata.Details resource={daemonSet} />
        <Form.Field label='Update Strategy'>{ds.selectors.specUpdateStrategyType(daemonSet)}</Form.Field>
      </Form>
    }
  >
    <cnt.ContainerList
      title='Containers'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      containers={ds.selectors.containers(daemonSet)} />
    <pods.List
      title='Pods'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUid={metadata.selectors.uid(daemonSet)} />
  </ResourceDetailPage>
);

const mapStateToProps = ({daemonSets}) => ({
  daemonSets
});

const mergeProps = ({daemonSets}, dispatchProps, {params: {uid}}) => ({
  daemonSet: daemonSets[uid]
});

export default withParams(connect(mapStateToProps, null, mergeProps)(DaemonSetsDetailPage));
