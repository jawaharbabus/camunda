/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ListenerEventType {
  START,
  END,
  UNSPECIFIED;

  private static final Logger LOGGER = LoggerFactory.getLogger(ListenerEventType.class);

  public static ListenerEventType fromZeebeExecutionListenerEventType(
      final String listenerEventType) {
    try {
      return ListenerEventType.valueOf(listenerEventType);
    } catch (final IllegalArgumentException e) {
      LOGGER.error(
          "Unknown listener event type [{}]. Setting it as {}}.", listenerEventType, UNSPECIFIED);
    }
    return UNSPECIFIED;
  }
}
