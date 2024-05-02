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

export const toDate = timestamp => {
  if (timestamp) {
    return new Date(timestamp);
  }
};
export const creationTimestamp = object =>
  toDate(object?.metadata?.creationTimestamp);
export const deletionTimestamp = object =>
  toDate(object?.metadata?.deletionTimestamp);

export const annotations = object => object?.metadata?.annotations ?? {};

export const labels = object => object?.metadata?.labels ?? {};

export const name = object => object?.metadata?.name ?? '';

export const namespace = object => object?.metadata?.namespace ?? '';

export const uid = object => object?.metadata?.uid ?? '';

export const ownerReferencesUids = object =>
  (object?.metadata?.ownerReferences ?? []).map(or => or.uid);

export const sortByCreationTimeStamp = (r1, r2) =>
  creationTimestamp(r2) - creationTimestamp(r1);

// Selectors for Map<uid, resource> of Metadata Resources

export const byUidOrName = (metadataResources, uidOrName) => {
  if (metadataResources[uidOrName]) {
    return metadataResources[uidOrName];
  }
  return Object.values(metadataResources).find(
    resource => name(resource) === uidOrName
  );
};
