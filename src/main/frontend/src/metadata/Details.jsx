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
import {Form, Icon, Link} from '../components';
import {
  KeyValueList,
  annotations,
  creationTimestamp,
  labels,
  name,
  namespace
} from './';

const LabelsRow = ({labels}) =>
  labels &&
  Object.values(labels).length > 0 && (
    <Form.Field width={Form.widths.full}>
      <KeyValueList maxEntries={4} keyValues={labels} />
    </Form.Field>
  );

const AnnotationsRow = ({annotations}) =>
  annotations &&
  Object.values(annotations).length > 0 && (
    <Form.Field width={Form.widths.full} label='Annotations'>
      <KeyValueList.Annotations maxEntries={4} keyValues={annotations} />
    </Form.Field>
  );

export const Details = ({resource}) => {
  const ns = namespace(resource);
  const creation = creationTimestamp(resource);
  return (
    <>
      <LabelsRow labels={labels(resource)} />
      <Form.Field label='Name'>
        <Icon icon='fa-id-card' className='text-gray-600 mr-2' />
        {name(resource)}
      </Form.Field>
      {ns && (
        <Form.Field label='Namespace'>
          <Link.Namespace to={`/namespaces/${ns}`}>{ns}</Link.Namespace>
        </Form.Field>
      )}
      <Form.Field label='Creation timestamp'>
        <Icon
          stylePrefix='far'
          icon='fa-clock'
          className='text-gray-600 mr-2'
        />
        {`${creation?.toLocaleDateString() ?? ''} ${
          creation?.toLocaleTimeString() ?? ''
        }`}
      </Form.Field>
      <AnnotationsRow annotations={annotations(resource)} />
    </>
  );
};
