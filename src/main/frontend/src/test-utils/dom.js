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
/* global DOMParser */

/**
 * Parses an HTML string into a DOM document for testing.
 * Uses the jsdom DOMParser available in the test environment.
 *
 * @param {string} html - The HTML string to parse
 * @returns {Document} A DOM document that can be queried
 */
export const parseHtml = html => {
  const parser = new DOMParser();
  return parser.parseFromString(html, 'text/html');
};
