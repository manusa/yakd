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

const variants = {
  default: 'text-white text-xs font-medium bg-blue-600 py-1 px-2 rounded-full align-middle'
};

const Tag = ({className, children, variant = variants.default, ...props}) => (
  <span
    className={`${variant} ${className ?? ''}`}
    {...props}
  >
    {children}
  </span>
);

Tag.variants = variants;

Tag.Double = ({
  className, leftContent, rightContent,
  leftTextColor = 'text-blue-800',
  leftBg = 'bg-blue-200',
  rightTextColor = 'text-white',
  rightBg = 'bg-blue-600',
  textSize = 'text-xs',
  paddingX = 'px-2',
  paddingY = 'py-1'
}) => (
  <div className={className}>
    <span
      className={`rounded-l-full inline-block max-w-xs truncate
        ${leftTextColor} ${leftBg}
        ${paddingX} ${paddingY} ${textSize}`}
    >
      {leftContent}
    </span>
    <span
      className={`rounded-r-full inline-block max-w-xs truncate
        ${rightTextColor} ${rightBg}
        ${paddingX} ${paddingY} ${textSize}`}
    >
      {rightContent}
    </span>
  </div>
);

export default Tag;