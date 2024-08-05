/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.scheduler.lifecycle;

import static org.assertj.core.util.Lists.newArrayList;

import io.camunda.zeebe.scheduler.Actor;
import io.camunda.zeebe.scheduler.ActorControl;
import io.camunda.zeebe.scheduler.ActorTask.ActorLifecyclePhase;
import io.camunda.zeebe.scheduler.future.ActorFuture;
import io.camunda.zeebe.scheduler.future.CompletableActorFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class LifecycleRecordingActor extends Actor {
  public static final List<ActorLifecyclePhase> FULL_LIFECYCLE =
      newArrayList(
          ActorLifecyclePhase.STARTING,
          ActorLifecyclePhase.STARTED,
          ActorLifecyclePhase.CLOSE_REQUESTED,
          ActorLifecyclePhase.CLOSING,
          ActorLifecyclePhase.CLOSED);
  private static final BiConsumer<Void, Throwable> NOOP_CONSUMER = (ok, err) -> {};

  public final List<ActorLifecyclePhase> phases = new ArrayList<>();

  @Override
  public void onActorStarting() {
    phases.add(actor.getLifecyclePhase());
  }

  @Override
  public void onActorStarted() {
    phases.add(actor.getLifecyclePhase());
  }

  @Override
  public void onActorClosing() {
    phases.add(actor.getLifecyclePhase());
  }

  @Override
  public void onActorClosed() {
    phases.add(actor.getLifecyclePhase());
  }

  @Override
  public void onActorCloseRequested() {
    phases.add(actor.getLifecyclePhase());
  }

  @Override
  public void onActorFailed() {
    phases.add(actor.getLifecyclePhase());
  }

  protected void blockPhase() {
    blockPhase(new CompletableActorFuture<>(), NOOP_CONSUMER);
  }

  protected void blockPhase(final ActorFuture<Void> future) {
    blockPhase(future, NOOP_CONSUMER);
  }

  @SuppressWarnings("unchecked")
  protected void blockPhase(
      final ActorFuture<Void> future, final BiConsumer<Void, Throwable> consumer) {
    actor.runOnCompletionBlockingCurrentPhase(future, consumer);
  }

  @SuppressWarnings("unchecked")
  protected void runOnCompletion() {
    actor.runOnCompletion(new CompletableActorFuture<>(), NOOP_CONSUMER);
  }

  @SuppressWarnings("unchecked")
  protected void runOnCompletion(final ActorFuture<Void> future) {
    actor.runOnCompletion(future, NOOP_CONSUMER);
  }

  public ActorControl control() {
    return actor;
  }
}
