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
import {name, sortByCreationTimeStamp, uid} from '../metadata';
import {useFilteredResources} from '../redux';
import {Icon, Link, ResourceListV2, Table} from '../components';
import {api, selectors, GroupLink} from './';

const headers = [
  <span>
    <Icon className='fa-id-card' /> Name
  </span>,
  'Group',
  'Version(s)',
  'Scope',
  'Kind',
  ''
];

const Rows = ({customResourceDefinitions}) => {
  const deleteCrd = customResourceDefinition => async () =>
    await api.deleteCrd(customResourceDefinition);
  return customResourceDefinitions
    .sort(sortByCreationTimeStamp)
    .map(customResourceDefinition => (
      <Table.ResourceRow
        key={uid(customResourceDefinition)}
        resource={customResourceDefinition}
      >
        <Table.Cell>
          <Link.CustomResourceDefinition
            to={`/customresourcedefinitions/${uid(customResourceDefinition)}`}
          >
            {name(customResourceDefinition)}
          </Link.CustomResourceDefinition>
        </Table.Cell>
        <Table.Cell>
          <GroupLink customResourceDefinition={customResourceDefinition} />
        </Table.Cell>
        <Table.Cell>
          {selectors.specVersions(customResourceDefinition).map(v => (
            <div key={v}>{v}</div>
          ))}
        </Table.Cell>
        <Table.Cell>{selectors.specScope(customResourceDefinition)}</Table.Cell>
        <Table.Cell>
          {selectors.specNamesKind(customResourceDefinition)}
        </Table.Cell>
        <Table.Cell>
          <Table.DeleteButton onClick={deleteCrd(customResourceDefinition)} />
        </Table.Cell>
      </Table.ResourceRow>
    ));
};

export const List = ({...properties}) => {
  const filteredResources = useFilteredResources({
    resource: 'customResourceDefinitions',
    filters: {...properties}
  });
  const resources = Object.values(
    selectors.crdsBy(filteredResources, properties)
  );
  return (
    <ResourceListV2 headers={headers} resources={resources} {...properties}>
      <Rows customResourceDefinitions={resources} />
    </ResourceListV2>
  );
};
