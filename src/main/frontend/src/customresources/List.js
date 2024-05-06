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
import React, {useState} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {crudDelete} from '../redux';
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import {api, selectors} from './';
import {selectors as crdSelectors} from '../customresourcedefinitions';
import {Icon, Link, ResourceEditModal, Table} from '../components';
import ResourceList from '../components/ResourceList';

const headers = customResourceDefinition => {
  const ret = [
    <span>
      <Icon className='fa-id-card' /> Name
    </span>
  ];
  if (crdSelectors.isNamespaced(customResourceDefinition)) {
    ret.push('Namespace');
  }
  ret.push('');
  return ret;
};

const Rows = ({
  customResources,
  customResourceDefinition,
  version,
  editResource,
  deleteResourceCallback
}) => {
  const deleteCustomResource = customResource => {
    const deleteFunc = api.deleteCr(customResourceDefinition, version);
    return async () => {
      await deleteFunc(customResource);
      deleteResourceCallback(customResource);
    };
  };
  return customResources.sort(sortByCreationTimeStamp).map(customResource => (
    <Table.ResourceRow key={uid(customResource)} resource={customResource}>
      <Table.Cell>
        <Link onClick={() => editResource(customResource)}>
          {name(customResource)}
        </Link>
      </Table.Cell>
      {crdSelectors.isNamespaced(customResourceDefinition) && (
        <Table.Cell className='whitespace-nowrap'>
          <Link.Namespace to={`/namespaces/${namespace(customResource)}`}>
            {namespace(customResource)}
          </Link.Namespace>
        </Table.Cell>
      )}
      <Table.Cell>
        <Table.DeleteButton onClick={deleteCustomResource(customResource)} />
      </Table.Cell>
    </Table.ResourceRow>
  ));
};

const mapDispatchToProps = dispatch =>
  bindActionCreators(
    {
      crudDelete
    },
    dispatch
  );

export const List = connect(
  null,
  mapDispatchToProps
)(({
  customResources,
  customResourceDefinition,
  version,
  deleteResourceCallback,
  crudDelete,
  ...properties
}) => {
  const [editedResource, editResource] = useState(null);
  return (
    <>
      <ResourceList
        headers={headers(customResourceDefinition)}
        resources={customResources}
        {...properties}
      >
        <Rows
          customResources={customResources}
          customResourceDefinition={customResourceDefinition}
          version={version}
          editResource={editResource}
          deleteResourceCallback={deleteResourceCallback}
        />
      </ResourceList>
      <ResourceEditModal
        resource={editedResource}
        title={`${selectors.apiVersion(editedResource)} - ${
          crdSelectors.isNamespaced(customResourceDefinition)
            ? `${namespace(editedResource)} - `
            : ''
        }${name(editedResource)}`}
        save={toSave => api.update(customResourceDefinition, version)(toSave)}
        close={() => editResource(null)}
      />
    </>
  );
});
