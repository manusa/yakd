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
import api from './api';
import reducer from './reducer';
import selectors from './selectors';
import useContainers from './useContainers';
import useExec from './useExec';
import useLogs from './useLogs';
import List from './List'
import PodsCard from './PodsCard';
import PodsDetailPage from './PodsDetailPage';
import PodsEditPage from './PodsEditPage';
import PodsExecPage from './PodsExecPage';
import PodsLogsPage from './PodsLogsPage';
import PodsPage from './PodsPage';
import StatusIcon from './StatusIcon';

const pods = {};

pods.api = api;
pods.reducer = reducer;
pods.selectors = selectors;
pods.useContainers = useContainers;
pods.useExec = useExec;
pods.useLogs = useLogs;
pods.List = List;
pods.PodsCard = PodsCard;
pods.PodsDetailPage = PodsDetailPage;
pods.PodsEditPage = PodsEditPage;
pods.PodsExecPage = PodsExecPage;
pods.PodsLogsPage = PodsLogsPage;
pods.PodsPage = PodsPage;
pods.StatusIcon = StatusIcon;

export default pods;
