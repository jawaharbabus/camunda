/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.service.util;

import static io.camunda.zeebe.protocol.Protocol.START_PARTITION_ID;

import io.camunda.zeebe.broker.client.api.BrokerClusterState;
import io.camunda.zeebe.broker.client.api.BrokerTopologyListener;
import io.camunda.zeebe.broker.client.api.BrokerTopologyManager;
import io.camunda.zeebe.broker.client.impl.BrokerClusterStateImpl;
import io.camunda.zeebe.dynamic.config.state.ClusterConfiguration;

public final class StubbedTopologyManager implements BrokerTopologyManager {

  private final BrokerClusterStateImpl clusterState;

  StubbedTopologyManager() {
    this(8);
  }

  StubbedTopologyManager(final int partitionsCount) {
    clusterState = new BrokerClusterStateImpl();
    clusterState.addBrokerIfAbsent(0);
    clusterState.setBrokerAddressIfPresent(0, "localhost:26501");
    for (int partitionOffset = 0; partitionOffset < partitionsCount; partitionOffset++) {
      clusterState.setPartitionLeader(START_PARTITION_ID + partitionOffset, 0, 1);
      clusterState.addPartitionIfAbsent(START_PARTITION_ID + partitionOffset);
    }
    clusterState.setPartitionsCount(partitionsCount);
  }

  @Override
  public BrokerClusterState getTopology() {
    return clusterState;
  }

  @Override
  public ClusterConfiguration getClusterConfiguration() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void addTopologyListener(final BrokerTopologyListener listener) {
    throw new UnsupportedOperationException("Not yet implemented; implement if need be");
  }

  @Override
  public void removeTopologyListener(final BrokerTopologyListener listener) {
    throw new UnsupportedOperationException("Not yet implemented; implement if need be");
  }

  @Override
  public void onClusterConfigurationUpdated(final ClusterConfiguration clusterConfiguration) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
