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
 * Created on 2020-09-04, 17:43
 */
package com.marcnuri.yakc.quickstarts.dashboard;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Map;

@Path("/")
public class GatewayResource {

  private static final String FALLBACK_RESOURCE = "/frontend/index.html";
  private static final Map<String, String> EXTENSION_TYPES = Map.of(
    "svg", "image/svg+xml"
  );
  private final ApiResource apiResource;

  @Inject
  public GatewayResource(ApiResource apiResource) {
    this.apiResource = apiResource;
  }

  @Path("/api/v1")
  public ApiResource getApiResource() {
    return apiResource;
  }

  @GET
  @Path("/")
  public Response getFrontendRoot() throws IOException {
    return getFrontendStaticFile("index.html");
  }

  @GET
  @Path("/{fileName:.+}")
  public Response getFrontendStaticFile(@PathParam("fileName") String fileName) throws IOException {
    final InputStream requestedFileStream = GatewayResource.class.getResourceAsStream("/frontend/" + fileName);
    final InputStream inputStream;
    final String fileToServe;
    if (requestedFileStream != null) {
      fileToServe = fileName;
      inputStream = requestedFileStream;
    } else {
      fileToServe = FALLBACK_RESOURCE;
      inputStream = GatewayResource.class.getResourceAsStream(FALLBACK_RESOURCE);
    }
    final StreamingOutput streamingOutput = outputStream -> IOUtils.copy(inputStream, outputStream);
    return Response
      .ok(streamingOutput)
      .cacheControl(CacheControl.valueOf("max-age=900"))
      .type(contentType(inputStream, fileToServe))
      .build();
  }

  private String contentType(InputStream inputStream, String file) throws IOException {
    return EXTENSION_TYPES.getOrDefault(
      FilenameUtils.getExtension(file),
      URLConnection.guessContentTypeFromStream(inputStream)
    );
  }
}
