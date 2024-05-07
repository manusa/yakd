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
import {resourcesBy, toObjectReducer} from '../redux';

export const statusPhase = pod => pod?.status?.phase ?? '';

export const statusPodIP = pod => pod?.status?.podIP ?? '';

export const nodeName = pod => pod?.spec?.nodeName ?? '';

export const restartPolicy = pod => pod?.spec?.restartPolicy ?? '';

export const containers = pod => pod?.spec?.containers ?? [];

export const containerStatuses = pod => pod?.status?.containerStatuses ?? [];

export const containersReady = pod => {
  const css = containerStatuses(pod);
  return css.length > 0 && css.every(cs => cs.ready);
};

export const isSucceded = pod => statusPhase(pod) === 'Succeeded';

export const succeededOrContainersReady = pod =>
  isSucceded(pod) || containersReady(pod);

export const restartCount = pod =>
  containerStatuses(pod).reduce(
    (acc, containerStatus) => acc + containerStatus.restartCount,
    0
  );

// Selectors for array of Pods

export const succeededCount = pods =>
  pods.reduce((count, pod) => (isSucceded(pod) ? count + 1 : count), 0);

export const readyCount = pods =>
  pods.reduce((count, pod) => (containersReady(pod) ? count + 1 : count), 0);

export const podsBy = (
  pods = {},
  {nodeName: filterNodeName, ...filters} = undefined
) =>
  Object.entries(resourcesBy(pods, filters))
    .filter(([, pod]) => {
      if (filterNodeName) {
        return nodeName(pod) === filterNodeName;
      }
      return true;
    })
    .reduce(toObjectReducer, {});
