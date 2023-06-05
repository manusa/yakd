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
package com.marcnuri.yakc.quickstarts.dashboard.ingresses;

import static com.marcnuri.yakc.quickstarts.dashboard.ClientUtil.tryWithFallback;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.api.extensions.v1beta1.ExtensionsV1beta1Api;
import com.marcnuri.yakc.api.networking.v1.NetworkingV1Api;
import com.marcnuri.yakc.api.networking.v1beta1.NetworkingV1beta1Api;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.HTTPIngressPath;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.HTTPIngressRuleValue;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.Ingress;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.IngressBackend;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.IngressRule;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.IngressServiceBackend;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.IngressSpec;
import com.marcnuri.yakc.model.io.k8s.api.networking.v1.ServiceBackendPort;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;

@Singleton
public class IngressService {

  private final KubernetesClient kubernetesClient;

  @Inject
  public IngressService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  static Ingress to(com.marcnuri.yakc.model.io.k8s.api.extensions.v1beta1.Ingress from) {
    return Ingress.builder()
      .apiVersion(from.getApiVersion())
      .kind(from.getKind())
      .metadata(from.getMetadata())
      .spec(to(from.getSpec()))
      .build();
  }

  static IngressSpec to(com.marcnuri.yakc.model.io.k8s.api.extensions.v1beta1.IngressSpec from) {
    return IngressSpec.builder()
      .ingressClassName(from.getIngressClassName())
      .rules(from.getRules().stream().map(IngressService::to).collect(Collectors.toList()))
      .build();
  }

  static IngressRule to(com.marcnuri.yakc.model.io.k8s.api.extensions.v1beta1.IngressRule from) {
    return IngressRule.builder()
      .host(from.getHost())
      .http(to(from.getHttp()))
      .build();
  }

  static HTTPIngressRuleValue to(com.marcnuri.yakc.model.io.k8s.api.extensions.v1beta1.HTTPIngressRuleValue from) {
    return HTTPIngressRuleValue.builder()
      .paths(from.getPaths().stream().map(IngressService::to).collect(Collectors.toList()))
      .build();
  }

  static HTTPIngressPath to(com.marcnuri.yakc.model.io.k8s.api.extensions.v1beta1.HTTPIngressPath from) {
    return HTTPIngressPath.builder()
      .path(from.getPath())
      .pathType(from.getPathType())
      .backend(to(from.getBackend()))
      .build();
  }

  static IngressBackend to(com.marcnuri.yakc.model.io.k8s.api.extensions.v1beta1.IngressBackend from) {
    final ServiceBackendPort.Builder sbp = ServiceBackendPort.builder();
    try {
      sbp.number(Integer.parseInt(from.getServicePort()));
    } catch (NumberFormatException ex) {
      sbp.name(from.getServicePort());
    }
    return IngressBackend.builder()
      .service(IngressServiceBackend.builder()
        .name(from.getServiceName())
        .port(sbp.build())
        .build())
      .build();
  }

  public List<Ingress> get() throws IOException {
    final NetworkingV1Api net = kubernetesClient.create(NetworkingV1Api.class);
    final ExtensionsV1beta1Api ext = kubernetesClient.create(ExtensionsV1beta1Api.class);

    return tryWithFallback(
      () -> net.listIngressForAllNamespaces().get().getItems(),
      () -> net.listNamespacedIngress(kubernetesClient.getConfiguration().getNamespace())
        .get().getItems(),
      () -> ext.listIngressForAllNamespaces().stream().map(IngressService::to).collect(Collectors.toList()),
      () -> ext.listNamespacedIngress(kubernetesClient.getConfiguration().getNamespace())
        .stream().map(IngressService::to).collect(Collectors.toList())
    );
  }

  public Status delete(String name, String namespace) throws IOException {
    return tryWithFallback(
      () -> kubernetesClient.create(NetworkingV1Api.class).deleteNamespacedIngress(name, namespace).get(),
      () -> kubernetesClient.create(NetworkingV1beta1Api.class).deleteNamespacedIngress(name, namespace).get(),
      () -> kubernetesClient.create(ExtensionsV1beta1Api.class).deleteNamespacedIngress(name, namespace).get()
    );
  }

  public Ingress updateIngress(String name, String namespace, Ingress ingress) throws IOException {
    return kubernetesClient.create(NetworkingV1Api.class).replaceNamespacedIngress(name, namespace, ingress).get();
  }
}
