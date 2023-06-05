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

selectors.statusReplicas = replicationController => replicationController?.status?.replicas ?? 0;

selectors.statusReadyReplicas = replicationController => replicationController?.status?.readyReplicas ?? 0;

selectors.isReady = replicationController =>
  selectors.statusReplicas(replicationController) === selectors.statusReadyReplicas(replicationController);

selectors.specReplicas = replicationController => replicationController?.spec?.replicas ?? 0;

selectors.containers = replicationController => replicationController?.spec?.template?.spec?.containers ?? [];

// Selectors for array of ReplicationControllers

selectors.readyCount = replicationControllers => replicationControllers.reduce(
  (count, replicationController) => selectors.isReady(replicationController) ? count + 1 : count,
  0
);

export default selectors;
