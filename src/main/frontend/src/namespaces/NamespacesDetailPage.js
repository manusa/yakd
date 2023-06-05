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
import ns from './';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';

const NamespacesDetailPage = ({namespace}) => (
  <ResourceDetailPage
    kind='Namespaces'
    path='namespaces'
    resource={namespace}
    isReadyFunction={ns.selectors.isReady}
    deleteFunction={ns.api.delete}
    editable={false}
    body={
      <Form>
        <metadata.Details resource={namespace} />
        <Form.Field label='Status'>
          {ns.selectors.statusPhase(namespace)}
        </Form.Field>
      </Form>
    } />
);

const mapStateToProps = ({namespaces}) => ({
  namespaces
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  namespace: metadata.selectors.byUidOrName(stateProps.namespaces, ownProps.params.uidOrName)
});

export default withParams(connect(mapStateToProps, null, mergeProps)(NamespacesDetailPage));
