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
// @ts-check
import React from 'react';
import {
  creationTimestamp,
  name,
  sortByCreationTimeStamp,
  uid
} from '../metadata';
import {api, selectors} from './';
import {useFilteredResources} from '../redux';
import {Age, Icon, Link, ResourceListV2, Table} from '../components';

const headers = [
  <span key='name'>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Cluster Role',
  <span key='age'>
    <Icon stylePrefix='far' icon='fa-clock' /> Age
  </span>,
  ''
];

const Rows = ({clusterRoleBindings}) => {
  const deleteCrb = clusterRoleBinding => async () =>
    await api.deleteCrb(clusterRoleBinding);
  return clusterRoleBindings
    .sort(sortByCreationTimeStamp)
    .map(clusterRoleBinding => (
      <Table.ResourceRow
        key={uid(clusterRoleBinding)}
        resource={clusterRoleBinding}
      >
        <Table.Cell>
          <Link.ClusterRoleBinding
            to={`/clusterrolebindings/${uid(clusterRoleBinding)}`}
          >
            {name(clusterRoleBinding)}
          </Link.ClusterRoleBinding>
        </Table.Cell>
        <Table.Cell>
          <Link.ClusterRole
            to={`/clusterroles/${selectors.roleRefName(clusterRoleBinding)}`}
          >
            {selectors.roleRefName(clusterRoleBinding)}
          </Link.ClusterRole>
        </Table.Cell>
        <Table.Cell>
          <Age date={creationTimestamp(clusterRoleBinding)} />
        </Table.Cell>
        <Table.Cell>
          <Table.DeleteButton onClick={deleteCrb(clusterRoleBinding)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

/**
 * @param {Object} props
 * @param {string} [props.nodeName]
 * @param {string[]} [props.ownerUids]
 * @param {string} [props.namespace]
 */
export const List = ({...properties}) => {
  const filteredResources = useFilteredResources({
    resource: 'clusterRoleBindings',
    filters: {...properties}
  });
  const resources = Object.values(
    selectors.crbsBy(filteredResources, properties)
  );
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows clusterRoleBindings={resources} />
    </ResourceListV2>
  );
};
