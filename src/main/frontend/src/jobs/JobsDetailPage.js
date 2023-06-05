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
import j from './';
import cnt from '../containers';
import pods from '../pods';
import Card from '../components/Card';
import DashboardPage from '../components/DashboardPage';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';

const JobsDetailPage = ({job}) => (
  <ResourceDetailPage
    path='jobs'
    title={
      <DashboardPage.Title
        path='jobs' kind='Jobs' namespace={metadata.selectors.namespace(job)} resource={job}
        isReadyFunction={j.selectors.isComplete} notReadyClassName='text-gray-500' notReadyIcon='fa-hourglass-half'
      />
    }
    resource={job}
    deleteFunction={j.api.delete}
    body={
      <Form>
        <metadata.Details resource={job} />
        <Form.Field label='Completions'>
          {j.selectors.specCompletions(job)}
        </Form.Field>
        <Form.Field label='Parallelism'>
          {j.selectors.specParallelism(job)}
        </Form.Field>
        <Form.Field label='Succeeded'>
          {j.selectors.statusSucceeded(job)}
        </Form.Field>
      </Form>
    }
  >
    <cnt.ContainerList
      title='Containers'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      containers={j.selectors.containers(job)} />
    <pods.List
      title='Pods'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ownerUid={metadata.selectors.uid(job)} />
  </ResourceDetailPage>
);

const mapStateToProps = ({jobs}) => ({
  jobs
});

const mergeProps = ({jobs}, dispatchProps, {params: {uid}}) => ({
  job: jobs[uid]
});

export default withParams(connect(mapStateToProps, null, mergeProps)(JobsDetailPage));
