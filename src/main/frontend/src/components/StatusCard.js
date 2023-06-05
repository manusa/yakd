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
import {useNavigate} from 'react-router-dom';
import icons from '../components/icons';
import Tooltip from './Tooltip';

const Progress = ({
  bg='bg-orange-400',
  ready,
  readyProgress,
  readyColor = 'bg-blue-700',
  succeeded,
  succeededProgress,
  succeededColor = 'bg-blue-400',
}) => (
  <div className='self-stretch mx-6'>
    <div className={`w-full rounded flex ${bg}`}>
      <Tooltip
        className={`leading-none py-1 rounded-l ${succeededProgress === 0 ? 'rounded-r' : ''} ${readyColor}`} gutter={12}
        style={{width: `${readyProgress}%`}} content={`Ready: ${ready}`}
      />
      <Tooltip
        className={`leading-none py-1 rounded-r ${succeededColor}`} gutter={12}
        style={{width: `${succeededProgress}%`}} content={`Succeeded: ${succeeded}`}
      />
    </div>
  </div>
);

const StatusCard = ({
  header,
  Icon = icons.k8s,
  ready = 0,
  succeeded = 0,
  total = 0,
  readyProgress = 0,
  succeededProgress = 0,
  responsiveClassName = '',
  className = '',
  to
}) => {
  const navigate = useNavigate();
  const onClick = () => {
    if (to) {
      navigate(to);
    }
  }
  return (
    <div className={responsiveClassName}>
      <div className={`
        flex flex-col items-center py-6 shadow-sm rounded-md bg-white border-gray-700
        ${className}
      `}>
        <h2 className='font-semibold text-lg mb-2'>{header}</h2>
        <Icon
          onClick={onClick}
          className='cursor-pointer w-20'
          alt={`Icon for ${header} status`}
        />
        <h3 className='text-lg my-2'>{ready} / {total}</h3>
        <Progress
          ready={ready} readyProgress={readyProgress} succeeded={succeeded} succeededProgress={succeededProgress}
        />
      </div>
    </div>
  );
}

export default StatusCard;
