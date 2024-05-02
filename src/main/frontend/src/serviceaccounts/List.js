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
import {
  KeyValueList,
  creationTimestamp,
  labels,
  name,
  namespace,
  sortByCreationTimeStamp,
  uid
} from '../metadata';
import {api} from './';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';
import {Age, Icon} from '../components';

const headers = [
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Namespace',
  <span>
    <Icon icon='fa-tags' /> Labels
  </span>,
  'Age',
  ''
];

const Rows = ({serviceAccounts}) => {
  const deleteSa = serviceAccount => async () =>
    await api.deleteSa(serviceAccount);
  return serviceAccounts.sort(sortByCreationTimeStamp).map(serviceAccount => (
    <Table.ResourceRow key={uid(serviceAccount)} resource={serviceAccount}>
      <Table.Cell className='whitespace-nowrap'>
        <Link.ServiceAccount to={`/serviceaccounts/${uid(serviceAccount)}`}>
          {name(serviceAccount)}
        </Link.ServiceAccount>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(serviceAccount)}`}>
          {namespace(serviceAccount)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>
        <KeyValueList keyValues={labels(serviceAccount)} maxEntries={2} />
      </Table.Cell>
      <Table.Cell>
        <Age date={creationTimestamp(serviceAccount)} />
      </Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteSa(serviceAccount)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('serviceAccounts')(
  ({resources, crudDelete, loadedResources, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows serviceAccounts={resources} loadedResources={loadedResources} />
    </ResourceList>
  )
);
