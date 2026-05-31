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
import {ResourceEditModal} from '../ResourceEditModal';

const renderResourceEditModal = () => {
  const html = renderToString(
    <ResourceEditModal
      resource={{metadata: {name: 'test-resource'}}}
      save={() => {}}
      close={() => {}}
    />
  );
  return parseHtml(html);
};

describe('ResourceEditModal component tests', () => {
  describe('data-testid hooks', () => {
    test('should emit a "resource-edit__save" data-testid on the Save button', () => {
      const doc = renderResourceEditModal();

      const save = doc.querySelector('[data-testid="resource-edit__save"]');
      expect(save).not.toBeNull();
    });

    test('should emit a "resource-edit__cancel" data-testid on the Cancel button', () => {
      const doc = renderResourceEditModal();

      const cancel = doc.querySelector('[data-testid="resource-edit__cancel"]');
      expect(cancel).not.toBeNull();
    });
  });
});
