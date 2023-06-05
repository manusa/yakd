/*
 * Copyright 2020 Marc Nuri
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import React from 'react';

const c = 20;
const r = 15.91549430918954;

const DonutChart = ({
  percent = 0,
  color = '#326ce5',
  background = '#efefef',
  width = 2,
  className = '',
  children,
  ...props
}) => (
  <div className={`relative flex items-center justify-center ${className}`} {...props}>
    <svg
      className={`absolute`}
      viewBox={`0 0 ${c*2} ${c*2}`}
    >
      <circle stroke={background} strokeWidth={width} fill='none'
              cx={c} cy={c} r={r}
      />
      <circle stroke={color} strokeWidth={width}
              strokeDasharray={`${percent},${100-percent}`}
              strokeLinecap={percent > 0 ? 'round' : ''}
              transform={`rotate(270, ${c}, ${c})`} fill='none'
              cx={c} cy={c} r={r} />
    </svg>
    <div>
      {children}
    </div>
  </div>
);

export default DonutChart;