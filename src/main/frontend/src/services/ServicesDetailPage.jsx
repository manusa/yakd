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
import {Details} from '../metadata';
import {Card, Form} from '../components';
import {ResourceDetailPage} from '../dashboard';
import {PortList, Type, api, selectors} from './';

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

const mapStateToProps = ({services}) => ({
  services
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  service: stateProps.services[ownProps.params.uid]
});

export const ServicesDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({service}) => (
    <ResourceDetailPage
      kind='Services'
      path='services'
      resource={service}
      deleteFunction={api.deleteService}
      body={
        <Form>
          <Details resource={service} />
          <Selectors selectors={selectors.specSelector(service)} />
          <Form.Field label='Type'>
            <Type service={service} />
          </Form.Field>
          <Form.Field label='Cluster IP'>
            {selectors.specClusterIP(service)}
          </Form.Field>
          <ExternalIps ips={selectors.specExternalIPs(service)} />
        </Form>
      }
    >
      <PortList
        title='Service Ports'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
        ports={selectors.specPorts(service)}
      />
    </ResourceDetailPage>
  ))
);
