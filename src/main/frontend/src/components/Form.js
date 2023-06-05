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

const Form = ({formClassName, className, children}) => (
  <form className={`w-full ${formClassName ?? ''}`} onSubmit={e => e.preventDefault()}>
    <div className={`flex flex-wrap -px-2 ${className ?? ''}`}>
      {children}
    </div>
  </form>
);

Form.widths = {
  full: 'w-full',
  half: 'w-full md:w-1/2',
  third: 'w-full md:w-1/3'
};

Form.Field = ({className, label, labelClassName, children, width = Form.widths.third, ...props}) => (
  <div
    className={`my-2 text-gray-900 ${width} ${className ?? ''}`}
    {...props}
  >
    <label
      className={`block relative uppercase tracking-wide text-gray-700 text-xs font-bold mb-1
        ${labelClassName ?? ''}`}
    >
      {label}
    </label>
    {children}
  </div>
);

export default Form;
