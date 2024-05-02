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
import {Card, DashboardPage, Form, Icon} from '../components';
import {withParams} from '../router';
import {Details, namespace, uid} from '../metadata';
import {api, selectors} from './';
import {JobsList} from '../jobs';
import Link from '../components/Link';
import ResourceDetailPage from '../components/ResourceDetailPage';

const SuspendField = ({cronJob}) => {
  const isSuspended = selectors.specSuspend(cronJob);
  const toggleSuspend = () => api.updateSuspend(cronJob, !isSuspended);
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
};

const mapStateToProps = ({cronJobs}) => ({
  cronJobs
});

const mergeProps = ({cronJobs}, dispatchProps, {params: {uid}}) => ({
  cronJob: cronJobs[uid]
});
export const CronJobsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({cronJob}) => (
    <ResourceDetailPage
      path='cronjobs'
      title={
        <DashboardPage.Title
          path='cronjobs'
          kind='CronJobs'
          namespace={namespace(cronJob)}
          resource={cronJob}
          isReadyFunction={selectors.isReady}
        />
      }
      resource={cronJob}
      deleteFunction={api.deleteCj}
      actions={
        <Link
          className='ml-2'
          size={Link.sizes.small}
          variant={Link.variants.outline}
          onClick={() => api.trigger(cronJob)}
          title='Manual Trigger'
        >
          <Icon icon='fa-play' className='mr-2' />
          Trigger
        </Link>
      }
      body={
        <Form>
          <Details resource={cronJob} />
          <Form.Field label='Schedule'>
            {selectors.specSchedule(cronJob)}
          </Form.Field>
          <SuspendField cronJob={cronJob} />
          <Form.Field label='Active'>
            {selectors.statusActive(cronJob).length}
          </Form.Field>
          <Form.Field label='Concurrency Policy'>
            {selectors.specConcurrencyPolicy(cronJob)}
          </Form.Field>
          <Form.Field label='Last Schedule'>
            {`${
              selectors.statusLastScheduleTime(cronJob)?.toLocaleDateString() ??
              ''
            }
          ${
            selectors.statusLastScheduleTime(cronJob)?.toLocaleTimeString() ??
            ''
          }`}
          </Form.Field>
        </Form>
      }
    >
      <JobsList
        title='Active Jobs'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        uids={selectors.statusActiveUids(cronJob)}
      />
      <JobsList
        title='Inactive Jobs'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(cronJob)}
        uidsNotIn={selectors.statusActiveUids(cronJob)}
      />
    </ResourceDetailPage>
  ))
);

export default withParams(
  connect(mapStateToProps, null, mergeProps)(CronJobsDetailPage)
);
