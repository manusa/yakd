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
import {Textfield} from '../Textfield';

const renderTextfield = props => {
  const html = renderToString(
    <Textfield value='' onChange={() => {}} {...props} />
  );
  return parseHtml(html);
};

describe('Textfield component tests', () => {
  describe('additional props spreading', () => {
    test('should spread a provided data-testid onto the input element', () => {
      const doc = renderTextfield({'data-testid': 'search__input'});

      const input = doc.querySelector('input');
      expect(input.getAttribute('data-testid')).toBe('search__input');
    });

    test('should keep the controlled value when additional props are spread', () => {
      const doc = renderTextfield({
        'data-testid': 'search__input',
        value: 'controlled-value'
      });

      const input = doc.querySelector('input');
      expect(input.getAttribute('value')).toBe('controlled-value');
    });
  });
});
