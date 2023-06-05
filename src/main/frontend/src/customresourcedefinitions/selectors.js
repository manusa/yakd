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
import _ from './index';
import redux from "../redux";

const selectors = {};

selectors.specScope = crd => crd?.spec?.scope ?? '';
selectors.isNamespaced = crd => _.selectors.specScope(crd) === 'Namespaced';

selectors.specGroup = crd => crd?.spec?.group ?? '';

selectors.specVersions = crd => (crd?.spec?.versions ?? []).map(v => v.name);
selectors.specVersionsLatest = crd => {
  const sorted = selectors.specVersions(crd).sort((a, b) => b.localeCompare(a));
  return sorted.length > 0 ? sorted[0] : '';
};

selectors.specNames = crd => crd?.spec?.names ?? {};
selectors.specNamesKind = crd => selectors.specNames(crd)?.kind ?? '';
selectors.specNamesPlural = crd => selectors.specNames(crd)?.plural ?? '';

// Selectors for array of CRDs

selectors.crdsBy = (crds = {}, {
  group,
  ...filters
} = undefined) =>
  Object.entries(redux.selectors.resourcesBy(crds, filters))
    .filter(([, crd]) => {
    if (group) {
      return selectors.specGroup(crd) === group;
    }
    return true;
  })
  .reduce(redux.selectors.toObjectReducer, {});

selectors.groups = (crds = {}) =>
  [...new Set(Object.values(crds).map(crd => selectors.specGroup(crd)))].sort();

export default selectors;
