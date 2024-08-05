/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.util.j5templates;

import io.camunda.operate.conditions.ElasticsearchCondition;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.qa.util.TestContainerUtil;
import io.camunda.operate.util.TestUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(ElasticsearchCondition.class)
@Component
public class ElasticsearchZeebeManager extends ZeebeContainerManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchZeebeManager.class);

  private final RestHighLevelClient zeebeEsClient;

  public ElasticsearchZeebeManager(
      OperateProperties operateProperties,
      TestContainerUtil testContainerUtil,
      @Qualifier("zeebeEsClient") RestHighLevelClient zeebeEsClient) {
    super(operateProperties, testContainerUtil);
    this.zeebeEsClient = zeebeEsClient;
  }

  @Override
  protected void updatePrefix() {
    LOGGER.info("Starting Zeebe with ELS prefix: " + prefix);
    operateProperties.getZeebeElasticsearch().setPrefix(prefix);
  }

  @Override
  protected void removeIndices() {
    TestUtil.removeAllIndices(zeebeEsClient, prefix);
  }
}
