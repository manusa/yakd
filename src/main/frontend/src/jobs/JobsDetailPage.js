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
import {Details, namespace, uid} from '../metadata';
import {api, selectors} from './';
import {ContainerList} from '../containers';
import pods from '../pods';
import {Card, Form} from '../components';
import {DashboardPage, ResourceDetailPage} from '../dashboard';

const mapStateToProps = ({jobs}) => ({
  jobs
});

const mergeProps = ({jobs}, dispatchProps, {params: {uid}}) => ({
  job: jobs[uid]
});

export const JobsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({job}) => (
    <ResourceDetailPage
      path='jobs'
      title={
        <DashboardPage.Title
          path='jobs'
          kind='Jobs'
          namespace={namespace(job)}
          resource={job}
          isReadyFunction={selectors.isComplete}
          notReadyClassName='text-gray-500'
          notReadyIcon='fa-hourglass-half'
        />
      }
      resource={job}
      deleteFunction={api.deleteJob}
      body={
        <Form>
          <Details resource={job} />
          <Form.Field label='Completions'>
            {selectors.specCompletions(job)}
          </Form.Field>
          <Form.Field label='Parallelism'>
            {selectors.specParallelism(job)}
          </Form.Field>
          <Form.Field label='Succeeded'>
            {selectors.statusSucceeded(job)}
          </Form.Field>
        </Form>
      }
    >
      <ContainerList
        title='Containers'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        containers={selectors.containers(job)}
      />
      <pods.List
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ownerUid={uid(job)}
      />
    </ResourceDetailPage>
  ))
);
