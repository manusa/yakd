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
import {
  describe,
  test,
  expect,
  beforeAll,
  afterAll,
  beforeEach,
  vi
} from 'vitest';
import {createTestServer} from '../../test-utils/ws-test-server.js';

const waitForOpen = ws =>
  new Promise((resolve, reject) => {
    if (ws.readyState === ws.OPEN) {
      resolve();
      return;
    }
    ws.onopen = resolve;
    ws.onerror = reject;
  });

describe('API test suite', () => {
  let testServer;
  let originalApiUrl;
  let api;

  beforeAll(async () => {
    originalApiUrl = import.meta.env.VITE_API_URL;
    testServer = createTestServer();
    const port = await testServer.start();
    import.meta.env.VITE_API_URL = `http://localhost:${port}`;
  });

  afterAll(async () => {
    import.meta.env.VITE_API_URL = originalApiUrl;
    await testServer.stop();
  });

  beforeEach(async () => {
    vi.resetModules();
    testServer.clearConnections();
    api = await import('../api.js');
  });

  describe('exec', () => {
    test('should connect to WebSocket server with correct path', async () => {
      // When
      const ws = api.exec('ns', 'name', 'container-name');
      await waitForOpen(ws);

      // Then
      expect(testServer.getConnectionCount()).toBe(1);
      expect(testServer.getConnectionPaths()).toContain(
        '/pods/ns/name/exec/container-name'
      );

      ws.close();
    });

    test('should construct URL with namespace, pod name and container', async () => {
      // When
      const ws = api.exec('my-namespace', 'my-pod', 'my-container');
      await waitForOpen(ws);

      // Then
      expect(testServer.getConnectionPaths()).toContain(
        '/pods/my-namespace/my-pod/exec/my-container'
      );

      ws.close();
    });
  });
});
