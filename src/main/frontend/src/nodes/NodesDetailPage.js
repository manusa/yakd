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
import metrics from '../metrics';
import n from './';
import p from '../pods';
import Card from '../components/Card';
import Form from '../components/Form';
import Minikube from '../components/icons/Minikube';
import ResourceDetailPage from '../components/ResourceDetailPage';
import DonutChart from '../components/DonutChart';

const Dial = ({title, description, partial, total, percent = partial / total * 100}) => (
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

const NodesDetailPage = ({node, isMinikube, pods}) => {
  const nodeName = metadata.selectors.name(node);
  const podsForNode = Object.values(p.selectors.podsBy(pods, {nodeName}));
  const requests = podsForNode
    .flatMap(p.selectors.containers)
    .map(c => c.resources.requests ?? {});
  const requestedCpu = requests
    .map(r => r.cpu ?? 0)
    .reduce((acc, c) => acc + metrics.selectors.quantityToScalar(c), 0);
  const allocatableMemory = metrics.selectors.quantityToScalar(n.selectors.statusAllocatableMemory(node));
  const requestedMemory = requests
    .map(r => r.memory ?? 0)
    .reduce((acc, c) => acc + metrics.selectors.quantityToScalar(c), 0);
  return (
    <ResourceDetailPage
      kind='Nodes'
      path='nodes'
      resource={node}
      isReadyFunction={n.selectors.isReady}
      actions={isMinikube && <Minikube className='ml-2 h-6' />}
      body={
        <Form>
          <div className='w-full mb-4 flex flex-wrap justify-around'>
            <Dial
              title='CPU'
              description='Requested vs. allocatable'
              partial={requestedCpu.toFixed(3)}
              total={metrics.selectors.quantityToScalar(n.selectors.statusAllocatableCpu(node)).toFixed(3)}
            />
            <Dial
              title='Memory'
              description='Requested vs. allocatable'
              percent={(requestedMemory / allocatableMemory * 100)}
              partial={metrics.selectors.bytesToHumanReadable(requestedMemory)}
              total={metrics.selectors.bytesToHumanReadable(allocatableMemory)}
            />
            <Dial
              title='Pods'
              description='Allocated vs. allocatable'
              partial={podsForNode.length}
              total={n.selectors.statusAllocatablePods(node)}
            />
          </div>

          <metadata.Details resource={node} />
          <Form.Field label='OS'>
            {n.selectors.statusNodeInfoOS(node)} ({n.selectors.statusNodeInfoArchitecture(node)})
          </Form.Field>
          <Form.Field label='Kernel Version'>
            {n.selectors.statusNodeInfoKernelVersion(node)}
          </Form.Field>
          <Form.Field label='Container Runtime'>
            {n.selectors.statusNodeInfoContainerRuntimeVersion(node)}
          </Form.Field>
          <Form.Field label='Kubelet Version'>
            {n.selectors.statusNodeInfoKubeletVersion(node)}
          </Form.Field>
        </Form>
      }
    >
      <p.List
        title='Pods'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        nodeName={nodeName}
      />
    </ResourceDetailPage>
  );
}

const mapStateToProps = ({nodes, pods}) => ({
  nodes,
  pods
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  isMinikube: n.selectors.isMinikube(stateProps.nodes),
  node: Object.values(stateProps.nodes).find(node =>
    metadata.selectors.name(node) === ownProps.params.name)
});

export default withParams(connect(mapStateToProps, null, mergeProps)(NodesDetailPage));
