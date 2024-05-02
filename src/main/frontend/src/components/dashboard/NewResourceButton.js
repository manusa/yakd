/*
 * Copyright 2023 Marc Nuri
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
import {uiSetCreatingNewResource} from '../../redux';
import Link from '../Link';
import Icon from '../Icon';

export const NewResourceButton = connect(undefined, dispatch =>
  bindActionCreators(
    {newResource: () => uiSetCreatingNewResource(true)},
    dispatch
  )
)(({newResource, ...props}) => {
  return (
    <Link title='Create new resource' onClick={newResource} {...props}>
      <Icon icon='fa-plus' />
    </Link>
  );
});
