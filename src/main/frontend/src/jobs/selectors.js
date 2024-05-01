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
export const specCompletions = job => job?.spec?.completions ?? 0;
export const specParallelism = job => job?.spec?.parallelism ?? 0;

export const containers = job => job?.spec?.template?.spec?.containers ?? [];

export const statusSucceeded = job => job?.status?.succeeded ?? 0;

export const isComplete = job =>
  job?.status?.conditions ??
  []
    .filter(condition => condition.type === 'Complete')
    .filter(condition => condition.status.equalsIgnoreCase('true')).length > 0;
