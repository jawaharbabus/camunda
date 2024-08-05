/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.webapps.zeebe;

import io.camunda.zeebe.broker.Broker;
import io.camunda.zeebe.broker.client.api.BrokerClient;
import io.camunda.zeebe.broker.system.configuration.BrokerCfg;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.gateway.Gateway;
import io.camunda.zeebe.util.Either;

public class PartitionSupplierConfigurer {

  private final Broker broker;

  private final Gateway gateway;
  private final ZeebeClient zeebeClient;

  public PartitionSupplierConfigurer(
      final Broker broker, final Gateway gateway, final ZeebeClient zeebeClient) {
    this.broker = broker;
    this.gateway = gateway;
    this.zeebeClient = zeebeClient;
  }

  public PartitionSupplier createPartitionSupplier() {
    if (broker != null) {
      final var brokerConfiguration = broker.getConfig();
      return new BrokerConfigurationPartitionSupplier(brokerConfiguration);
    } else if (gateway != null) {
      final var brokerClient = gateway.getBrokerClient();
      return new BrokerClientPartitionSupplier(brokerClient);
    } else {
      // default use Standalone Partition Holder by using the Zeebe Client
      return new StandalonePartitionSupplier(zeebeClient);
    }
  }

  private static final class BrokerConfigurationPartitionSupplier implements PartitionSupplier {

    private final BrokerCfg configuration;

    private BrokerConfigurationPartitionSupplier(final BrokerCfg configuration) {
      this.configuration = configuration;
    }

    @Override
    public Either<Exception, Integer> getPartitionsCount() {
      final var clusterConfiguration = configuration.getCluster();
      final var partitionsCount = clusterConfiguration.getPartitionsCount();
      return Either.right(partitionsCount);
    }
  }

  private static final class BrokerClientPartitionSupplier implements PartitionSupplier {

    private final BrokerClient brokerClient;

    private BrokerClientPartitionSupplier(final BrokerClient brokerClient) {
      this.brokerClient = brokerClient;
    }

    @Override
    public Either<Exception, Integer> getPartitionsCount() {
      final var topologyManager = brokerClient.getTopologyManager();
      final var topology = topologyManager.getTopology();
      return Either.right(topology.getPartitionsCount());
    }
  }
}
