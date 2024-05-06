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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import s from './';
import {Icon, Link, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Namespace',
  'Type',
  ''
];

const Rows = ({secrets}) => {
  const deleteSecret = secret => async () => await s.api.delete(secret);
  return secrets.sort(sortByCreationTimeStamp).map(secret => (
    <Table.ResourceRow key={uid(secret)} resource={secret}>
      <Table.Cell>
        <Link.Secret to={`/secrets/${uid(secret)}`}>{name(secret)}</Link.Secret>
      </Table.Cell>
      <Table.Cell className='whitespace-nowrap'>
        <Link.Namespace to={`/namespaces/${namespace(secret)}`}>
          {namespace(secret)}
        </Link.Namespace>
      </Table.Cell>
      <Table.Cell>{s.selectors.type(secret)}</Table.Cell>
      <Table.Cell>
        <Table.DeleteButton onClick={deleteSecret(secret)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList headers={headers} resources={resources} {...properties}>
    <Rows secrets={resources} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('secrets')(List);
