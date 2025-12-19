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
import {Provider} from 'react-redux';
import {createTestStore, parseHtml} from '../../test-utils';
import {FilterBar} from '../FilterBar';

const renderFilterBar = (storeState = {}, props = {}) => {
  const store = createTestStore(storeState);
  const html = renderToString(
    <Provider store={store}>
      <FilterBar {...props} />
    </Provider>
  );
  return parseHtml(html);
};

describe('FilterBar component tests', () => {
  describe('rendering', () => {
    test('should render as a div element', () => {
      const doc = renderFilterBar();

      const container = doc.body.firstChild;
      expect(container.tagName.toLowerCase()).toBe('div');
    });

    test('should render with flex layout', () => {
      const doc = renderFilterBar();

      const container = doc.body.firstChild;
      expect(container.classList.contains('flex')).toBe(true);
    });

    test('should render with justify-end alignment', () => {
      const doc = renderFilterBar();

      const container = doc.body.firstChild;
      expect(container.classList.contains('justify-end')).toBe(true);
    });

    test('should render namespace dropdown button', () => {
      const doc = renderFilterBar();

      const button = doc.querySelector('button');
      expect(button).not.toBeNull();
    });
  });

  describe('className prop', () => {
    test('should apply custom className', () => {
      const doc = renderFilterBar({}, {className: 'my-custom-class'});

      const container = doc.body.firstChild;
      expect(container.classList.contains('my-custom-class')).toBe(true);
    });

    test('should combine custom className with default classes', () => {
      const doc = renderFilterBar({}, {className: 'extra-class'});

      const container = doc.body.firstChild;
      expect(container.classList.contains('extra-class')).toBe(true);
      expect(container.classList.contains('flex')).toBe(true);
      expect(container.classList.contains('justify-end')).toBe(true);
    });

    test('should render correctly with empty className', () => {
      const doc = renderFilterBar({}, {className: ''});

      const container = doc.body.firstChild;
      expect(container.classList.contains('flex')).toBe(true);
    });
  });

  describe('namespace dropdown', () => {
    test('should show "Namespace" text when no namespace is selected', () => {
      const doc = renderFilterBar();

      const button = doc.querySelector('button');
      expect(button.textContent).toContain('Namespace');
    });

    test('should show selected namespace name when one is selected', () => {
      const doc = renderFilterBar({
        ui: {selectedNamespace: 'kube-system'}
      });

      const button = doc.querySelector('button');
      expect(button.textContent).toContain('kube-system');
    });

    test('should apply text-blue-700 class when namespace is selected', () => {
      const doc = renderFilterBar({
        ui: {selectedNamespace: 'default'}
      });

      const button = doc.querySelector('button');
      expect(button.classList.contains('text-blue-700')).toBe(true);
    });

    test('should apply text-gray-500 class when no namespace is selected', () => {
      const doc = renderFilterBar();

      const button = doc.querySelector('button');
      expect(button.classList.contains('text-gray-500')).toBe(true);
    });
  });

  describe('namespace list', () => {
    test('should render "All namespaces" option in dropdown menu', () => {
      const doc = renderFilterBar();

      const menu = doc.querySelector('[role="menu"]');
      expect(menu.textContent).toContain('All namespaces');
    });

    test('should render namespaces from store in dropdown menu', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'default'}
        },
        'uid-2': {
          kind: 'Namespace',
          metadata: {uid: 'uid-2', name: 'kube-system'}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menu = doc.querySelector('[role="menu"]');
      expect(menu.textContent).toContain('default');
      expect(menu.textContent).toContain('kube-system');
    });

    test('should render three namespaces from store', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'production'}
        },
        'uid-2': {
          kind: 'Namespace',
          metadata: {uid: 'uid-2', name: 'staging'}
        },
        'uid-3': {
          kind: 'Namespace',
          metadata: {uid: 'uid-3', name: 'development'}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menu = doc.querySelector('[role="menu"]');
      expect(menu.textContent).toContain('production');
      expect(menu.textContent).toContain('staging');
      expect(menu.textContent).toContain('development');
    });

    test('should render namespace items with cursor-pointer class', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'my-namespace'}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menuItems = doc.querySelectorAll('[role="menu"] a');
      const hasPointerCursor = Array.from(menuItems).some(item =>
        item.classList.contains('cursor-pointer')
      );
      expect(hasPointerCursor).toBe(true);
    });
  });

  describe('dropdown structure', () => {
    test('should render dropdown with chevron icon', () => {
      const doc = renderFilterBar();

      const icon = doc.querySelector('.fa-chevron-down');
      expect(icon).not.toBeNull();
    });

    test('should render dropdown button with aria-haspopup attribute', () => {
      const doc = renderFilterBar();

      const button = doc.querySelector('button');
      expect(button.getAttribute('aria-haspopup')).toBe('true');
    });

    test('should render dropdown panel as hidden initially', () => {
      const doc = renderFilterBar();

      // Panel structure: outer div (hidden, shadow-lg) > middle div (overflow-y-auto) > [role="menu"]
      const panelOuter =
        doc.querySelector('[role="menu"]').parentElement.parentElement;
      expect(panelOuter.classList.contains('hidden')).toBe(true);
    });

    test('should render dropdown with shadow styling', () => {
      const doc = renderFilterBar();

      const panelOuter =
        doc.querySelector('[role="menu"]').parentElement.parentElement;
      expect(panelOuter.classList.contains('shadow-lg')).toBe(true);
    });

    test('should render dropdown with rounded-md class', () => {
      const doc = renderFilterBar();

      const panelOuter =
        doc.querySelector('[role="menu"]').parentElement.parentElement;
      expect(panelOuter.classList.contains('rounded-md')).toBe(true);
    });
  });

  describe('dropdown panel', () => {
    test('should render panel with role menu', () => {
      const doc = renderFilterBar();

      const menu = doc.querySelector('[role="menu"]');
      expect(menu).not.toBeNull();
    });

    test('should render panel with aria-orientation vertical', () => {
      const doc = renderFilterBar();

      const menu = doc.querySelector('[role="menu"]');
      expect(menu.getAttribute('aria-orientation')).toBe('vertical');
    });

    test('should render panel with overflow-y-auto class', () => {
      const doc = renderFilterBar();

      // overflow-y-auto is on the parent of [role="menu"]
      const menuContainer = doc.querySelector('[role="menu"]').parentElement;
      expect(menuContainer.classList.contains('overflow-y-auto')).toBe(true);
    });
  });

  describe('dropdown items styling', () => {
    test('should render items with hover:bg-gray-100 class', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'test-ns'}
        }
      };

      const doc = renderFilterBar({namespaces});

      // Dropdown.Item renders as <a> elements
      const menuItems = doc.querySelectorAll('[role="menu"] a');
      const hasHoverClass = Array.from(menuItems).some(item =>
        item.classList.contains('hover:bg-gray-100')
      );
      expect(hasHoverClass).toBe(true);
    });

    test('should render items with text-sm class', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'test-ns'}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menuItems = doc.querySelectorAll('[role="menu"] a');
      const hasTextClass = Array.from(menuItems).some(item =>
        item.classList.contains('text-sm')
      );
      expect(hasTextClass).toBe(true);
    });

    test('should render items with px-4 and py-2 padding classes', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'test-ns'}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menuItems = doc.querySelectorAll('[role="menu"] a');
      const hasPaddingClasses = Array.from(menuItems).some(
        item =>
          item.classList.contains('px-4') && item.classList.contains('py-2')
      );
      expect(hasPaddingClasses).toBe(true);
    });
  });

  describe('edge cases', () => {
    test('should handle empty namespaces object', () => {
      const doc = renderFilterBar({namespaces: {}});

      const menu = doc.querySelector('[role="menu"]');
      const button = doc.querySelector('button');
      expect(menu.textContent).toContain('All namespaces');
      expect(button.textContent).toContain('Namespace');
    });

    test('should handle namespace with special characters in name', () => {
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: 'my-app-123'}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menu = doc.querySelector('[role="menu"]');
      expect(menu.textContent).toContain('my-app-123');
    });

    test('should handle long namespace names', () => {
      const longName = 'very-long-namespace-name-that-might-overflow';
      const namespaces = {
        'uid-1': {
          kind: 'Namespace',
          metadata: {uid: 'uid-1', name: longName}
        }
      };

      const doc = renderFilterBar({namespaces});

      const menu = doc.querySelector('[role="menu"]');
      expect(menu.textContent).toContain(longName);
    });
  });
});
