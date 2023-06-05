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
import redux from '../redux';

const selectors = {};

selectors.statusPhase = pod => pod?.status?.phase ?? '';

selectors.statusPodIP = pod => pod?.status?.podIP ?? '';

selectors.nodeName = pod => pod?.spec?.nodeName ?? '';

selectors.restartPolicy = pod => pod?.spec?.restartPolicy ?? '';

selectors.containers = pod => (pod?.spec?.containers ?? []);

selectors.containerStatuses = pod => (pod?.status?.containerStatuses ?? []);

selectors.containersReady = pod => {
  const css = selectors.containerStatuses(pod);
  return css.length > 0 && css.every(cs => cs.ready);
};

selectors.isSucceded = pod => selectors.statusPhase(pod) === 'Succeeded';

selectors.succeededOrContainersReady = pod =>
  selectors.isSucceded(pod) || selectors.containersReady(pod);

selectors.restartCount = pod => selectors.containerStatuses(pod).reduce(
  (acc, containerStatus) => acc + containerStatus.restartCount,
  0
);

// Selectors for array of Pods

selectors.succeededCount = pods => pods.reduce(
  (count, pod) => selectors.isSucceded(pod) ? count + 1 : count,
  0
);

selectors.readyCount = pods => pods.reduce(
  (count, pod) => selectors.containersReady(pod) ? count + 1 : count,
  0
);

selectors.podsBy = (pods = {}, {
  nodeName,
  ...filters
} = undefined) =>
  Object.entries(redux.selectors.resourcesBy(pods, filters))
  .filter(([, pod]) => {
    if (nodeName) {
      return selectors.nodeName(pod) === nodeName;
    }
    return true;
  })
  .reduce(redux.selectors.toObjectReducer, {});

export default selectors;
