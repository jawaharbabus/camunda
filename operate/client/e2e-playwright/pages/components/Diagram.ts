/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {Locator, Page} from '@playwright/test';

export class Diagram {
  private page: Page;
  readonly diagram: Locator;
  readonly popover: Locator;

  constructor(page: Page) {
    this.page = page;
    this.diagram = this.page.getByTestId('diagram');
    this.popover = this.page.getByTestId('popover');
  }

  async moveCanvasHorizontally(dx: number) {
    const boundingBox = await this.page
      .getByTestId('diagram-body')
      .boundingBox();

    if (boundingBox === null) {
      throw new Error(
        'An error occurred when dragging the diagram: diagram bounding box is null',
      );
    }

    const startX = boundingBox.x + boundingBox.width / 2;
    const startY = boundingBox.y + 50;

    // move diagram into viewport to be fully visible
    await this.page.mouse.move(startX, startY);
    await this.page.mouse.down();
    await this.page.mouse.move(startX + dx, startY);
    await this.page.mouse.up();
  }

  clickFlowNode(flowNodeName: string) {
    return this.getFlowNode(flowNodeName).click();
  }

  getFlowNode(flowNodeName: string) {
    return this.diagram
      .locator('.djs-element')
      .filter({hasText: new RegExp(`^${flowNodeName}$`, 'i')});
  }

  getExecutionCount(elementId: string) {
    return this.diagram.evaluate(
      (node, {elementId}) => {
        const completedOverlay: HTMLDivElement | null = node.querySelector(
          `[data-container-id="${elementId}"] [data-testid="state-overlay-completed"]`,
        );

        return completedOverlay?.innerText;
      },
      {elementId},
    );
  }
}
