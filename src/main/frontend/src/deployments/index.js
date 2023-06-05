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
import DeploymentsCard from './DeploymentsCard';
import DeploymentsDetailPage from './DeploymentsDetailPage';
import DeploymentsEditPage from './DeploymentsEditPage';
import DeploymentsPage from './DeploymentsPage';

const deployments = {};

deployments.api = api;
deployments.reducer = reducer;
deployments.selectors = selectors;
deployments.List = List;
deployments.DeploymentsCard = DeploymentsCard;
deployments.DeploymentsDetailPage = DeploymentsDetailPage;
deployments.DeploymentsEditPage = DeploymentsEditPage;
deployments.DeploymentsPage = DeploymentsPage;

export default deployments;