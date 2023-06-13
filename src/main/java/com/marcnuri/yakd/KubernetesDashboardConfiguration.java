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
package com.marcnuri.yakd;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class KubernetesDashboardConfiguration {

  public static final String WATCH_EXECUTOR_SERVICE = "watchExecutorService";

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesDashboardConfiguration.class);

  @ConfigProperty(name = "yakd.dashboard.insecureSkipTlsVerify", defaultValue = "false")
  boolean insecureSkipTlsVerify;

  void onStart(@Observes StartupEvent event) {
    // Keep logs clean
    Infrastructure.setDroppedExceptionHandler(ex ->
      LOG.error("Mutiny subscription closed with dropped exception {}", ex.getMessage()));
  }

  void onEnd(
    @Observes ShutdownEvent event,
    @Named(WATCH_EXECUTOR_SERVICE) ScheduledExecutorService executorService
  ) {
    executorService.shutdown();
  }

  @Produces
  @Singleton
  @Named(WATCH_EXECUTOR_SERVICE)
  public ScheduledExecutorService watchExecutorService() {
    return Executors.newScheduledThreadPool(1);
  }

  @Produces
  @Singleton
  @Priority(Integer.MAX_VALUE)
  public Config fabric8Config() {
    LOG.info("Initializing Kubernetes Client Configuration...");
    LOG.info(" - Skip TLS verification: {}", insecureSkipTlsVerify);
    return new ConfigBuilder(Config.autoConfigure(null))
      .withTrustCerts(insecureSkipTlsVerify)
      .withConnectionTimeout(0)
      .withRequestTimeout(0)
      .build();
  }
}
