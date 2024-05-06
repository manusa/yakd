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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import sts from './';
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

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

const Rows = ({statefulSets}) => {
  const deleteStatefulSet = statefulSet => async () =>
    await sts.api.delete(statefulSet);
  const restartStatefulSet = statefulSet => async () =>
    await sts.api.restart(statefulSet);
  return statefulSets.sort(sortByCreationTimeStamp).map(statefulSet => (
    <Table.ResourceRow key={uid(statefulSet)} resource={statefulSet}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            sts.selectors.isReady(statefulSet)
              ? 'text-green-500'
              : 'text-red-500'
          }
          icon={
            sts.selectors.isReady(statefulSet)
              ? 'fa-check'
              : 'fa-exclamation-circle'
          }
        />
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.StatefulSet to={`/statefulsets/${uid(statefulSet)}`}>
          {name(statefulSet)}
        </Link.StatefulSet>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(statefulSet)}`}>
          {namespace(statefulSet)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell className='break-all'>
        {sts.selectors.images(statefulSet).map((image, idx) => (
          <div key={idx}>{image}</div>
        ))}
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Link
          variant={Link.variants.outline}
          onClick={restartStatefulSet(statefulSet)}
          title='Restart'
        >
          <Icon stylePrefix='fas' icon='fa-redo-alt' />
        </Link>
        <Table.DeleteButton
          className='ml-1'
          onClick={deleteStatefulSet(statefulSet)}
        />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

const List = ({resources, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows statefulSets={resources} loadedResources={loadedResources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('statefulSets')(List);
