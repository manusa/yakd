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
import Icon from './Icon';

const Textfield = ({
  className,
  value,
  onChange,
  placeholder,
  icon,
  inputRef,
  borderColor = 'border-gray-300'
}) => (
  <div
    className={`
    inline-block px-4 py-2 rounded-md shadow-sm bg-white text-gray-700 text-sm leading-5
    flex items-center
    border ${borderColor}
    ${className ?? ''}
  `}
  >
    {icon && <Icon icon={icon} className='mr-2' />}
    <input
      ref={inputRef}
      type='text'
      className='flex-1 outline-none'
      placeholder={placeholder}
      value={value}
      onChange={onChange}
    />
  </div>
);

export default Textfield;
