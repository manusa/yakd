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
import nodesModule from './'

const NodesCard = ({nodes, ...properties}) => {
  const nodeObjects = Object.values(nodes);
  const ready = nodesModule.selectors.readyCount(nodeObjects);
  const total = nodeObjects.length;
  return (
    <StatusCard
      header='Nodes'
      to={'/nodes'}
      Icon={icons.Node}
      ready={ready}
      total={total}
      readyProgress={Math.round(ready/total*100)}
      {...properties}
    />
  );
};

const mapStateToProps = ({nodes}) => ({
  nodes
});

export default connect(mapStateToProps)(NodesCard);