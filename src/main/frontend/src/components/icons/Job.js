/*
 * Copyright 2020 Marc Nuri
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import React from 'react';

const Job = ({
  ...props
}) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 18.035 17.5"
    {...props}
  >
    <path
      d="M8.958.463a1.136 1.126 0 00-.435.11l-5.94 2.838a1.136 1.126 0 00-.614.764L.504 10.55a1.136 1.126 0 00.154.864 1.136 1.126 0 00.064.09l4.112 5.111a1.136 1.126 0 00.888.424l6.592-.001a1.136 1.126 0 00.888-.424l4.11-5.112a1.136 1.126 0 00.22-.953l-1.468-6.375a1.136 1.126 0 00-.615-.764L9.51.573a1.136 1.126 0 00-.55-.11z"
      fill="#326ce5"
    />
    <path
      d="M8.955.002a1.199 1.19 0 00-.459.117l-6.27 2.995a1.199 1.19 0 00-.65.806L.03 10.65a1.199 1.19 0 00.163.912 1.199 1.19 0 00.068.095l4.34 5.396a1.199 1.19 0 00.937.447l6.96-.001a1.199 1.19 0 00.937-.447l4.339-5.397a1.199 1.19 0 00.231-1.006l-1.549-6.73a1.199 1.19 0 00-.648-.806L9.537.118a1.199 1.19 0 00-.582-.116zm.003.461a1.136 1.126 0 01.55.11L15.45 3.41a1.136 1.126 0 01.615.764l1.467 6.375a1.136 1.126 0 01-.22.953l-4.109 5.113a1.136 1.126 0 01-.888.423l-6.593.001a1.136 1.126 0 01-.888-.424l-4.11-5.111a1.136 1.126 0 01-.065-.09 1.136 1.126 0 01-.154-.863l1.465-6.375a1.136 1.126 0 01.614-.764L8.523.574a1.136 1.126 0 01.435-.11z"
      style={{
        lineHeight: "normal",
        InkscapeFontSpecification: "Sans",
        textIndent: 0,
        textAlign: "start",
        textDecorationLine: "none",
        textTransform: "none",
        marker: "none",
      }}
      color="#000"
      fontWeight={400}
      fontFamily="Sans"
      overflow="visible"
      fill="#fff"
    />
    <g fill="#fff">
      <path d="M4.764 10.796h2.203v2.143H4.764zM7.916 10.796h2.203v2.143H7.916zM11.068 10.796h2.203v2.143h-2.203zM11.068 7.646h2.203V9.79h-2.203zM7.926 7.68h2.203v2.142H7.926zM4.784 7.68h2.203v2.142H4.784zM11.068 4.562h2.203v2.143h-2.203z" />
    </g>
  </svg>
);

export default Job;
