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
import PropTypes from 'prop-types';
import {useDispatch, useSelector} from 'react-redux';
import redux from '../redux';
import metadata from '../metadata';
import {Dropdown} from './';

const NamespaceDropdown = () => {
  const {namespaces, selectedNamespace} = useSelector(
    ({namespaces, ui: {selectedNamespace}}) => ({namespaces, selectedNamespace})
  );
  const dispatch = useDispatch();
  const selectNamespace = namespace =>
    dispatch(redux.actions.selectNamespace(namespace));
  const clearSelectedNamespace = () =>
    dispatch(redux.actions.clearSelectedNamespace);
  return (
    <Dropdown
      closeOnPanelClick={true}
      text={selectedNamespace ?? 'Namespace'}
      textColor={selectedNamespace ? 'text-blue-700' : 'text-gray-500'}
      textColorActive={selectedNamespace ? 'text-blue-800' : null}
    >
      <Dropdown.Item onClick={clearSelectedNamespace}>
        All namespaces
      </Dropdown.Item>
      {Object.values(namespaces)
        .map(ns => metadata.selectors.name(ns))
        .map(namespace => (
          <Dropdown.Item
            key={namespace}
            onClick={() => selectNamespace(namespace)}
          >
            {namespace}
          </Dropdown.Item>
        ))}
    </Dropdown>
  );
};

export const FilterBar = ({className = '', ...props}) => {
  return (
    <div className={`flex justify-end ${className}`}>
      <NamespaceDropdown {...props} />
    </div>
  );
};

FilterBar.propTypes = {
  className: PropTypes.string
};
