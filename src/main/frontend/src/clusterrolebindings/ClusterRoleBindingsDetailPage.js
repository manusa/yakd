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
import crb from './index';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';
import Link from '../components/Link';

const ClusterRoleBindingsDetailPage = ({clusterRoleBinding}) => (
  <ResourceDetailPage
    kind='ClusterRoleBindings'
    path='clusterrolebindings'
    resource={clusterRoleBinding}
    deleteFunction={crb.api.delete}
    body={
      <Form>
        <metadata.Details resource={clusterRoleBinding}/>
        <Form.Field label='Role'>
          <Link.ClusterRole to={`/clusterroles/${crb.selectors.roleRefName(clusterRoleBinding)}`}>
            {crb.selectors.roleRefName(clusterRoleBinding)}
          </Link.ClusterRole>
        </Form.Field>
      </Form>
    }
  />
);

const mapStateToProps = ({clusterRoleBindings}) => ({
  clusterRoleBindings
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  clusterRoleBinding: metadata.selectors.byUidOrName(stateProps.clusterRoleBindings, ownProps.params.uidOrName)
});

export default withParams(connect(mapStateToProps, null, mergeProps)(ClusterRoleBindingsDetailPage));
