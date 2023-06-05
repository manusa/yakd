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
package com.marcnuri.yakc.quickstarts.dashboard.persistentvolumes;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.core.v1.CoreV1Api;
import com.marcnuri.yakc.model.io.k8s.api.core.v1.PersistentVolume;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.ignoreForbidden;

@Singleton
public class PersistentVolumeService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public PersistentVolumeService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public List<PersistentVolume> get() throws IOException {
    return ignoreForbidden(
      () -> kubernetesClient.create(CoreV1Api.class).listPersistentVolume().get().getItems(),
      Collections.emptyList()
    );
  }

  public PersistentVolume deletePersistentVolume(String name) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).deletePersistentVolume(name).get();
  }

  public PersistentVolume updatePersistentVolume(String name, PersistentVolume persistentVolume) throws IOException {
    return kubernetesClient.create(CoreV1Api.class).replacePersistentVolume(name, persistentVolume).get();
  }
}
