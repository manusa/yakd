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
import metadata from '../metadata';
import ing from './';
import Icon from '../components/Icon';
import Link from '../components/Link';
import ResourceList from '../components/ResourceList';
import Table from '../components/Table';

const headers = [
  <span><Icon icon='fa-id-card' /> Name</span>,
  'Namespace',
  'Hosts',
  'Paths',
  ''
];

const Rows = ({ingresses, crudDelete}) => {
  const deleteIngress = ingress => async () => {
    await ing.api.delete(ingress);
    crudDelete(ingress);
  };
  return ingresses
    .sort(metadata.selectors.sortByCreationTimeStamp)
    .map(ingress => (
        <Table.ResourceRow key={metadata.selectors.uid(ingress)} resource={ingress}>
          <Table.Cell>
            <Link.Ingress to={`/ingresses/${metadata.selectors.uid(ingress)}`}>
              {metadata.selectors.name(ingress)}
            </Link.Ingress>
          </Table.Cell>
          <Table.Cell className='whitespace-no-wrap'>
            <Link.Namespace to={`/namespaces/${metadata.selectors.namespace(ingress)}`}>
              {metadata.selectors.namespace(ingress)}
            </Link.Namespace>
          </Table.Cell>
          <Table.Cell>
            {ing.selectors.allHosts(ingress).map((host, idx) =>
              <div key={idx}><Link href={`http://${host}`} target='_blank'>{host}</Link ></div>
            )}
          </Table.Cell>
          <Table.Cell>
            {ing.selectors.allPaths(ingress).map((path, idx) =>
              <div key={idx}>{path}</div>
            )}
          </Table.Cell>
          <Table.Cell>
            <Table.DeleteButton onClick={deleteIngress(ingress)} />
          </Table.Cell>
        </Table.ResourceRow>
    ));
};

const List = ({resources, loadedResources, crudDelete, ...properties}) => (
  <ResourceList
    headers={headers} resources={resources} loading={!loadedResources['Ingress']} {...properties}>
    <Rows ingresses={resources} loadedResources={loadedResources} crudDelete={crudDelete} />
  </ResourceList>
);

export default ResourceList.resourceListConnect('ingresses')(List);

