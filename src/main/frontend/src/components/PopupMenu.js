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
import usePopup from './popup/usePopup';
import Icon from './Icon';

const PopupPanel = ({visible, onClick, children}) => (
  <div
    onClick={onClick}
    className={`${visible ? '' : 'hidden'}
      z-20 origin-top-right absolute right-0 mt-2 rounded-md shadow-lg`}>
    <div className='rounded-md bg-white shadow-xs overflow-y-auto'
         style={{maxHeight: '15rem'}}
    >
      <div className='py-1' role='menu'
           aria-orientation='vertical' aria-labelledby='options-menu'
      >
        {children}
      </div>
    </div>
  </div>
);

const PopupMenu = ({
  textColor = 'text-gray-700',
  textColorActive = 'text-gray-800',
  children
}) => {
  const {popupRef, panelVisible, setPanelVisible} = usePopup();
  const togglePanel = () => setPanelVisible(!panelVisible);
  return (
    <div
      ref={popupRef} className='relative inline-block text-left'
    >
      <button
        type='button' aria-haspopup='true' aria-expanded={panelVisible} onClick={togglePanel}
        className={`
          ml-1 border-0 outline-none focus:outline-none text-center
          ${textColor} hover:text-gray-500 active:${textColorActive} `}
      >
        <Icon icon='fa-ellipsis-v' className='px-3'/>
      </button>
      <PopupPanel
        visible={panelVisible}
      >
        {children}
      </PopupPanel>
    </div>
  );
};

PopupMenu.Item = ({
  children, ...props
}) => (
  <a
    className='flex items-center px-4 py-2 text-sm leading-5 text-gray-700 cursor-pointer select-none hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900'
    {...props}
  >
    {children}
  </a>
);

export default PopupMenu;