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
import {describe, test, expect, beforeEach} from 'vitest';
import {reducer} from '../reducer';
import {
  crudAddOrReplace,
  crudDelete,
  crudSetAll,
  crudClear,
  clear
} from '../actions';

describe('Redux reducer tests', () => {
  let podReducer;

  beforeEach(() => {
    podReducer = reducer('Pod');
  });

  describe('initial state', () => {
    test('should return empty object when no state provided', () => {
      const result = podReducer(undefined, {});
      expect(result).toEqual({});
    });

    test('should return state copy when action type is unknown', () => {
      const state = {'uid-1': {metadata: {uid: 'uid-1'}}};
      const result = podReducer(state, {type: 'UNKNOWN_ACTION'});
      expect(result).toEqual(state);
      expect(result).not.toBe(state);
    });
  });

  describe('CRUD_ADD_OR_REPLACE', () => {
    test('should add resource to empty state', () => {
      const resource = {
        kind: 'Pod',
        metadata: {uid: 'pod-123', name: 'my-pod'}
      };
      const result = podReducer({}, crudAddOrReplace(resource));
      expect(result['pod-123']).toEqual(resource);
    });

    test('should add resource to existing state', () => {
      const existingResource = {
        kind: 'Pod',
        metadata: {uid: 'pod-1', name: 'existing-pod'}
      };
      const newResource = {
        kind: 'Pod',
        metadata: {uid: 'pod-2', name: 'new-pod'}
      };
      const state = {'pod-1': existingResource};

      const result = podReducer(state, crudAddOrReplace(newResource));

      expect(Object.keys(result)).toHaveLength(2);
      expect(result['pod-1']).toEqual(existingResource);
      expect(result['pod-2']).toEqual(newResource);
    });

    test('should replace existing resource with same UID', () => {
      const oldResource = {
        kind: 'Pod',
        metadata: {uid: 'pod-123', name: 'old-name'}
      };
      const updatedResource = {
        kind: 'Pod',
        metadata: {uid: 'pod-123', name: 'updated-name'}
      };
      const state = {'pod-123': oldResource};

      const result = podReducer(state, crudAddOrReplace(updatedResource));

      expect(Object.keys(result)).toHaveLength(1);
      expect(result['pod-123'].metadata.name).toBe('updated-name');
    });

    test('should ignore resources of different kind', () => {
      const deploymentResource = {
        kind: 'Deployment',
        metadata: {uid: 'deploy-123', name: 'my-deployment'}
      };
      const state = {};

      const result = podReducer(state, crudAddOrReplace(deploymentResource));

      expect(result).toEqual({});
    });

    test('should not mutate original state', () => {
      const state = {'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}}};
      const newResource = {kind: 'Pod', metadata: {uid: 'pod-2'}};

      podReducer(state, crudAddOrReplace(newResource));

      expect(Object.keys(state)).toHaveLength(1);
      expect(state['pod-2']).toBeUndefined();
    });
  });

  describe('CRUD_DELETE', () => {
    test('should delete resource from state', () => {
      const resource = {kind: 'Pod', metadata: {uid: 'pod-123'}};
      const state = {'pod-123': resource};

      const result = podReducer(state, crudDelete(resource));

      expect(result['pod-123']).toBeUndefined();
      expect(Object.keys(result)).toHaveLength(0);
    });

    test('should only delete matching resource', () => {
      const pod1 = {kind: 'Pod', metadata: {uid: 'pod-1'}};
      const pod2 = {kind: 'Pod', metadata: {uid: 'pod-2'}};
      const state = {'pod-1': pod1, 'pod-2': pod2};

      const result = podReducer(state, crudDelete(pod1));

      expect(result['pod-1']).toBeUndefined();
      expect(result['pod-2']).toEqual(pod2);
    });

    test('should ignore delete for different kind', () => {
      const pod = {kind: 'Pod', metadata: {uid: 'pod-1'}};
      const deployment = {kind: 'Deployment', metadata: {uid: 'pod-1'}};
      const state = {'pod-1': pod};

      const result = podReducer(state, crudDelete(deployment));

      expect(result['pod-1']).toEqual(pod);
    });

    test('should handle delete of non-existent resource', () => {
      const pod = {kind: 'Pod', metadata: {uid: 'non-existent'}};
      const existingPod = {kind: 'Pod', metadata: {uid: 'pod-1'}};
      const state = {'pod-1': existingPod};

      const result = podReducer(state, crudDelete(pod));

      expect(result['pod-1']).toEqual(existingPod);
    });

    test('should not mutate original state', () => {
      const resource = {kind: 'Pod', metadata: {uid: 'pod-123'}};
      const state = {'pod-123': resource};

      podReducer(state, crudDelete(resource));

      expect(state['pod-123']).toBeDefined();
    });
  });

  describe('CRUD_SET_ALL', () => {
    test('should set all resources from empty state', () => {
      const resources = [
        {kind: 'Pod', metadata: {uid: 'pod-1', name: 'pod-one'}},
        {kind: 'Pod', metadata: {uid: 'pod-2', name: 'pod-two'}}
      ];

      const result = podReducer({}, crudSetAll({kind: 'Pod', resources}));

      expect(Object.keys(result)).toHaveLength(2);
      expect(result['pod-1'].metadata.name).toBe('pod-one');
      expect(result['pod-2'].metadata.name).toBe('pod-two');
    });

    test('should replace all existing resources', () => {
      const existingState = {
        'pod-old': {kind: 'Pod', metadata: {uid: 'pod-old', name: 'old-pod'}}
      };
      const newResources = [
        {kind: 'Pod', metadata: {uid: 'pod-new', name: 'new-pod'}}
      ];

      const result = podReducer(
        existingState,
        crudSetAll({kind: 'Pod', resources: newResources})
      );

      expect(result['pod-old']).toBeUndefined();
      expect(result['pod-new']).toBeDefined();
      expect(Object.keys(result)).toHaveLength(1);
    });

    test('should set empty state when resources array is empty', () => {
      const existingState = {
        'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}}
      };

      const result = podReducer(
        existingState,
        crudSetAll({kind: 'Pod', resources: []})
      );

      expect(Object.keys(result)).toHaveLength(0);
    });

    test('should ignore set all for different kind', () => {
      const existingState = {
        'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}}
      };
      const deployments = [{kind: 'Deployment', metadata: {uid: 'deploy-1'}}];

      const result = podReducer(
        existingState,
        crudSetAll({kind: 'Deployment', resources: deployments})
      );

      expect(result['pod-1']).toBeDefined();
    });
  });

  describe('CRUD_CLEAR', () => {
    test('should clear all resources for matching kind', () => {
      const state = {
        'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}},
        'pod-2': {kind: 'Pod', metadata: {uid: 'pod-2'}}
      };

      const result = podReducer(state, crudClear('Pod'));

      expect(Object.keys(result)).toHaveLength(0);
    });

    test('should not clear resources for different kind', () => {
      const state = {
        'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}}
      };

      const result = podReducer(state, crudClear('Deployment'));

      expect(result['pod-1']).toBeDefined();
    });

    test('should return state copy when kind does not match', () => {
      const state = {'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}}};

      const result = podReducer(state, crudClear('Service'));

      expect(result).toEqual(state);
      expect(result).not.toBe(state);
    });
  });

  describe('CLEAR', () => {
    test('should clear all resources regardless of kind', () => {
      const state = {
        'pod-1': {kind: 'Pod', metadata: {uid: 'pod-1'}},
        'pod-2': {kind: 'Pod', metadata: {uid: 'pod-2'}}
      };

      const result = podReducer(state, clear());

      expect(Object.keys(result)).toHaveLength(0);
    });

    test('should return empty object for already empty state', () => {
      const result = podReducer({}, clear());

      expect(result).toEqual({});
    });
  });

  describe('reducer factory', () => {
    test('should create isolated reducers for different kinds', () => {
      const deploymentReducer = reducer('Deployment');
      const serviceReducer = reducer('Service');

      const deploymentResource = {
        kind: 'Deployment',
        metadata: {uid: 'deploy-1'}
      };
      const serviceResource = {
        kind: 'Service',
        metadata: {uid: 'svc-1'}
      };

      const deploymentResult = deploymentReducer(
        {},
        crudAddOrReplace(deploymentResource)
      );
      const serviceResult = serviceReducer(
        {},
        crudAddOrReplace(serviceResource)
      );

      expect(deploymentResult['deploy-1']).toBeDefined();
      expect(deploymentResult['svc-1']).toBeUndefined();

      expect(serviceResult['svc-1']).toBeDefined();
      expect(serviceResult['deploy-1']).toBeUndefined();
    });

    test('should handle resources without kind property', () => {
      const resource = {metadata: {uid: 'pod-123'}};

      const result = podReducer({}, crudAddOrReplace(resource));

      expect(result['pod-123']).toBeUndefined();
    });
  });
});
