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
 * Created on 2020-10-25, 7:22
 */
package com.marcnuri.yakd.quickstarts.dashboard.secrets;

import com.marcnuri.yakd.quickstarts.dashboard.watch.WatchEvent;
import com.marcnuri.yakd.quickstarts.dashboard.watch.Watchable;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.reactivex.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.LIMIT_1;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.observable;
import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class SecretService implements Watchable<Secret> {

  private final KubernetesClient kubernetesClient;

  @Inject
  public SecretService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  @Override
  public Observable<WatchEvent<Secret>> watch() {
    return tryInOrder(
      () -> {
        kubernetesClient.secrets().inAnyNamespace().list(LIMIT_1);
        return observable(kubernetesClient.secrets().inAnyNamespace());
      },
      () -> observable(kubernetesClient.secrets().inNamespace(kubernetesClient.getConfiguration().getNamespace()))
    );
  }

  public List<Secret> get() {
    return tryInOrder(
      () -> kubernetesClient.secrets().inAnyNamespace().list().getItems(),
      () -> kubernetesClient.secrets().inNamespace(kubernetesClient.getConfiguration().getNamespace()).list().getItems()
    );
  }

  public void deleteSecret(String name, String namespace) {
    kubernetesClient.secrets().inNamespace(namespace).withName(name).delete();
  }

  public Secret updateSecret(String name, String namespace, Secret secret) {
    return kubernetesClient.secrets().inNamespace(namespace)
      .resource(new SecretBuilder(secret).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
