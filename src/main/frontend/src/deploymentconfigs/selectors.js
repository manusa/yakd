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

selectors.specReplicas = deploymentConfig => deploymentConfig?.spec?.replicas ?? 0;

selectors.statusReadyReplicas = deploymentConfig => deploymentConfig?.status?.readyReplicas ?? 0;

selectors.isReady = deploymentConfig =>
  selectors.specReplicas(deploymentConfig) === selectors.statusReadyReplicas(deploymentConfig);

selectors.containers = deploymentConfig => deploymentConfig?.spec?.template?.spec?.containers ?? [];

selectors.images = deploymentConfig => selectors.containers(deploymentConfig).map(c => c.image);

selectors.specReplicas = deploymentConfig => deploymentConfig?.spec?.replicas ?? 0;

selectors.specStrategyType = deploymentConfig => deploymentConfig?.spec?.strategy?.type ?? '';

// Selectors for array of deploymentConfigs

selectors.readyCount = deploymentConfigs => deploymentConfigs.reduce(
  (count, deploymentConfig) => selectors.isReady(deploymentConfig) ? count + 1 : count,
  0
);

export default selectors;
