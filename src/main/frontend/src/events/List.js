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
import {uid} from '../metadata';
import {useFilteredResources} from '../redux';
import {Age, Icon, Link, ResourceListV2, Table} from '../components';
import {selectors} from './';

const headers = [
  '',
  'Type',
  <span>
    <Icon icon='fa-id-card' /> Name
  </span>,
  'Reason',
  'Event',
  <div className='text-right'>
    <Icon stylePrefix='far' icon='fa-clock' /> Last Seen
  </div>
];

const EventName = ({event}) => {
  let Component = Link.RouterLink;
  let url;
  const uid = selectors.involvedObjectUid(event);
  const name = selectors.involvedObjectName(event);
  switch (selectors.involvedObjectKind(event)) {
    case 'ConfigMap':
      Component = Link.ConfigMap;
      url = `/configmaps/${uid}`;
      break;
    case 'CronJob':
      Component = Link.CronJob;
      url = `/cronjobs/${uid}`;
      break;
    case 'Deployment':
      Component = Link.Deployment;
      url = `/deployments/${uid}`;
      break;
    case 'DeploymentConfig':
      Component = Link.DeploymentConfig;
      url = `/deploymentconfigs/${uid}`;
      break;
    case 'HorizontalPodAutoscaler':
      Component = Link.HorizontalPodAutoscaler;
      url = `/horizontalpodautoscalers/${uid}`;
      break;
    case 'Ingress':
      Component = Link.Ingress;
      url = `/ingresses/${uid}`;
      break;
    case 'Job':
      Component = Link.Job;
      url = `/jobs/${uid}`;
      break;
    case 'Node':
      Component = Link.Node;
      url = `/nodes/${uid}`;
      break;
    case 'Pod':
      Component = Link.Pod;
      url = `/pods/${uid}`;
      break;
    case 'ReplicationController':
      Component = Link.ReplicationController;
      url = `/replicationcontrollers/${uid}`;
      break;
    case 'StatefulSet':
      Component = Link.StatefulSet;
      url = `/statefulsets/${uid}`;
      break;
    default:
      url = null;
  }
  if (url) {
    return <Component to={url}>{name}</Component>;
  }
  return name;
};

const timestamp = event =>
  event.lastTimestamp ?? event.eventTime ?? event.metadata.creationTimestamp;

const sort = (ev1, ev2) => new Date(timestamp(ev2)) - new Date(timestamp(ev1));

const Rows = ({events}) => {
  return events
    .sort(sort)
    .slice(0, 10)
    .map(event => {
      const lastTimestamp = new Date(timestamp(event));
      return (
        <Table.ResourceRow
          key={uid(event)}
          resource={event}
          data-testid='list__events-row'
        >
          <Table.Cell className='whitespace-nowrap w-3 text-center'>
            <Icon
              className={
                selectors.typeIsNormal(event)
                  ? 'text-green-500'
                  : 'text-red-500'
              }
              icon={
                selectors.typeIsNormal(event)
                  ? 'fa-check'
                  : 'fa-exclamation-circle'
              }
            />
          </Table.Cell>
          <Table.Cell className='whitespace-nowrap'>
            {selectors.involvedObjectKind(event)}
          </Table.Cell>
          <Table.Cell>
            <EventName event={event} />
          </Table.Cell>
          <Table.Cell>{event.reason}</Table.Cell>
          <Table.Cell>{event.message}</Table.Cell>
          <Table.Cell className='text-right'>
            <Age date={lastTimestamp} />
          </Table.Cell>
        </Table.ResourceRow>
      );
    });
};

const filterEvents = (events = [], {namespace} = undefined) =>
  Object.entries(events)
    .filter(([, event]) => {
      if (namespace) {
        return selectors.involvedObjectNamespace(event) === namespace;
      }
      return true;
    })
    .reduce((acc, [key, event]) => {
      acc[key] = event;
      return acc;
    }, {});

export const List = ({...properties}) => {
  const filteredResources = useFilteredResources({
    resource: 'events',
    filters: {...properties}
  });
  const resources = Object.values(filterEvents(filteredResources, properties));
  return (
    <ResourceListV2
      data-testid='list__events'
      title='Latest Events'
      headers={headers}
      resources={resources}
      {...properties}
    >
      <Rows events={resources} />
    </ResourceListV2>
  );
};
