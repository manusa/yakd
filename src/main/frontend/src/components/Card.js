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

const Card = ({className, dispatch, children, ...props}) => (
  <div
    className={`block overflow-x-auto bg-white shadow border-b border-gray-500 sm:rounded-lg ${className ?? ''}`}
    {...props}
  >
    {children}
  </div>
);

Card.titleVariants = {
  large: 'p-3 text-gray-700 font-medium text-lg',
  medium: 'px-3 py-2 text-gray-700 font-medium text-md',
  small: 'px-3 py-1 text-gray-700 font-semibold text-gray-600'
}

Card.Title = ({className, children,
  titleVariant = Card.titleVariants.large
}) => (
  <div
    className={`
      ${titleVariant}
      border-b border-blue-700 border-opacity-25
      ${className ?? ''}`}
  >
    {children}
  </div>
);

Card.Body = ({
  padding = 'p-3',
  className, children}) => (
  <div className={`${padding} ${className ?? ''}`}>
    {children}
  </div>
);

export default Card;
