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
import React, {useEffect, useState} from 'react';
import {connect} from 'react-redux';
import {withParams} from '../router';
import {Details} from '../metadata';
import {api as crApi, CustomResourceList} from '../customresources';
import {api, selectors, DashboardPageTitle, GroupLink} from './';
import {Card, Form, Link} from '../components';
import {ResourceDetailPage} from '../dashboard';

const useCustomResourceList = customResourceDefinition => {
  const [customResourceList, setCustomResourceList] = useState([]);
  const [version, setVersion] = useState(undefined);
  const [timeoutHandle, setTimeoutHandle] = useState(null);
  const changeVersion = newVersion => {
    clearTimeout(timeoutHandle);
    setTimeoutHandle(null);
    setVersion(newVersion);
  };
  useEffect(() => {
    if (!timeoutHandle && customResourceDefinition) {
      const updateCustomResources = async () => {
        try {
          const customResources = await crApi.list(
            customResourceDefinition,
            version
          )();
          setCustomResourceList(customResources);
        } catch (e) {
          setCustomResourceList(null);
        }
        setTimeoutHandle(setTimeout(updateCustomResources, 5000));
      };
      updateCustomResources().then(() => {});
    }
  }, [
    timeoutHandle,
    setTimeoutHandle,
    setCustomResourceList,
    customResourceDefinition,
    version
  ]);
  useEffect(
    () => () => {
      clearTimeout(timeoutHandle);
    },
    [timeoutHandle, version]
  );
  return [customResourceList, setCustomResourceList, version, changeVersion];
};

const mapStateToProps = ({customResourceDefinitions}) => ({
  customResourceDefinitions
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  customResourceDefinition:
    stateProps.customResourceDefinitions[ownProps.params.uid]
});

export const CustomResourceDefinitionsDetailPage = withParams(
  connect(
    mapStateToProps,
    null,
    mergeProps
  )(({customResourceDefinition}) => {
    const [customResourceList, setCustomResourceList, version, changeVersion] =
      useCustomResourceList(customResourceDefinition);
    const kind = 'CustomResourceDefinitions';
    const path = 'customresourcedefinitions';
    const applicableVersion = version
      ? version
      : selectors.specVersionsLatest(customResourceDefinition);
    return (
      <ResourceDetailPage
        kind={kind}
        path={path}
        title={
          <DashboardPageTitle
            customResourceDefinition={customResourceDefinition}
          />
        }
        resource={customResourceDefinition}
        deleteFunction={api.deleteCrd}
        body={
          <Form>
            <Details resource={customResourceDefinition} />
            <Form.Field label='Group'>
              <GroupLink customResourceDefinition={customResourceDefinition} />
            </Form.Field>
            <Form.Field label='Versions'>
              {selectors.specVersions(customResourceDefinition).map(v => (
                <div key={v}>
                  {v === applicableVersion ? (
                    <span>{v}</span>
                  ) : (
                    <Link onClick={() => changeVersion(v)}>{v}</Link>
                  )}
                </div>
              ))}
            </Form.Field>
            <Form.Field label='Scope'>
              {selectors.specScope(customResourceDefinition)}
            </Form.Field>
            <Form.Field label='Kind'>
              {selectors.specNamesKind(customResourceDefinition)}
            </Form.Field>
          </Form>
        }
      >
        <CustomResourceList
          customResourceDefinition={customResourceDefinition}
          version={applicableVersion}
          customResources={customResourceList}
          deleteResourceCallback={customResource => {
            setCustomResourceList(
              customResourceList.filter(c => c !== customResource)
            );
          }}
          title={applicableVersion}
          titleVariant={Card.titleVariants.small}
          className='mt-2'
        />
      </ResourceDetailPage>
    );
  })
);
