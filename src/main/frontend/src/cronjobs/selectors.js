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
export const specSchedule = cronJob => cronJob?.spec?.schedule ?? '';
export const specSuspend = cronJob => cronJob?.spec?.suspend === true;
export const specConcurrencyPolicy = cronJob =>
  cronJob?.spec?.concurrencyPolicy ?? '';

export const statusLastScheduleTime = cronJob => {
  const lst = cronJob?.status?.lastScheduleTime;
  if (lst) {
    return new Date(lst);
  }
};
export const statusActive = cronJob => cronJob?.status?.active ?? [];
export const statusActiveUids = cronJob =>
  statusActive(cronJob).map(aj => aj.uid);

export const containers = cronJob =>
  cronJob?.spec?.jobTemplate?.spec?.template?.spec?.containers ?? [];

export const isReady = cronJob => specSuspend(cronJob) === false;
