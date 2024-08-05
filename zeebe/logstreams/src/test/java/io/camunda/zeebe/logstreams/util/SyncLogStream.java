/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.logstreams.util;

import io.camunda.zeebe.logstreams.impl.flowcontrol.FlowControl;
import io.camunda.zeebe.logstreams.log.LogAppendEntry;
import io.camunda.zeebe.logstreams.log.LogRecordAwaiter;
import io.camunda.zeebe.logstreams.log.LogStream;
import io.camunda.zeebe.logstreams.log.LogStreamBuilder;
import io.camunda.zeebe.logstreams.log.LogStreamReader;
import io.camunda.zeebe.logstreams.log.LogStreamWriter;
import io.camunda.zeebe.logstreams.log.LogStreamWriter.WriteFailure;
import io.camunda.zeebe.logstreams.log.WriteContext;
import io.camunda.zeebe.util.Either;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.awaitility.Awaitility;

public class SyncLogStream implements SynchronousLogStream {

  private final LogStream logStream;
  private long lastWrittenPosition = -1;

  public SyncLogStream(final LogStream logStream) {
    this.logStream = logStream;
  }

  public static SyncLogStreamBuilder builder() {
    return new SyncLogStreamBuilder();
  }

  public static SyncLogStreamBuilder builder(final LogStreamBuilder builder) {
    return new SyncLogStreamBuilder(builder);
  }

  @Override
  public LogStream getAsyncLogStream() {
    return logStream;
  }

  @Override
  public long getLastWrittenPosition() {
    return lastWrittenPosition;
  }

  @Override
  public void setLastWrittenPosition(final long position) {
    lastWrittenPosition = position;
  }

  @Override
  public SynchronousLogStreamWriter newSyncLogStreamWriter() {
    return new Writer(newLogStreamWriter());
  }

  @Override
  public void awaitPositionWritten(final long position) {
    Awaitility.await("until position " + position + " is written")
        .atMost(Duration.ofSeconds(5))
        .pollDelay(Duration.ZERO)
        .pollInterval(Duration.ofMillis(50))
        .pollInSameThread()
        .until(this::getLastWrittenPosition, p -> p >= position);
  }

  @Override
  public void close() {
    logStream.close();
  }

  @Override
  public int getPartitionId() {
    return logStream.getPartitionId();
  }

  @Override
  public String getLogName() {
    return logStream.getLogName();
  }

  @Override
  public LogStreamReader newLogStreamReader() {
    return logStream.newLogStreamReader();
  }

  @Override
  public LogStreamWriter newLogStreamWriter() {
    return logStream.newLogStreamWriter();
  }

  @Override
  public FlowControl getFlowControl() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void registerRecordAvailableListener(final LogRecordAwaiter recordAwaiter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeRecordAvailableListener(final LogRecordAwaiter recordAwaiter) {
    throw new UnsupportedOperationException();
  }

  private Either<WriteFailure, Long> syncTryWrite(
      final Supplier<Either<WriteFailure, Long>> writeOperation) {
    final var written =
        Awaitility.await("until dispatcher accepts writer")
            .pollDelay(Duration.ZERO)
            .pollInterval(Duration.ofMillis(50))
            .pollInSameThread()
            .until(writeOperation::get, Either::isRight);

    final var position = written.get();
    // 0 is a special position which is returned when a 'write' is "skipped"
    if (position > 0) {
      awaitPositionWritten(position);
    }

    return written;
  }

  private final class Writer implements SynchronousLogStreamWriter {
    private final LogStreamWriter delegate;

    private Writer(final LogStreamWriter delegate) {
      this.delegate = delegate;
    }

    @Override
    public Either<WriteFailure, Long> tryWrite(
        final WriteContext context,
        final List<LogAppendEntry> appendEntries,
        final long sourcePosition) {
      return syncTryWrite(() -> delegate.tryWrite(context, appendEntries, sourcePosition));
    }
  }
}
