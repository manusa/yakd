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
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = [
  '',
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Schedule',
  'Suspended',
  'Active',
  ''
];

const Rows = ({cronJobs}) => {
  const deleteJob = cronJob => () => api.deleteCj(cronJob);
  return cronJobs.sort(sortByCreationTimeStamp).map(cronJob => (
    <Table.ResourceRow key={uid(cronJob)} resource={cronJob}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isReady(cronJob) ? 'text-green-500' : 'text-red-500'
          }
          icon={
            selectors.isReady(cronJob) ? 'fa-check' : 'fa-exclamation-circle'
          }
        />
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.CronJob to={`/cronjobs/${uid(cronJob)}`}>
          {name(cronJob)}
        </Link.CronJob>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(cronJob)}`}>
          {namespace(cronJob)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>{selectors.specSchedule(cronJob)}</Table.Cell>
      <Table.Cell>{selectors.specSuspend(cronJob).toString()}</Table.Cell>
      <Table.Cell>{selectors.statusActive(cronJob).length}</Table.Cell>
      <Table.Cell className='whitespace-nowrap text-center'>
        <Table.DeleteButton onClick={deleteJob(cronJob)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

export const List = ResourceList.resourceListConnect('cronJobs')(
  ({resources, crudDelete, loadedResources, ...properties}) => (
    <ResourceList headers={headers} resources={resources} {...properties}>
      <Rows cronJobs={resources} loadedResources={loadedResources} />
    </ResourceList>
  )
);
