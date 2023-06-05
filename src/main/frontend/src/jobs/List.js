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
import j from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  'Completions',
  ''
];

const Rows = ({jobs}) => {
  const deleteJob = job => () => j.api.delete(job);
  return jobs
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(job => (
      <Table.ResourceRow key={metadata.selectors.uid(job)} resource={job}>
        <Table.Cell className='whitespace-no-wrap w-3 text-center'>
          <Icon
            className={j.selectors.isComplete(job) ? 'text-green-500' : 'text-gray-500'}
            icon={j.selectors.isComplete(job) ? 'fa-check' : 'fa-hourglass-half'}
          />
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.Job to={`/jobs/${metadata.selectors.uid(job)}`}>
            {metadata.selectors.name(job)}
          </Link.Job>
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(job)}`}>
            {metadata.selectors.namespace(job)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell className=''>
          {j.selectors.statusSucceeded(job)}/{j.selectors.specCompletions(job)}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap text-center'>
          <Table.DeleteButton onClick={deleteJob(job)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const List = ({resources, ownerUid, uids, uidsNotIn, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows jobs={resources} loadedResources={loadedResources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('jobs')(List);
