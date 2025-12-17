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
import {name, sortByCreationTimeStamp, uid} from '../metadata';
import {useFilteredResources} from '../redux';
import {Icon, Link, ResourceListV2, Table} from '../components';
import {api, selectors} from './';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Storage Class',
  'Capacity',
  'Status',
  ''
];

const Rows = ({persistentVolumes}) => {
  const deletePersistentVolume = persistentVolume => async () =>
    await api.deletePv(persistentVolume);
  return persistentVolumes
    .sort(sortByCreationTimeStamp)
    .map(persistentVolume => (
      <Table.ResourceRow
        key={uid(persistentVolume)}
        resource={persistentVolume}
      >
        <Table.Cell>
          <Link.PersistentVolume
            to={`/persistentvolumes/${uid(persistentVolume)}`}
          >
            {name(persistentVolume)}
          </Link.PersistentVolume>
        </Table.Cell>
        <Table.Cell>
          {selectors.specStorageClassName(persistentVolume)}
        </Table.Cell>
        <Table.Cell>
          {selectors.specCapacityStorage(persistentVolume)}
        </Table.Cell>
        <Table.Cell>{selectors.statusPhase(persistentVolume)}</Table.Cell>
        <Table.Cell>
          <Table.DeleteButton
            onClick={deletePersistentVolume(persistentVolume)}
          />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

export const List = ({...properties}) => {
  const resources = useFilteredResources({
    resource: 'persistentVolumes',
    filters: {...properties}
  });
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows persistentVolumes={resources} />
    </ResourceListV2>
  );
};
