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
import {withParams} from '../router';
import md from '../metadata';
import p from './';
import ResourceEditPage from '../components/ResourceEditPage';
import Link from '../components/Link';

const PodsEditPage = ({params: {uid}}) => (
  <ResourceEditPage
    kind='Pods'
    path='pods'
    cardTitle={resource =>
      <Link.RouterLink to={`/pods/${uid}`}>{md.selectors.name(resource)}</Link.RouterLink>
    }
    save={async resource => await p.api.update(resource)}
    resourceFromState={state => state.pods[uid]}
  />
);

export default withParams(PodsEditPage);
