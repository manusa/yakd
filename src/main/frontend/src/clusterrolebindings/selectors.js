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
import redux from "../redux";

const selectors = {};

selectors.roleRefName = crb => crb?.roleRef?.name ?? '';

// Selectors for array of ClusterRoleBindings

selectors.crbsBy = (crbs = {}, {
  roleRefName,
  ...filters
} = undefined) =>
  Object.entries(redux.selectors.resourcesBy(crbs, filters))
    .filter(([, crb]) => {
      if (roleRefName) {
        return selectors.roleRefName(crb) === roleRefName;
      }
      return true;
    })
    .reduce(redux.selectors.toObjectReducer, {});

export default selectors;
