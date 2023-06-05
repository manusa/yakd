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
import cm from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Namespace',
  ''
];

const Rows = ({configMaps}) => {
  const deleteConfigMap = configMap => async () => await cm.api.delete(configMap);
  return configMaps
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(configMap => (
        <Table.ResourceRow key={metadata.selectors.uid(configMap)} resource={configMap}>
          <Table.Cell>
            <Link.ConfigMap to={`/configmaps/${metadata.selectors.uid(configMap)}`}>
              {metadata.selectors.name(configMap)}
            </Link.ConfigMap>
          </Table.Cell>
          <Table.Cell className='whitespace-no-wrap'>
            <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(configMap)}`}>
              {metadata.selectors.namespace(configMap)}
            </Link.Namespace>
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteConfigMap(configMap)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows configMaps={resources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('configMaps')(List);

