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
import React, {useState, useEffect} from 'react';
import {connect} from 'react-redux';
import {withParams} from '../router';
import {Details, uid} from '../metadata';
import {ContainerList} from '../containers';
import {bytesToHumanReadable, podMetrics} from '../metrics';
import {Card, Form, Icon, Link} from '../components';
import {ResourceDetailPage} from '../dashboard';
import {api, selectors, StatusIcon} from './';

const useMetrics = pod => {
  const [metrics, setMetrics] = useState(null);
  const [timeoutHandle, setTimeoutHandle] = useState(null);
  useEffect(() => {
    if (!timeoutHandle && pod) {
      const updateMetrics = async () => {
        try {
          const metrics = await api.metrics(pod);
          setMetrics(metrics);
        } catch (e) {
          setMetrics(null);
        }
        setTimeoutHandle(setTimeout(updateMetrics, 15000));
      };
      updateMetrics().then(() => {});
    }
  }, [timeoutHandle, setTimeoutHandle, setMetrics, pod]);
  useEffect(
    () => () => {
      clearTimeout(timeoutHandle);
    },
    [timeoutHandle]
  );
  return metrics;
};

const ActionLink = ({to, title, stylePrefix, icon}) => (
  <Link.RouterLink
    className='ml-2'
    size={Link.sizes.small}
    variant={Link.variants.outline}
    to={to}
    title={title}
  >
    <Icon stylePrefix={stylePrefix} icon={icon} className='mr-2' />
    {title}
  </Link.RouterLink>
);

const mapStateToProps = ({pods}) => ({
  pods
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  pod: stateProps.pods[ownProps.params.uid]
});

export const PodsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({pod}) => {
    const metrics = useMetrics(pod);
    const currentPodMetrics = metrics && podMetrics(metrics);
    return (
      <ResourceDetailPage
        kind='Pods'
        path='pods'
        resource={pod}
        isReadyFunction={selectors.succeededOrContainersReady}
        deleteFunction={api.deletePod}
        actions={
          <>
            <ActionLink
              to={`/pods/${uid(pod)}/logs`}
              title='Logs'
              stylePrefix='far'
              icon='fa-file-alt'
            />
            <ActionLink
              to={`/pods/${uid(pod)}/exec`}
              title='Exec'
              stylePrefix='fas'
              icon='fa-terminal'
            />
          </>
        }
        body={
          <Form>
            <Details resource={pod} />
            <Form.Field label='Node'>
              <Link.Node to={`/nodes/${selectors.nodeName(pod)}`}>
                {selectors.nodeName(pod)}
              </Link.Node>
            </Form.Field>
            <Form.Field label='Phase'>
              <StatusIcon
                className='text-gray-700 mr-1'
                statusPhase={selectors.statusPhase(pod)}
              />
              {selectors.statusPhase(pod)}
            </Form.Field>
            <Form.Field label='Restart Policy'>
              {selectors.restartPolicy(pod)}
            </Form.Field>
            <Form.Field label='Pod IP'>{selectors.statusPodIP(pod)}</Form.Field>
            {currentPodMetrics && (
              <>
                <Form.Field label='Used CPU'>
                  <Icon icon='fa-microchip' className='text-gray-600 mr-2' />
                  {currentPodMetrics.totalCpu().toFixed(3)}
                </Form.Field>
                <Form.Field label='Used Memory'>
                  <Icon icon='fa-memory' className='text-gray-600 mr-2' />
                  {bytesToHumanReadable(currentPodMetrics.totalMemory())}
                </Form.Field>
              </>
            )}
          </Form>
        }
      >
        <ContainerList
          title='Containers'
          titleVariant={Card.titleVariants.medium}
          className='mt-2'
          containers={selectors.containers(pod)}
          podMetrics={currentPodMetrics}
        />
      </ResourceDetailPage>
    );
  })
);
