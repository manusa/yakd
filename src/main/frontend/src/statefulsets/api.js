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
import {
  deleteNamespacedResource,
  restartNamespacedResource,
  updateNamespacedResource,
  updateReplicasInNamespacedResource
} from '../fetch';

const api = {};

api.delete = deleteNamespacedResource('statefulsets');

api.update = updateNamespacedResource('statefulsets');

api.restart = restartNamespacedResource('statefulsets');

api.updateReplicas = updateReplicasInNamespacedResource('statefulsets');

export default api;
