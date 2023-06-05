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
import Tooltip from '../components/Tooltip';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Namespace',
  <span><Icon stylePrefix='far' icon='fa-clock' /> Time</span>,
  ''
];

const Rows = ({roles}) => {
  const deleteRole = role => async () => await r.api.delete(role);
  return roles
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(role => (
        <Table.ResourceRow key={metadata.selectors.uid(role)} resource={role}>
          <Table.Cell>
            <Link.Role to={`/roles/${metadata.selectors.uid(role)}`}>
              {metadata.selectors.name(role)}
            </Link.Role>
          </Table.Cell>
          <Table.Cell className='whitespace-no-wrap'>
            <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(role)}`}>
              {metadata.selectors.namespace(role)}
            </Link.Namespace>
          </Table.Cell>
          <Table.Cell>
            <Tooltip
              content={`${metadata.selectors.creationTimestamp(role).toLocaleDateString()}
                ${metadata.selectors.creationTimestamp(role).toLocaleTimeString()}`}
              className='cursor-default'
            >
              <span>{metadata.selectors.creationTimestamp(role).toLocaleDateString()}</span>
            </Tooltip>
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteRole(role)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows roles={resources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('roles')(List);

