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
import {connect} from 'react-redux';
import metadata from '../metadata';
import {selectors} from './';
import {Icon} from '../components';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  '',
  <span className='whitespace-nowrap'>
    <Icon icon='fa-id-card' /> Name
  </span>,
  <span className='whitespace-nowrap'>
    <Icon icon='fa-server' /> Roles
  </span>,
  <span>
    <Icon icon='fa-tags' /> Labels
  </span>
];

const Rows = ({nodes}) => {
  return nodes.sort(metadata.selectors.sortByCreationTimeStamp).map(node => (
    <Table.ResourceRow key={metadata.selectors.uid(node)} resource={node}>
      <Table.Cell className='whitespace-nowrap w-3 text-center'>
        <Icon
          className={
            selectors.isReady(node) ? 'text-green-500' : 'text-red-500'
          }
          icon={selectors.isReady(node) ? 'fa-check' : 'fa-exclamation-circle'}
        />
      </Table.Cell>
      <Table.Cell className='text-nowrap'>
        <Link.Node to={`/nodes/${metadata.selectors.name(node)}`}>
          {metadata.selectors.name(node)}
        </Link.Node>
      </Table.Cell>
      <Table.Cell className='text-nowrap'>
        {(roles => {
          if (roles.length === 0) {
            return '<none>';
          }
          return roles.map((role, idx) => <div key={idx}>{role}</div>);
        })(selectors.roles(node))}
      </Table.Cell>
      <Table.Cell>
        <metadata.KeyValueList
          keyValues={metadata.selectors.labels(node)}
          maxEntries={2}
        />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

const mapStateToProps = ({nodes}) => ({
  nodes: Object.values(nodes)
});

export const List = connect(mapStateToProps)(({nodes, ...properties}) => (
  <ResourceList headers={headers} resources={nodes} {...properties}>
    <Rows nodes={nodes} />
  </ResourceList>
));
