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
import {api, selectors} from './';
import metadata from '../metadata';
import {Age, Icon} from '../components';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

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
  return endpoints
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(endpoint => (
      <Table.ResourceRow
        key={metadata.selectors.uid(endpoint)}
        resource={endpoint}
      >
        <Table.Cell>
          <Link.Endpoints to={`/endpoints/${metadata.selectors.uid(endpoint)}`}>
            {metadata.selectors.name(endpoint)}
          </Link.Endpoints>
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.Namespace
            to={`/namespaces/${metadata.selectors.namespace(endpoint)}`}
          >
            {metadata.selectors.namespace(endpoint)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell>{selectors.subsetsAsText(endpoint)}</Table.Cell>
        <Table.Cell>
          <Age date={metadata.selectors.creationTimestamp(endpoint)} />
        </Table.Cell>
        <Table.Cell>
          <Table.DeleteButton onClick={deleteEndpoint(endpoint)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

export const List = ResourceList.resourceListConnect('endpoints')(
  ({resources, loadedResources, crudDelete, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows endpoints={resources} />
    </ResourceList>
  )
);
