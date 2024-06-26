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
import {StatusCard, PodIcon} from '../components';
import {selectors} from './';

const mapStateToProps = ({pods}) => ({
  pods
});

export const PodsCard = connect(mapStateToProps)(({pods, ...properties}) => {
  const podObjects = Object.values(pods);
  const ready = selectors.readyCount(podObjects);
  const succeeded = selectors.succeededCount(podObjects);
  const total = podObjects.length;
  return (
    <StatusCard
      header='Pods'
      to={'/pods'}
      Icon={PodIcon}
      ready={ready}
      succeeded={succeeded}
      total={total}
      readyProgress={Math.round((ready / total) * 100)}
      succeededProgress={Math.round((succeeded / total) * 100)}
      {...properties}
    />
  );
});
