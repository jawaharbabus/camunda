/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.deployment.model;

import io.camunda.zeebe.el.ExpressionLanguage;
import io.camunda.zeebe.el.ExpressionLanguageFactory;
import io.camunda.zeebe.engine.processing.bpmn.clock.ZeebeFeelEngineClock;
import io.camunda.zeebe.engine.processing.common.ExpressionProcessor;
import io.camunda.zeebe.engine.processing.deployment.model.transformation.BpmnTransformer;
import io.camunda.zeebe.engine.processing.deployment.transform.BpmnValidator;
import io.camunda.zeebe.scheduler.clock.ActorClock;

public final class BpmnFactory {

  public static BpmnTransformer createTransformer() {
    return new BpmnTransformer(
        createExpressionLanguage(new ZeebeFeelEngineClock(ActorClock.current())));
  }

  public static BpmnValidator createValidator(
      final ExpressionProcessor expressionProcessor, final int validatorResultsOutputMaxSize) {
    return new BpmnValidator(
        createExpressionLanguage(new ZeebeFeelEngineClock(ActorClock.current())),
        expressionProcessor,
        validatorResultsOutputMaxSize);
  }

  private static ExpressionLanguage createExpressionLanguage(
      final ZeebeFeelEngineClock zeebeFeelEngineClock) {
    return ExpressionLanguageFactory.createExpressionLanguage(zeebeFeelEngineClock);
  }
}
