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
import {api, selectors} from './';
import {Icon, Link} from '../components';
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

const Rows = ({daemonSets}) => {
  const deleteDS = daemonSet => () => api.deleteDs(daemonSet);
  const restart = daemonSet => () => api.restart(daemonSet);
  return daemonSets.sort(sortByCreationTimeStamp).map(daemonSet => (
    <Table.ResourceRow key={uid(daemonSet)} resource={daemonSet}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isReady(daemonSet) ? 'text-green-500' : 'text-red-500'
          }
          icon={
            selectors.isReady(daemonSet) ? 'fa-check' : 'fa-exclamation-circle'
          }
        />
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.DaemonSet to={`/daemonsets/${uid(daemonSet)}`}>
          {name(daemonSet)}
        </Link.DaemonSet>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(daemonSet)}`}>
          {namespace(daemonSet)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell className='break-all'>
        {selectors.images(daemonSet).map((image, idx) => (
          <div key={idx}>{image}</div>
        ))}
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Link
          variant={Link.variants.outline}
          onClick={restart(daemonSet)}
          title='Restart'
        >
          <Icon stylePrefix='fas' icon='fa-redo-alt' />
        </Link>
        <Table.DeleteButton className='ml-1' onClick={deleteDS(daemonSet)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('daemonSets')(
  ({resources, crudDelete, loadedResources, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows daemonSets={resources} loadedResources={loadedResources} />
    </ResourceList>
  )
);
