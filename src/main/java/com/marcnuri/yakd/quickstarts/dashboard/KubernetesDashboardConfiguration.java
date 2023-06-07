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
 * Created on 2020-09-04, 16:09
 */
package com.marcnuri.yakd.quickstarts.dashboard;

import com.marcnuri.yakc.KubernetesClient;
import com.marcnuri.yakc.config.ConfigurationResolver;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

@Singleton
public class KubernetesDashboardConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesDashboardConfiguration.class);

  @ConfigProperty(name = "yakd.dashboard.insecureSkipTlsVerify", defaultValue = "false")
  boolean insecureSkipTlsVerify;

  void onStart(@Observes StartupEvent event) {
    // Keep logs clean
    Infrastructure.setDroppedExceptionHandler(ex ->
      LOG.error("Mutiny subscription closed with dropped exception {}", ex.getMessage()));
  }

  @Produces
  @Singleton
  @Priority(Integer.MAX_VALUE)
  public Config fabric8Config() {
    return new ConfigBuilder(Config.autoConfigure(null))
      .withTrustCerts(insecureSkipTlsVerify)
      .withConnectionTimeout(0)
      .withRequestTimeout(0)
      .build();
  }

  @Produces
  @Singleton
  public KubernetesClient kubernetesClient() throws IOException {
    LOG.info("Initializing KubernetesClient...");
    LOG.info(" - Skip TLS verification: {}", insecureSkipTlsVerify);
    return new KubernetesClient(
      ConfigurationResolver.resolveConfig().toBuilder()
        .insecureSkipTlsVerify(insecureSkipTlsVerify)
        .readTimeout(Duration.ZERO)
        .build());
  }
}