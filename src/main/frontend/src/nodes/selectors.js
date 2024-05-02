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
import {labels, name} from '../metadata';

export const isReady = node => {
  const ready = (node?.status?.conditions ?? []).find(
    condition => condition.type === 'Ready'
  );
  return ready && ready.status;
};

export const statusAllocatable = node => node?.status?.allocatable ?? {};
export const statusAllocatablePods = node => statusAllocatable(node).pods ?? 0;
export const statusAllocatableCpu = node => statusAllocatable(node).cpu ?? 0;
export const statusAllocatableMemory = node =>
  statusAllocatable(node).memory ?? 0;

export const statusNodeInfo = node => node?.status?.nodeInfo ?? {};
export const statusNodeInfoArchitecture = node =>
  statusNodeInfo(node).architecture ?? '';
export const statusNodeInfoContainerRuntimeVersion = node =>
  statusNodeInfo(node).containerRuntimeVersion ?? '';
export const statusNodeInfoKernelVersion = node =>
  statusNodeInfo(node).kernelVersion ?? '';
export const statusNodeInfoKubeletVersion = node =>
  statusNodeInfo(node).kubeletVersion ?? '';
export const statusNodeInfoOS = node =>
  statusNodeInfo(node).operatingSystem ?? '';

export const statusAddresses = node => node?.status?.addresses ?? [];

export const statusAddressExternalIPOrFirst = node =>
  statusAddresses(node)
    .filter(a => a.type === 'ExternalIP')
    .map(a => a.address)
    .find(a => a) ?? statusAddressesFirstAddress(node);

export const statusAddressesFirstAddress = node =>
  statusAddresses(node)
    .map(a => a.address ?? '')
    .find(a => a) ?? '';

export const roles = node =>
  Object.keys(labels(node))
    .filter(key => key.indexOf('node-role.kubernetes.io/') === 0)
    .map(key => key.split('/')[1]);

// Selectors for array of Nodes

export const readyCount = nodes =>
  nodes.reduce((count, node) => (isReady(node) ? count + 1 : count), 0);

export const isMinikube = nodes =>
  Object.values(nodes).length === 1 &&
  Object.values(nodes)
    .filter(node => name(node) === 'minikube')
    .filter(node => labels(node)['minikube.k8s.io/name'] === 'minikube')
    .filter(node => labels(node).hasOwnProperty('minikube.k8s.io/version'))
    .length === 1;
