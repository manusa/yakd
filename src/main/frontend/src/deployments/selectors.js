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

selectors.statusReplicas = deployment => deployment?.status?.replicas ?? 0;

selectors.statusReadyReplicas = deployment => deployment?.status?.readyReplicas ?? 0;

selectors.isReady = deployment => selectors.statusReplicas(deployment) === selectors.statusReadyReplicas(deployment);

selectors.containers = deployment => deployment?.spec?.template?.spec?.containers ?? [];

selectors.images = deployment => selectors.containers(deployment).map(c => c.image);

selectors.specReplicas = deployment => deployment?.spec?.replicas ?? 0;

selectors.specStrategyType = deployment => deployment?.spec?.strategy?.type ?? '';

// Selectors for array of Deployments

selectors.readyCount = deployments => deployments.reduce(
  (count, deployment) => selectors.isReady(deployment) ? count + 1 : count,
  0
);

export default selectors;
