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
import redux from '../redux';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import editor from '../components/editor';
import metadata from '../metadata';
import cr from './';
import crd from '../customresourcedefinitions';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = customResourceDefinition => {
  const ret = [<span><Icon className='fa-id-card' /> Name</span>];
  if (crd.selectors.isNamespaced(customResourceDefinition)) {
    ret.push('Namespace');
  }
  ret.push('');
  return ret;
};

const Rows = ({customResources, customResourceDefinition, version, editResource, deleteResourceCallback}) => {
  const deleteCustomResource = customResource => {
    const deleteFunc = cr.api.delete(customResourceDefinition, version);
    return async () => {
      await deleteFunc(customResource);
      deleteResourceCallback(customResource);
    }
  };
  return customResources
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(customResource => (
        <Table.ResourceRow key={metadata.selectors.uid(customResource)} resource={customResource}>
          <Table.Cell>
            <Link onClick={() => editResource(customResource)} >
              {metadata.selectors.name(customResource)}
            </Link>
          </Table.Cell>
          {crd.selectors.isNamespaced(customResourceDefinition) &&
            <Table.Cell className='whitespace-no-wrap'>
              <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(customResource)}`}>
                {metadata.selectors.namespace(customResource)}
              </Link.Namespace>
            </Table.Cell>
          }
          <Table.Cell>
            <Table.DeleteButton onClick={deleteCustomResource(customResource)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({customResources, customResourceDefinition, version, deleteResourceCallback, crudDelete, ...properties}) => {
  const [editedResource, editResource] = useState(null);
  return (
    <>
      <ResourceList headers={headers(customResourceDefinition)} resources={customResources} {...properties}>
        <Rows
          customResources={customResources}
          customResourceDefinition={customResourceDefinition}
          version={version}
          editResource={editResource}
          deleteResourceCallback={deleteResourceCallback}
        />
      </ResourceList>
      <editor.ResourceEditModal
        resource={editedResource}
        title={`${cr.selectors.apiVersion(editedResource)} - ${
          crd.selectors.isNamespaced(customResourceDefinition) ? `${metadata.selectors.namespace(editedResource)} - ` : ''}${
          metadata.selectors.name(editedResource)
        }`}
        save={toSave => cr.api.update(customResourceDefinition, version)(toSave)}
        close={() => editResource(null)}
      />
    </>
  );
};

const mapDispatchToProps = dispatch =>  bindActionCreators({
  crudDelete: redux.actions.crudDelete
}, dispatch);

export default connect(null, mapDispatchToProps)(List);

