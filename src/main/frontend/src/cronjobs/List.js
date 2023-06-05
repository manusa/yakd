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
import cj from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  'Schedule',
  'Suspended',
  'Active',
  ''
];

const Rows = ({cronJobs}) => {
  const deleteJob = cronJob => () => cj.api.delete(cronJob);
  return cronJobs
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(cronJob => (
      <Table.ResourceRow key={metadata.selectors.uid(cronJob)} resource={cronJob}>
        <Table.Cell className='whitespace-no-wrap w-3 text-center'>
          <Icon
            className={cj.selectors.isReady(cronJob) ? 'text-green-500' : 'text-red-500'}
            icon={cj.selectors.isReady(cronJob) ? 'fa-check' : 'fa-exclamation-circle'}
          />
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.CronJob to={`/cronjobs/${metadata.selectors.uid(cronJob)}`}>
            {metadata.selectors.name(cronJob)}
          </Link.CronJob>
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap'>
          <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(cronJob)}`}>
            {metadata.selectors.namespace(cronJob)}
          </Link.Namespace>
        </Table.Cell>
        <Table.Cell>
          {cj.selectors.specSchedule(cronJob)}
        </Table.Cell>
        <Table.Cell>
          {cj.selectors.specSuspend(cronJob).toString()}
        </Table.Cell>
        <Table.Cell>
          {cj.selectors.statusActive(cronJob).length}
        </Table.Cell>
        <Table.Cell className='whitespace-no-wrap text-center'>
          <Table.DeleteButton onClick={deleteJob(cronJob)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

const List = ({resources, crudDelete, loadedResources, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows cronJobs={resources} loadedResources={loadedResources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('cronJobs')(List);
