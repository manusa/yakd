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
import {api, selectors} from './';
import {withParams} from '../router';
import {Details, byUidOrName, namespace} from '../metadata';
import {Card, Form, Icon, Link, Table} from '../components';
import {ResourceDetailPage} from '../dashboard';

const mapStateToProps = ({roleBindings}) => ({
  roleBindings
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  roleBinding: byUidOrName(stateProps.roleBindings, ownProps.params.uidOrName)
});

export const RoleBindingsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({roleBinding}) => (
    <ResourceDetailPage
      kind='RoleBindings'
      path='rolebindings'
      resource={roleBinding}
      deleteFunction={api.deleteRb}
      body={
        <Form>
          <Details resource={roleBinding} />
          <Form.Field label='Kind' width={Form.widths.quarter}>
            {selectors.roleRefKind(roleBinding)}
          </Form.Field>
          <Form.Field label='Role' className='flex-1'>
            <Link.Role to={`/roles/${selectors.roleRefName(roleBinding)}`}>
              {selectors.roleRefName(roleBinding)}
            </Link.Role>
          </Form.Field>
          <Form.Field label='API Group'>
            {selectors.roleRefApiGroup(roleBinding)}
          </Form.Field>
        </Form>
      }
    >
      <Table
        title='Subjects List'
        titleVariant={Card.titleVariants.medium}
        className='mt-2'
      >
        <Table.Head
          columns={[
            'kind',
            <span key='name'>
              <Icon className='fa-id-card' /> Name
            </span>,
            'namespace'
          ]}
        />
        <Table.Body>
          {selectors.subjects(roleBinding).map(subject => (
            <Table.Row key={subject.name}>
              <Table.Cell>{subject.kind}</Table.Cell>
              <Table.Cell>{subject.name}</Table.Cell>
              <Table.Cell className='whitespace-nowrap'>
                <Link.Namespace to={`/namespaces/${namespace(roleBinding)}`}>
                  {namespace(roleBinding)}
                </Link.Namespace>
              </Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table>
    </ResourceDetailPage>
  ))
);
