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
import cRoles from './';
import Table from '../components/Table';

const headers = [
  'Resources',
  'Non-resource URL',
  'Resource Names',
  'Verbs',
  'API Groups'
];

const Rows = ({rules}) => rules.map((rule, idx) => (
  <Table.Row key={idx}>
    <Table.Cell>{cRoles.selectors.rules.resources(rule).join(', ')}</Table.Cell>
    <Table.Cell>{cRoles.selectors.rules.nonResourceURLs(rule).join(', ')}</Table.Cell>
    <Table.Cell>{cRoles.selectors.rules.resourceNames(rule).join(', ')}</Table.Cell>
    <Table.Cell>{cRoles.selectors.rules.verbs(rule).join(', ')}</Table.Cell>
    <Table.Cell>{cRoles.selectors.rules.apiGroups(rule)}</Table.Cell>
  </Table.Row>
));

const RuleList = ({rules, ...properties}) => (
  <Table {...properties}>
    <Table.Head columns={headers} />
    <Table.Body>
      <Rows rules={rules} />
    </Table.Body>
  </Table>
);

export default RuleList;