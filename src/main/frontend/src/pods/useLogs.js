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
import {useEffect, useRef, useState} from 'react';
import throttle from 'lodash/throttle';
import {api, useContainers} from './';

const LOADING_LOG = ['Loading logs...'];

const initEventSource = (namespace, name, containerName, setLog) => {
  const es = api.logs(namespace, name, containerName);
  es.onopen = () => {
    es.currentLog = [];
    setLog(['Waiting for container to log messages...']);
  };
  es.onmessage = ({data}) => {
    es.currentLog.push(data);
    setLog([...es.currentLog]);
  };
  es.addEventListener('log-complete', () => {
    es.close();
  });
  return es;
};

export const useLogs = (namespace, name, containers) => {
  const listRef = useRef();
  const {selectedContainer, setSelectedContainer} = useContainers(containers);
  const containerName = selectedContainer?.name;
  const logKey =
    namespace && name && containerName
      ? `${namespace}/${name}/${containerName}`
      : null;
  // Lines are tagged with the stream they belong to, so switching containers
  // shows the loading message until the new stream reports (derived state)
  const [logState, setLogState] = useState({key: null, lines: LOADING_LOG});
  const [follow, setFollow] = useState(true);
  useEffect(() => {
    if (!logKey) {
      return undefined;
    }
    const throttledSetLog = throttle(
      lines => setLogState({key: logKey, lines}),
      100,
      {trailing: true}
    );
    const es = initEventSource(namespace, name, containerName, throttledSetLog);
    return () => {
      throttledSetLog.cancel();
      es.close();
    };
  }, [logKey, namespace, name, containerName]);
  const log = logState.key === logKey ? logState.lines : LOADING_LOG;
  useEffect(() => {
    if (follow) {
      const {current} = listRef;
      current.scrollToRow(current.props.rowCount);
    }
  }, [log, follow]);
  return {
    listRef,
    log,
    follow,
    setFollow,
    selectedContainer,
    setSelectedContainer
  };
};
