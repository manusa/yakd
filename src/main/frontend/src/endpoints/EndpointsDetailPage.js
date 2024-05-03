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
import React, {Fragment} from 'react';
import {connect} from 'react-redux';
import {withParams} from '../router';
import {api, selectors} from './';
import {Details} from '../metadata';
import {Card, Form, Link} from '../components';
import ResourceDetailPage from '../components/ResourceDetailPage';
import Table from '../components/Table';

const mapStateToProps = ({endpoints}) => ({
  endpoints
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  endpoint: stateProps.endpoints[ownProps.params.uid]
});

const Target = ({targetRef}) => {
  const Component = Link[targetRef.kind] ?? Fragment;
  return (
    <Component to={`/${targetRef.kind.toLowerCase()}s/${targetRef.uid}`}>
      {targetRef.name}
    </Component>
  );
};

const Addresses = ({addresses = []}) => (
  <>
    <Card.Title titleVariant={Card.titleVariants.medium}>Addresses</Card.Title>
    <table className='min-w-full'>
      <Table.Head columns={['IP', 'Hostname', 'Target']} />
      {addresses.map(a => (
        <Table.Row key={a.ip}>
          <Table.Cell>{a.ip}</Table.Cell>
          <Table.Cell>{a.hostname}</Table.Cell>
          <Table.Cell>
            <Target targetRef={a.targetRef} />
          </Table.Cell>
        </Table.Row>
      ))}
    </table>
  </>
);

const Ports = ({ports = []}) => (
  <>
    <Card.Title
      className='border-t border-blue-700/25'
      titleVariant={Card.titleVariants.medium}
    >
      Ports
    </Card.Title>
    <table className='min-w-full'>
      <Table.Head columns={['Port', 'Name', 'Protocol']} />
      {ports.map(p => (
        <Table.Row key={p.port}>
          <Table.Cell>{p.port}</Table.Cell>
          <Table.Cell>{p.name}</Table.Cell>
          <Table.Cell>{p.protocol}</Table.Cell>
        </Table.Row>
      ))}
    </table>
  </>
);

const Subsets = ({subsets}) => (
  <Card className='mt-2'>
    <Card.Title>Subsets</Card.Title>
    {subsets.map((subset, idx) => (
      <div key={idx}>
        <Addresses addresses={subset.addresses} />
        <Ports ports={subset.ports} />
      </div>
    ))}
  </Card>
);

export const EndpointsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({endpoint}) => (
    <ResourceDetailPage
      kind='Endpoints'
      path='endpoints'
      resource={endpoint}
      deleteFunction={api.deleteEndpoint}
      editable={false}
      body={
        <Form>
          <Details resource={endpoint} />
        </Form>
      }
    >
      <Subsets subsets={selectors.subsets(endpoint)} />
    </ResourceDetailPage>
  ))
);
