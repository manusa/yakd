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
import ds from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  <span><Icon icon='fa-layer-group'/> Images</span>,
  ''
];

const Rows = ({daemonSets}) => {
  const deleteDS = daemonSet => () => ds.api.delete(daemonSet);
  const restart = daemonSet => () => ds.api.restart(daemonSet);
  return daemonSets
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(daemonSet => (
      <Table.ResourceRow key={metadata.selectors.uid(daemonSet)} resource={daemonSet}>
        <Table.Cell className='whitespace-no-wrap w-3 text-center'>
          <Icon
            className={ds.selectors.isReady(daemonSet) ? 'text-green-500' : 'text-red-500'}
            icon={ds.selectors.isReady(daemonSet) ? 'fa-check' : 'fa-exclamation-circle'}
          />
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.DaemonSet to={`/daemonsets/${metadata.selectors.uid(daemonSet)}`}>
            {metadata.selectors.name(daemonSet)}
          </Link.DaemonSet>
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(daemonSet)}`}>
            {metadata.selectors.namespace(daemonSet)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell>
          {ds.selectors.images(daemonSet).map((image, idx) =>
            <div key={idx}>{image}</div>
          )}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap text-center'>
          <Link
            variant={Link.variants.outline}
            onClick={restart(daemonSet)}
            title='Restart'
          ><Icon stylePrefix='fas' icon='fa-redo-alt' /></Link>
          <Table.DeleteButton
            className='ml-1' onClick={deleteDS(daemonSet)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const List = ({resources, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows daemonSets={resources} loadedResources={loadedResources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('daemonSets')(List);
