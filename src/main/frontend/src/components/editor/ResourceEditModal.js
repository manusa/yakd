/*
 * Copyright 2020 Marc Nuri
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import React from 'react';
import YAML from 'yaml';
import metadata from '../../metadata';
import editor from './index';
import Modal from '../Modal';
import Card from '../Card';
import Link from '../Link';
import Icon from '../Icon';
import Alert from "../Alert";
import useEditor from "./useEditor";

const ToolbarButton = ({title, onClick, children, ...props}) => (
    <Link
      size={Link.sizes.small}
      variant={Link.variants.outline}
      title={title}
      onClick={onClick}
      {...props}
    >
      {children}
    </Link>
)

const ResourceEditModal = ({
  resource,
  save,
  close,
  title = metadata.selectors.name(resource)
}) => {
  const {resourceYaml, setResourceYaml, error, setError, save: handleSave} = useEditor(save);
  if (resourceYaml === null && resource) {
    setError(null);
    setResourceYaml(YAML.stringify(resource));
  }
  const handleClose = () => {
    setError(null);
    setResourceYaml(null);
    close();
  }
  return (
    <Modal visible={resource !== null}>
      <div className='h-screen max-h-screen flex flex-col'>
        <Card className='flex-1 flex flex-col md:m-4'>
          <Card.Title className='flex items-center'>
            <div className='flex-1'>
              {title}
            </div>
            <div>
              <ToolbarButton title='Save' onClick={handleSave} className='mr-2'>
                <Icon stylePrefix='far' icon='fa-save' className='mr-2'/>Save
              </ToolbarButton>
              <ToolbarButton title='Cancel' onClick={handleClose}>
                <Icon stylePrefix='far' icon='fa-window-close' className='mr-2'/>Cancel
              </ToolbarButton>
            </div>
          </Card.Title>
          <Card.Body className='flex-1 relative' padding='p-0'>
            <Alert
              className='absolute left-0 right-0 z-10'
              visible={error != null} margin='m-2'
              clearError={() => setError(null)}>
              {error}
            </Alert>
            <editor.YamlEditor value={resourceYaml} onChange={value => setResourceYaml(value)} />
          </Card.Body>
        </Card>
      </div>
    </Modal>
  );
};

export default ResourceEditModal;