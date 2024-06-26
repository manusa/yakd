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
import {useParams} from 'react-router-dom';

/**
 * HOC that injects the current route params into the wrapped component
 * @param {React.Component} Component - Component to wrap
 * @returns {React.Component} - Wrapped Component with route params injected as props
 */
export const withParams = Component => props => {
  const params = useParams();
  return <Component params={params} {...props} />;
};
