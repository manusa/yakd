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
import metadata from '../metadata';
import crd from './index';
import DashboardPage from '../components/DashboardPage';

const DashboardPageTitle = ({customResourceDefinition, children}) => (
  <DashboardPage.Title path='customresourcedefinitions' kind='CustomResourceDefinitions'>
    &nbsp;-&nbsp;<crd.GroupLink customResourceDefinition={customResourceDefinition} />
    &nbsp;- {metadata.selectors.name(customResourceDefinition)}
    {children}
  </DashboardPage.Title>
);

export default DashboardPageTitle;