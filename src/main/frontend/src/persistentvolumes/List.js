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
import pv from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Storage Class',
  'Capacity',
  'Status',
  ''
];

const Rows = ({persistentVolumes, crudDelete}) => {
  const deletePersistentVolume = persistentVolume => async () => {
    await pv.api.delete(persistentVolume);
    crudDelete(persistentVolume);
  };
  return persistentVolumes
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(persistentVolume => (
        <Table.ResourceRow key={metadata.selectors.uid(persistentVolume)} resource={persistentVolume}>
          <Table.Cell>
            <Link.PersistentVolume to={`/persistentvolumes/${metadata.selectors.uid(persistentVolume)}`}>
              {metadata.selectors.name(persistentVolume)}
            </Link.PersistentVolume>
          </Table.Cell>
          <Table.Cell>
            {pv.selectors.specStorageClassName(persistentVolume)}
          </Table.Cell>
          <Table.Cell>
            {pv.selectors.specCapacityStorage(persistentVolume)}
          </Table.Cell>
          <Table.Cell>
            {pv.selectors.statusPhase(persistentVolume)}
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deletePersistentVolume(persistentVolume)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} loading={!loadedResources['PersistentVolume']} {...properties}>
    <Rows persistentVolumes={resources} crudDelete={crudDelete} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('persistentVolumes')(List);

