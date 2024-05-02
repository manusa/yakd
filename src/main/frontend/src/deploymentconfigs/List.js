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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import dc from './';
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

const Rows = ({deploymentConfigs}) => {
  const deleteDC = deploymentConfig => () => dc.api.delete(deploymentConfig);
  const restartDC = deploymentConfig => () => dc.api.restart(deploymentConfig);
  return deploymentConfigs
    .sort(sortByCreationTimeStamp)
    .map(deploymentConfig => (
      <Table.ResourceRow
        key={uid(deploymentConfig)}
        resource={deploymentConfig}
      >
        <Table.Cell className='whitespace-nowrap w-3 text-center'>
          <Icon
            className={
              dc.selectors.isReady(deploymentConfig)
                ? 'text-green-500'
                : 'text-red-500'
            }
            icon={
              dc.selectors.isReady(deploymentConfig)
                ? 'fa-check'
                : 'fa-exclamation-circle'
            }
          />
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.DeploymentConfig
            to={`/deploymentconfigs/${uid(deploymentConfig)}`}
          >
            {name(deploymentConfig)}
          </Link.DeploymentConfig>
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap'>
          <Link.Namespace to={`/namespaces/${namespace(deploymentConfig)}`}>
            {namespace(deploymentConfig)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell className='break-all'>
          {dc.selectors.images(deploymentConfig).map((image, idx) => (
            <div key={idx}>{image}</div>
          ))}
        </Table.Cell>
        <Table.Cell className='whitespace-nowrap text-center'>
          <Link
            variant={Link.variants.outline}
            onClick={restartDC(deploymentConfig)}
            title='Restart'
          >
            <Icon stylePrefix='fas' icon='fa-redo-alt' />
          </Link>
          <Table.DeleteButton
            className='ml-1'
            onClick={deleteDC(deploymentConfig)}
          />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const List = ({deploymentConfigs, ...properties}) => (
  <ResourceList headers={headers} resources={deploymentConfigs} {...properties}>
    <Rows deploymentConfigs={deploymentConfigs} />
  </ResourceList>
);

const mapStateToProps = ({deploymentConfigs}) => ({
  deploymentConfigs
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  deploymentConfigs: Object.values(
    resourcesBy(stateProps.deploymentConfigs, ownProps)
  )
});

export default connect(mapStateToProps, null, mergeProps)(List);
