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
import React, {useState} from 'react';
import {useStore} from 'react-redux';
import cloneDeep from 'lodash/cloneDeep';
import YAML from 'yaml';
import editor from './editor';
import md from '../metadata';
import DashboardPage from '../components/DashboardPage';
import Card from '../components/Card';
import Link from './Link';
import Icon from './Icon';
import Alert from './Alert';
import useEditor from "./editor/useEditor";

const ResourceEditPage = ({
  kind,
  path,
  dashboardPageTitle,
  cardTitle = () => 'Edit - Resource',
  save,
  resourceFromState
}) => {
  const [currentAttempt, setForceReload] = useState(1);
  const store = useStore();
  const [resource, setResource] = useState();
  const {resourceYaml, setResourceYaml, error, setError, save: handleSave} = useEditor(save);
  if (resource === undefined) {
    const stateResource = cloneDeep(resourceFromState(store.getState()));
    if (stateResource !== undefined) {
      setResource(stateResource);
      setResourceYaml(YAML.stringify(stateResource));
    } else {
      setTimeout(() => setForceReload(currentAttempt + 1), 100);
    }
  }
  const namespace = md.selectors.namespace(resource);
  const name = md.selectors.name(resource);
  return (
    <DashboardPage
      title={dashboardPageTitle ? dashboardPageTitle(resource) :
        <DashboardPage.Title path={path} kind={kind} namespace={namespace} name={name}>
          &nbsp;- Edit
        </DashboardPage.Title>
      }
    >
      <div className='absolute inset-0 md:p-4 flex flex-col'>
        <Card className='flex-1 flex flex-col'>
          <Card.Title className='flex items-center'>
            <div className='flex-1'>{cardTitle(resource)}</div>
            <div>
              <Link
                size={Link.sizes.small}
                variant={Link.variants.outline}
                title='Save'
                onClick={handleSave}
              >
                <Icon stylePrefix='far' icon='fa-save' className='mr-2'/>Save
              </Link>
            </div>
          </Card.Title>
          <Card.Body
            className='relative flex-1 flex flex-col bg-black text-white font-mono text-sm'
            padding='p-0'
          >
            <div className='flex-1'>
              <editor.YamlEditor value={resourceYaml} onChange={value => setResourceYaml(value)} />
            </div>
            <Alert
              className='absolute left-0 right-0 z-10'
              visible={error != null} margin='m-2'
              clearError={() => setError(null)}>
              {error}
            </Alert>
          </Card.Body>
        </Card>
      </div>
    </DashboardPage>
  );
};

export default ResourceEditPage;