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
import {connect} from 'react-redux';
import {withParams} from '../router';
import cm from './';
import metadata from '../metadata';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';

const DataField = ({label, value}) => (
  <Form.Field width={Form.widths.full} label={label}>
    <div
      className='bg-black text-white font-mono text-sm p-2 overflow-auto custom-scroll-dark'
      style={{maxHeight: '10rem'}}
    >
      <pre>{value}</pre>
    </div>
  </Form.Field>
)
const ConfigMapsDetailPage = ({configMap}) => (
  <ResourceDetailPage
    kind='ConfigMaps'
    path='configmaps'
    resource={configMap}
    deleteFunction={cm.api.delete}
    body={
      <Form>
        <metadata.Details resource={configMap} />
        {Object.entries(cm.selectors.data(configMap)).map(([key, value]) =>
          <DataField key={key} label={key} value={value} />)}
      </Form>
    } />
);

const mapStateToProps = ({configMaps}) => ({
  configMaps
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  configMap: stateProps.configMaps[ownProps.params.uid],
});

export default withParams(connect(mapStateToProps, null, mergeProps)(ConfigMapsDetailPage));
