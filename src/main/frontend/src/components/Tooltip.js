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
import React, {useState} from 'react';

const Tooltip = ({
  content,
  children,
  className = '',
  gutter = 6,
  bg = 'bg-black bg-opacity-75',
  color = 'text-white',
  ...props
}) => {
  const [visible, setVisible] = useState(false);
  const [{x, y}, setCoords] = useState({x: 0 , y: 0})
  return (
    <div
      className={`relative ${className}`}
      onMouseEnter={() => setVisible(true)}
      onMouseMove={({clientX, clientY}) =>
        setCoords({x: clientX, y: clientY})}
      onMouseLeave={() => setVisible(false)}
      {...props}
    >
      <div
        className={`
          ${visible ? 'visible' : 'invisible'}
          fixed block
          ${bg} ${color} py-1 px-2 border-0  z-20 font-normal leading-normal text-sm max-w-xs break-words rounded-lg
        `}
        style={{left: `${x + gutter}px` , top: `${y + gutter}px`}}
      >
        {content}
      </div>
      <div className='z-10'>
        {children}
      </div>
    </div>
  );
};

export default Tooltip;