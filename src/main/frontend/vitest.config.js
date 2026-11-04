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
import {defineConfig} from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['src/**/*.test.{js,jsx}'],
    setupFiles: ['./src/test-utils/vitest.setup.js'],
    server: {
      deps: {
        // Workaround for a react-virtualized 10.0.0 packaging bug: its ESM build
        // imports 'dom-helpers/scrollbarSize', a directory (dom-helpers 5 has no
        // exports map) that Node's ESM loader rejects. Inlining lets Vite
        // resolve it, as it does for the app build. Remove this once a
        // react-virtualized release imports a file path there instead.
        inline: ['react-virtualized']
      }
    }
  }
});
