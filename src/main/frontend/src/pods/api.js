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
import {getApiURL} from '../env';
import {
  deleteNamespacedResource,
  toJson,
  updateNamespacedResource
} from '../fetch';
import {name, namespace} from '../metadata';

export const logs = (namespace, name, container) =>
  new EventSource(`${getApiURL()}/pods/${namespace}/${name}/logs/${container}`);

export const metrics = async pod => {
  const response = await fetch(
    `${getApiURL()}/pods/${namespace(pod)}/${name(pod)}/metrics`
  );
  return await toJson(response);
};

const isAbsolute = url => /^https?:\/\//i.test(url);
const getWsUrl = () => {
  const apiURL = getApiURL();
  if (isAbsolute(apiURL)) {
    return apiURL.replace(/^http/i, 'ws');
  }
  const wsOrigin = window.location.origin.replace(/^http/i, 'ws');
  return `${wsOrigin}/${apiURL.replace(/^\//, '')}`;
};
export const exec = (namespace, name, container) =>
  new WebSocket(`${getWsUrl()}/pods/${namespace}/${name}/exec/${container}`);

export const deletePod = deleteNamespacedResource('pods');
export const update = updateNamespacedResource('pods');
