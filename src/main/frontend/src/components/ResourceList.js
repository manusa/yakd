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
import {bindActionCreators} from 'redux';
import redux from '../redux';
import Table from './Table';

const Content = ({headers, resources, loading, children}) => {
  if (resources.length > 0) {
    return children;
  }
  if (loading) {
    return <Table.Loading colSpan={headers.length} />
  }
  return <Table.NoResultsRow colSpan={headers.length} />
};

const ResourceList = ({
  headers, title, resources, children,
  nameLike /* don't propagate */,
  loading = false,
  hideWhenNoResults = false,
  ...properties
}) => {
  if (hideWhenNoResults && resources.length === 0) {
    return null;
  }
  return (
    <Table title={title} {...properties}>
      <Table.Head columns={headers} />
      <Table.Body>
        <Content headers={headers} resources={resources} loading={loading}>
          {children}
        </Content>
      </Table.Body>
    </Table>
  );
};

ResourceList.mapStateToProps = resource => ({ui: {loadedResources}, ...state}) => {
  return {resources: state[resource], loadedResources}
};

ResourceList.resourceListConnect = resource => connect(
  ResourceList.mapStateToProps(resource),
  dispatch =>  bindActionCreators({
    crudDelete: redux.actions.crudDelete
  }, dispatch),
  (stateProps, dispatchProps, ownProps) => ({
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
    resources: Object.values(redux.selectors.resourcesBy(stateProps.resources, ownProps))
  })
);

export default ResourceList;
