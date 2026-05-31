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
import {Modal} from '../Modal';

const renderModal = props => {
  const html = renderToString(
    <Modal visible {...props}>
      <div>Modal content</div>
    </Modal>
  );
  return parseHtml(html);
};

describe('Modal component tests', () => {
  describe('data-testid hooks', () => {
    test('should emit a default data-testid of "modal" on the dialog when none is provided', () => {
      const doc = renderModal();

      const dialog = doc.querySelector('[role="dialog"]');
      expect(dialog.getAttribute('data-testid')).toBe('modal');
    });

    test('should allow overriding the default dialog data-testid', () => {
      const doc = renderModal({'data-testid': 'resource-edit__modal'});

      const dialog = doc.querySelector('[role="dialog"]');
      expect(dialog.getAttribute('data-testid')).toBe('resource-edit__modal');
    });
  });
});
