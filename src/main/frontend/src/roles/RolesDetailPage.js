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
import {Details} from '../metadata';
import {RuleList} from '../clusterroles';
import {api, selectors} from './';
import {Form, ResourceDetailPage} from '../components';

const mapStateToProps = ({roles}) => ({
  roles
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  role: stateProps.roles[ownProps.params.uid]
});

export const RolesDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({role}) => (
    <ResourceDetailPage
      kind='Roles'
      path='roles'
      resource={role}
      deleteFunction={api.deleteRole}
      body={
        <Form>
          <Details resource={role} />
        </Form>
      }
    >
      <RuleList className='mt-2' rules={selectors.rules(role)} />
    </ResourceDetailPage>
  ))
);
