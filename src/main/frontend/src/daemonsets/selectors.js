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
export const statusDesiredNumberScheduled = daemonSet =>
  daemonSet?.status?.desiredNumberScheduled ?? 0;

export const statusCurrentNumberScheduled = daemonSet =>
  daemonSet?.status?.currentNumberScheduled ?? 0;

export const isReady = daemonSet =>
  statusDesiredNumberScheduled(daemonSet) ===
  statusCurrentNumberScheduled(daemonSet);

export const containers = daemonSet =>
  daemonSet?.spec?.template?.spec?.containers ?? [];

export const images = daemonSet => containers(daemonSet).map(c => c.image);

export const specUpdateStrategyType = daemonSet =>
  daemonSet?.spec?.updateStrategy?.type ?? '';

// Selectors for array of daemonSets

export const readyCount = daemonSets =>
  daemonSets.reduce(
    (count, daemonSet) => (isReady(daemonSet) ? count + 1 : count),
    0
  );
