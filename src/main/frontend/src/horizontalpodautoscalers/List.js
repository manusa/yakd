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
import hpa from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  <span>Scale Target</span>,
  ''
];

const Rows = ({horizontalPodAutoscalers}) => {
  const deleteAction = horizontalPodAutoscaler => () => hpa.api.delete(horizontalPodAutoscaler);
  return horizontalPodAutoscalers
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(horizontalPodAutoscaler => (
      <Table.ResourceRow key={metadata.selectors.uid(horizontalPodAutoscaler)} resource={horizontalPodAutoscaler}>
        <Table.Cell className='whitespace-no-wrap w-3 text-center'>
          <Icon
            className={hpa.selectors.isReady(horizontalPodAutoscaler) ? 'text-green-500' : 'text-red-500'}
            icon={hpa.selectors.isReady(horizontalPodAutoscaler) ? 'fa-check' : 'fa-exclamation-circle'}
          />
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.HorizontalPodAutoscaler to={`/horizontalpodautoscalers/${metadata.selectors.uid(horizontalPodAutoscaler)}`}>
            {metadata.selectors.name(horizontalPodAutoscaler)}
          </Link.HorizontalPodAutoscaler>
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(horizontalPodAutoscaler)}`}>
            {metadata.selectors.namespace(horizontalPodAutoscaler)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell>
          {hpa.selectors.scaleTargetRefName(horizontalPodAutoscaler)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap text-center'>
          <Table.DeleteButton onClick={deleteAction(horizontalPodAutoscaler)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const List = ({resources, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows horizontalPodAutoscalers={resources} loadedResources={loadedResources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('horizontalPodAutoscalers')(List);
