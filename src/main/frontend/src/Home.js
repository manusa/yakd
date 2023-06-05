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
import {connect} from 'react-redux';
import deployments from './deployments';
import events from './events';
import nodes from './nodes';
import pods from './pods';
import DashboardPage from './components/DashboardPage';
import FilterBar from './components/FilterBar';

const cardResponsiveClass = 'w-full sm:w-1/2 md:w-1/3';
const cardClass = 'm-2'

const Home = ({selectedNamespace}) => (
  <DashboardPage title='Kubernetes Dashboard'>
    <div className='flex flex-wrap -m-2'>
      <nodes.NodesCard responsiveClassName={cardResponsiveClass} className={cardClass} />
      <deployments.DeploymentsCard responsiveClassName={cardResponsiveClass} className={cardClass} />
      <pods.PodsCard responsiveClassName={cardResponsiveClass} className={cardClass} />
    </div>
    <FilterBar className='mt-4' />
    <events.List className='mt-4' namespace={selectedNamespace} />
  </DashboardPage>
);

const mapStateToProps = ({ui: {selectedNamespace}}) => ({
  selectedNamespace
});

export default connect(mapStateToProps)(Home);
