/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.webapp.zeebe.operation;

import static io.camunda.operate.entities.OperationType.ADD_VARIABLE;
import static io.camunda.operate.entities.OperationType.UPDATE_VARIABLE;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.zeebe.client.api.response.SetVariablesResponse;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Update the variable. */
@Component
public class UpdateVariableHandler extends AbstractOperationHandler implements OperationHandler {

  @Override
  public void handleWithException(final OperationEntity operation) throws Exception {
    final String updateVariableJson =
        mergeVariableJson(operation.getVariableName(), operation.getVariableValue());
    final SetVariablesResponse response =
        zeebeClient
            .newSetVariablesCommand(operation.getScopeKey())
            .variables(updateVariableJson)
            .local(true)
            .send()
            .join();
    markAsSent(operation, response.getKey());
  }

  @Override
  public Set<OperationType> getTypes() {
    return Set.of(UPDATE_VARIABLE, ADD_VARIABLE);
  }

  private String mergeVariableJson(final String variableName, final String variableValue) {
    return String.format("{\"%s\":%s}", variableName, variableValue);
  }
}
