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
import {connect} from 'react-redux';
import metadata from '../metadata';
import {api, selectors} from './';
import {resourcesBy} from '../redux';
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
  <span>
    <Icon icon='fa-layer-group' /> Images
  </span>,
  ''
];

const Rows = ({deployments}) => {
  const deleteDeployment = deployment => async () =>
    await api.deleteDeployment(deployment);
  const restartDeployment = deployment => async () =>
    await api.restart(deployment);
  return deployments
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(deployment => (
      <Table.ResourceRow
        key={metadata.selectors.uid(deployment)}
        resource={deployment}
      >
        <Table.Cell className='whitespace-nowrap w-3 text-center'>
          <Icon
            className={
              selectors.isReady(deployment) ? 'text-green-500' : 'text-red-500'
            }
            icon={
              selectors.isReady(deployment)
                ? 'fa-check'
                : 'fa-exclamation-circle'
            }
          />
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.Deployment
            to={`/deployments/${metadata.selectors.uid(deployment)}`}
          >
            {metadata.selectors.name(deployment)}
          </Link.Deployment>
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.Namespace
            to={`/namespaces/${metadata.selectors.namespace(deployment)}`}
          >
            {metadata.selectors.namespace(deployment)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell className='break-all'>
          {selectors.images(deployment).map((image, idx) => (
            <div key={idx}>{image}</div>
          ))}
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap text-center'>
          <Link
            variant={Link.variants.outline}
            onClick={restartDeployment(deployment)}
            title='Restart'
          >
            <Icon stylePrefix='fas' icon='fa-redo-alt' />
          </Link>
          <Table.DeleteButton
            className='ml-1'
            onClick={deleteDeployment(deployment)}
          />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const mapStateToProps = ({deployments}) => ({
  deployments
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  deployments: Object.values(resourcesBy(stateProps.deployments, ownProps))
});

export const List = connect(
  mapStateToProps,
  null,
  mergeProps
)(({deployments, ...properties}) => (
  <ResourceList headers={headers} resources={deployments} {...properties}>
    <Rows deployments={deployments} />
  </ResourceList>
));
