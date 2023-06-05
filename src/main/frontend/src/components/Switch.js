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

const Switch = ({
  checked, onChange, label, labelClassName,
  width = 'w-10',
  height = 'h-6',
  checkedBg = 'bg-blue-600',
  uncheckedBg = 'bg-gray-400',
  border = 'border-gray-900',
  nobColor = 'bg-white',
  duration = 'duration-300'
}) => (
  <div className='inline-flex items-center cursor-pointer' onClick={onChange}>
    <div className='relative'>
      <span className={`
        ${checked ? checkedBg : uncheckedBg}
        block ${width} ${height} ${border} rounded-full shadow-inner
        transition-background ${duration} ease-in-out
        `}
      />
      <div className={`
        ${checked? 'translate-x-full' : ''}
        absolute block w-4 h-4 mt-1 ml-1 ${nobColor} rounded-full shadow inset-y-0 left-0 focus-within:shadow-outline
        transform transition-transform ${duration} ease-in-out
        `}
      >
        <input type='checkbox' className='absolute opacity-0 w-0 h-0' checked={checked} onChange={onChange} />
      </div>
    </div>
    {label && <span className={`ml-2 text-sm ${labelClassName ?? ''}`}>
      {label}
    </span>}
  </div>
);

export default Switch;
