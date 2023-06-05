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

const toDate = timestamp => {
  if (timestamp) {
    return new Date(timestamp);
  }
}
selectors.creationTimestamp = object => toDate(object?.metadata?.creationTimestamp);
selectors.deletionTimestamp = object => toDate(object?.metadata?.deletionTimestamp);

selectors.annotations = object => object?.metadata?.annotations ?? {};

selectors.labels = object => object?.metadata?.labels ?? {};

selectors.name = object => object?.metadata?.name ?? '';

selectors.namespace = object => object?.metadata?.namespace ?? '';

selectors.uid = object => object?.metadata?.uid ?? '';

selectors.ownerReferencesUids = object => (object?.metadata?.ownerReferences ?? [])
  .map(or => or.uid);

selectors.sortByCreationTimeStamp = (r1, r2) =>
  selectors.creationTimestamp(r2) - selectors.creationTimestamp(r1)

// Selectors for Map<uid, resource> of Metadata Resources

selectors.byUidOrName = (metadataResources, uidOrName) => {
  if (metadataResources[uidOrName]) {
    return metadataResources[uidOrName];
  }
  return Object.values(metadataResources)
  .find(resource => selectors.name(resource) === uidOrName);
};

export default selectors;
