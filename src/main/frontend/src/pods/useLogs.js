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
import p from './index';

const LOADING_MESSAGE = 'Loading logs...';

const initEventSource = (namespace, name, selectedContainer, throttledSetLog) => {
  const es = p.api.logs(namespace, name, selectedContainer.name);
  es.onopen = () => {
    es.currentLog = [];
    throttledSetLog(['Waiting for container to log messages...'])
  }
  es.onmessage = ({data}) => {
    es.currentLog.push(data);
    throttledSetLog([...es.currentLog]);
  };
  es.addEventListener('log-complete', () => {
    es.close();
  });
  es.selectedContainer = selectedContainer;
  return es;
};

const useLogs = (namespace, name, containers) => {
  const listRef = useRef();
  const {selectedContainer, setSelectedContainer} = p.useContainers(containers);
  const [log, setLog] = useState([LOADING_MESSAGE]);
  const [follow, setFollow] = useState(true);
  const throttledSetLog = throttle(setLog, 100, {trailing: true});
  const [eventSource, setEventSource] = useState();
  useEffect(() => {
    if (!eventSource && namespace && name && selectedContainer) {
      setEventSource(initEventSource(namespace, name, selectedContainer, throttledSetLog));
    } else if (eventSource && eventSource.selectedContainer !== selectedContainer) {
      eventSource.close();
      throttledSetLog([LOADING_MESSAGE]);
      setEventSource(initEventSource(namespace, name, selectedContainer, throttledSetLog));
    }
  }, [eventSource, namespace, name, throttledSetLog, selectedContainer]);
  useEffect(() => {
      if (follow) {
        const {current} = listRef;
        current.scrollToRow(current.props.rowCount);
      }
    },
    [log, follow]);
  useEffect(() => () =>  eventSource && eventSource.close(), [eventSource]);
  return {listRef, log, follow, setFollow, selectedContainer, setSelectedContainer};
};

export default useLogs;