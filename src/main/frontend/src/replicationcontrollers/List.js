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
import rc from './';
import Icon from '../components/Icon';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';
import Link from "../components/Link";

const headers = [
  '',
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  'Replicas',
  ''
];

const Rows = ({replicationControllers}) => {
  const deleteReplicationController = replicationController => async () => await rc.api.delete(replicationController);
  return replicationControllers
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(replicationController => (
      <Table.ResourceRow key={metadata.selectors.uid(replicationController)} resource={replicationController}>
        <Table.Cell className='whitespace-no-wrap w-3 text-center'>
          <Icon
            className={rc.selectors.isReady(replicationController) ? 'text-green-500' : 'text-red-500'}
            icon={rc.selectors.isReady(replicationController) ? 'fa-check' : 'fa-exclamation-circle'}
          />
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.ReplicationController to={`/replicationcontrollers/${metadata.selectors.uid(replicationController)}`}>
            {metadata.selectors.name(replicationController)}
          </Link.ReplicationController>
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          {metadata.selectors.namespace(replicationController)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          {rc.selectors.specReplicas(replicationController)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap text-center'>
          <Table.DeleteButton onClick={deleteReplicationController(replicationController)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
}

const List = ({resources, ownerUid, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows replicationControllers={resources} />
  </ResourceList>
);

List.propTypes = {
  ownerUid: PropTypes.string
};

export default ResourceList.resourceListConnect('replicationControllers')(List);

