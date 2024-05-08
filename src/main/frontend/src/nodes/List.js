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
  KeyValueList,
  labels,
  name,
  sortByCreationTimeStamp,
  uid
} from '../metadata';
import {useFilteredResources} from '../redux';
import {Icon, Link, ResourceListV2, Table} from '../components';
import {selectors} from './';

const headers = [
  '',
  <span className='whitespace-nowrap'>
    <Icon icon='fa-id-card' /> Name
  </span>,
  <span className='whitespace-nowrap'>
    <Icon icon='fa-server' /> Roles
  </span>,
  <span>
    <Icon icon='fa-tags' /> Labels
  </span>
];

const Rows = ({nodes}) => {
  return nodes.sort(sortByCreationTimeStamp).map(node => (
    <Table.ResourceRow key={uid(node)} resource={node}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isReady(node) ? 'text-green-500' : 'text-red-500'
          }
          icon={selectors.isReady(node) ? 'fa-check' : 'fa-exclamation-circle'}
        />
      </Table.Cell>
      <Table.Cell className='text-nowrap'>
        <Link.Node to={`/nodes/${name(node)}`}>{name(node)}</Link.Node>
      </Table.Cell>
      <Table.Cell className='text-nowrap'>
        {(roles => {
          if (roles.length === 0) {
            return '<none>';
          }
          return roles.map((role, idx) => <div key={idx}>{role}</div>);
        })(selectors.roles(node))}
      </Table.Cell>
      <Table.Cell>
        <KeyValueList keyValues={labels(node)} maxEntries={2} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ({...properties}) => {
  const resources = useFilteredResources({
    resource: 'nodes',
    filters: {...properties}
  });
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows nodes={resources} />
    </ResourceListV2>
  );
};
