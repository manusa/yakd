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
 * Created on 2020-10-18, 8:22
 */
package com.marcnuri.yakc.quickstarts.dashboard;


import com.marcnuri.yakc.api.KubernetesException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class YAKCExceptionMapper implements ExceptionMapper<KubernetesException> {

  @Override
  public Response toResponse(KubernetesException e) {
    return Response
      .status(e.getCode())
      .header("YAKC-Exception-Type", e.getClass().getName())
      .type(MediaType.TEXT_PLAIN_TYPE)
      .entity(e.getMessage())
      .build();
  }
}
