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
import {Details, name} from '../metadata';
import {bytesToHumanReadable, quantityToScalar} from '../metrics';
import {selectors} from './';
import {PodsList, selectors as podSelectors} from '../pods';
import {Card, DonutChart, Form, MinikubeIcon} from '../components';
import {ResourceDetailPage} from '../dashboard';

const Dial = ({
  title,
  description,
  partial,
  total,
  percent = (partial / total) * 100
}) => (
  <div>
    <div className='font-semibold text-center text-lg'>{title}</div>
    <DonutChart className='w-40 h-40 mx-auto' percent={percent}>
      <div className='flex flex-col items-center'>
        <div className='font-semibold'>{partial}</div>
        <div>of</div>
        <div className='font-semibold'>{total}</div>
      </div>
    </DonutChart>
    <div className='text-center text-sm'>{description}</div>
  </div>
);

const mapStateToProps = ({nodes, pods}) => ({
  nodes,
  pods
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  isMinikube: selectors.isMinikube(stateProps.nodes),
  node: Object.values(stateProps.nodes).find(
    node => name(node) === ownProps.params.name
  )
});

export const NodesDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({node, isMinikube, pods}) => {
    const nodeName = name(node);
    const podsForNode = Object.values(podSelectors.podsBy(pods, {nodeName}));
    const requests = podsForNode
      .flatMap(podSelectors.containers)
      .map(c => c.resources.requests ?? {});
    const requestedCpu = requests
      .map(r => r.cpu ?? 0)
      .reduce((acc, c) => acc + quantityToScalar(c), 0);
    const allocatableMemory = quantityToScalar(
      selectors.statusAllocatableMemory(node)
    );
    const requestedMemory = requests
      .map(r => r.memory ?? 0)
      .reduce((acc, c) => acc + quantityToScalar(c), 0);
    return (
      <ResourceDetailPage
        kind='Nodes'
        path='nodes'
        resource={node}
        isReadyFunction={selectors.isReady}
        actions={isMinikube && <MinikubeIcon className='ml-2 h-6' />}
        body={
          <Form>
            <div className='w-full mb-4 flex flex-wrap justify-around'>
              <Dial
                title='CPU'
                description='Requested vs. allocatable'
                partial={requestedCpu.toFixed(3)}
                total={quantityToScalar(
                  selectors.statusAllocatableCpu(node)
                ).toFixed(3)}
              />
              <Dial
                title='Memory'
                description='Requested vs. allocatable'
                percent={(requestedMemory / allocatableMemory) * 100}
                partial={bytesToHumanReadable(requestedMemory)}
                total={bytesToHumanReadable(allocatableMemory)}
              />
              <Dial
                title='Pods'
                description='Allocated vs. allocatable'
                partial={podsForNode.length}
                total={selectors.statusAllocatablePods(node)}
              />
            </div>

            <Details resource={node} />
            <Form.Field label='OS'>
              {selectors.statusNodeInfoOS(node)} (
              {selectors.statusNodeInfoArchitecture(node)})
            </Form.Field>
            <Form.Field label='Kernel Version'>
              {selectors.statusNodeInfoKernelVersion(node)}
            </Form.Field>
            <Form.Field label='Container Runtime'>
              {selectors.statusNodeInfoContainerRuntimeVersion(node)}
            </Form.Field>
            <Form.Field label='Kubelet Version'>
              {selectors.statusNodeInfoKubeletVersion(node)}
            </Form.Field>
          </Form>
        }
      >
        <PodsList
          title='Pods'
          titleVariant={Card.titleVariants.medium}
          className='mt-2'
          nodeName={nodeName}
        />
      </ResourceDetailPage>
    );
  })
);
