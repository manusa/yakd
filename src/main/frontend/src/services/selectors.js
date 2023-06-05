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

selectors.specClusterIP = service => service?.spec?.clusterIP ?? '';

selectors.specExternalIPs = service => service?.spec?.externalIPs ?? [];

selectors.specSelector = service => service?.spec?.selector ?? {};

selectors.specPorts = service => service?.spec?.ports ?? [];

selectors.specPortsFirstNodePort = service => selectors.specPorts(service)
  .map(p => p.nodePort).find(np => np) ?? 0;

selectors.specType = service => service?.spec?.type ?? '';

export default selectors;
