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
  name,
  namespace as metadataNamespace,
  ownerReferencesUids,
  uid
} from '../metadata';

export const toObjectReducer = (acc, [key, configMap]) => {
  acc[key] = configMap;
  return acc;
};

export const resourcesBy = (
  resources = {},
  {namespace, names, nameLike, ownerUid, ownerUids, uids, uidsNotIn} = undefined
) =>
  Object.entries(resources)
    .filter(([, resource]) => {
      if (namespace && metadataNamespace(resource) !== namespace) {
        return false;
      }
      if (names && !names.includes(name(resource))) {
        return false;
      }
      if (
        nameLike &&
        !name(resource).toUpperCase().includes(nameLike.toUpperCase())
      ) {
        return false;
      }
      const ownerRefs = ownerReferencesUids(resource);
      if (ownerUid && !ownerRefs.includes(ownerUid)) {
        return false;
      }
      if (
        ownerUids &&
        !ownerRefs.some(ownerUid => ownerUids.includes(ownerUid))
      ) {
        return false;
      }
      if (uids && !uids.includes(uid(resource))) {
        return false;
      }
      if (uidsNotIn?.includes(uid(resource))) {
        return false;
      }
      return true;
    })
    .reduce(toObjectReducer, {});
