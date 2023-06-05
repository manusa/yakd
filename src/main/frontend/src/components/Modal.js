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

const Modal = ({visible, children}) => (visible ?
    <div className='fixed z-10 inset-0 overflow-hidden'>
      <div className='flex items-center justify-center min-h-screen lg:pl-64'> {/* pl-64 for sidebar */}
        <div className='fixed inset-0 transition-opacity' aria-hidden='true'>
          <div className='absolute inset-0 bg-gray-500 opacity-75' />
        </div>
        <div
          className='inline-block w-full max-h-screen max-w-6xl overflow-hidden shadow-xl transform transition-all'
          role='dialog' aria-modal='true' aria-labelledby='modal-headline'
        >
          {children}
        </div>
      </div>
    </div> :
    ''
);

export default Modal;
