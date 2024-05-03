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
import {api} from './';
import {Icon, Link} from '../components';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Namespace',
  ''
];

const Rows = ({configMaps}) => {
  const deleteConfigMap = configMap => async () =>
    await api.deleteCm(configMap);
  return configMaps.sort(sortByCreationTimeStamp).map(configMap => (
    <Table.ResourceRow key={uid(configMap)} resource={configMap}>
      <Table.Cell>
        <Link.ConfigMap to={`/configmaps/${uid(configMap)}`}>
          {name(configMap)}
        </Link.ConfigMap>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(configMap)}`}>
          {namespace(configMap)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteConfigMap(configMap)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('configMaps')(
  ({resources, loadedResources, crudDelete, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows configMaps={resources} />
    </ResourceList>
  )
);
