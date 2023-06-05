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
import {withParams} from '../router';
import metadata from '../metadata';
import cj from './';
import jobs from '../jobs';
import Card from '../components/Card';
import DashboardPage from '../components/DashboardPage';
import Form from '../components/Form';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceDetailPage from '../components/ResourceDetailPage';

const SuspendField = ({cronJob}) => {
  const isSuspended = cj.selectors.specSuspend(cronJob);
  const toggleSuspend = () => cj.api.updateSuspend(cronJob, !isSuspended);
  return (
    <Form.Field label='Suspended'>
      <div className='flex items-center'>
        <div className='flex flex-col mr-1 text-blue-600'>
          <Icon
            icon={isSuspended ? 'fa-play-circle' : 'fa-pause-circle'}
            className='pr-1 py-1 leading-3 hover:text-blue-800 cursor-pointer'
            onClick={toggleSuspend}
          />
        </div>
        {isSuspended.toString()}
      </div>
    </Form.Field>
  );
}

const CronJobsDetailPage = ({cronJob}) => (
  <ResourceDetailPage
    path='cronjobs'
    title={
      <DashboardPage.Title
        path='cronjobs' kind='CronJobs' namespace={metadata.selectors.namespace(cronJob)} resource={cronJob}
        isReadyFunction={cj.selectors.isReady}
      />
    }
    resource={cronJob}
    deleteFunction={cj.api.delete}
    actions={
      <Link
        className='ml-2'
        size={Link.sizes.small}
        variant={Link.variants.outline}
        onClick={() => cj.api.trigger(cronJob)}
        title='Manual Trigger'
      >
        <Icon icon='fa-play' className='mr-2'/>
        Trigger
      </Link>
    }
    body={
      <Form>
        <metadata.Details resource={cronJob} />
        <Form.Field label='Schedule'>
          {cj.selectors.specSchedule(cronJob)}
        </Form.Field>
        <SuspendField cronJob={cronJob} />
        <Form.Field label='Active'>
          {cj.selectors.statusActive(cronJob).length}
        </Form.Field>
        <Form.Field label='Concurrency Policy'>
          {cj.selectors.specConcurrencyPolicy(cronJob)}
        </Form.Field>
        <Form.Field label='Last Schedule'>
          {`${cj.selectors.statusLastScheduleTime(cronJob)?.toLocaleDateString() ?? ''}
          ${cj.selectors.statusLastScheduleTime(cronJob)?.toLocaleTimeString() ?? ''}`}
        </Form.Field>
      </Form>
    }
  >
    <jobs.List
      title='Active Jobs'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      uids={cj.selectors.statusActiveUids(cronJob)} />
    <jobs.List
      title='Inactive Jobs'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUid={metadata.selectors.uid(cronJob)}
      uidsNotIn={cj.selectors.statusActiveUids(cronJob)} />
  </ResourceDetailPage>
);

const mapStateToProps = ({cronJobs}) => ({
  cronJobs
});

const mergeProps = ({cronJobs}, dispatchProps, {params: {uid}}) => ({
  cronJob: cronJobs[uid]
});

export default withParams(connect(mapStateToProps, null, mergeProps)(CronJobsDetailPage));
