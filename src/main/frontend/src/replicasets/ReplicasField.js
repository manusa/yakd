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
import Form from '../components/Form';
import Icon from '../components/Icon';

const ReplicasField = ({replicas, resource, updateReplicas}) => (
  <Form.Field label='Replicas'>
    <div className='flex items-center'>
      <Icon stylePrefix='far' icon='fa-copy' className='text-gray-600 mr-2'/>
      {replicas}
      <div className='flex flex-col ml-2 text-blue-600'>
        <Icon
          icon='fa-caret-up'
          className='leading-3 hover:text-blue-800 cursor-pointer'
          onClick={() => updateReplicas(resource, replicas + 1)}
        />
        <Icon
          icon='fa-caret-down'
          className={`leading-3 ${replicas > 0
            ? 'hover:text-blue-800 cursor-pointer' : 'text-gray-600'}`}
          onClick={() => updateReplicas(resource, replicas - 1)}
        />
      </div>
    </div>
  </Form.Field>
);

export default ReplicasField;
