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
import {Icon, Link, Tooltip, Table, ResourceListV2} from '../components';
import {api} from './';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Namespace',
  <span>
    <Icon stylePrefix='far' icon='fa-clock' /> Time
  </span>,
  ''
];

const Rows = ({roles}) => {
  const deleteRole = role => async () => await api.deleteRole(role);
  return roles.sort(sortByCreationTimeStamp).map(role => (
    <Table.ResourceRow key={uid(role)} resource={role}>
      <Table.Cell>
        <Link.Role to={`/roles/${uid(role)}`}>{name(role)}</Link.Role>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(role)}`}>
          {namespace(role)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>
        <Tooltip
          content={`${creationTimestamp(role).toLocaleDateString()}
                ${creationTimestamp(role).toLocaleTimeString()}`}
          className='cursor-default'
        >
          <span>{creationTimestamp(role).toLocaleDateString()}</span>
        </Tooltip>
      </Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteRole(role)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ({...properties}) => {
  const resources = useFilteredResources({
    resource: 'roles',
    filters: {...properties}
  });
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows roles={resources} />
    </ResourceListV2>
  );
};
