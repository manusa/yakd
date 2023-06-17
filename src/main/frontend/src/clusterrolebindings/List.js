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
import {connect} from 'react-redux';
import metadata from '../metadata';
import {api, selectors} from './';
import {Age} from '../components';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span key='name'>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Role',
  <span key='time'>
    <Icon stylePrefix='far' icon='fa-clock' /> Age
  </span>,
  ''
];

const Rows = ({clusterRoleBindings}) => {
  const deleteCrb = clusterRoleBinding => async () =>
    await api.deleteCrb(clusterRoleBinding);
  return clusterRoleBindings
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(clusterRoleBinding => (
      <Table.ResourceRow
        key={metadata.selectors.uid(clusterRoleBinding)}
        resource={clusterRoleBinding}
      >
        <Table.Cell>
          <Link.ClusterRoleBinding
            to={`/clusterrolebindings/${metadata.selectors.uid(
              clusterRoleBinding
            )}`}
          >
            {metadata.selectors.name(clusterRoleBinding)}
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
          <Age
            date={metadata.selectors.creationTimestamp(clusterRoleBinding)}
          />
        </Table.Cell>
        <Table.Cell>
          <Table.DeleteButton onClick={deleteCrb(clusterRoleBinding)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  resources: Object.values(selectors.crbsBy(stateProps.resources, ownProps))
});

export const List = connect(
  ResourceList.mapStateToProps('clusterRoleBindings'),
  null,
  mergeProps
)(({resources, roleRefName, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows clusterRoleBindings={resources} />
  </ResourceList>
));

List.propTypes = {
  nodeName: PropTypes.string,
  ownerUids: PropTypes.arrayOf(PropTypes.string),
  namespace: PropTypes.string
};
