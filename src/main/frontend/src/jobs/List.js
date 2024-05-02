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
  'Completions',
  ''
];

const Rows = ({jobs}) => {
  const deleteJob = job => () => api.deleteJob(job);
  return jobs.sort(sortByCreationTimeStamp).map(job => (
    <Table.ResourceRow key={uid(job)} resource={job}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isComplete(job) ? 'text-green-500' : 'text-gray-500'
          }
          icon={selectors.isComplete(job) ? 'fa-check' : 'fa-hourglass-half'}
        />
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Job to={`/jobs/${uid(job)}`}>{name(job)}</Link.Job>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(job)}`}>
          {namespace(job)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell className=''>
        {selectors.statusSucceeded(job)}/{selectors.specCompletions(job)}
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Table.DeleteButton onClick={deleteJob(job)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('jobs')(
  ({
    resources,
    ownerUid,
    uids,
    uidsNotIn,
    crudDelete,
    loadedResources,
    ...properties
  }) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows jobs={resources} loadedResources={loadedResources} />
    </ResourceList>
  )
);
