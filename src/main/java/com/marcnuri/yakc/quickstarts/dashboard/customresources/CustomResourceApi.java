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
 * Created on 2020-11-25, 19:24
 */
package com.marcnuri.yakc.quickstarts.dashboard.customresources;

import com.marcnuri.yakc.api.Api;
import com.marcnuri.yakc.api.KubernetesCall;
import com.marcnuri.yakc.api.KubernetesListCall;
import com.marcnuri.yakc.model.io.k8s.apiextensionsapiserver.pkg.apis.apiextensions.v1.CustomResourceDefinition;
import com.marcnuri.yakc.model.io.k8s.apiextensionsapiserver.pkg.apis.apiextensions.v1.CustomResourceDefinitionVersion;
import com.marcnuri.yakc.model.io.k8s.apimachinery.pkg.apis.meta.v1.Status;

import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface CustomResourceApi extends Api {

  @HTTP(
    method = "GET",
    path = "/apis/{group}/{version}/{plural}"
  )
  @Headers({
    "Accept: */*"
  })
  KubernetesListCall<UntypedCustomResourceList, UntypedResource> listCustomResourceForAllNamespaces(
    @Path("group") String group,
    @Path("version") String version,
    @Path("plural") String plural
  );

  default KubernetesListCall<UntypedCustomResourceList, UntypedResource> listCustomResourceForAllNamespaces(
    CustomResourceDefinition crd) {

    return listCustomResourceForAllNamespaces(
      crd.getSpec().getGroup(),
      crd.getSpec().getVersions().stream().map(CustomResourceDefinitionVersion::getName).findFirst().orElseThrow(),
      crd.getSpec().getNames().getPlural()
    );
  }

  @HTTP(
    method = "GET",
    path = "/apis/{group}/{version}/namespaces/{namespace}/{plural}"
  )
  @Headers({
    "Accept: */*"
  })
  KubernetesListCall<UntypedCustomResourceList, UntypedResource> listNamespacedCustomResource(
    @Path("group") String group,
    @Path("version") String version,
    @Path("namespace") String namespace,
    @Path("plural") String plural
  );

  @HTTP(
    method = "DELETE",
    path = "/apis/{group}/{version}/{plural}/{name}"
  )
  @Headers({"Accept: */*"})
  KubernetesCall<Status> deleteCustomResource(
    @Path("group") String group,
    @Path("version") String version,
    @Path("plural") String plural,
    @Path("name") String name
  );

  @HTTP(
    method = "DELETE",
    path = "/apis/{group}/{version}/namespaces/{namespace}/{plural}/{name}"
  )
  @Headers({"Accept: */*"})
  KubernetesCall<Status> deleteNamespacedCustomResource(
    @Path("group") String group,
    @Path("version") String version,
    @Path("namespace") String namespace,
    @Path("plural") String plural,
    @Path("name") String name
  );

  @HTTP(
    method = "PUT",
    path = "/apis/{group}/{version}/{plural}/{name}",
    hasBody = true
  )
  @Headers({"Content-Type: application/json", "Accept: */*"})
  KubernetesCall<UntypedResource> replaceCustomResource(
    @Path("group") String group,
    @Path("version") String version,
    @Path("plural") String plural,
    @Path("name") String name,
    @Body UntypedResource customResource
  );

  @HTTP(
    method = "PUT",
    path = "/apis/{group}/{version}/namespaces/{namespace}/{plural}/{name}",
    hasBody = true
  )
  @Headers({"Content-Type: application/json", "Accept: */*"})
  KubernetesCall<UntypedResource> replaceNamespacedCustomResource(
    @Path("group") String group,
    @Path("version") String version,
    @Path("namespace") String namespace,
    @Path("plural") String plural,
    @Path("name") String name,
    @Body UntypedResource customResource
  );
}
