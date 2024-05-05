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
  listResource,
  deleteResource,
  deleteNamespacedResource,
  updateNamespacedResource,
  updateResource
} from '../fetch';
import {selectors as crdSelectors} from '../customresourcedefinitions';

const basePath = (crd, version = crdSelectors.specVersionsLatest(crd)) =>
  `customresources/${crdSelectors.specGroup(
    crd
  )}/${version}/${crdSelectors.specNamesPlural(crd)}`;

export const list = (crd, version) =>
  listResource(
    `customresources/${crdSelectors.specGroup(crd)}/${
      version ? version : crdSelectors.specVersionsLatest(crd)
    }/${crdSelectors.specNamesPlural(crd)}`,
    crdSelectors.specNamesKind(crd)
  );
export const deleteCr = (crd, version) => {
  const path = basePath(crd, version);
  if (crdSelectors.isNamespaced(crd)) {
    return deleteNamespacedResource(`${path}/namespaces`);
  }
  return deleteResource(path);
};
export const update = (crd, version) => {
  const path = basePath(crd, version);
  if (crdSelectors.isNamespaced(crd)) {
    return updateNamespacedResource(`${path}/namespaces`);
  }
  return updateResource(path);
};
