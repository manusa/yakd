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
import {withParams} from '../router';
import metadata from '../metadata';
import svc from './';
import Card from '../components/Card';
import Form from '../components/Form';
import ResourceDetailPage from '../components/ResourceDetailPage';

const Selectors = ({selectors}) => (
  <Form.Field label='Selectors' width={Form.widths.full}>
    {Object.entries(selectors).reduce(
      (acc, [key, value]) => `${acc ? `${acc}, ` : ''}${key}=${value}`,
      ''
    )}
  </Form.Field>
);

const ExternalIps = ({ips}) => (
  <Form.Field label='External IPs'>
    {ips.length > 0 && ips.map((ip, idx) => <div key={idx}>{ip}</div>)}
    {ips.length === 0 && <div>None</div>}
  </Form.Field>
);

const ServicesDetailPage = ({service}) => (
  <ResourceDetailPage
    kind='Services'
    path='services'
    resource={service}
    deleteFunction={svc.api.delete}
    body={
      <Form>
        <metadata.Details resource={service} />
        <Selectors selectors={svc.selectors.specSelector(service)} />
        <Form.Field label='Type'>
          <svc.Type service={service} />
        </Form.Field>
        <Form.Field label='Cluster IP'>{svc.selectors.specClusterIP(service)}</Form.Field>
        <ExternalIps ips={svc.selectors.specExternalIPs(service)} />
      </Form>
    }
  >
    <svc.PortList
      title='Service Ports'
      titleVariant={Card.titleVariants.medium}
      className='mt-2'
      ports={svc.selectors.specPorts(service)}
    />
  </ResourceDetailPage>
);

const mapStateToProps = ({services}) => ({
  services
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  service: stateProps.services[ownProps.params.uid],
});

export default withParams(connect(mapStateToProps, null, mergeProps)(ServicesDetailPage));
