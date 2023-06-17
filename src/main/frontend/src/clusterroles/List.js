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
import {api} from './';
import {Tooltip} from '../components';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  <span>
    <Icon stylePrefix='far' icon='fa-clock' /> Time
  </span>,
  ''
];

const Rows = ({clusterRoles}) => {
  const deleteClusterRole = clusterRole => async () =>
    await api.deleteCr(clusterRole);
  return clusterRoles
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(clusterRole => (
      <Table.ResourceRow
        key={metadata.selectors.uid(clusterRole)}
        resource={clusterRole}
      >
        <Table.Cell>
          <Link.ClusterRole
            to={`/clusterroles/${metadata.selectors.uid(clusterRole)}`}
          >
            {metadata.selectors.name(clusterRole)}
          </Link.ClusterRole>
        </Table.Cell>
        <Table.Cell>
          <Tooltip
            content={`${metadata.selectors
              .creationTimestamp(clusterRole)
              .toLocaleDateString()}
                ${metadata.selectors
                  .creationTimestamp(clusterRole)
                  .toLocaleTimeString()}`}
            className='cursor-default'
          >
            <span>
              {metadata.selectors
                .creationTimestamp(clusterRole)
                .toLocaleDateString()}
            </span>
          </Tooltip>
        </Table.Cell>
        <Table.Cell>
          <Table.DeleteButton onClick={deleteClusterRole(clusterRole)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

export const List = ResourceList.resourceListConnect('clusterRoles')(
  ({resources, loadedResources, crudDelete, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows clusterRoles={resources} />
    </ResourceList>
  )
);
