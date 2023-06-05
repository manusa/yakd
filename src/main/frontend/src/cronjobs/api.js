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
import {deleteNamespacedResource, updateNamespacedResource,} from '../fetch';
import {getApiURL} from "../env";
import metadata from "../metadata";

const updateSuspend = async (resource, suspend) => {
  await fetch(
    `${getApiURL()}/cronjobs/${metadata.selectors.namespace(resource)}/${metadata.selectors.name(resource)}/spec/suspend/${suspend}`,
    {method: 'PUT'}
  );
};

const trigger =  async resource => {
  await fetch(
    `${getApiURL()}/cronjobs/${metadata.selectors.namespace(resource)}/${metadata.selectors.name(resource)}/trigger`,
    {method: 'PUT'}
  );
};

const api = {
  delete: deleteNamespacedResource('cronjobs'),
  update: updateNamespacedResource('cronjobs'),
  updateSuspend,
  trigger
};

export default api;
