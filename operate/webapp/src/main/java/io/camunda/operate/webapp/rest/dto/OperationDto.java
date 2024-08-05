/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.entities.OperationType;
import java.time.OffsetDateTime;
import java.util.Objects;

public class OperationDto implements CreatableFromEntity<OperationDto, OperationEntity> {

  private String id;

  private String batchOperationId;

  private OperationType type;

  private OperationState state;

  private String errorMessage;

  private OffsetDateTime completedDate;

  public String getId() {
    return id;
  }

  public OperationDto setId(final String id) {
    this.id = id;
    return this;
  }

  public String getBatchOperationId() {
    return batchOperationId;
  }

  public OperationDto setBatchOperationId(final String batchOperationId) {
    this.batchOperationId = batchOperationId;
    return this;
  }

  public OperationType getType() {
    return type;
  }

  public OperationDto setType(final OperationType type) {
    this.type = type;
    return this;
  }

  public OperationState getState() {
    return state;
  }

  public OperationDto setState(final OperationState state) {
    this.state = state;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public OperationDto setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public OffsetDateTime getCompletedDate() {
    return completedDate;
  }

  public OperationDto setCompletedDate(final OffsetDateTime completedDate) {
    this.completedDate = completedDate;
    return this;
  }

  @Override
  public OperationDto fillFrom(final OperationEntity operationEntity) {
    return setId(operationEntity.getId())
        .setType(operationEntity.getType())
        .setState(operationEntity.getState())
        .setErrorMessage(operationEntity.getErrorMessage())
        .setBatchOperationId(operationEntity.getBatchOperationId())
        .setCompletedDate(operationEntity.getCompletedDate());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, batchOperationId, type, state, errorMessage, completedDate);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final OperationDto that = (OperationDto) o;
    return Objects.equals(id, that.id)
        && Objects.equals(batchOperationId, that.batchOperationId)
        && type == that.type
        && state == that.state
        && Objects.equals(errorMessage, that.errorMessage)
        && Objects.equals(completedDate, that.completedDate);
  }

  @Override
  public String toString() {
    return "OperationDto{"
        + "id='"
        + id
        + '\''
        + ", batchOperationId='"
        + batchOperationId
        + '\''
        + ", type="
        + type
        + ", state="
        + state
        + ", errorMessage='"
        + errorMessage
        + '\''
        + ", completedDate="
        + completedDate
        + '}';
  }
}
