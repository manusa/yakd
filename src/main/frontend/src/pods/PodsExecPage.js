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
import metadata from '../metadata';
import cnt from '../containers';
import p from './index';
import Card from '../components/Card';
import DashboardPage from '../components/DashboardPage';

import Link from '../components/Link';
import 'xterm/css/xterm.css';

const PodsExecPage = ({uid, namespace, name, containers}) => {
  const {ref, selectedContainer, setSelectedContainer} = p.useExec(namespace, name, containers);
  return (
    <DashboardPage
      title={
        <DashboardPage.Title path='pods' kind='Pods' namespace={namespace} name={name}>
          &nbsp;- Terminal
        </DashboardPage.Title>
      }
      className='pods-logs-page'
    >
      <div className='absolute inset-0 md:p-4 flex flex-col'>
        <Card className='flex-1 flex flex-col'>
          <Card.Title className='flex items-center'>
            <div className='flex-1 flex items-center flex-wrap'>
              <span className='mr-2'>
                Terminal
                <Link.RouterLink className='ml-2' to={`/pods/${uid}`}>{name}</Link.RouterLink>
              </span>
              <cnt.ContainerDropdown
                containers={containers} onContainerSelect={setSelectedContainer} selectedContainer={selectedContainer}
              />
            </div>
          </Card.Title>
          <Card.Body padding='p-0' className='relative flex-1 bg-black'>
            <div ref={ref} className='absolute' style={{top: '1rem', bottom: '1rem', left: '1rem', right: '1rem'}}/>
          </Card.Body>
        </Card>
      </div>
    </DashboardPage>
  );
};

const mapStateToProps = ({pods}) => ({pods});

const mergeProps = ({pods}, dispatchProps, {params: {uid}}) => ({
  ...dispatchProps,
  uid,
  namespace: metadata.selectors.namespace(pods[uid]),
  name: metadata.selectors.name(pods[uid]),
  containers: p.selectors.containers(pods[uid])
});

export default withParams(connect(mapStateToProps, null, mergeProps)(PodsExecPage));
