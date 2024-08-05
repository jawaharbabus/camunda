/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.service.search.query;

import io.camunda.util.ObjectBuilder;
import java.util.function.Function;

public final class SearchQueryBuilders {

  private SearchQueryBuilders() {}

  public static ProcessInstanceQuery.Builder processInstanceSearchQuery() {
    return new ProcessInstanceQuery.Builder();
  }

  public static ProcessInstanceQuery processInstanceSearchQuery(
      final Function<ProcessInstanceQuery.Builder, ObjectBuilder<ProcessInstanceQuery>> fn) {
    return fn.apply(processInstanceSearchQuery()).build();
  }

  public static UserTaskQuery.Builder userTaskSearchQuery() {
    return new UserTaskQuery.Builder();
  }

  public static UserTaskQuery userTaskSearchQuery(
      final Function<UserTaskQuery.Builder, ObjectBuilder<UserTaskQuery>> fn) {
    return fn.apply(userTaskSearchQuery()).build();
  }

  public static VariableQuery.Builder variableSearchQuery() {
    return new VariableQuery.Builder();
  }

  public static VariableQuery variableSearchQuery(
      final Function<VariableQuery.Builder, ObjectBuilder<VariableQuery>> fn) {
    return fn.apply(variableSearchQuery()).build();
  }

  public static DecisionDefinitionQuery.Builder decisionDefinitionSearchQuery() {
    return new DecisionDefinitionQuery.Builder();
  }

  public static DecisionDefinitionQuery decisionDefinitionSearchQuery(
      final Function<DecisionDefinitionQuery.Builder, ObjectBuilder<DecisionDefinitionQuery>> fn) {
    return fn.apply(decisionDefinitionSearchQuery()).build();
  }
}
