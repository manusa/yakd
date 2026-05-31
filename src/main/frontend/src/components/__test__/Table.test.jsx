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
import {Table} from '../Table';

const renderResourceRow = props => {
  const html = renderToString(
    <table>
      <tbody>
        <Table.ResourceRow resource={{}} {...props}>
          <Table.Cell>Cell content</Table.Cell>
        </Table.ResourceRow>
      </tbody>
    </table>
  );
  return parseHtml(html);
};

describe('Table component tests', () => {
  describe('ResourceRow data-testid hooks', () => {
    test('should emit a default data-testid of "resource-list__row" on the row when none is provided', () => {
      const doc = renderResourceRow();

      const row = doc.querySelector('tr');
      expect(row.getAttribute('data-testid')).toBe('resource-list__row');
    });

    test('should allow overriding the default row data-testid', () => {
      const doc = renderResourceRow({'data-testid': 'list__events-row'});

      const row = doc.querySelector('tr');
      expect(row.getAttribute('data-testid')).toBe('list__events-row');
    });
  });
});
