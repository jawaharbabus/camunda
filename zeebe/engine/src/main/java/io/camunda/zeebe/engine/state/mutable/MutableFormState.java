/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.mutable;

import io.camunda.zeebe.engine.state.immutable.FormState;
import io.camunda.zeebe.protocol.impl.record.value.deployment.FormRecord;

public interface MutableFormState extends FormState {

  /**
   * Put the given form in FORMS column family
   *
   * @param record the record of the form
   */
  void storeFormInFormColumnFamily(FormRecord record);

  /**
   * Put the given form in FORM_BY_ID_AND_VERSION column family
   *
   * @param record the record of the form
   */
  void storeFormInFormByIdAndVersionColumnFamily(FormRecord record);

  /**
   * Put the given form in FORM_KEY_BY_FORM_ID_AND_DEPLOYMENT_KEY column family
   *
   * @param record the record of the form
   */
  void storeFormInFormKeyByFormIdAndDeploymentKeyColumnFamily(FormRecord record);

  /**
   * Update the latest version of the form if it is newer.
   *
   * @param record the record of the form
   */
  void updateLatestVersion(FormRecord record);

  /**
   * Deletes a form from FORMS column family
   *
   * @param record the record of the form that is deleted
   */
  void deleteFormInFormsColumnFamily(FormRecord record);

  /**
   * Deletes a form from FORM_BY_ID_AND_VERSION column family
   *
   * @param record the record of the form that is deleted
   */
  void deleteFormInFormByIdAndVersionColumnFamily(FormRecord record);

  /**
   * Deletes a form from FORM_VERSION column family
   *
   * @param record the record of the form that is deleted
   */
  void deleteFormInFormVersionColumnFamily(FormRecord record);

  /**
   * Deletes a form from FORM_KEY_BY_FORM_ID_AND_DEPLOYMENT_KEY column family
   *
   * @param record the record of the form that is deleted
   */
  void deleteFormInFormKeyByFormIdAndDeploymentKeyColumnFamily(FormRecord record);
}
