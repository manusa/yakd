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
import {name, namespace, sortByCreationTimeStamp, uid} from '../metadata';
import {selectors as crdSelectors} from '../customresourcedefinitions';
import {ResourceEditModal} from '../editor';
import {Icon, Link, ResourceListV2, Table} from '../components';
import {api, selectors} from './';

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

export const List = ({
  customResources,
  customResourceDefinition,
  version,
  deleteResourceCallback,
  ...properties
}) => {
  const [editedResource, setEditedResource] = useState(null);
  return (
    <>
      <ResourceListV2
        headers={headers(customResourceDefinition)}
        resources={customResources}
        {...properties}
      >
        <Rows
          customResources={customResources}
          customResourceDefinition={customResourceDefinition}
          version={version}
          editResource={setEditedResource}
          deleteResourceCallback={deleteResourceCallback}
        />
      </ResourceListV2>
      <ResourceEditModal
        resource={editedResource}
        title={`${selectors.apiVersion(editedResource)} - ${
          crdSelectors.isNamespaced(customResourceDefinition)
            ? `${namespace(editedResource)} - `
            : ''
        }${name(editedResource)}`}
        save={toSave => api.update(customResourceDefinition, version)(toSave)}
        close={() => setEditedResource(null)}
      />
    </>
  );
};
