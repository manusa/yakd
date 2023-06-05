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

import redux from './index';

const defaultState = {
  offline: false,
  error: '',
  loadedResources: {},
  selectedNamespace: null,
  query: ''
};

const uiReducer = (state = defaultState, action = {}) => {
  const {actions: {Types}} = redux;
  switch (action.type) {
    case Types.UI_SET_OFFLINE: {
      return {...state, offline: action.payload}
    }
    case Types.UI_SET_ERROR: {
      const newState = {...state};
      newState.error = action.payload
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
    default:
      return {...state};
  }
};

export default uiReducer;