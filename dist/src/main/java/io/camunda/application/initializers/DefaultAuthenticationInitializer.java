/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.application.initializers;

import static io.camunda.application.Profile.DEFAULT_AUTH_PROFILE;
import static io.camunda.application.Profile.getAuthProfiles;
import static io.camunda.application.Profile.getWebappProfiles;

import io.camunda.application.Profile;
import java.util.Set;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/** Adds the "auth" if none of the {@link Profile#getAuthProfiles()} is set as an active profile. */
public class DefaultAuthenticationInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(final ConfigurableApplicationContext context) {
    final var env = context.getEnvironment();
    final var activeProfiles = Set.of(env.getActiveProfiles());
    if (shouldApplyDefaultAuthenticationProfile(activeProfiles)) {
      env.addActiveProfile(DEFAULT_AUTH_PROFILE.getId());
    }
  }

  protected boolean shouldApplyDefaultAuthenticationProfile(final Set<String> activeProfiles) {
    return webappProfileIsPresent(activeProfiles) && !authProfileIsPresent(activeProfiles);
  }

  private boolean webappProfileIsPresent(final Set<String> activeProfiles) {
    return getWebappProfiles().stream()
        .anyMatch(profile -> activeProfiles.contains(profile.getId()));
  }

  private boolean authProfileIsPresent(final Set<String> activeProfiles) {
    return getAuthProfiles().stream().anyMatch(profile -> activeProfiles.contains(profile.getId()));
  }
}
