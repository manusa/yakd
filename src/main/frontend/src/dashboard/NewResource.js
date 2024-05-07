/*
 * Copyright 2023 Marc Nuri
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
import React, {useState, useEffect} from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {uiSetCreatingNewResource} from '../redux';
import {createResource} from '../fetch';
import {ResourceEditModal} from '../editor';

const mapStateToProps = ({ui: {creatingNewResource}}) => ({
  creatingNewResource
});

const mapDispatchToProps = dispatch =>
  bindActionCreators(
    {closeModal: () => uiSetCreatingNewResource(false)},
    dispatch
  );

export const NewResource = connect(
  mapStateToProps,
  mapDispatchToProps
)(({creatingNewResource, closeModal}) => {
  const [newResource, setNewResource] = useState(null);
  useEffect(() => {
    if (!creatingNewResource) {
      setNewResource(null);
    } else if (creatingNewResource && newResource === null) {
      setNewResource('');
    }
  }, [newResource, creatingNewResource]);
  const save = async resource => {
    await createResource(resource);
    closeModal();
  };
  return (
    <>
      <ResourceEditModal
        resource={newResource}
        save={save}
        close={closeModal}
        title='Create new resource'
        preserveSideBar={false}
      />
    </>
  );
});
