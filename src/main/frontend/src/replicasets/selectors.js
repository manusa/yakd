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
export const statusReplicas = replicaSet => replicaSet?.status?.replicas ?? 0;

export const statusReadyReplicas = replicaSet =>
  replicaSet?.status?.readyReplicas ?? 0;

export const isReady = replicaSet =>
  statusReplicas(replicaSet) === statusReadyReplicas(replicaSet);

export const specReplicas = replicaSet => replicaSet?.spec?.replicas ?? 0;

// Selectors for array of ReplicaSets

export const readyCount = replicaSets =>
  replicaSets.reduce(
    (count, replicaSet) => (isReady(replicaSet) ? count + 1 : count),
    0
  );
