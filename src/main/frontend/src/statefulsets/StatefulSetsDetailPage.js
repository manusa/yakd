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
import sts from './';
import pods from '../pods';
import rs from '../replicasets';
import Card from '../components/Card';
import Form from '../components/Form';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceDetailPage from '../components/ResourceDetailPage';

const StatefulSetsDetailPage = ({statefulSet}) => (
  <ResourceDetailPage
    kind='StatefulSets'
    path='statefulsets'
    resource={statefulSet}
    isReadyFunction={sts.selectors.isReady}
    deleteFunction={sts.api.delete}
    actions={
      <Link
        className='ml-2'
        size={Link.sizes.small}
        variant={Link.variants.outline}
        onClick={() => sts.api.restart(statefulSet)}
        title='Restart'
      >
        <Icon stylePrefix='fas' icon='fa-redo-alt' className='mr-2'/>
        Restart
      </Link>
    }
    body={
      <Form>
        <metadata.Details resource={statefulSet} />
        <rs.ReplicasField
          resource={statefulSet}
          replicas={sts.selectors.specReplicas(statefulSet)}
          updateReplicas={sts.api.updateReplicas}
        />
      </Form>
    }
  >
    <cnt.ContainerList
      title='Containers'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      containers={sts.selectors.containers(statefulSet)} />
    <pods.List
      title='Pods'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUid={metadata.selectors.uid(statefulSet)} />
  </ResourceDetailPage>
);

const mapStateToProps = ({statefulSets}) => ({
  statefulSets,
});

const mergeProps = ({statefulSets}, dispatchProps, ownProps) => ({
  statefulSet: statefulSets[ownProps.params.uid],
});

export default withParams(connect(mapStateToProps, null, mergeProps)(StatefulSetsDetailPage));
