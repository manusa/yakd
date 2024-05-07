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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import {api, selectors} from './';
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = [
  '',
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Replicas',
  ''
];

const Rows = ({replicationControllers}) => {
  const deleteReplicationController = replicationController => async () =>
    await api.deleteRc(replicationController);
  return replicationControllers
    .sort(sortByCreationTimeStamp)
    .map(replicationController => (
      <Table.ResourceRow
        key={uid(replicationController)}
        resource={replicationController}
      >
        <Table.Cell className='whitespace-nowrap w-3 text-center'>
          <Icon
            className={
              selectors.isReady(replicationController)
                ? 'text-green-500'
                : 'text-red-500'
            }
            icon={
              selectors.isReady(replicationController)
                ? 'fa-check'
                : 'fa-exclamation-circle'
            }
          />
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.ReplicationController
            to={`/replicationcontrollers/${uid(replicationController)}`}
          >
            {name(replicationController)}
          </Link.ReplicationController>
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          {namespace(replicationController)}
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          {selectors.specReplicas(replicationController)}
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap text-center'>
          <Table.DeleteButton
            onClick={deleteReplicationController(replicationController)}
          />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

export const List = ResourceList.resourceListConnect('replicationControllers')(
  ({resources, ownerUid, crudDelete, loadedResources, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows replicationControllers={resources} />
    </ResourceList>
  )
);

List.propTypes = {
  ownerUid: PropTypes.string
};
