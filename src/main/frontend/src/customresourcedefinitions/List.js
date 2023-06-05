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
import crd from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon className='fa-id-card' /> Name</span>,
  'Group',
  'Version(s)',
  'Scope',
  'Kind',
  ''
];

const Rows = ({customResourceDefinitions}) => {
  const deleteCrd = customResourceDefinition => async () => await crd.api.delete(customResourceDefinition);
  return customResourceDefinitions
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(customResourceDefinition => (
        <Table.ResourceRow key={metadata.selectors.uid(customResourceDefinition)} resource={customResourceDefinition}>
          <Table.Cell>
            <Link.CustomResourceDefinition to={`/customresourcedefinitions/${metadata.selectors.uid(customResourceDefinition)}`}>
              {metadata.selectors.name(customResourceDefinition)}
            </Link.CustomResourceDefinition>
          </Table.Cell>
          <Table.Cell>
            <crd.GroupLink customResourceDefinition={customResourceDefinition} />
          </Table.Cell>
          <Table.Cell>
            {crd.selectors.specVersions(customResourceDefinition).map(v => (
              <div key={v}>{v}</div>
            ))}
          </Table.Cell>
          <Table.Cell>
            {crd.selectors.specScope(customResourceDefinition)}
          </Table.Cell>
          <Table.Cell>
            {crd.selectors.specNamesKind(customResourceDefinition)}
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteCrd(customResourceDefinition)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({customResourceDefinitions, group, ...properties}) => (
  <ResourceList headers={headers} resources={customResourceDefinitions} {...properties}>
    <Rows customResourceDefinitions={customResourceDefinitions} />
  </ResourceList>
);
const mapStateToProps = ({customResourceDefinitions}) => ({
  customResourceDefinitions
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  customResourceDefinitions: Object.values(crd.selectors.crdsBy(stateProps.customResourceDefinitions, ownProps))
});

export default connect(mapStateToProps, null, mergeProps)(List);

