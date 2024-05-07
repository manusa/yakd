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

export const Types = {
  CLEAR: 'CLEAR',
  CRUD_CLEAR: 'CRUD_CLEAR',
  CRUD_ADD_OR_REPLACE: 'CRUD_ADD_OR_REPLACE',
  CRUD_DELETE: 'CRUD_DELETE',
  CRUD_SET_ALL: 'CRUD_SET_ALL',
  API_GROUPS_SET: 'API_GROUPS_SET',
  UI_SIDEBAR_SCROLL: 'UI_SIDEBAR_SCROLL',
  UI_SIDEBAR_TOGGLE_ITEM: 'UI_SIDEBAR_TOGGLE_ITEM',
  UI_SET_OFFLINE: 'UI_SET_OFFLINE',
  UI_SET_ERROR: 'UI_SET_ERROR',
  UI_CLEAR_ERROR: 'UI_CLEAR_ERROR',
  UI_SET_RESOURCE_LOADED: 'UI_SET_RESOURCE_LOADED',
  UI_SELECT_NAMESPACE: 'SELECT_NAMESPACE',
  UI_SET_QUERY: 'UI_SET_QUERY',
  UI_SET_CREATING_NEW_RESOURCE: 'UI_SET_CREATING_NEW_RESOURCE'
};

export const clear = () => ({
  type: Types.CLEAR
});

export const crudClear = kind => ({
  type: Types.CRUD_CLEAR,
  payload: kind
});

export const crudAddOrReplace = object => ({
  type: Types.CRUD_ADD_OR_REPLACE,
  payload: object
});

export const crudDelete = object => ({
  type: Types.CRUD_DELETE,
  payload: object
});

export const crudSetAll = ({kind, resources}) => ({
  type: Types.CRUD_SET_ALL,
  payload: {kind, resources}
});

export const apiGroupsSet = apiGroups => ({
  type: Types.API_GROUPS_SET,
  payload: apiGroups
});

export const setOffline = (offline = false) => ({
  type: Types.UI_SET_OFFLINE,
  payload: offline
});

export const setError = error => ({
  type: Types.UI_SET_ERROR,
  payload: error
});

export const clearError = () => ({
  type: Types.UI_CLEAR_ERROR
});

export const setResourceLoaded = ({kind, loaded = false}) => ({
  type: Types.UI_SET_RESOURCE_LOADED,
  payload: {kind, loaded}
});

export const setQuery = query => ({
  type: Types.UI_SET_QUERY,
  payload: query
});

export const uiSetCreatingNewResource = creatingNewResource => ({
  type: Types.UI_SET_CREATING_NEW_RESOURCE,
  payload: creatingNewResource
});
