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
  <span key='name'>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Role',
  <span key='age'>
    <Icon stylePrefix='far' icon='fa-clock' /> Age
  </span>,
  ''
];

const Rows = ({roleBindings}) => {
  const deleteRb = roleBinding => () => api.deleteRb(roleBinding);
  return roleBindings.sort(sortByCreationTimeStamp).map(roleBinding => (
    <Table.ResourceRow key={uid(roleBinding)} resource={roleBinding}>
      <Table.Cell>
        <Link.RoleBinding to={`/rolebindings/${uid(roleBinding)}`}>
          {name(roleBinding)}
        </Link.RoleBinding>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(roleBinding)}`}>
          {namespace(roleBinding)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>
        <Link.Role to={`/roles/${selectors.roleRefName(roleBinding)}`}>
          {selectors.roleRefName(roleBinding)}
        </Link.Role>
      </Table.Cell>
      <Table.Cell>
        <Age date={creationTimestamp(roleBinding)} />
      </Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteRb(roleBinding)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ({...properties}) => {
  const filteredResources = useFilteredResources({
    resource: 'roleBindings',
    filters: {...properties}
  });
  const resources = Object.values(
    selectors.roleBindingsBy(filteredResources, properties)
  );
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows roleBindings={resources} />
    </ResourceListV2>
  );
};
