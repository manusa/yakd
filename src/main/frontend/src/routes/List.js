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
import metadata from '../metadata';
import r from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  'Hosts',
  'Paths',
  ''
];

const Rows = ({routes}) => {
  const deleteRoute = route => () => r.api.delete(route);
  return routes
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(route => (
        <Table.ResourceRow key={metadata.selectors.uid(route)} resource={route}>
          <Table.Cell>
            <Link.Route to={`/routes/${metadata.selectors.uid(route)}`}>
              {metadata.selectors.name(route)}
            </Link.Route>
          </Table.Cell>
          <Table.Cell className='whitespace-no-wrap'>
            <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(route)}`}>
              {metadata.selectors.namespace(route)}
            </Link.Namespace>
          </Table.Cell>
          <Table.Cell>
            <r.Host route={route} />
          </Table.Cell>
          <Table.Cell>
            {r.selectors.specPath(route)}
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteRoute(route)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows routes={resources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('routes')(List);

