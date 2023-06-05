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
const actions = {};

actions.Types = {
  CLEAR: 'CLEAR',
  CRUD_CLEAR: 'CRUD_CLEAR',
  CRUD_ADD_OR_REPLACE: 'CRUD_ADD_OR_REPLACE',
  CRUD_DELETE: 'CRUD_DELETE',
  CRUD_SET_ALL: 'CRUD_SET_ALL',
  API_GROUPS_SET: 'API_GROUPS_SET',
  SIDEBAR_SCROLL: 'SIDEBAR_SCROLL',
  SIDEBAR_TOGGLE_ITEM: 'SIDEBAR_TOGGLE_ITEM',
  UI_SET_OFFLINE: 'UI_SET_OFFLINE',
  UI_SET_ERROR: 'UI_SET_ERROR',
  UI_CLEAR_ERROR: 'UI_CLEAR_ERROR',
  UI_SET_RESOURCE_LOADED: 'UI_SET_RESOURCE_LOADED',
  UI_SELECT_NAMESPACE: 'SELECT_NAMESPACE',
  UI_SET_QUERY: 'UI_SET_QUERY'
}

actions.clear = () => ({
  type: actions.Types.CLEAR
});

actions.crudClear = kind => ({
  type: actions.Types.CRUD_CLEAR,
  payload: kind,
});

actions.crudAddOrReplace = object => ({
  type: actions.Types.CRUD_ADD_OR_REPLACE,
  payload: object
});

actions.crudDelete = object => ({
  type: actions.Types.CRUD_DELETE,
  payload: object
});

actions.crudSetAll = ({kind, resources}) => ({
  type: actions.Types.CRUD_SET_ALL,
  payload: {kind, resources}
});

actions.apiGroupsSet = apiGroups => ({
  type: actions.Types.API_GROUPS_SET,
  payload: apiGroups
});

actions.setOffline = (offline = false) => ({
  type: actions.Types.UI_SET_OFFLINE,
  payload: offline
});

actions.setError = error => ({
  type: actions.Types.UI_SET_ERROR,
  payload: error
});

actions.clearError = () => ({
  type: actions.Types.UI_CLEAR_ERROR
});

actions.setResourceLoaded = ({kind, loaded = false}) => ({
  type: actions.Types.UI_SET_RESOURCE_LOADED,
  payload: {kind, loaded}
});

actions.selectNamespace = namespace => ({
  type: actions.Types.UI_SELECT_NAMESPACE,
  payload: namespace
});

actions.setQuery = query => ({
  type: actions.Types.UI_SET_QUERY,
  payload: query
});

actions.clearSelectedNamespace = () => actions.selectNamespace(null);

actions.sideBarScroll = ({scrollTop = 0, scrollLeft = 0}) => ({
  type: actions.Types.SIDEBAR_SCROLL,
  payload: {scrollTop, scrollLeft}
});

actions.sideBarToggleItem = item => ({
  type: actions.Types.SIDEBAR_TOGGLE_ITEM,
  payload: item
});

export default actions;
