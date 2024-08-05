/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.ListenerState;
import io.camunda.operate.entities.ListenerEventType;
import io.camunda.operate.entities.ListenerType;
import java.time.OffsetDateTime;
import java.util.Objects;

public class ListenerDto {
  private ListenerType listenerType;
  private String listenerKey;
  private ListenerState state;
  private String jobType;
  private ListenerEventType event;
  private OffsetDateTime time;

  public ListenerType getListenerType() {
    return listenerType;
  }

  public ListenerDto setListenerType(final ListenerType listenerType) {
    this.listenerType = listenerType;
    return this;
  }

  public String getListenerKey() {
    return listenerKey;
  }

  public ListenerDto setListenerKey(final String listenerKey) {
    this.listenerKey = listenerKey;
    return this;
  }

  public ListenerState getState() {
    return state;
  }

  public ListenerDto setState(final ListenerState state) {
    this.state = state;
    return this;
  }

  public String getJobType() {
    return jobType;
  }

  public ListenerDto setJobType(final String jobType) {
    this.jobType = jobType;
    return this;
  }

  public ListenerEventType getEvent() {
    return event;
  }

  public ListenerDto setEvent(final ListenerEventType event) {
    this.event = event;
    return this;
  }

  public OffsetDateTime getTime() {
    return time;
  }

  public ListenerDto setTime(final OffsetDateTime time) {
    this.time = time;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(listenerType, listenerKey, state, jobType, event, time);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ListenerDto that = (ListenerDto) o;
    return Objects.equals(listenerType, that.listenerType)
        && Objects.equals(listenerKey, that.listenerKey)
        && Objects.equals(state, that.state)
        && Objects.equals(jobType, that.jobType)
        && Objects.equals(event, that.event)
        && Objects.equals(time, that.time);
  }

  @Override
  public String toString() {
    return "ListenerDto{"
        + "listenerType='"
        + listenerType
        + '\''
        + ", listenerKey='"
        + listenerKey
        + '\''
        + ", state='"
        + state
        + '\''
        + ", jobType='"
        + jobType
        + '\''
        + ", event='"
        + event
        + '\''
        + ", time='"
        + time
        + '\''
        + '}';
  }
}
