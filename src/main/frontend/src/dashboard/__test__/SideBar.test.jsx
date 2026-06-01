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
import {describe, test, expect} from 'vitest';
import {renderToString} from 'react-dom/server';
import {StaticRouter} from 'react-router-dom/server';
import {Provider} from 'react-redux';
import {createTestStore, parseHtml} from '../../test-utils';
import {SideBar} from '../SideBar';

const renderSideBar = (storeState = {}) => {
  const store = createTestStore(storeState);
  const html = renderToString(
    <Provider store={store}>
      <StaticRouter location='/'>
        <SideBar sideBarOpen={true} />
      </StaticRouter>
    </Provider>
  );
  return parseHtml(html);
};

describe('SideBar component tests', () => {
  describe('data-testid hooks', () => {
    test('should mark the Search nav link with a data-testid', () => {
      const doc = renderSideBar();

      expect(
        doc.querySelector('[data-testid="side-bar__nav-search"]')
      ).not.toBeNull();
    });

    test('should mark the Home nav link with a data-testid', () => {
      const doc = renderSideBar();

      expect(
        doc.querySelector('[data-testid="side-bar__nav-home"]')
      ).not.toBeNull();
    });

    test('should mark the Pods nav link with a data-testid', () => {
      const doc = renderSideBar();

      expect(
        doc.querySelector('[data-testid="side-bar__nav-pods"]')
      ).not.toBeNull();
    });

    test('should mark the Deployments nav link with a data-testid', () => {
      const doc = renderSideBar();

      expect(
        doc.querySelector('[data-testid="side-bar__nav-deployments"]')
      ).not.toBeNull();
    });
  });

  describe('OpenShift-mode nav', () => {
    test('hides the Deployment Configs nav link on vanilla Kubernetes', () => {
      const doc = renderSideBar();

      expect(
        doc.querySelector('[data-testid="side-bar__nav-deploymentconfigs"]')
      ).toBeNull();
    });

    test('shows the Deployment Configs nav link in OpenShift mode', () => {
      const doc = renderSideBar({apiGroups: ['apps.openshift.io']});

      expect(
        doc.querySelector('[data-testid="side-bar__nav-deploymentconfigs"]')
      ).not.toBeNull();
    });
  });
});
