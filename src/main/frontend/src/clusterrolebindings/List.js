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
import crb from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';
import Tooltip from '../components/Tooltip';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Role',
  <span><Icon stylePrefix='far' icon='fa-clock' /> Time</span>,
  ''
];

const Rows = ({clusterRoleBindings}) => {
  const deleteCRB = clusterRoleBinding => async () => await crb.api.delete(clusterRoleBinding);
  return clusterRoleBindings
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(clusterRoleBinding => (
        <Table.ResourceRow key={metadata.selectors.uid(clusterRoleBinding)} resource={clusterRoleBinding}>
          <Table.Cell>
            <Link.ClusterRoleBinding to={`/clusterrolebindings/${metadata.selectors.uid(clusterRoleBinding)}`}>
              {metadata.selectors.name(clusterRoleBinding)}
            </Link.ClusterRoleBinding>
          </Table.Cell>
          <Table.Cell>
            <Link.ClusterRole to={`/clusterroles/${crb.selectors.roleRefName(clusterRoleBinding)}`}>
              {crb.selectors.roleRefName(clusterRoleBinding)}
            </Link.ClusterRole>
          </Table.Cell>
          <Table.Cell>
            <Tooltip
              content={`${metadata.selectors.creationTimestamp(clusterRoleBinding).toLocaleDateString()}
                ${metadata.selectors.creationTimestamp(clusterRoleBinding).toLocaleTimeString()}`}
              className='cursor-default'
            >
              <span>{metadata.selectors.creationTimestamp(clusterRoleBinding).toLocaleDateString()}</span>
            </Tooltip>
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteCRB(clusterRoleBinding)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, roleRefName, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows clusterRoleBindings={resources} />
  </ResourceList>
);

List.propTypes = {
  nodeName: PropTypes.string,
  ownerUids: PropTypes.arrayOf(PropTypes.string),
  namespace: PropTypes.string
};

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  resources: Object.values(crb.selectors.crbsBy(stateProps.resources, ownProps))
});

export default connect(ResourceList.mapStateToProps('clusterRoleBindings'), null, mergeProps)(List);

