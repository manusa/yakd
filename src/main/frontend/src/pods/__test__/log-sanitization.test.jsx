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
import {LogRow} from '../PodsLogsPage';

// Helper to render LogRow and get the HTML output
const renderLogRow = logLine => renderToString(<LogRow logLine={logLine} />);

describe('LogRow component integration tests', () => {
  describe('ANSI color rendering', () => {
    test('should render ANSI red color code as styled span', () => {
      const logLine = '\x1b[31mError: Something went wrong\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#A00');
      expect(html).toContain('Error: Something went wrong');
    });

    test('should render ANSI green color code as styled span', () => {
      const logLine = '\x1b[32mSuccess: Operation completed\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#0A0');
      expect(html).toContain('Success: Operation completed');
    });

    test('should render ANSI blue color code as styled span', () => {
      const logLine = '\x1b[34mInfo: Starting process\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#00A');
      expect(html).toContain('Info: Starting process');
    });

    test('should render ANSI yellow color code as styled span', () => {
      const logLine = '\x1b[33mWarning: Check configuration\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#A50');
      expect(html).toContain('Warning: Check configuration');
    });

    test('should render bold text with b tag', () => {
      const logLine = '\x1b[1mBold text\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('<b>');
      expect(html).toContain('Bold text');
    });

    test('should render multiple ANSI codes in single line', () => {
      const logLine =
        '\x1b[31mError\x1b[0m: \x1b[33mWarning\x1b[0m - \x1b[32mOK\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#A00');
      expect(html).toContain('color:#A50');
      expect(html).toContain('color:#0A0');
    });

    test('should preserve plain text without ANSI codes', () => {
      const logLine = 'Plain log message without any formatting';
      const html = renderLogRow(logLine);
      expect(html).toContain('Plain log message without any formatting');
    });

    test('should render background color codes', () => {
      const logLine = '\x1b[41mRed background\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('background-color:#A00');
      expect(html).toContain('Red background');
    });
  });

  describe('XSS sanitization', () => {
    test('should strip script tags from log input', () => {
      const logLine = '<script>alert("XSS")</script>Normal log message';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<script>');
      expect(html).not.toContain('</script>');
      expect(html).not.toContain('alert(');
      expect(html).toContain('Normal log message');
    });

    test('should strip onclick event handlers', () => {
      const logLine = '<div onclick="alert(\'XSS\')">Click me</div>';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('onclick');
      expect(html).not.toContain('alert(');
    });

    test('should strip onerror event handlers', () => {
      const logLine = '<img src="x" onerror="alert(\'XSS\')">';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('onerror');
      expect(html).not.toContain('alert(');
    });

    test('should strip onload event handlers', () => {
      const logLine = '<body onload="alert(\'XSS\')">Content</body>';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('onload');
      expect(html).not.toContain('alert(');
    });

    test('should strip onmouseover event handlers', () => {
      const logLine = '<a onmouseover="alert(\'XSS\')">Hover me</a>';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('onmouseover');
      expect(html).not.toContain('alert(');
    });

    test('should strip javascript: protocol in href', () => {
      const logLine = '<a href="javascript:alert(\'XSS\')">Click</a>';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('javascript:');
    });

    test('should strip iframe elements', () => {
      const logLine = '<iframe src="http://evil.com"></iframe>Log text';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<iframe');
      expect(html).not.toContain('</iframe>');
      expect(html).toContain('Log text');
    });

    test('should strip object and embed elements', () => {
      const logLine =
        '<object data="malware.swf"></object><embed src="evil.swf">';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<object');
      expect(html).not.toContain('<embed');
    });

    test('should strip dangerous form action handlers', () => {
      const logLine =
        '<form action="javascript:alert(\'XSS\')"><input type="submit"></form>';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('javascript:');
    });

    test('should strip SVG with embedded script', () => {
      const logLine = '<svg><script>alert("XSS")</script></svg>';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<script>');
    });

    test('should handle encoded XSS attempts', () => {
      const logLine = '&lt;script&gt;alert("XSS")&lt;/script&gt;';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<script>');
    });

    test('should preserve ANSI colors while stripping XSS', () => {
      const logLine =
        '\x1b[31m<script>alert("XSS")</script>Error message\x1b[0m';
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#A00');
      expect(html).toContain('Error message');
      expect(html).not.toContain('<script>');
    });

    test('should handle meta refresh XSS attempt', () => {
      const logLine =
        '<meta http-equiv="refresh" content="0;url=http://evil.com">';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<meta');
    });

    test('should handle base tag injection', () => {
      const logLine = '<base href="http://evil.com/">';
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<base');
    });
  });

  describe('large log handling', () => {
    test('should handle logs with 1000 characters', () => {
      const logLine = 'A'.repeat(1000);
      const html = renderLogRow(logLine);
      expect(html).toContain(logLine);
    });

    test('should handle logs with 10000 characters', () => {
      const logLine = 'B'.repeat(10000);
      const html = renderLogRow(logLine);
      expect(html).toContain(logLine);
    });

    test('should handle large logs with ANSI codes', () => {
      const chunk = '\x1b[32mOK\x1b[0m ';
      const logLine = chunk.repeat(1000);
      const html = renderLogRow(logLine);
      expect(html).toContain('color:#0A0');
      expect(html.match(/color:#0A0/g).length).toBe(1000);
    });

    test('should handle logs with many lines concatenated', () => {
      const lines = [];
      for (let i = 0; i < 100; i++) {
        lines.push(`Line ${i}: Some log content here`);
      }
      const logLine = lines.join('\n');
      const html = renderLogRow(logLine);
      expect(html).toContain('Line 0:');
      expect(html).toContain('Line 99:');
    });

    test('should handle mixed large content with potential XSS', () => {
      const normalContent = 'Normal log message. '.repeat(100);
      const xssAttempt = '<script>alert("XSS")</script>';
      const logLine = normalContent + xssAttempt + normalContent;
      const html = renderLogRow(logLine);
      expect(html).not.toContain('<script>');
      expect(html).toContain('Normal log message.');
    });
  });

  describe('empty and null log handling', () => {
    test('should handle empty string', () => {
      const logLine = '';
      const html = renderLogRow(logLine);
      expect(html).toContain('whitespace-nowrap');
    });

    test('should handle string with only whitespace', () => {
      const logLine = '   ';
      const html = renderLogRow(logLine);
      expect(html).toContain('   ');
    });

    test('should handle string with only newlines', () => {
      const logLine = '\n\n\n';
      const html = renderLogRow(logLine);
      expect(html).toContain('\n\n\n');
    });

    test('should handle string with tabs', () => {
      const logLine = '\t\tIndented content';
      const html = renderLogRow(logLine);
      expect(html).toContain('Indented content');
    });

    test('should handle undefined by converting to string first', () => {
      const html = renderLogRow(String(undefined));
      expect(html).toContain('undefined');
    });

    test('should handle null by converting to string first', () => {
      const html = renderLogRow(String(null));
      expect(html).toContain('null');
    });

    test('should handle numeric input converted to string', () => {
      const html = renderLogRow(String(12345));
      expect(html).toContain('12345');
    });
  });

  describe('special characters handling', () => {
    test('should sanitize angle brackets that look like tags', () => {
      const logLine = 'User <admin@example.com> logged in';
      const html = renderLogRow(logLine);
      expect(html).toContain('User');
      expect(html).toContain('logged in');
    });

    test('should handle JSON in logs', () => {
      const logLine = '{"key": "value", "nested": {"array": [1, 2, 3]}}';
      const html = renderLogRow(logLine);
      expect(html).toContain('"key"');
      expect(html).toContain('"value"');
      expect(html).toContain('[1, 2, 3]');
    });

    test('should handle angle brackets in error messages', () => {
      const logLine = 'Error: Expected <number> but got <string>';
      const html = renderLogRow(logLine);
      expect(html).toContain('Expected');
      expect(html).toContain('but got');
    });

    test('should handle ampersand in logs', () => {
      const logLine = 'Request: param1=value1&param2=value2';
      const html = renderLogRow(logLine);
      expect(html).toContain('param1=value1');
      expect(html).toContain('param2=value2');
    });

    test('should handle quotes in logs', () => {
      const logLine = 'Error: "File not found" - path \'/usr/local/bin\'';
      const html = renderLogRow(logLine);
      expect(html).toContain('File not found');
      expect(html).toContain('/usr/local/bin');
    });

    test('should handle Unicode characters', () => {
      const logLine = 'Status: ✓ Success | 警告 Warning | Ошибка Error';
      const html = renderLogRow(logLine);
      expect(html).toContain('✓');
      expect(html).toContain('警告');
      expect(html).toContain('Ошибка');
    });

    test('should handle emoji in logs', () => {
      const logLine =
        '🚀 Deployment started | ✅ Build complete | ❌ Tests failed';
      const html = renderLogRow(logLine);
      expect(html).toContain('🚀');
      expect(html).toContain('✅');
      expect(html).toContain('❌');
    });
  });

  describe('component structure', () => {
    test('should render with whitespace-nowrap class', () => {
      const logLine = 'Test log line';
      const html = renderLogRow(logLine);
      expect(html).toContain('whitespace-nowrap');
    });

    test('should apply custom style prop', () => {
      const logLine = 'Test log line';
      const html = renderToString(
        <LogRow logLine={logLine} style={{top: 100, height: 19}} />
      );
      expect(html).toContain('top:100px');
      expect(html).toContain('height:19px');
    });

    test('should render as div element', () => {
      const logLine = 'Test log line';
      const html = renderLogRow(logLine);
      expect(html).toMatch(/^<div/);
      expect(html).toMatch(/<\/div>$/);
    });
  });
});
