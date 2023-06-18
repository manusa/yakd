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
import {api, selectors, RuleList} from './';
import {List as CrbList} from '../clusterrolebindings';
import {Card, Form} from '../components';
import ResourceDetailPage from '../components/ResourceDetailPage';

const mapStateToProps = ({clusterRoles}) => ({
  clusterRoles
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  clusterRole: metadata.selectors.byUidOrName(
    stateProps.clusterRoles,
    ownProps.params.uidOrName
  )
});

export const ClusterRolesDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({clusterRole}) => (
    <ResourceDetailPage
      kind='ClusterRoles'
      path='clusterroles'
      resource={clusterRole}
      deleteFunction={api.deleteCr}
      body={
        <Form>
          <metadata.Details resource={clusterRole} />
        </Form>
      }
    >
      <RuleList className='mt-2' rules={selectors.rules(clusterRole)} />
      <CrbList
        title='Bindings'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        roleRefName={metadata.selectors.name(clusterRole)}
      />
    </ResourceDetailPage>
  ))
);
