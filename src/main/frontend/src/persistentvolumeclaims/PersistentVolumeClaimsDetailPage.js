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
import pvc from './';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';

const PersistentVolumeClaimsDetailPage = ({persistentVolumeClaim}) => (
  <ResourceDetailPage
    kind='PersistentVolumeClaims'
    path='persistentvolumeclaims'
    resource={persistentVolumeClaim}
    deleteFunction={pvc.api.delete}
    body={
      <>
        <Form>
          <metadata.Details resource={persistentVolumeClaim} />
          <Form.Field label='Access Modes'>
            {pvc.selectors.specAccessModes(persistentVolumeClaim).map((am, idx) => (
              <p key={idx}>{am}</p>
            ))}
          </Form.Field>
          <Form.Field label='Storage Class'>
            {pvc.selectors.specStorageClassName(persistentVolumeClaim)}
          </Form.Field>
          <Form.Field label='Capacity'>
            {pvc.selectors.statusCapacityStorage(persistentVolumeClaim)}
          </Form.Field>
          <Form.Field label='Status'>
            {pvc.selectors.statusPhase(persistentVolumeClaim)}
          </Form.Field>
        </Form>
      </>
    }
  />
);

const mapStateToProps = ({persistentVolumeClaims}) => ({
  persistentVolumeClaims
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  persistentVolumeClaim: stateProps.persistentVolumeClaims[ownProps.params.uid],
});

export default withParams(connect(mapStateToProps, null, mergeProps)(PersistentVolumeClaimsDetailPage));
