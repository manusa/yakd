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
import {useFilteredResources} from '../redux';
import {Icon, ResourceListV2, Table} from '../components';
import {api, selectors} from './';

const headers = [
  '',
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Replicas',
  ''
];

const Rows = ({replicaSets}) => {
  const deleteReplicaSet = replicaSet => async () =>
    await api.requestDelete(replicaSet);
  return replicaSets.sort(sortByCreationTimeStamp).map(replicaSet => (
    <Table.ResourceRow key={uid(replicaSet)} resource={replicaSet}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isReady(replicaSet) ? 'text-green-500' : 'text-red-500'
          }
          icon={
            selectors.isReady(replicaSet) ? 'fa-check' : 'fa-exclamation-circle'
          }
        />
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>{name(replicaSet)}</Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        {namespace(replicaSet)}
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        {selectors.specReplicas(replicaSet)}
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Table.DeleteButton onClick={deleteReplicaSet(replicaSet)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ({...properties}) => {
  const resources = useFilteredResources({
    resource: 'replicaSets',
    filters: {...properties}
  });
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows replicaSets={resources} />
    </ResourceListV2>
  );
};
