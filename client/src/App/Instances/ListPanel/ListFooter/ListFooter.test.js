/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import {shallow} from 'enzyme';
import {DataManagerProvider} from 'modules/DataManager';

import ListFooter from './ListFooter';
import Paginator from './Paginator';
import AddSelection from './AddSelection';

jest.mock('modules/utils/bpmn');

describe('ListFooter', () => {
  let node;

  beforeEach(() => {
    node = shallow(
      <ListFooter
        onFirstElementChange={jest.fn()}
        onAddNewSelection={jest.fn()}
        onAddToSelectionById={jest.fn()}
        onAddToOpenSelection={jest.fn()}
        perPage={10}
        firstElement={0}
        filterCount={9}
        selection={{ids: [], excludeIds: []}}
        selections={[{selectionId: 0}, {selectionId: 1}]}
        dataManager={{}}
      />
    );
  });

  it('should pagination only if required', () => {
    expect(node.find(Paginator).exists()).toBe(false);
    node.setProps({filterCount: 11});
    expect(node.find(Paginator).exists()).toBe(true);
  });

  it('should render button if no selection exists', () => {
    node.setProps({selections: []});
    expect(node.find(AddSelection).exists()).toBe(true);
  });
});
