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
 * Created on 2020-09-06, 8:36
 */
package com.marcnuri.yakd.quickstarts.dashboard.ingresses;

import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressRuleValueBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBackend;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBackendBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressServiceBackendBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpec;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.ServiceBackendPortBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import static com.marcnuri.yakd.quickstarts.dashboard.fabric8.ClientUtil.tryInOrder;

@Singleton
public class IngressService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public IngressService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  static Ingress to(io.fabric8.kubernetes.api.model.extensions.Ingress from) {
    return new IngressBuilder()
      .withMetadata(from.getMetadata())
      .withSpec(to(from.getSpec()))
      .build();
  }

  static IngressSpec to(io.fabric8.kubernetes.api.model.extensions.IngressSpec from) {
    return new IngressSpecBuilder()
      .withIngressClassName(from.getIngressClassName())
      .withRules(from.getRules().stream().map(IngressService::to).toList())
      .build();
  }

  static IngressRule to(io.fabric8.kubernetes.api.model.extensions.IngressRule from) {
    return new IngressRuleBuilder()
      .withHost(from.getHost())
      .withHttp(to(from.getHttp()))
      .build();
  }

  static HTTPIngressRuleValue to(io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue from) {
    return new HTTPIngressRuleValueBuilder()
      .withPaths(from.getPaths().stream().map(IngressService::to).toList())
      .build();
  }

  static HTTPIngressPath to(io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath from) {
    return new HTTPIngressPathBuilder()
      .withPath(from.getPath())
      .withPathType(from.getPathType())
      .withBackend(to(from.getBackend()))
      .build();
  }

  static IngressBackend to(io.fabric8.kubernetes.api.model.extensions.IngressBackend from) {
    final var portBuilder = new ServiceBackendPortBuilder();
    portBuilder.withName(from.getServicePort().getStrVal());
    portBuilder.withNumber(from.getServicePort().getIntVal());
    return new IngressBackendBuilder()
      .withService(new IngressServiceBackendBuilder()
        .withName(from.getServiceName())
        .withPort(portBuilder.build())
        .build())
      .build();
  }

  public List<Ingress> get() {
    final String namespace = kubernetesClient.getConfiguration().getNamespace();
    return tryInOrder(
      () -> kubernetesClient.network().v1().ingresses().inAnyNamespace().list().getItems(),
      () -> kubernetesClient.network().v1().ingresses().inNamespace(namespace).list().getItems(),
      () -> kubernetesClient.extensions().ingresses().inAnyNamespace().list().getItems().stream().map(IngressService::to).toList(),
      () -> kubernetesClient.extensions().ingresses().inNamespace(namespace).list().getItems().stream().map(IngressService::to).toList()
    );
  }

  public void delete(String name, String namespace) {
    tryInOrder(
      () -> kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(name).delete(),
      () -> kubernetesClient.network().v1beta1().ingresses().inNamespace(namespace).withName(name).delete(),
      () -> kubernetesClient.extensions().ingresses().inNamespace(namespace).withName(name).delete()
    );
  }

  public Ingress updateIngress(String name, String namespace, Ingress ingress) {
    return kubernetesClient.network().v1().ingresses().inNamespace(namespace)
      .resource(new IngressBuilder(ingress).editMetadata().withName(name).endMetadata().build())
      .update();
  }
}
