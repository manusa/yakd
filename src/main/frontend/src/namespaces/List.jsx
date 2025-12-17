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
import {api, selectors} from './';

const headers = [
  '',
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Phase',
  <span>
    <Icon icon='fa-tags' /> Labels
  </span>,
  ''
];

const Rows = ({namespaces}) => {
  const deleteNamespace = namespace => () => api.deleteNs(namespace);
  return namespaces.sort(sortByCreationTimeStamp).map(namespace => (
    <Table.ResourceRow key={uid(namespace)} resource={namespace}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isReady(namespace) ? 'text-green-500' : 'text-red-500'
          }
          icon={
            selectors.isReady(namespace) ? 'fa-check' : 'fa-exclamation-circle'
          }
        />
      </Table.Cell>
      <Table.Cell className='text-nowrap'>
        <Link.Namespace to={`/namespaces/${uid(namespace)}`}>
          {name(namespace)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell className='text-nowrap'>
        {selectors.statusPhase(namespace)}
      </Table.Cell>
      <Table.Cell>
        <KeyValueList keyValues={labels(namespace)} maxEntries={2} />
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Table.DeleteButton onClick={deleteNamespace(namespace)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ({...properties}) => {
  const resources = useFilteredResources({
    resource: 'namespaces',
    filters: {...properties}
  });
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows namespaces={resources} />
    </ResourceListV2>
  );
};
