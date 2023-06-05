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
 * Created on 2020-12-05, 10:00
 */
package com.marcnuri.yakc.quickstarts.dashboard;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.Optional;

/**
 * https://github.com/quarkusio/quarkus/issues/9671#issuecomment-704050467
 * Custom gzip support, workaround for https://github.com/quarkusio/quarkus/issues/9671
 *
 * NB: must be deleted when quarkus will have proper gzip support.
 *
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class GZIPSupport implements ContainerResponseFilter {

  private boolean isGZIPSupported(ContainerRequestContext containerRequestContext) {
    return Optional.ofNullable(containerRequestContext)
      .filter(r -> !r.getAcceptableMediaTypes().contains(MediaType.SERVER_SENT_EVENTS_TYPE)) // Vert.x still doesn't work
      .map(r -> r.getHeaderString(HttpHeaders.ACCEPT_ENCODING))
      .map(s -> s.split(","))
      .map(encodings -> Arrays.stream(encodings).anyMatch("gzip"::equals))
      .orElse(false);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (isGZIPSupported(requestContext)) {
      MultivaluedMap<String, Object> headers = responseContext.getHeaders();
      headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
      // Do nothing
      // GZIPEncodingInterceptor will take care of encapsulating the outputStream
    }
  }
}
