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
import {PopupMenu} from '../PopupMenu';

const renderPopupMenu = () => {
  const html = renderToString(
    <PopupMenu>
      <PopupMenu.Item>An item</PopupMenu.Item>
    </PopupMenu>
  );
  return parseHtml(html);
};

describe('PopupMenu component tests', () => {
  describe('data-testid hooks', () => {
    test('should emit a "popup-menu__trigger" data-testid on the trigger button', () => {
      const doc = renderPopupMenu();

      const trigger = doc.querySelector('button');
      expect(trigger.getAttribute('data-testid')).toBe('popup-menu__trigger');
    });
  });
});
