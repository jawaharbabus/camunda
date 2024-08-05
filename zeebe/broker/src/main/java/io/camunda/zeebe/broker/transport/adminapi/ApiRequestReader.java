/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.broker.transport.adminapi;

import io.camunda.zeebe.broker.transport.AsyncApiRequestHandler.RequestReader;
import io.camunda.zeebe.protocol.management.AdminRequestDecoder;
import io.camunda.zeebe.protocol.management.MessageHeaderDecoder;
import org.agrona.DirectBuffer;

public class ApiRequestReader implements RequestReader<AdminRequestDecoder> {
  private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
  private final AdminRequestDecoder messageDecoder = new AdminRequestDecoder();

  @Override
  public void reset() {}

  @Override
  public AdminRequestDecoder getMessageDecoder() {
    return messageDecoder;
  }

  @Override
  public void wrap(final DirectBuffer buffer, final int offset, final int length) {
    messageDecoder.wrapAndApplyHeader(buffer, offset, headerDecoder);
  }

  public long key() {
    return messageDecoder.key();
  }

  public String payload() {
    return messageDecoder.payload();
  }
}
