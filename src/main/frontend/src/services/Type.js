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
import {connect} from 'react-redux';
import svc from './index';
import Link from '../components/Link';
import n from '../nodes';

const Type = ({service, firstNode}) => {
  const type = svc.selectors.specType(service);
  const nodeExternalIP = n.selectors.statusAddressExternalIPOrFirst(firstNode);
  const nodePort = svc.selectors.specPortsFirstNodePort(service);
  if (type !== 'NodePort') {
    return <>{type}</>;
  }
  return (
    <Link href={`http://${nodeExternalIP}:${nodePort}/`} target='_blank'>
      {type}
    </Link>
  );
};

const mapStateToProps = ({nodes}) => ({
  firstNode: Object.values(nodes)[0]
});

export default connect(mapStateToProps)(Type);