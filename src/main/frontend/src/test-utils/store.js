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
// TODO: Replace createStore with configureStore from @reduxjs/toolkit when redux is updated
// createStore is deprecated: https://redux.js.org/introduction/why-rtk-is-redux-today
import {combineReducers, createStore} from 'redux';
// Import the redux barrel before the apis barrel: redux re-exports the
// production store, whose module-load combineReducers reads apis.apiGroupsReducer.
// Loading apis first would re-enter it mid-initialization (apis <-> redux cycle).
import {reducer as reduxReducer, uiReducer} from '../redux';
import {apiGroupsReducer} from '../apis';

/**
 * Creates a Redux store for testing with optional initial state.
 *
 * @param {Object} initialState - Optional initial state to merge with defaults
 * @returns {import('redux').Store} A Redux store instance
 */
export const createTestStore = (initialState = {}) => {
  const appReducer = combineReducers({
    apiGroups: apiGroupsReducer,
    customResourceDefinitions: reduxReducer('CustomResourceDefinition'),
    namespaces: reduxReducer('Namespace'),
    ui: uiReducer
  });

  // Let the reducers initialize their default state first
  const defaultState = appReducer(undefined, {type: '@@INIT'});

  // Deep merge the initial state with defaults
  const mergedState = {
    ...defaultState,
    ...initialState,
    ui: {
      ...defaultState.ui,
      ...(initialState.ui || {})
    }
  };

  return createStore(appReducer, mergedState);
};
