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
import {selectors} from './';
import {Icon, Link} from '../components';

export const Host = ({route}) => {
  const url = `http${
    selectors.specTls(route) ? 's' : ''
  }://${selectors.specHost(route)}${selectors.specPath(route)}`;
  return (
    <Link href={url} target='_blank'>
      {selectors.specHost(route)}
      <Icon
        className='ml-1 text-xs'
        icon='fa-external-link-alt'
        stylePrefix='fas'
      />
    </Link>
  );
};
