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
import pv from './';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';
import Card from '../components/Card';
import Link from '../components/Link';

const PersistentVolumesDetailPage = ({persistentVolume}) => (
  <ResourceDetailPage
    kind='PersistentVolumes'
    path='persistentvolumes'
    resource={persistentVolume}
    deleteFunction={pv.api.delete}
    body={
      <>
        <Form>
          <metadata.Details resource={persistentVolume} />
          <Form.Field label='Storage Class'>
            {pv.selectors.specStorageClassName(persistentVolume)}
          </Form.Field>
          <Form.Field label='Capacity'>
            {pv.selectors.specCapacityStorage(persistentVolume)}
          </Form.Field>
          <Form.Field label='Status'>
            {pv.selectors.statusPhase(persistentVolume)}
          </Form.Field>
          <Form.Field label='Reclaim Policy'>
            {pv.selectors.specReclaimPolicy(persistentVolume)}
          </Form.Field>
          <Form.Field label='Volume Mode'>
            {pv.selectors.specVolumeMode(persistentVolume)}
          </Form.Field>
        </Form>
        <Card.Title
          className='-mx-3'
          titleVariant={Card.titleVariants.medium}
        >Claim</Card.Title>
        <Form>
          <Form.Field label='Kind'>
            {pv.selectors.specClaimKind(persistentVolume)}
          </Form.Field>
          <Form.Field label='Name'>
            <Link.PersistentVolume to={`/persistentvolumeclaims/${pv.selectors.specClaimUid(persistentVolume)}`}>
              {pv.selectors.specClaimName(persistentVolume)}
            </Link.PersistentVolume>
          </Form.Field>
          <Form.Field label='Namespace'>
            <Link.Namespace to={`/namespaces/${pv.selectors.specClaimNamespace(persistentVolume)}`}>
              {pv.selectors.specClaimNamespace(persistentVolume)}
            </Link.Namespace>
          </Form.Field>
        </Form>
      </>
    }
  />
);

const mapStateToProps = ({persistentVolumes}) => ({
  persistentVolumes
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  persistentVolume: stateProps.persistentVolumes[ownProps.params.uid],
});

export default withParams(connect(mapStateToProps, null, mergeProps)(PersistentVolumesDetailPage));
