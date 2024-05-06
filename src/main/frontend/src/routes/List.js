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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import {Host, api, selectors} from './';
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = [
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Hosts',
  'Paths',
  ''
];

const Rows = ({routes}) => {
  const deleteRoute = route => () => api.deleteRoute(route);
  return routes.sort(sortByCreationTimeStamp).map(route => (
    <Table.ResourceRow key={uid(route)} resource={route}>
      <Table.Cell>
        <Link.Route to={`/routes/${uid(route)}`}>{name(route)}</Link.Route>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(route)}`}>
          {namespace(route)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>
        <Host route={route} />
      </Table.Cell>
      <Table.Cell>{selectors.specPath(route)}</Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteRoute(route)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('routes')(
  ({resources, crudDelete, loadedResources, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows routes={resources} />
    </ResourceList>
  )
);
