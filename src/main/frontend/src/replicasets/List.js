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
import PropTypes from 'prop-types';
import metadata from '../metadata';
import rs from './';
import Icon from '../components/Icon';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  'Replicas',
  ''
];

const Rows = ({replicaSets}) => {
  const deleteReplicaSet = replicaSet => async () => await rs.api.requestDelete(replicaSet);
  return replicaSets
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(replicaSet => (
      <Table.ResourceRow key={metadata.selectors.uid(replicaSet)} resource={replicaSet}>
        <Table.Cell className='whitespace-no-wrap w-3 text-center'>
          <Icon
            className={rs.selectors.isReady(replicaSet) ? 'text-green-500' : 'text-red-500'}
            icon={rs.selectors.isReady(replicaSet) ? 'fa-check' : 'fa-exclamation-circle'}
          />
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          {metadata.selectors.name(replicaSet)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          {metadata.selectors.namespace(replicaSet)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          {rs.selectors.specReplicas(replicaSet)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap text-center'>
          <Table.DeleteButton onClick={deleteReplicaSet(replicaSet)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
}

const List = ({resources, ownerUid, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows replicaSets={resources} />
  </ResourceList>
);

List.propTypes = {
  ownerUid: PropTypes.string
};

export default ResourceList.resourceListConnect('replicaSets')(List);

