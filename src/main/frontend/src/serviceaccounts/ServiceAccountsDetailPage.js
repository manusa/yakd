/*
 * Copyright 2023 Marc Nuri
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
import {api, selectors} from './';
import {Details, namespace} from '../metadata';
import {Card, Form} from '../components';
import ResourceDetailPage from '../components/ResourceDetailPage';
import secrets from '../secrets';

const mapStateToProps = ({serviceAccounts}) => ({
  serviceAccounts
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  serviceAccount: stateProps.serviceAccounts[ownProps.params.uid]
});

export const ServiceAccountsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({serviceAccount}) => (
    <ResourceDetailPage
      kind='ServiceAccounts'
      path='serviceaccounts'
      resource={serviceAccount}
      deleteFunction={api.deleteSa}
      body={
        <Form>
          <Details resource={serviceAccount} />
        </Form>
      }
    >
      <secrets.List
        title='Secrets'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        namespace={namespace(serviceAccount)}
        names={selectors.secretsNames(serviceAccount)}
      />
      <secrets.List
        title='Image Pull Secrets'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        namespace={namespace(serviceAccount)}
        names={selectors.imagePullSecretsNames(serviceAccount)}
      />
    </ResourceDetailPage>
  ))
);
