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
import api from './api';
import reducer from './reducer';
import selectors from './selectors';
import List from './List';
import ClusterRolesDetailPage from './ClusterRolesDetailPage';
import ClusterRolesEditPage from './ClusterRolesEditPage';
import ClusterRolesPage from './ClusterRolesPage';
import RuleList from './RuleList';

const cRoles = {}

cRoles.api = api;
cRoles.reducer = reducer;
cRoles.selectors = selectors;
cRoles.List = List;
cRoles.ClusterRolesDetailPage = ClusterRolesDetailPage;
cRoles.ClusterRolesEditPage = ClusterRolesEditPage;
cRoles.ClusterRolesPage = ClusterRolesPage;
cRoles.RuleList = RuleList;

export default cRoles;
