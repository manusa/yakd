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

selectors.specStorageClassName = persistentVolume => persistentVolume?.spec?.storageClassName ?? '';

selectors.specCapacityStorage = persistentVolume => persistentVolume?.spec?.capacity?.storage ?? '';

selectors.specClaim = persistentVolume => persistentVolume?.spec?.claimRef ?? {};
selectors.specClaimKind = persistentVolume => selectors.specClaim(persistentVolume).kind ?? '';
selectors.specClaimUid = persistentVolume => selectors.specClaim(persistentVolume).uid ?? '';
selectors.specClaimName = persistentVolume => selectors.specClaim(persistentVolume).name ?? '';
selectors.specClaimNamespace = persistentVolume => selectors.specClaim(persistentVolume).namespace ?? '';

selectors.specReclaimPolicy = persistentVolume => persistentVolume?.spec?.persistentVolumeReclaimPolicy ?? '';

selectors.specVolumeMode = persistentVolume => persistentVolume?.spec?.volumeMode ?? '';

selectors.statusPhase = persistentVolume => persistentVolume?.status?.phase ?? '';

export default selectors;
