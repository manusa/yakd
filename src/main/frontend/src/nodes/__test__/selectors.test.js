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
import {describe, test, expect} from 'vitest';
import {isMinikube} from '../selectors';

describe('Nodes selectors tests', () => {
  describe('isMinikube', () => {
    const minikubeLabels = {
      'minikube.k8s.io/name': 'minikube',
      'minikube.k8s.io/version': 'v1.32.0'
    };
    const nodesWith = labels => ({
      'minikube-uid': {
        metadata: {name: 'minikube', uid: 'minikube-uid', labels}
      }
    });

    test('should return true for a single minikube node with the required labels', () => {
      expect(isMinikube(nodesWith(minikubeLabels))).toBe(true);
    });

    test('should return true when the version label is present but empty (own-property check, not truthiness)', () => {
      expect(
        isMinikube(
          nodesWith({...minikubeLabels, 'minikube.k8s.io/version': ''})
        )
      ).toBe(true);
    });

    test('should return false when the minikube.k8s.io/version label is absent', () => {
      expect(isMinikube(nodesWith({'minikube.k8s.io/name': 'minikube'}))).toBe(
        false
      );
    });

    test('should return false when the minikube.k8s.io/name label is not minikube', () => {
      expect(
        isMinikube(
          nodesWith({
            'minikube.k8s.io/name': 'not-minikube',
            'minikube.k8s.io/version': 'v1.32.0'
          })
        )
      ).toBe(false);
    });

    test('should return false when the single node is not named minikube', () => {
      expect(
        isMinikube({
          'node-uid': {
            metadata: {name: 'node-1', uid: 'node-uid', labels: minikubeLabels}
          }
        })
      ).toBe(false);
    });

    test('should return false when there is more than one node', () => {
      expect(
        isMinikube({
          'minikube-uid': {
            metadata: {
              name: 'minikube',
              uid: 'minikube-uid',
              labels: minikubeLabels
            }
          },
          'extra-uid': {
            metadata: {
              name: 'minikube',
              uid: 'extra-uid',
              labels: minikubeLabels
            }
          }
        })
      ).toBe(false);
    });

    test('should return false for an empty node collection', () => {
      expect(isMinikube({})).toBe(false);
    });
  });
});
