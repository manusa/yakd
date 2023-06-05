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
import StatusCard from '../components/StatusCard';
import icons from '../components/icons';
import deploymentsModule from './';

const DeploymentsCard = ({deployments, ...properties}) => {
  const objects = Object.values(deployments);
  const ready = deploymentsModule.selectors.readyCount(objects);
  const total = objects.length
  return (
    <StatusCard
      header='Deployments'
      to={'/deployments'}
      Icon={icons.Deployment}
      ready={ready}
      total={total}
      readyProgress={Math.round(ready/total*100)}
      {...properties}
    />
  );
};

const mapStateToProps = ({deployments}) => ({
  deployments
});

export default connect(mapStateToProps)(DeploymentsCard);
