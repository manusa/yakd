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
import {WebSocketServer} from 'ws';

export const createTestServer = () => {
  let server = null;
  let connections = [];
  let connectionPaths = [];
  let connectionResolvers = [];

  const start = () =>
    new Promise((resolve, reject) => {
      server = new WebSocketServer({port: 0});
      server.on('listening', () => {
        const {port} = server.address();
        resolve(port);
      });
      server.on('error', reject);
      server.on('connection', (ws, req) => {
        connectionPaths.push(req.url);
        connections.push(ws);
        connectionResolvers.forEach(resolver => resolver());
        connectionResolvers = [];
      });
    });

  const stop = () =>
    new Promise(resolve => {
      connections.forEach(ws => ws.close());
      connections = [];
      connectionPaths = [];
      connectionResolvers = [];
      if (server) {
        server.close(resolve);
        server = null;
      } else {
        resolve();
      }
    });

  const clearConnections = () => {
    connections.forEach(ws => ws.close());
    connections = [];
    connectionPaths = [];
  };

  const waitForConnection = (timeout = 5000) =>
    new Promise((resolve, reject) => {
      if (connections.length > 0) {
        resolve();
        return;
      }
      const timeoutId = setTimeout(() => {
        reject(
          new Error(
            `Timed out waiting for WebSocket connection after ${timeout}ms`
          )
        );
      }, timeout);
      connectionResolvers.push(() => {
        clearTimeout(timeoutId);
        resolve();
      });
    });

  const getConnectionPaths = () => [...connectionPaths];
  const getConnectionCount = () => connections.length;

  return {
    start,
    stop,
    clearConnections,
    waitForConnection,
    getConnectionPaths,
    getConnectionCount
  };
};
