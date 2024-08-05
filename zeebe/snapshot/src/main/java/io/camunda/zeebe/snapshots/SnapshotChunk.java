/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.snapshots;

/** A chunk of an already persisted Snapshot. */
public interface SnapshotChunk {

  /**
   * @return a unique snapshot identifier *
   */
  String getSnapshotId();

  /**
   * @return the total count of snapshot chunks, which correspond to the same snapshot
   */
  int getTotalCount();

  /**
   * @return the name of the current chunk (e.g. file name)
   */
  String getChunkName();

  /**
   * @return the checksum of the content, can be use to verify the integrity of the content
   */
  long getChecksum();

  /**
   * @return the content of the current chunk
   */
  byte[] getContent();

  /**
   * @return the checksum of the entire snapshot
   */
  long getSnapshotChecksum();

  /**
   * @return the index of the part of the chunk contents.
   */
  long getFileBlockPosition();

  long getTotalFileSize();
}
