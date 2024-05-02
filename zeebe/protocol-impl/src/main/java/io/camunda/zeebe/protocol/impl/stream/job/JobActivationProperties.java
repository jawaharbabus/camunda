/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.protocol.impl.stream.job;

import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.util.buffer.BufferWriter;
import java.util.Collection;
import org.agrona.DirectBuffer;

/**
 * {@link JobActivationProperties} represents the minimum set of properties required to activate a
 * {@link JobRecordValue} in the engine.
 */
public interface JobActivationProperties extends BufferWriter {

  /**
   * Returns the name of the worker. This is mostly used for debugging purposes.
   *
   * @see JobRecordValue#getWorker()
   */
  DirectBuffer worker();

  /**
   * Returns the variables requested by the worker, or an empty collection if all variables are
   * requested.
   *
   * @see JobRecordValue#getVariables()
   */
  Collection<DirectBuffer> fetchVariables();

  /**
   * Returns the activation timeout of the job, i.e. how long before the job is made activate-able
   * again after activation
   *
   * @see JobRecordValue#getDeadline()
   */
  long timeout();

  /**
   * Returns the identifiers of the tenants that own the jobs requested to be activated by the
   * worker.
   *
   * @return the identifiers of the tenants for which to activate jobs
   */
  Collection<String> tenantIds();
}
