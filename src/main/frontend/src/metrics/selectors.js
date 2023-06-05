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
 */
const selectors = {};

selectors.bytesToHumanReadable = (bytes = 0) => {
  if (bytes > Math.pow(1024, 3)) {
    return `${(bytes / Math.pow(1024, 3)).toFixed(3)} GiB`;
  } else if (bytes > Math.pow(1024, 2)) {
    return `${(bytes / Math.pow(1024, 2)).toFixed(0)} MiB`;
  } else if (bytes > 1024) {
    return `${(bytes / 1024).toFixed(0)}KiB`;
  }
  return bytes;
};

selectors.quantityToScalar = (quantity = 0) => {
  const quantityString = quantity.toString();
  if (quantityString.endsWith("n")) {
    return quantityString.substring(0, quantityString.length - 1) / 1000_000_000;
  }
  if (quantityString.endsWith("m")) {
    return quantityString.substring(0, quantityString.length - 1) / 1000;
  }
  if (quantityString.endsWith("Ki")) {
    return quantityString.substring(0, quantityString.length - 2) * 1024;
  }
  if (quantityString.endsWith("Mi")) {
    return quantityString.substring(0, quantityString.length - 2) * Math.pow(1024, 2);
  }
  return parseFloat(quantityString);
};

selectors.podMetrics = (podMetrics = {containers: []}) => {
  const ret = {};
  ret.totalCpu = () => podMetrics.containers?.reduce((total, c) =>
    total + selectors.quantityToScalar(c.usage?.cpu), 0);
  ret.totalMemory = () => podMetrics.containers?.reduce((total, c) =>
    total + selectors.quantityToScalar(c.usage?.memory), 0);
  ret.container = containerName => podMetrics.containers?.find(c => c.name === containerName) ?? {};
  ret.containerCpu = containerName =>
    selectors.quantityToScalar(ret.container(containerName)?.usage?.cpu);
  ret.containerMemory = containerName =>
    selectors.quantityToScalar(ret.container(containerName)?.usage?.memory);
  return ret;
};

export default selectors;
