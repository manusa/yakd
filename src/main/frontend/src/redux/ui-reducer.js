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
import {shallowEqual, useDispatch, useSelector} from 'react-redux';
import {Types} from './';

const defaultState = {
  offline: false,
  error: '',
  loadedResources: {},
  creatingNewResource: false,
  selectedNamespace: null,
  query: '',
  sidebarExpandedItems: [],
  sidebarScroll: {scrollTop: 0, scrollLeft: 0}
};

export const uiReducer = (state = defaultState, action = {}) => {
  switch (action.type) {
    case Types.UI_SET_OFFLINE: {
      return {...state, offline: action.payload};
    }
    case Types.UI_SET_ERROR: {
      const newState = {...state};
      newState.error = action.payload;
      return newState;
    }
    case Types.UI_CLEAR_ERROR: {
      const newState = {...state};
      delete newState.error;
      return newState;
    }
    case Types.CLEAR: {
      return {...state, loadedResources: {}};
    }
    case Types.UI_SET_RESOURCE_LOADED: {
      const newState = {...state};
      newState.loadedResources[action.payload.kind] = action.payload.loaded;
      return newState;
    }
    case Types.UI_SELECT_NAMESPACE: {
      return {...state, selectedNamespace: action.payload};
    }
    case Types.UI_SET_QUERY: {
      return {...state, query: action.payload};
    }
    case Types.UI_SET_CREATING_NEW_RESOURCE: {
      return {...state, creatingNewResource: action.payload};
    }
    case Types.UI_SIDEBAR_SCROLL: {
      return {...state, sidebarScroll: action.payload};
    }
    case Types.UI_SIDEBAR_TOGGLE_ITEM: {
      const newState = {...state};
      const item = action.payload;
      if (state.sidebarExpandedItems.includes(item)) {
        newState.sidebarExpandedItems = state.sidebarExpandedItems.filter(
          i => i !== item
        );
      } else {
        newState.sidebarExpandedItems = [...state.sidebarExpandedItems, item];
      }
      return newState;
    }
    default:
      return {...state};
  }
};

// Actions
const actionSelectNamespace = namespace => ({
  type: Types.UI_SELECT_NAMESPACE,
  payload: namespace
});
const actionClearSelectedNamespace = () => actionSelectNamespace(null);

const actionSidebarScroll = ({scrollTop = 0, scrollLeft = 0}) => ({
  type: Types.UI_SIDEBAR_SCROLL,
  payload: {scrollTop, scrollLeft}
});
const actionSidebarToggleItem = item => ({
  type: Types.UI_SIDEBAR_TOGGLE_ITEM,
  payload: item
});

// Hooks
export const useUiLoadedResources = () => {
  const loadedResources = useSelector(
    ({ui: {loadedResources}}) => loadedResources,
    shallowEqual
  );
  return {loadedResources};
};

export const useUiNamespace = () => {
  const dispatch = useDispatch();
  const selectNamespace = namespace =>
    dispatch(actionSelectNamespace(namespace));
  const clearSelectedNamespace = () => dispatch(actionClearSelectedNamespace());
  const {namespaces, selectedNamespace} = useSelector(
    ({namespaces, ui: {selectedNamespace}}) => ({
      namespaces,
      selectedNamespace
    }),
    shallowEqual
  );
  return {
    namespaces,
    selectedNamespace,
    selectNamespace,
    clearSelectedNamespace
  };
};

export const useUiSidebar = () => {
  const dispatch = useDispatch();
  const sidebarScroll = ({scrollTop, scrollLeft}) =>
    dispatch(actionSidebarScroll({scrollTop, scrollLeft}));
  const sidebarToggleItem = item => dispatch(actionSidebarToggleItem(item));
  const {
    sidebarExpandedItems,
    sidebarScroll: {scrollTop: sidebarScrollTop, scrollLeft: sidebarScrollLeft}
  } = useSelector(
    ({ui: {sidebarExpandedItems, sidebarScroll}}) => ({
      sidebarExpandedItems,
      sidebarScroll
    }),
    shallowEqual
  );
  return {
    sidebarExpandedItems,
    sidebarScrollTop,
    sidebarScrollLeft,
    sidebarScroll,
    sidebarToggleItem
  };
};
