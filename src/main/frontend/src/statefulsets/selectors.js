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
const selectors = {};

selectors.statusReplicas = statefulSet => statefulSet?.status?.replicas ?? 0;

selectors.statusReadyReplicas = statefulSet => statefulSet?.status?.readyReplicas ?? 0;

selectors.isReady = statefulSet => selectors.statusReplicas(statefulSet) === selectors.statusReadyReplicas(statefulSet);

selectors.containers = statefulSet => statefulSet?.spec?.template?.spec?.containers ?? [];

selectors.images = statefulSet => selectors.containers(statefulSet).map(c => c.image);

selectors.specReplicas = statefulSet => statefulSet?.spec?.replicas ?? 0;

// Selectors for array of StatefulSets

selectors.readyCount = statefulSets => statefulSets.reduce(
  (count, statefulSet) => selectors.isReady(statefulSet) ? count + 1 : count,
  0
);

export default selectors;
