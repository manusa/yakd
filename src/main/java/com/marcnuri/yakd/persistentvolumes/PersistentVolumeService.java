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
 * Created on 2020-11-01, 16:38
 */
package com.marcnuri.yakd.persistentvolumes;

import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

import static com.marcnuri.yakd.fabric8.ClientUtil.ignoreForbidden;

@Singleton
public class PersistentVolumeService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public PersistentVolumeService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<PersistentVolume> get() {
    return ignoreForbidden(
      () -> kubernetesClient.persistentVolumes().list().getItems(),
      Collections.emptyList()
    );
  }

  public void deletePersistentVolume(String name) {
    kubernetesClient.persistentVolumes().withName(name).delete();
  }

  public PersistentVolume updatePersistentVolume(String name, PersistentVolume persistentVolume) {
    return kubernetesClient.persistentVolumes()
      .resource(new PersistentVolumeBuilder(persistentVolume).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
