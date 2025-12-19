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
import {parseHtml} from '../../test-utils';
import {ResourceListV2} from '../ResourceListV2';
import {Table} from '../Table';

const renderList = props => {
  const html = renderToString(
    <ResourceListV2 headers={['Name', 'Status']} resources={[]} {...props}>
      <tr>
        <td>Child content</td>
      </tr>
    </ResourceListV2>
  );
  return parseHtml(html);
};

describe('ResourceListV2 component tests', () => {
  describe('rendering with resources', () => {
    test('should render table element with min-w-full class', () => {
      const resources = [{id: 1, name: 'resource-1'}];
      const doc = renderList({resources});

      const table = doc.querySelector('table');
      expect(table).not.toBeNull();
      expect(table.classList.contains('min-w-full')).toBe(true);
    });

    test('should render children inside tbody when resources are present', () => {
      const resources = [{id: 1, name: 'resource-1'}];
      const doc = renderList({resources});

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).toContain('Child content');
    });

    test('should render table headers inside thead', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        headers: ['Name', 'Namespace', 'Status']
      });

      const thead = doc.querySelector('thead');
      expect(thead.textContent).toContain('Name');
      expect(thead.textContent).toContain('Namespace');
      expect(thead.textContent).toContain('Status');
    });

    test('should render headers in th elements', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        headers: ['Column1']
      });

      const thElements = doc.querySelectorAll('th');
      expect(thElements.length).toBeGreaterThan(0);
      const hasColumn1 = Array.from(thElements).some(th =>
        th.textContent.includes('Column1')
      );
      expect(hasColumn1).toBe(true);
    });

    test('should render multiple resources as children in tbody', () => {
      const resources = [
        {id: 1, name: 'pod-1'},
        {id: 2, name: 'pod-2'},
        {id: 3, name: 'pod-3'}
      ];
      const html = renderToString(
        <ResourceListV2 headers={['Name']} resources={resources}>
          {resources.map(r => (
            <Table.Row key={r.id}>
              <Table.Cell>{r.name}</Table.Cell>
            </Table.Row>
          ))}
        </ResourceListV2>
      );
      const doc = parseHtml(html);

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).toContain('pod-1');
      expect(tbody.textContent).toContain('pod-2');
      expect(tbody.textContent).toContain('pod-3');
    });
  });

  describe('empty state', () => {
    test('should show "No results found" inside tbody when resources array is empty', () => {
      const doc = renderList({resources: []});

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).toContain('No results found');
    });

    test('should not show children when resources array is empty', () => {
      const doc = renderList({resources: []});

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).not.toContain('Child content');
    });

    test('should render thead with headers even with empty resources', () => {
      const doc = renderList({resources: []});

      const thead = doc.querySelector('thead');
      expect(thead).not.toBeNull();
      expect(thead.textContent).toContain('Name');
      expect(thead.textContent).toContain('Status');
    });
  });

  describe('loading state', () => {
    test('should not show "No results found" when loading is true and no resources', () => {
      const doc = renderList({resources: [], loading: true});

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).not.toContain('No results found');
    });

    test('should show children inside tbody when loading is true but resources exist', () => {
      const resources = [{id: 1}];
      const doc = renderList({resources, loading: true});

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).toContain('Child content');
    });

    test('should render thead when loading', () => {
      const doc = renderList({resources: [], loading: true});

      const thead = doc.querySelector('thead');
      expect(thead).not.toBeNull();
    });
  });

  describe('hideWhenNoResults prop', () => {
    test('should return empty document when hideWhenNoResults is true and no resources', () => {
      const doc = renderList({
        resources: [],
        hideWhenNoResults: true
      });

      expect(doc.body.children.length).toBe(0);
    });

    test('should render table when hideWhenNoResults is true but resources exist', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        hideWhenNoResults: true
      });

      expect(doc.querySelector('table')).not.toBeNull();
      expect(doc.querySelector('tbody').textContent).toContain('Child content');
    });

    test('should render "No results found" inside tbody when hideWhenNoResults is false and no resources', () => {
      const doc = renderList({
        resources: [],
        hideWhenNoResults: false
      });

      const tbody = doc.querySelector('tbody');
      expect(tbody.textContent).toContain('No results found');
    });
  });

  describe('title prop', () => {
    test('should render title before table element', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        title: 'My Resources'
      });

      const container = doc.body.firstChild;
      const titleElement = container.firstChild;
      const table = doc.querySelector('table');

      expect(titleElement.textContent).toContain('My Resources');
      expect(
        titleElement.compareDocumentPosition(table) &
          Node.DOCUMENT_POSITION_FOLLOWING
      ).toBeTruthy();
    });

    test('should not render title element when title not provided', () => {
      const resources = [{id: 1}];
      const doc = renderList({resources});

      const container = doc.body.firstChild;
      expect(container.firstChild.tagName.toLowerCase()).toBe('table');
    });
  });

  describe('className prop', () => {
    test('should apply className to outer container', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        className: 'custom-class'
      });

      const container = doc.body.firstChild;
      expect(container.classList.contains('custom-class')).toBe(true);
    });

    test('should preserve default Card classes with custom className', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        className: 'my-custom-class'
      });

      const container = doc.body.firstChild;
      expect(container.classList.contains('my-custom-class')).toBe(true);
      expect(container.classList.contains('overflow-x-auto')).toBe(true);
    });
  });

  describe('table structure', () => {
    test('should render tbody with divide-y class for row separation', () => {
      const resources = [{id: 1}];
      const doc = renderList({resources});

      const tbody = doc.querySelector('tbody');
      expect(tbody.classList.contains('divide-y')).toBe(true);
    });

    test('should render thead tr with border-b class', () => {
      const resources = [{id: 1}];
      const doc = renderList({resources});

      const theadTr = doc.querySelector('thead tr');
      expect(theadTr.classList.contains('border-b')).toBe(true);
    });

    test('should render header cells with uppercase class', () => {
      const resources = [{id: 1}];
      const doc = renderList({
        resources,
        headers: ['Name']
      });

      const th = doc.querySelector('th');
      expect(th.classList.contains('uppercase')).toBe(true);
    });
  });

  describe('additional props', () => {
    test('should pass through data attributes to Card container', () => {
      const resources = [{id: 1}];
      const html = renderToString(
        <ResourceListV2
          headers={['Name']}
          resources={resources}
          data-testid='my-table'
        >
          <tr>
            <td>Content</td>
          </tr>
        </ResourceListV2>
      );
      const doc = parseHtml(html);

      const container = doc.body.firstChild;
      expect(container.getAttribute('data-testid')).toBe('my-table');
    });
  });
});
