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

selectors.statusDesiredNumberScheduled = daemonSet => daemonSet?.status?.desiredNumberScheduled ?? 0;

selectors.statusCurrentNumberScheduled = daemonSet => daemonSet?.status?.currentNumberScheduled ?? 0;

selectors.isReady = daemonSet =>
  selectors.statusDesiredNumberScheduled(daemonSet) === selectors.statusCurrentNumberScheduled(daemonSet);

selectors.containers = daemonSet => daemonSet?.spec?.template?.spec?.containers ?? [];

selectors.images = daemonSet => selectors.containers(daemonSet).map(c => c.image);

selectors.specUpdateStrategyType = daemonSet => daemonSet?.spec?.updateStrategy?.type ?? '';

// Selectors for array of daemonSets

selectors.readyCount = daemonSets => daemonSets.reduce(
  (count, daemonSet) => selectors.isReady(daemonSet) ? count + 1 : count,
  0
);

export default selectors;
