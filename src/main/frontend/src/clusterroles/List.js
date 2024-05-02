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
import {
  creationTimestamp,
  name,
  sortByCreationTimeStamp,
  uid
} from '../metadata';
import {api} from './';
import {Age, Icon} from '../components';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span key='name'>
    <Icon className='fa-id-card' /> Name
  </span>,
  <span key='age'>
    <Icon stylePrefix='far' icon='fa-clock' /> Age
  </span>,
  ''
];

const Rows = ({clusterRoles}) => {
  const deleteClusterRole = clusterRole => async () =>
    await api.deleteCr(clusterRole);
  return clusterRoles.sort(sortByCreationTimeStamp).map(clusterRole => (
    <Table.ResourceRow key={uid(clusterRole)} resource={clusterRole}>
      <Table.Cell>
        <Link.ClusterRole to={`/clusterroles/${uid(clusterRole)}`}>
          {name(clusterRole)}
        </Link.ClusterRole>
      </Table.Cell>
      <Table.Cell>
        <Age date={creationTimestamp(clusterRole)} />
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
