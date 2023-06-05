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
import {connect} from 'react-redux';
import {withParams} from '../router';
import s from './';
import metadata from '../metadata';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';
import Icon from '../components/Icon';

const DataField = ({label, value}) => {
  const [hidden, setHidden] = useState(true);
  const style = {
    maxHeight: '10rem'
  };
  if (hidden) {
    style.overflow = 'hidden';
    style.userSelect = 'none';
    style.color = 'transparent'
    style.textShadow = '0 0 8px white'
  }
  return (
    <Form.Field
      width={Form.widths.full}
      label={
        <>
          <a  href='#ignore'
            className='inline-block mr-2 focus:outline-none'
            onClick={() => setHidden(!hidden)}
          >
            <Icon icon={hidden ? 'fa-lock' : 'fa-lock-open'}/>
          </a>
          {label}
        </>
      }
    >
      <div
        className='bg-black text-white font-mono text-sm p-2 overflow-auto custom-scroll-dark'
        style={style}
      >
        <pre>{atob(value)}</pre>
      </div>
    </Form.Field>
  );
};

const SecretsDetailPage = ({secret}) => (
  <ResourceDetailPage
    kind='Secrets'
    path='secrets'
    resource={secret}
    deleteFunction={s.api.delete}
    body={
      <Form>
        <metadata.Details resource={secret} />
        <Form.Field label='Type'>x{s.selectors.type(secret)}</Form.Field>
        {Object.entries(s.selectors.data(secret)).map(([key, value]) =>
          <DataField key={key} label={key} value={value} />)}
      </Form>
    } />
);

const mapStateToProps = ({secrets}) => ({
  secrets
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  secret: stateProps.secrets[ownProps.params.uid],
});

export default withParams(connect(mapStateToProps, null, mergeProps)(SecretsDetailPage));
