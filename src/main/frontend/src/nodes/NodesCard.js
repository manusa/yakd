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
import {shallowEqual, useSelector} from 'react-redux';
import {StatusCard, NodeIcon} from '../components';
import {selectors} from './';

export const NodesCard = ({...properties}) => {
  const nodes = useSelector(({nodes}) => Object.values(nodes), shallowEqual);
  const ready = selectors.readyCount(nodes);
  const total = nodes.length;
  return (
    <StatusCard
      header='Nodes'
      to={'/nodes'}
      Icon={NodeIcon}
      ready={ready}
      total={total}
      readyProgress={Math.round((ready / total) * 100)}
      {...properties}
    />
  );
};
