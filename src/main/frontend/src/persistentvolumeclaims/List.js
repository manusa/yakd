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
import pvc from './';
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
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
    .sort(sortByCreationTimeStamp)
    .map(persistentVolumeClaim => (
      <Table.ResourceRow
        key={uid(persistentVolumeClaim)}
        resource={persistentVolumeClaim}
      >
        <Table.Cell>
          <Link.PersistentVolumeClaim
            to={`/persistentvolumeclaims/${uid(persistentVolumeClaim)}`}
          >
            {name(persistentVolumeClaim)}
          </Link.PersistentVolumeClaim>
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.Namespace
            to={`/namespaces/${namespace(persistentVolumeClaim)}`}
          >
            {namespace(persistentVolumeClaim)}
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
          <Table.DeleteButton
            onClick={deletePersistentVolumeClaim(persistentVolumeClaim)}
          />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList
    headers={headers}
    resources={resources}
    loading={!loadedResources['PersistentVolumeClaim']}
    {...properties}
  >
    <Rows persistentVolumeClaims={resources} crudDelete={crudDelete} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('persistentVolumeClaims')(List);
