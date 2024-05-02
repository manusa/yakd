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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import p from './';
import {Icon} from '../components';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Status',
  'Restarts',
  ''
];

const Rows = ({pods}) => {
  const deletePod = pod => async () => await p.api.delete(pod);
  return pods.sort(sortByCreationTimeStamp).map(pod => (
    <Table.ResourceRow key={uid(pod)} resource={pod}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            p.selectors.succeededOrContainersReady(pod)
              ? 'text-green-500'
              : 'text-red-500'
          }
          icon={
            p.selectors.succeededOrContainersReady(pod)
              ? 'fa-check'
              : 'fa-exclamation-circle'
          }
        />
      </Table.Cell>
      <Table.Cell>
        <Link.Pod to={`/pods/${uid(pod)}`}>{name(pod)}</Link.Pod>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(pod)}`}>
          {namespace(pod)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <p.StatusIcon
          className='mr-1'
          statusPhase={p.selectors.statusPhase(pod)}
        />
        {p.selectors.statusPhase(pod)}
      </Table.Cell>
      <Table.Cell>{p.selectors.restartCount(pod)}</Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Link.RouterLink
          variant={Link.variants.outline}
          to={`/pods/${uid(pod)}/logs`}
          title='Logs'
        >
          <Icon stylePrefix='far' icon='fa-file-alt' />
        </Link.RouterLink>
        <Link.RouterLink
          variant={Link.variants.outline}
          size={Link.sizes.small}
          to={`/pods/${uid(pod)}/exec`}
          title='Terminal'
          className='ml-1'
        >
          <Icon stylePrefix='fas' icon='fa-terminal' />
        </Link.RouterLink>
        <Table.DeleteButton className='ml-1' onClick={deletePod(pod)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

const List = ({
  resources,
  nodeName,
  ownerUids,
  ownerUid,
  crudDelete,
  loadedResources,
  ...properties
}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows pods={resources} />
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
  resources: Object.values(p.selectors.podsBy(stateProps.resources, ownProps))
});

export default connect(
  ResourceList.mapStateToProps('pods'),
  null,
  mergeProps
)(List);
