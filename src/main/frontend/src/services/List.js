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
import {Type, api, selectors} from './';
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Type',
  'Cluster IP',
  ''
];

const Rows = ({services}) => {
  const deleteService = service => async () => await api.deleteService(service);
  return services.sort(sortByCreationTimeStamp).map(service => (
    <Table.ResourceRow key={uid(service)} resource={service}>
      <Table.Cell>
        <Link.Service to={`/services/${uid(service)}`}>
          {name(service)}
        </Link.Service>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(service)}`}>
          {namespace(service)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>
        <Type service={service} />
      </Table.Cell>
      <Table.Cell>{selectors.specClusterIP(service)}</Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteService(service)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('services')(
  ({resources, loadedResources, crudDelete, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows services={resources} />
    </ResourceList>
  )
);
