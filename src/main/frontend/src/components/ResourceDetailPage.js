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
import YAML from 'yaml';
import metadata from '../metadata';
import Card from './Card';
import DashboardPage from './DashboardPage';
import Icon from './Icon';
import Link from './Link';
import PopupMenu from './PopupMenu';
import {useNavigate} from 'react-router-dom';

const downloadResource = resource => {
  const mimeType = 'text/yaml';
  const blob = new Blob([YAML.stringify(resource)], {type: mimeType});
  const url = URL.createObjectURL(blob);
  const tempLink  = document.createElement('a');
  tempLink.href = url;
  tempLink.download = `${metadata.selectors.name(resource)}.yaml`;
  document.body.appendChild(tempLink);
  tempLink.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));
  document.body.removeChild(tempLink);
  URL.revokeObjectURL(url);
};

const ResourceDetailPage = ({
  kind,
  path,
  resource,
  actions,
  body,
  isReadyFunction,
  deleteFunction,
  title,
  editable = true,
  children
}) => {
  const namespace = metadata.selectors.namespace(resource);
  const navigate = useNavigate();
  const deleteAction = () => {
    deleteFunction(resource);
    navigate(`/${path}`);
  };
  return (
    <DashboardPage
      title={title ?? <DashboardPage.Title
        path={path} kind={kind} namespace={namespace} resource={resource} isReadyFunction={isReadyFunction} />
      }
    >
      <Card>
        <Card.Title className='flex items-center'>
          <div className='flex-1'>
            {namespace && <>{namespace} - </>}
            {metadata.selectors.name(resource)}
          </div>
          {editable && <Link.EditLink path={path} resource={resource} />}
          {actions}
          <PopupMenu>
            <PopupMenu.Item onClick={() => downloadResource(resource)}>
              <Icon icon='fa-save' className='mr-2' /> Download
            </PopupMenu.Item>
            {deleteFunction &&
              <PopupMenu.Item onClick={deleteAction}>
                <Icon stylePrefix='far' icon='fa-trash-alt' className='mr-2' /> Delete
              </PopupMenu.Item>
            }
          </PopupMenu>
        </Card.Title>
        <Card.Body>
          {body}
        </Card.Body>
      </Card>
      {children}
    </DashboardPage>
  );
};

export default ResourceDetailPage;

