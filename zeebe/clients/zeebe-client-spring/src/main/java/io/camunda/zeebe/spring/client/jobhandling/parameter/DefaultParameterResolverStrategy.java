/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.spring.client.jobhandling.parameter;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.CustomHeaders;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import io.camunda.zeebe.spring.client.bean.ParameterInfo;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultParameterResolverStrategy implements ParameterResolverStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultParameterResolverStrategy.class);
  protected final JsonMapper jsonMapper;

  public DefaultParameterResolverStrategy(final JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public ParameterResolver createResolver(final ParameterInfo parameterInfo) {
    final Class<?> parameterType = parameterInfo.getParameterInfo().getType();
    if (JobClient.class.isAssignableFrom(parameterType)) {
      return new JobClientParameterResolver();
    } else if (ActivatedJob.class.isAssignableFrom(parameterType)) {
      return new ActivatedJobParameterResolver();
    } else if (isVariable(parameterInfo)) {
      final String variableName = getVariableName(parameterInfo);
      return new VariableResolver(variableName, parameterType, jsonMapper);
    } else if (isVariablesAsType(parameterInfo)) {
      return new VariablesAsTypeResolver(parameterType);
    } else if (isCustomHeaders(parameterInfo)) {
      return new CustomHeadersResolver();
    }
    throw new IllegalStateException(
        "Could not create parameter resolver for parameter " + parameterInfo);
  }

  protected boolean isVariable(final ParameterInfo parameterInfo) {
    return parameterInfo.getParameterInfo().isAnnotationPresent(Variable.class);
  }

  protected boolean isVariablesAsType(final ParameterInfo parameterInfo) {
    return parameterInfo.getParameterInfo().isAnnotationPresent(VariablesAsType.class);
  }

  protected boolean isCustomHeaders(final ParameterInfo parameterInfo) {
    return parameterInfo.getParameterInfo().isAnnotationPresent(CustomHeaders.class);
  }

  protected String getVariableName(final ParameterInfo param) {
    if (param.getParameterInfo().isAnnotationPresent(Variable.class)) {
      final String nameFromAnnotation =
          param.getParameterInfo().getAnnotation(Variable.class).name();
      if (!Objects.equals(nameFromAnnotation, Variable.DEFAULT_NAME)) {
        LOG.trace("Extracting name {} from Variable.name", nameFromAnnotation);
        return nameFromAnnotation;
      }
      final String valueFromAnnotation =
          param.getParameterInfo().getAnnotation(Variable.class).value();
      if (!Objects.equals(valueFromAnnotation, Variable.DEFAULT_NAME)) {
        LOG.trace("Extracting name {} from Variable.value", valueFromAnnotation);
        return valueFromAnnotation;
      }
    }
    LOG.trace("Extracting variable name from parameter name");
    return param.getParameterName();
  }
}
