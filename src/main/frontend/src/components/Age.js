/*
 * Copyright 2023 Marc Nuri
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
import React, {useEffect, useState} from 'react';
import {Tooltip} from './';

const components = ms => ({
  y: Math.floor(ms / 31_536_000_000),
  d: Math.floor(ms / 86_400_000) % 365,
  h: Math.floor(ms / 3_600_000) % 24,
  m: Math.floor(ms / 60_000) % 60,
  s: Math.floor(ms / 1000) % 60,
  ms: ms % 1000
});

const toHumanReadable = ms => {
  const {y, d, h, m, s} = components(ms);
  if (y > 0) {
    return `${y}y${d}d`;
  } else if (d > 0) {
    return `${d}d${h}h`;
  } else if (h > 0) {
    return `${h}h${m}m`;
  } else if (m > 0) {
    return `${m}m${s}s`;
  } else {
    return `${s}s`;
  }
};
export const Age = ({date}) => {
  const [age, setAge] = useState(new Date() - date);
  useEffect(() => {
    const intervalPeriod = new Date() - date < 3600_000 ? 100 : 1_000;
    const timeout = setTimeout(() => setAge(new Date() - date), intervalPeriod);
    return () => clearTimeout(timeout);
  });
  return (
    <Tooltip
      content={`${date.toLocaleDateString()} ${date.toLocaleTimeString()}`}
    >
      {toHumanReadable(age)}
    </Tooltip>
  );
};
