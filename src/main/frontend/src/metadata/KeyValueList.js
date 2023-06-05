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
import Link from '../components/Link';
import Tag from '../components/Tag';

const KeyValueList = ({
  leftBg, leftTextColor, rightBg, rightTextColor,
  keyValues = {}, maxEntries = 10
}) => {
  const keyValueEntries = Object.entries(keyValues);
  const [collapsed, setCollapsed] = useState(true);
  const toggle = () => setCollapsed(!collapsed);
  const truncate = keyValueEntries.length > maxEntries;
  const displayedAnnotations = truncate && collapsed ? keyValueEntries.slice(0, maxEntries) : keyValueEntries;
  return (
    <div className='flex flex-wrap items-center'>
      {displayedAnnotations.map(([key, value], idx) => {
        return <Tag.Double
          key={idx} className='mr-1 mb-px' paddingY=''
          leftContent={key}
          leftBg={leftBg} leftTextColor={leftTextColor} rightBg={rightBg}
          rightContent={value.trim() ? value : '\u200B'}
        />;
      })}
      {truncate && <Link className='text-xs' onClick={toggle}>{collapsed ? '...' : 'Show less'}</Link>}
    </div>
  );
};

KeyValueList.Annotations = props => (
  <KeyValueList
    leftBg='bg-gray-300' leftTextColor='text-black' rightBg='bg-gray-700'
    {...props}
  />
)

export default KeyValueList;
