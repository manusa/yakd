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
import crds from '../customresourcedefinitions';

const basePath = (crd, version = crds.selectors.specVersionsLatest(crd)) =>
  `customresources/${crds.selectors.specGroup(crd)}/${version}/${crds.selectors.specNamesPlural(crd)}`;

const api = {
  list: (crd, version) => listResource(
    `customresources/${
      crds.selectors.specGroup(crd)
    }/${
      version ? version : crds.selectors.specVersionsLatest(crd)
    }/${
      crds.selectors.specNamesPlural(crd)
    }`,
    crds.selectors.specNamesKind(crd)
  ),
  delete: (crd, version) => {
    const path = basePath(crd, version);
    if (crds.selectors.isNamespaced(crd)) {
      return deleteNamespacedResource(`${path}/namespaces`);
    }
    return deleteResource(path);
  },
  update: (crd, version) => {
    const path = basePath(crd, version);
    if (crds.selectors.isNamespaced(crd)) {
      return updateNamespacedResource(`${path}/namespaces`);
    }
    return updateResource(path);
  }
};

export default api;
