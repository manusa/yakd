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
import {
  creationTimestamp,
  name,
  namespace,
  sortByCreationTimeStamp,
  uid
} from '../metadata';
import {useFilteredResources} from '../redux';
import {Age, Icon, Link, ResourceListV2, Table} from '../components';
import {api, selectors} from './';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Endpoints',
  'Age',
  ''
];

const Rows = ({endpoints}) => {
  const deleteEndpoint = endpoint => async () =>
    await api.deleteEndpoint(endpoint);
  return endpoints.sort(sortByCreationTimeStamp).map(endpoint => (
    <Table.ResourceRow key={uid(endpoint)} resource={endpoint}>
      <Table.Cell>
        <Link.Endpoints to={`/endpoints/${uid(endpoint)}`}>
          {name(endpoint)}
        </Link.Endpoints>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(endpoint)}`}>
          {namespace(endpoint)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>{selectors.subsetsAsText(endpoint)}</Table.Cell>
      <Table.Cell>
        <Age date={creationTimestamp(endpoint)} />
      </Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteEndpoint(endpoint)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ({...properties}) => {
  const resources = useFilteredResources({
    resource: 'endpoints',
    filters: {...properties}
  });
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows endpoints={resources} />
    </ResourceListV2>
  );
};
