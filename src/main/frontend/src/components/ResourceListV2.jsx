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
import PropTypes from 'prop-types';
import React from 'react';
import {Table} from './';

const Content = ({headers, resources, loading, children}) => {
  if (resources?.length > 0) {
    return children;
  }
  if (loading) {
    return <Table.Loading colSpan={headers.length} />;
  }
  return <Table.NoResultsRow colSpan={headers.length} />;
};

export const ResourceListV2 = ({
  resources,
  headers,
  title,
  titleVariant,
  className,
  children,
  loading = false,
  hideWhenNoResults = false,
  ...properties
}) => {
  if (hideWhenNoResults && resources.length === 0) {
    return null;
  }
  return (
    <Table
      title={title}
      titleVariant={titleVariant}
      className={className}
      {...properties}
    >
      <Table.Head columns={headers} />
      <Table.Body>
        <Content headers={headers} resources={resources} loading={loading}>
          {children}
        </Content>
      </Table.Body>
    </Table>
  );
};

ResourceListV2.propTypes = {
  resources: PropTypes.arrayOf(PropTypes.object).isRequired,
  children: PropTypes.node.isRequired,
  title: PropTypes.string,
  loading: PropTypes.bool
};
