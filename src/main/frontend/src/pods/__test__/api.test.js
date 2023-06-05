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
import {getApiURL} from "../../env";

describe('API test suite', () => {
  let api;
  let mockApiURL = '';
  let WebSocket;
  beforeEach(() => {
    jest.resetModules();
    jest.mock('../../env', () => ({
      getApiURL: () => mockApiURL
    }));
    WebSocket = jest.fn();
    global.WebSocket = WebSocket;
    api = require('../api').default;
  });
  describe('exec', () => {
    describe('With absolute API url', () => {
      test('and HTTPS protocol, should call WebSocket with replaced protocol', () => {
        // Given
        mockApiURL = 'https://example.com/api/v1';
        // When
        api.exec('ns', 'name', 'container-name');
        // Then
        expect(WebSocket).toHaveBeenCalledWith('wss://example.com/api/v1/pods/ns/name/exec/container-name');
        expect(WebSocket).toHaveBeenCalledTimes(1);
      });
      test('and HTTP protocol, should call WebSocket with replaced protocol', () => {
        // Given
        mockApiURL = 'http://example.com/api/v1';
        // When
        api.exec('ns', 'name', 'container-name');
        // Then
        expect(WebSocket).toHaveBeenCalledWith('ws://example.com/api/v1/pods/ns/name/exec/container-name');
        expect(WebSocket).toHaveBeenCalledTimes(1);
      });
    });
    describe('With relative API url', () => {
      beforeEach(() => {
        mockApiURL = '/api/v1';
        Object.defineProperty(window, 'location', {value: {}});
      });
      test('and HTTPS protocol, should call WebSocket with replaced protocol', () => {
        // Given
        window.location.origin = 'https://example.com'
        // When
        api.exec('ns', 'name', 'container-name');
        // Then
        expect(WebSocket).toHaveBeenCalledWith('wss://example.com/api/v1/pods/ns/name/exec/container-name');
        expect(WebSocket).toHaveBeenCalledTimes(1);
      });
      test('and HTTP protocol, should call WebSocket with replaced protocol', () => {
        // Given
        window.location.origin = 'http://example.com:1337'
        // When
        api.exec('ns', 'name', 'container-name');
        // Then
        expect(WebSocket).toHaveBeenCalledWith('ws://example.com:1337/api/v1/pods/ns/name/exec/container-name');
        expect(WebSocket).toHaveBeenCalledTimes(1);
      });
    });
  });
});