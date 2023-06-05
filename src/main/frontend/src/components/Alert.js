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

const Alert = ({
  clearError,
  visible = true,
  margin = 'm-2',
  className = '',
  children}) => (
  <div
    className={`
      ${children ? `block translate-y-0 ${margin} py-1` : '-translate-y-full py-0'}
      ${visible ? '' : 'invisible'}
      border rounded bg-red-200 border-red-300 p-2
      cursor-pointer text-red-500 text-sm select-none
      transform transition-transform duration-200
      ${className}
      `}
    onClick={clearError}
  >
    {children}
  </div>
);
export default Alert;
