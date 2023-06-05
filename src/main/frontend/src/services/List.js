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
import svc from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Namespace',
  'Type',
  'Cluster IP',
  ''
];

const Rows = ({services}) => {
  const deleteService = service => async () => await svc.api.delete(service);
  return services
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(service => (
        <Table.ResourceRow key={metadata.selectors.uid(service)} resource={service}>
          <Table.Cell>
            <Link.Service to={`/services/${metadata.selectors.uid(service)}`}>
              {metadata.selectors.name(service)}
            </Link.Service>
          </Table.Cell>
          <Table.Cell className='whitespace-no-wrap'>
            <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(service)}`}>
              {metadata.selectors.namespace(service)}
            </Link.Namespace>
          </Table.Cell>
          <Table.Cell>
            <svc.Type service={service} />
          </Table.Cell>
          <Table.Cell>
            {svc.selectors.specClusterIP(service)}
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteService(service)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows services={resources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('services')(List);

