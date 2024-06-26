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
import {name} from '../metadata';
import {Link} from '../components';
import {ResourceEditPage} from '../editor';
import {api, DashboardPageTitle} from './';

export const CustomResourceDefinitionsEditPage = withParams(
  ({params: {uid}}) => (
    <ResourceEditPage
      kind='CustomResourceDefinitions'
      path='customresourcedefinitions'
      dashboardPageTitle={resource => (
        <DashboardPageTitle customResourceDefinition={resource}>
          &nbsp;- Edit
        </DashboardPageTitle>
      )}
      cardTitle={resource => (
        <Link.RouterLink to={`/customresourcedefinitions/${uid}`}>
          {name(resource)}
        </Link.RouterLink>
      )}
      save={async resource => await api.update(resource)}
      resourceFromState={state => state.customResourceDefinitions[uid]}
    />
  )
);
