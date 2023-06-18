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
import React from 'react';
import metrics from '../metrics';
import Icon from '../components/Icon';
import Table from '../components/Table';

const containerHeaders = [
  <span key='name'>
    <Icon icon='fa-id-card' /> Name
  </span>,
  <span key='image'>
    <Icon icon='fa-layer-group' /> Image
  </span>,
  <span key='ports'>
    <Icon icon='fa-ethernet' /> Ports
  </span>,
  <span key='cpu'>
    <Icon icon='fa-microchip' /> CPU
  </span>,
  <span key='memory'>
    <Icon icon='fa-memory' /> Memory
  </span>
];

export const ContainerList = ({containers, podMetrics, ...properties}) => (
  <Table {...properties}>
    <Table.Head columns={containerHeaders} />
    <Table.Body>
      {containers.map(c => (
        <Table.Row key={c.name}>
          <Table.Cell>{c.name}</Table.Cell>
          <Table.Cell>{c.image}</Table.Cell>
          <Table.Cell>
            {(c.ports ?? []).map(p => (
              <div key={`${p.name}-${p.containerPort}-${p.protocol}`}>
                {p.name} {p.containerPort} {p.protocol}
              </div>
            ))}
          </Table.Cell>
          <Table.Cell>
            {podMetrics?.containerCpu(c.name).toFixed(3) ?? ''}
          </Table.Cell>
          <Table.Cell>
            {podMetrics &&
              metrics.selectors.bytesToHumanReadable(
                podMetrics.containerMemory(c.name)
              )}
          </Table.Cell>
        </Table.Row>
      ))}
    </Table.Body>
  </Table>
);
