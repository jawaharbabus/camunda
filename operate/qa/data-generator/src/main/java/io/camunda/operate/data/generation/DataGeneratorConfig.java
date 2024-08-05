/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.data.generation;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import java.util.concurrent.ThreadFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class DataGeneratorConfig {

  private static final int JOB_WORKER_MAX_JOBS_ACTIVE = 5;

  @Autowired private DataGeneratorProperties dataGeneratorProperties;

  public ZeebeClient createZeebeClient() {
    final String gatewayAddress = dataGeneratorProperties.getZeebeGatewayAddress();
    final ZeebeClientBuilder builder =
        ZeebeClient.newClientBuilder()
            .gatewayAddress(gatewayAddress)
            .defaultJobWorkerMaxJobsActive(JOB_WORKER_MAX_JOBS_ACTIVE)
            .usePlaintext();
    return builder.build();
  }

  @Bean
  public ZeebeClient getZeebeClient() {
    return createZeebeClient();
  }

  @Bean
  public RestHighLevelClient createRestHighLevelClient() {
    return new RestHighLevelClient(
        RestClient.builder(
            new HttpHost(
                dataGeneratorProperties.getElasticsearchHost(),
                dataGeneratorProperties.getElasticsearchPort(),
                "http")));
  }

  @Bean("dataGeneratorThreadPoolExecutor")
  public ThreadPoolTaskExecutor getDataGeneratorTaskExecutor(
      final DataGeneratorProperties dataGeneratorProperties) {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadFactory(getThreadFactory());
    executor.setCorePoolSize(dataGeneratorProperties.getThreadCount());
    executor.setMaxPoolSize(dataGeneratorProperties.getThreadCount());
    executor.setQueueCapacity(1);
    executor.initialize();
    return executor;
  }

  @Bean
  public ThreadFactory getThreadFactory() {
    return new CustomizableThreadFactory("data_generator_") {
      @Override
      public Thread newThread(final Runnable runnable) {
        final Thread thread =
            new DataGeneratorThread(
                getThreadGroup(), runnable, nextThreadName(), createZeebeClient());
        thread.setPriority(getThreadPriority());
        thread.setDaemon(isDaemon());
        return thread;
      }
    };
  }

  public class DataGeneratorThread extends Thread {

    private final ZeebeClient zeebeClient;

    public DataGeneratorThread(final ZeebeClient zeebeClient) {
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(final Runnable target, final ZeebeClient zeebeClient) {
      super(target);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(
        final ThreadGroup group, final Runnable target, final ZeebeClient zeebeClient) {
      super(group, target);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(final String name, final ZeebeClient zeebeClient) {
      super(name);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(
        final ThreadGroup group, final String name, final ZeebeClient zeebeClient) {
      super(group, name);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(
        final Runnable target, final String name, final ZeebeClient zeebeClient) {
      super(target, name);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(
        final ThreadGroup group,
        final Runnable target,
        final String name,
        final ZeebeClient zeebeClient) {
      super(group, target, name);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(
        final ThreadGroup group,
        final Runnable target,
        final String name,
        final long stackSize,
        final ZeebeClient zeebeClient) {
      super(group, target, name, stackSize);
      this.zeebeClient = zeebeClient;
    }

    public DataGeneratorThread(
        final ThreadGroup group,
        final Runnable target,
        final String name,
        final long stackSize,
        final boolean inheritThreadLocals,
        final ZeebeClient zeebeClient) {
      super(group, target, name, stackSize, inheritThreadLocals);
      this.zeebeClient = zeebeClient;
    }

    public ZeebeClient getZeebeClient() {
      return zeebeClient;
    }
  }
}
