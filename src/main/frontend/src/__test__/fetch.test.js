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
/* global Response */
import {describe, test, expect} from 'vitest';
import {processResponse, toJson, fixKind} from '../fetch';

describe('Fetch wrapper tests', () => {
  describe('processResponse', () => {
    describe('with successful responses', () => {
      test('should return response when status is 200', async () => {
        const response = new Response('{"data": "test"}', {status: 200});
        const result = await processResponse(response);
        expect(result).toBe(response);
      });

      test('should return response when status is 201', async () => {
        const response = new Response('{"created": true}', {status: 201});
        const result = await processResponse(response);
        expect(result).toBe(response);
      });

      test('should return response when status is 204', async () => {
        const response = new Response(null, {status: 204});
        const result = await processResponse(response);
        expect(result).toBe(response);
      });
    });

    describe('with error responses', () => {
      test('should throw error for 400 Bad Request', async () => {
        const response = new Response('Bad Request', {
          status: 400,
          statusText: 'Bad Request'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '400 Bad Request: Bad Request'
        );
      });

      test('should throw error for 401 Unauthorized', async () => {
        const response = new Response('Unauthorized', {
          status: 401,
          statusText: 'Unauthorized'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '401 Unauthorized: Unauthorized'
        );
      });

      test('should throw error for 403 Forbidden', async () => {
        const response = new Response('Forbidden', {
          status: 403,
          statusText: 'Forbidden'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '403 Forbidden: Forbidden'
        );
      });

      test('should throw error for 404 Not Found', async () => {
        const response = new Response('Resource not found', {
          status: 404,
          statusText: 'Not Found'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '404 Not Found: Resource not found'
        );
      });

      test('should throw error for 500 Internal Server Error', async () => {
        const response = new Response('Server error', {
          status: 500,
          statusText: 'Internal Server Error'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '500 Internal Server Error: Server error'
        );
      });

      test('should throw error for 502 Bad Gateway', async () => {
        const response = new Response('Bad gateway', {
          status: 502,
          statusText: 'Bad Gateway'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '502 Bad Gateway: Bad gateway'
        );
      });

      test('should throw error for 503 Service Unavailable', async () => {
        const response = new Response('Service unavailable', {
          status: 503,
          statusText: 'Service Unavailable'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '503 Service Unavailable: Service unavailable'
        );
      });

      test('should extract message from JSON error response', async () => {
        const errorBody = JSON.stringify({
          message: 'Pod my-pod not found in namespace default'
        });
        const response = new Response(errorBody, {
          status: 404,
          statusText: 'Not Found'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '404 Not Found: Pod my-pod not found in namespace default'
        );
      });

      test('should handle Kubernetes API error format', async () => {
        const k8sError = JSON.stringify({
          kind: 'Status',
          apiVersion: 'v1',
          status: 'Failure',
          message: 'deployments.apps "my-deployment" not found',
          reason: 'NotFound',
          code: 404
        });
        const response = new Response(k8sError, {
          status: 404,
          statusText: 'Not Found'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '404 Not Found: deployments.apps "my-deployment" not found'
        );
      });

      test('should handle JSON response without message field', async () => {
        const errorBody = JSON.stringify({
          error: 'Something went wrong',
          code: 500
        });
        const response = new Response(errorBody, {
          status: 500,
          statusText: 'Internal Server Error'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '500 Internal Server Error: ' + errorBody
        );
      });

      test('should handle empty error response body', async () => {
        const response = new Response('', {
          status: 500,
          statusText: 'Internal Server Error'
        });

        await expect(processResponse(response)).rejects.toThrow(
          '500 Internal Server Error: '
        );
      });
    });
  });

  describe('toJson', () => {
    test('should parse valid JSON response', async () => {
      const jsonData = {name: 'my-pod', namespace: 'default'};
      const response = new Response(JSON.stringify(jsonData), {status: 200});

      const result = await toJson(response);

      expect(result).toEqual(jsonData);
    });

    test('should parse array JSON response', async () => {
      const jsonData = [{id: 1}, {id: 2}, {id: 3}];
      const response = new Response(JSON.stringify(jsonData), {status: 200});

      const result = await toJson(response);

      expect(result).toEqual(jsonData);
    });

    test('should parse complex nested JSON response', async () => {
      const jsonData = {
        metadata: {
          name: 'my-pod',
          namespace: 'default',
          labels: {
            app: 'my-app'
          }
        },
        spec: {
          containers: [{name: 'nginx', image: 'nginx:latest'}]
        }
      };
      const response = new Response(JSON.stringify(jsonData), {status: 200});

      const result = await toJson(response);

      expect(result).toEqual(jsonData);
    });

    test('should throw error for non-ok response', async () => {
      const response = new Response('Not found', {
        status: 404,
        statusText: 'Not Found'
      });

      await expect(toJson(response)).rejects.toThrow(
        '404 Not Found: Not found'
      );
    });
  });

  describe('fixKind', () => {
    test('should add kind to single resource', () => {
      const resources = [{metadata: {name: 'pod-1'}}];

      const result = fixKind('Pod')(resources);

      expect(result).toHaveLength(1);
      expect(result[0].kind).toBe('Pod');
      expect(result[0].metadata.name).toBe('pod-1');
    });

    test('should add kind to multiple resources', () => {
      const resources = [
        {metadata: {name: 'deploy-1'}},
        {metadata: {name: 'deploy-2'}},
        {metadata: {name: 'deploy-3'}}
      ];

      const result = fixKind('Deployment')(resources);

      expect(result).toHaveLength(3);
      result.forEach(r => {
        expect(r.kind).toBe('Deployment');
      });
    });

    test('should preserve existing properties', () => {
      const resources = [
        {
          metadata: {name: 'svc-1', namespace: 'default'},
          spec: {type: 'ClusterIP', ports: [{port: 80}]}
        }
      ];

      const result = fixKind('Service')(resources);

      expect(result[0].kind).toBe('Service');
      expect(result[0].metadata.namespace).toBe('default');
      expect(result[0].spec.type).toBe('ClusterIP');
    });

    test('should return empty array for empty input', () => {
      const result = fixKind('Pod')([]);

      expect(result).toEqual([]);
    });

    test('should not override existing kind property due to spread order', () => {
      const resources = [{kind: 'OldKind', metadata: {name: 'resource-1'}}];

      const result = fixKind('NewKind')(resources);

      expect(result[0].kind).toBe('OldKind');
    });

    test('should work with different Kubernetes resource kinds', () => {
      const kinds = [
        'Pod',
        'Deployment',
        'Service',
        'ConfigMap',
        'Secret',
        'Namespace',
        'Node',
        'PersistentVolume',
        'StatefulSet',
        'DaemonSet'
      ];

      kinds.forEach(kind => {
        const resources = [{metadata: {name: 'test'}}];
        const result = fixKind(kind)(resources);
        expect(result[0].kind).toBe(kind);
      });
    });

    test('should not mutate original resources array', () => {
      const original = [{metadata: {name: 'pod-1'}}];

      fixKind('Pod')(original);

      expect(original[0].kind).toBeUndefined();
    });
  });
});
