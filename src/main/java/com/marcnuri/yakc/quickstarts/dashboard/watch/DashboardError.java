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
 * Created on 2020-09-20, 9:25
 */
package com.marcnuri.yakc.quickstarts.dashboard.watch;

import com.marcnuri.yakc.api.KubernetesException;
import com.marcnuri.yakc.api.WatchEvent;
import com.marcnuri.yakc.api.WatchEvent.Type;
import com.marcnuri.yakc.model.Model;


public class DashboardError implements Model {

  private final String message;
  private final String cause;
  private int code;

  public DashboardError(Throwable throwable) {
    if (throwable != null) {
      message = throwable.getMessage();
      cause = throwable.getCause() != null ? throwable.getCause().getMessage() : "";
    } else {
      message = null;
      cause = null;
    }
    if (throwable instanceof KubernetesException) {
      final KubernetesException kubernetesException = (KubernetesException)throwable;
      code = kubernetesException.getCode();
    }
  }

  public String getDashboardError() {
    return getClass().getSimpleName();
  }

  public String getMessage() {
    return message;
  }

  public String getCause() {
    return cause;
  }

  public int getCode() {
    return code;
  }

  static WatchEvent<DashboardError> watchEvent(Throwable throwable) {
    return new WatchEvent<>(Type.ERROR, new DashboardError(throwable));
  }
}
