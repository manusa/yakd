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
import {shallowEqual, useDispatch, useSelector} from 'react-redux';
import {clearError} from '../redux';
import * as apis from '../apis';
import {name as metadataName} from '../metadata';
import {selectors as nodeSelectors} from '../nodes';
import {
  Alert,
  Icon,
  Link,
  Tooltip,
  MinikubeIcon,
  OpenShiftIcon,
  YAKDLogo
} from '../components';
import {NewResource} from './NewResource';
import {NewResourceButton} from './NewResourceButton';
import {SideBar} from './SideBar';

const OfflineIcon = () => (
  <div className='fa-stack text-red-700' title='Watchers stopped (No network)'>
    <Icon icon='fa-wifi fa-stack-1x' />
    <Icon icon='fa-slash fa-stack-1x' />
  </div>
);

const Header = ({setSideBarOpen, title}) => {
  const {isMinikube, isOpenShift, offline} = useSelector(
    ({apiGroups, nodes, ui: {offline}}) => ({
      isMinikube: nodeSelectors.isMinikube(nodes),
      isOpenShift: apis.selectors.isOpenShift(apiGroups),
      offline
    }),
    shallowEqual
  );
  return (
    <header className='flex justify-between items-center py-4 px-6 bg-white border-b-2 border-blue-700/75'>
      <div className='flex w-full items-center'>
        <button
          onClick={() => setSideBarOpen(true)}
          className='flex items-center text-gray-500 focus:outline-none lg:hidden'
        >
          <svg
            className='h-6 w-6'
            viewBox='0 0 24 24'
            fill='none'
            xmlns='http://www.w3.org/2000/svg'
          >
            <path
              d='M4 6H20M4 12H20M4 18H11'
              stroke='currentColor'
              strokeWidth='2'
              strokeLinecap='round'
              strokeLinejoin='round'
            />
          </svg>
          <YAKDLogo className='block ml-2 h-6' />
        </button>
        <div className='flex relative items-center mx-4 lg:text-xl lg:mx-0 flex-1 truncate'>
          {isMinikube && (
            <Tooltip content='Minikube cluster detected'>
              <MinikubeIcon className='h-6 mr-2' />
            </Tooltip>
          )}
          {isOpenShift && (
            <Tooltip content='OpenShift cluster detected'>
              <OpenShiftIcon className='h-6 mr-2' />
            </Tooltip>
          )}
          {title}
        </div>
        <NewResourceButton />
        {offline && <OfflineIcon />}
      </div>
    </header>
  );
};

const Footer = () => (
  <footer className='flex items-center p-3 text-sm bg-white border-t border-blue-700/75 text-gray-700'>
    <div>
      Copyright © 2024 - <Link href='https://www.marcnuri.com'>Marc Nuri</Link>{' '}
      - Licensed under the{' '}
      <Link href='https://www.apache.org/licenses/LICENSE-2.0'>
        Apache License 2.0
      </Link>
    </div>
  </footer>
);

export const DashboardPage = ({className, title, children}) => {
  const dispatch = useDispatch();
  const doClearError = () => dispatch(clearError());
  const [sideBarOpen, setSideBarOpen] = useState(false);
  const error = useSelector(({ui: {error}}) => error);
  return (
    <div
      className={`dashboard-page flex h-screen bg-gray-200 overflow-hidden ${
        className ?? ''
      }`}
    >
      <div
        onClick={() => setSideBarOpen(false)}
        className={`${sideBarOpen ? 'visible opacity-50' : 'invisible'}
          fixed z-20 inset-0 bg-black opacity-0 transition-all duration-300 lg:hidden`}
      />
      <SideBar sideBarOpen={sideBarOpen} />
      <div className='flex-1 flex flex-col overflow-hidden'>
        <Header setSideBarOpen={setSideBarOpen} title={title} />
        <main className='flex-1 flex flex-col overflow-x-hidden overflow-y-auto bg-gray-200'>
          <Alert clearError={doClearError}>{error}</Alert>
          <div className='flex-1 w-100 p-4 relative'>{children}</div>
          <Footer />
          <NewResource />
        </main>
      </div>
    </div>
  );
};

DashboardPage.Title = ({
  path,
  kind,
  namespace,
  name,
  resource,
  isReadyFunction,
  readyIcon = 'fa-check',
  readyClassName = 'text-green-500',
  notReadyClassName = 'text-red-500',
  notReadyIcon = 'fa-exclamation-circle',
  children
}) => (
  <div className='flex items-center'>
    <Link.ResourceLink to={`/${path}`}>{kind}</Link.ResourceLink>
    {namespace && <>&nbsp;- {namespace}</>}
    {name && <>&nbsp;- {name}</>}
    {!name && resource && <>&nbsp;- {metadataName(resource)}</>}
    {isReadyFunction && (
      <Icon
        className={`ml-2 ${
          isReadyFunction(resource) ? readyClassName : notReadyClassName
        }`}
        icon={isReadyFunction(resource) ? readyIcon : notReadyIcon}
      />
    )}
    {children}
  </div>
);
