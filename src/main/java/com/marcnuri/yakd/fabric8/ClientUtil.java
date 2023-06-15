/*
 * Copyright 2023 Marc Nuri
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
 * Created on 2023-06-07, 14:30
 */
package com.marcnuri.yakd.fabric8;

import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ClientUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ClientUtil.class);

  private ClientUtil() {
  }

  public static final ListOptions LIMIT_1 = new ListOptionsBuilder().withLimit(1L).build();

  @SafeVarargs
  public static <T> T tryInOrder(Supplier<T>... functions) {
    KubernetesClientException lastException = new KubernetesClientException(
      "Error while executing Kubernetes Client function");
    for (var function : functions) {
      try {
        return function.get();
      } catch (KubernetesClientException ex) {
        lastException = ex;
      }
    }
    throw lastException;
  }

  public static <T> T ignoreForbidden(Supplier<T> function, T defaultIfForbidden) {
    try {
      return function.get();
    } catch (KubernetesClientException ex) {
      if (ex.getCode() != 403) {
        throw ex;
      }
      LOG.debug("Access to resource is forbidden, ignoring:\n{}", ex.getMessage());
      return defaultIfForbidden;
    }
  }
}
