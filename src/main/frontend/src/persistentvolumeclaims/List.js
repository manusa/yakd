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
import pvc from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Namespace',
  'Storage Class',
  'Capacity',
  'Status',
  ''
];

const Rows = ({persistentVolumeClaims, crudDelete}) => {
  const deletePersistentVolumeClaim = persistentVolumeClaim => async () => {
    await pvc.api.delete(persistentVolumeClaim);
    crudDelete(persistentVolumeClaim);
  };
  return persistentVolumeClaims
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(persistentVolumeClaim => (
        <Table.ResourceRow key={metadata.selectors.uid(persistentVolumeClaim)} resource={persistentVolumeClaim}>
          <Table.Cell>
            <Link.PersistentVolumeClaim to={`/persistentvolumeclaims/${metadata.selectors.uid(persistentVolumeClaim)}`}>
              {metadata.selectors.name(persistentVolumeClaim)}
            </Link.PersistentVolumeClaim>
          </Table.Cell>
          <Table.Cell className='whitespace-no-wrap'>
            <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(persistentVolumeClaim)}`}>
              {metadata.selectors.namespace(persistentVolumeClaim)}
            </Link.Namespace>
          </Table.Cell>
          <Table.Cell>
            {pvc.selectors.specStorageClassName(persistentVolumeClaim)}
          </Table.Cell>
          <Table.Cell>
            {pvc.selectors.statusCapacityStorage(persistentVolumeClaim)}
          </Table.Cell>
          <Table.Cell>
            {pvc.selectors.statusPhase(persistentVolumeClaim)}
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deletePersistentVolumeClaim(persistentVolumeClaim)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} loading={!loadedResources['PersistentVolumeClaim']} {...properties}>
    <Rows persistentVolumeClaims={resources} crudDelete={crudDelete} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('persistentVolumeClaims')(List);
