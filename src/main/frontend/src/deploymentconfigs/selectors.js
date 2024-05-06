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
export const specReplicas = deploymentConfig =>
  deploymentConfig?.spec?.replicas ?? 0;

export const statusReadyReplicas = deploymentConfig =>
  deploymentConfig?.status?.readyReplicas ?? 0;

export const isReady = deploymentConfig =>
  specReplicas(deploymentConfig) === statusReadyReplicas(deploymentConfig);

export const containers = deploymentConfig =>
  deploymentConfig?.spec?.template?.spec?.containers ?? [];

export const images = deploymentConfig =>
  containers(deploymentConfig).map(c => c.image);

export const specStrategyType = deploymentConfig =>
  deploymentConfig?.spec?.strategy?.type ?? '';

// Selectors for array of deploymentConfigs

export const readyCount = deploymentConfigs =>
  deploymentConfigs.reduce(
    (count, deploymentConfig) =>
      isReady(deploymentConfig) ? count + 1 : count,
    0
  );
