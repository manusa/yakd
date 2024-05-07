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
import {Form} from '../components';
import {ResourceDetailPage} from '../dashboard';
import {Host, api, selectors} from './';

const mapStateToProps = ({routes}) => ({
  routes
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  route: stateProps.routes[ownProps.params.uid]
});

export const RoutesDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({route}) => (
    <ResourceDetailPage
      kind='Routes'
      path='routes'
      resource={route}
      deleteFunction={api.deleteRoute}
      body={
        <Form>
          <Details resource={route} />
          <Form.Field label='Host'>
            <Host route={route} />
          </Form.Field>
          <Form.Field label='Path'>{selectors.specPath(route)}</Form.Field>
        </Form>
      }
    />
  ))
);
