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
import {resourcesBy, toObjectReducer} from '../redux';

export const specScope = crd => crd?.spec?.scope ?? '';
export const isNamespaced = crd => specScope(crd) === 'Namespaced';

export const specGroup = crd => crd?.spec?.group ?? '';

export const specVersions = crd => (crd?.spec?.versions ?? []).map(v => v.name);
export const specVersionsLatest = crd => {
  const sorted = specVersions(crd).sort((a, b) => b.localeCompare(a));
  return sorted.length > 0 ? sorted[0] : '';
};

export const specNames = crd => crd?.spec?.names ?? {};
export const specNamesKind = crd => specNames(crd)?.kind ?? '';
export const specNamesPlural = crd => specNames(crd)?.plural ?? '';

// Selectors for array of CRDs

export const crdsBy = (crds = {}, {group, ...filters} = undefined) =>
  Object.entries(resourcesBy(crds, filters))
    .filter(([, crd]) => {
      if (group) {
        return specGroup(crd) === group;
      }
      return true;
    })
    .reduce(toObjectReducer, {});

export const groups = (crds = {}) =>
  [...new Set(Object.values(crds).map(crd => specGroup(crd)))].sort();
