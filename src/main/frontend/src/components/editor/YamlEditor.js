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
import AceEditor from 'react-ace';

import 'ace-builds/src-noconflict/mode-yaml';
import 'ace-builds/src-noconflict/theme-twilight'
import 'ace-builds/src-noconflict/ext-searchbox';

const YamlEditor = ({
                  onChange = () => {},
                  value = ''
                }) => (
  <AceEditor
    className='relative w-full h-full outline-none'
    style={{'--scrollbar-color': 'rgba(235, 218, 180, 0.4)'}}
    mode='yaml'
    theme='twilight'
    onChange={onChange}
    value={value}
    fontSize='0.9rem'
    width='100%'
    height='100%'
    tabSize={2}
    showPrintMargin={false}
  />
);

export default YamlEditor;