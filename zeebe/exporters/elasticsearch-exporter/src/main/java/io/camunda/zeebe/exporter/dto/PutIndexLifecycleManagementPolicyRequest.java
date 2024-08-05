/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.exporter.dto;

public record PutIndexLifecycleManagementPolicyRequest(Policy policy) {
  public record Policy(Phases phases) {}

  public record Phases(Delete delete) {}

  public record Delete(String min_age, Actions actions) {}

  public record Actions(DeleteAction delete) {}

  public record DeleteAction() {}
}
