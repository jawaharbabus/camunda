/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.entities;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class OperationEntity extends OperateEntity<OperationEntity> {

  private Long processInstanceKey;

  /** Attention! This field will be filled in only for data imported after v. 8.2.0. */
  private Long processDefinitionKey;

  /** Attention! This field will be filled in only for data imported after v. 8.2.0. */
  private String bpmnProcessId;

  /** Attention! This field will be filled in only for data imported after v. 8.3.0. */
  private Long decisionDefinitionKey;

  private Long incidentKey;
  private Long scopeKey;
  private String variableName;
  private String variableValue;
  private OperationType type;
  private OffsetDateTime lockExpirationTime;
  private String lockOwner;
  private OperationState state;
  private String errorMessage;
  private String batchOperationId;
  private Long zeebeCommandKey;
  private String username;
  private String modifyInstructions;
  private String migrationPlan;

  private OffsetDateTime completedDate;

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public OperationEntity setProcessInstanceKey(final Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
    return this;
  }

  public Long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public OperationEntity setProcessDefinitionKey(final Long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public OperationEntity setBpmnProcessId(final String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
    return this;
  }

  public Long getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public void setDecisionDefinitionKey(final Long decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
  }

  public Long getIncidentKey() {
    return incidentKey;
  }

  public OperationEntity setIncidentKey(final Long incidentKey) {
    this.incidentKey = incidentKey;
    return this;
  }

  public Long getScopeKey() {
    return scopeKey;
  }

  public OperationEntity setScopeKey(final Long scopeKey) {
    this.scopeKey = scopeKey;
    return this;
  }

  public String getVariableName() {
    return variableName;
  }

  public OperationEntity setVariableName(final String variableName) {
    this.variableName = variableName;
    return this;
  }

  public String getVariableValue() {
    return variableValue;
  }

  public OperationEntity setVariableValue(final String variableValue) {
    this.variableValue = variableValue;
    return this;
  }

  public OperationType getType() {
    return type;
  }

  public OperationEntity setType(final OperationType type) {
    this.type = type;
    return this;
  }

  public Long getZeebeCommandKey() {
    return zeebeCommandKey;
  }

  public OperationEntity setZeebeCommandKey(final Long zeebeCommandKey) {
    this.zeebeCommandKey = zeebeCommandKey;
    return this;
  }

  public OperationState getState() {
    return state;
  }

  public OperationEntity setState(final OperationState state) {
    this.state = state;
    return this;
  }

  public OffsetDateTime getLockExpirationTime() {
    return lockExpirationTime;
  }

  public OperationEntity setLockExpirationTime(final OffsetDateTime lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
    return this;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public OperationEntity setLockOwner(final String lockOwner) {
    this.lockOwner = lockOwner;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public OperationEntity setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public String getBatchOperationId() {
    return batchOperationId;
  }

  public OperationEntity setBatchOperationId(final String batchOperationId) {
    this.batchOperationId = batchOperationId;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public OperationEntity setUsername(final String username) {
    this.username = username;
    return this;
  }

  public String getModifyInstructions() {
    return modifyInstructions;
  }

  public OperationEntity setModifyInstructions(final String modifyInstructions) {
    this.modifyInstructions = modifyInstructions;
    return this;
  }

  public String getMigrationPlan() {
    return migrationPlan;
  }

  public OperationEntity setMigrationPlan(final String migrationPlan) {
    this.migrationPlan = migrationPlan;
    return this;
  }

  public OffsetDateTime getCompletedDate() {
    return completedDate;
  }

  public OperationEntity setCompletedDate(final OffsetDateTime completedDate) {
    this.completedDate = completedDate;
    return this;
  }

  public void generateId() {
    setId(UUID.randomUUID().toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        processInstanceKey,
        processDefinitionKey,
        bpmnProcessId,
        decisionDefinitionKey,
        incidentKey,
        scopeKey,
        variableName,
        variableValue,
        type,
        lockExpirationTime,
        lockOwner,
        state,
        errorMessage,
        batchOperationId,
        zeebeCommandKey,
        username,
        modifyInstructions,
        migrationPlan);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final OperationEntity that = (OperationEntity) o;
    return Objects.equals(processInstanceKey, that.processInstanceKey)
        && Objects.equals(processDefinitionKey, that.processDefinitionKey)
        && Objects.equals(bpmnProcessId, that.bpmnProcessId)
        && Objects.equals(decisionDefinitionKey, that.decisionDefinitionKey)
        && Objects.equals(incidentKey, that.incidentKey)
        && Objects.equals(scopeKey, that.scopeKey)
        && Objects.equals(variableName, that.variableName)
        && Objects.equals(variableValue, that.variableValue)
        && type == that.type
        && Objects.equals(lockExpirationTime, that.lockExpirationTime)
        && Objects.equals(lockOwner, that.lockOwner)
        && state == that.state
        && Objects.equals(errorMessage, that.errorMessage)
        && Objects.equals(batchOperationId, that.batchOperationId)
        && Objects.equals(zeebeCommandKey, that.zeebeCommandKey)
        && Objects.equals(username, that.username)
        && Objects.equals(modifyInstructions, that.modifyInstructions)
        && Objects.equals(migrationPlan, that.migrationPlan);
  }

  @Override
  public String toString() {
    return "OperationEntity{"
        + "processInstanceKey="
        + processInstanceKey
        + ", processDefinitionKey="
        + processDefinitionKey
        + ", bpmnProcessId='"
        + bpmnProcessId
        + '\''
        + ", decisionDefinitionKey="
        + decisionDefinitionKey
        + ", incidentKey="
        + incidentKey
        + ", scopeKey="
        + scopeKey
        + ", variableName='"
        + variableName
        + '\''
        + ", variableValue='"
        + variableValue
        + '\''
        + ", type="
        + type
        + ", lockExpirationTime="
        + lockExpirationTime
        + ", lockOwner='"
        + lockOwner
        + '\''
        + ", state="
        + state
        + ", errorMessage='"
        + errorMessage
        + '\''
        + ", batchOperationId='"
        + batchOperationId
        + '\''
        + ", zeebeCommandKey="
        + zeebeCommandKey
        + ", username='"
        + username
        + '\''
        + ", modifyInstructions='"
        + modifyInstructions
        + '\''
        + ", migrationPlan='"
        + migrationPlan
        + '\''
        + '}';
  }
}
