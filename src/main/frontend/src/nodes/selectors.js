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
import metadata from '../metadata';

const s = {};

s.isReady = node => {
  const ready = (node?.status?.conditions ?? [])
    .find(condition => condition.type === 'Ready');
  return ready && ready.status;
};

s.statusAllocatable = node => node?.status?.allocatable ?? {};
s.statusAllocatablePods = node => s.statusAllocatable(node).pods ?? 0;
s.statusAllocatableCpu = node => s.statusAllocatable(node).cpu ?? 0;
s.statusAllocatableMemory = node => s.statusAllocatable(node).memory ?? 0;

s.statusNodeInfo = node => node?.status?.nodeInfo ?? {};
s.statusNodeInfoArchitecture = node => s.statusNodeInfo(node).architecture ?? '';
s.statusNodeInfoContainerRuntimeVersion = node => s.statusNodeInfo(node).containerRuntimeVersion ?? '';
s.statusNodeInfoKernelVersion = node => s.statusNodeInfo(node).kernelVersion ?? '';
s.statusNodeInfoKubeletVersion = node => s.statusNodeInfo(node).kubeletVersion ?? '';
s.statusNodeInfoOS = node => s.statusNodeInfo(node).operatingSystem ?? '';

s.statusAddresses = node => node?.status?.addresses ?? [];

s.statusAddressExternalIPOrFirst = node =>
  s.statusAddresses(node).filter(a => a.type === 'ExternalIP')
    .map(a => a.address).find(a => a) ?? s.statusAddressesFirstAddress(node);

s.statusAddressesFirstAddress = node =>
  s.statusAddresses(node).map(a => a.address ?? '').find(a => a) ?? '';

s.roles = node => Object.keys(metadata.selectors.labels(node))
  .filter(key => key.indexOf('node-role.kubernetes.io/') === 0)
  .map(key => key.split('/')[1]);

// Selectors for array of Nodes

s.readyCount = nodes => nodes.reduce(
  (count, node) => s.isReady(node) ? count + 1 : count,
  0
);

s.isMinikube = nodes => Object.values(nodes).length === 1 && Object.values(nodes)
  .filter(node => metadata.selectors.name(node) === 'minikube')
  .filter(node => metadata.selectors.labels(node)['minikube.k8s.io/name'] === 'minikube')
  .filter(node => metadata.selectors.labels(node).hasOwnProperty('minikube.k8s.io/version'))
  .length === 1;

export default s;
