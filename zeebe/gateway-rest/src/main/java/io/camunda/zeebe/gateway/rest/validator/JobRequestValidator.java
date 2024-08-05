/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest.validator;

import static io.camunda.zeebe.gateway.rest.validator.ErrorMessages.ERROR_MESSAGE_EMPTY_ATTRIBUTE;
import static io.camunda.zeebe.gateway.rest.validator.ErrorMessages.ERROR_MESSAGE_INVALID_ATTRIBUTE_VALUE;
import static io.camunda.zeebe.gateway.rest.validator.RequestValidator.createProblemDetail;

import io.camunda.zeebe.gateway.protocol.rest.JobActivationRequest;
import io.camunda.zeebe.gateway.protocol.rest.JobErrorRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ProblemDetail;

public final class JobRequestValidator {

  public static Optional<ProblemDetail> validateJobActivationRequest(
      final JobActivationRequest activationRequest) {
    final List<String> violations = new ArrayList<>();
    if (activationRequest.getType() == null || activationRequest.getType().isBlank()) {
      violations.add(ERROR_MESSAGE_EMPTY_ATTRIBUTE.formatted("type"));
    }
    if (activationRequest.getTimeout() == null) {
      violations.add(ERROR_MESSAGE_EMPTY_ATTRIBUTE.formatted("timeout"));
    } else if (activationRequest.getTimeout() < 1) {
      violations.add(
          ERROR_MESSAGE_INVALID_ATTRIBUTE_VALUE.formatted(
              "timeout", activationRequest.getTimeout(), "greater than 0"));
    }
    if (activationRequest.getMaxJobsToActivate() == null) {
      violations.add(ERROR_MESSAGE_EMPTY_ATTRIBUTE.formatted("maxJobsToActivate"));
    } else if (activationRequest.getMaxJobsToActivate() < 1) {
      violations.add(
          ERROR_MESSAGE_INVALID_ATTRIBUTE_VALUE.formatted(
              "maxJobsToActivate", activationRequest.getTimeout(), "greater than 0"));
    }
    return createProblemDetail(violations);
  }

  public static Optional<ProblemDetail> validateJobErrorRequest(
      final JobErrorRequest errorRequest) {
    final List<String> violations = new ArrayList<>();
    // errorCode can't be null or empty
    if (errorRequest.getErrorCode() == null || errorRequest.getErrorCode().isBlank()) {
      violations.add(ERROR_MESSAGE_EMPTY_ATTRIBUTE.formatted("errorCode"));
    }
    return createProblemDetail(violations);
  }
}
