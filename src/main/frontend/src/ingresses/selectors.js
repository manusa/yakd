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

export const specRules = ingress => ingress?.spec?.rules ?? [];

export const allHosts = ingress => specRules(ingress).map(r => r.host);

export const allPaths = ingress =>
  specRules(ingress)
    .flatMap(r => r.http.paths)
    .map(p => p.path);
