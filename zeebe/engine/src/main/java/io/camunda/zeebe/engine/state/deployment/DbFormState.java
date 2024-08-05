/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.deployment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.camunda.zeebe.db.ColumnFamily;
import io.camunda.zeebe.db.TransactionContext;
import io.camunda.zeebe.db.ZeebeDb;
import io.camunda.zeebe.db.impl.DbCompositeKey;
import io.camunda.zeebe.db.impl.DbForeignKey;
import io.camunda.zeebe.db.impl.DbLong;
import io.camunda.zeebe.db.impl.DbString;
import io.camunda.zeebe.db.impl.DbTenantAwareKey;
import io.camunda.zeebe.db.impl.DbTenantAwareKey.PlacementType;
import io.camunda.zeebe.engine.EngineConfiguration;
import io.camunda.zeebe.engine.state.mutable.MutableFormState;
import io.camunda.zeebe.protocol.ZbColumnFamilies;
import io.camunda.zeebe.protocol.impl.record.value.deployment.FormRecord;
import java.util.Optional;
import org.agrona.DirectBuffer;

public class DbFormState implements MutableFormState {

  private static final int DEFAULT_VERSION_VALUE = 0;

  private final DbString tenantIdKey;
  private final DbLong dbFormKey;
  private final DbTenantAwareKey<DbLong> tenantAwareFormKey;
  private final DbForeignKey<DbTenantAwareKey<DbLong>> fkFormKey;
  private final PersistedForm dbPersistedForm;
  private final ColumnFamily<DbTenantAwareKey<DbLong>, PersistedForm> formsByKey;
  private final DbString dbFormId;
  private final VersionManager versionManager;

  private final DbLong formVersion;
  private final DbCompositeKey<DbString, DbLong> idAndVersionKey;
  private final DbTenantAwareKey<DbCompositeKey<DbString, DbLong>> tenantAwareIdAndVersionKey;
  private final ColumnFamily<DbTenantAwareKey<DbCompositeKey<DbString, DbLong>>, PersistedForm>
      formByIdAndVersionColumnFamily;

  private final DbLong dbDeploymentKey;
  private final DbTenantAwareKey<DbCompositeKey<DbString, DbLong>>
      tenantAwareFormIdAndDeploymentKey;

  /**
   * <b>Note</b>: Will only be filled with entries deployed from 8.6 onwards; previously deployed
   * forms will not have an entry in this column family.
   */
  private final ColumnFamily<
          DbTenantAwareKey<DbCompositeKey<DbString, DbLong>>,
          DbForeignKey<DbTenantAwareKey<DbLong>>>
      formKeyByFormIdAndDeploymentKeyColumnFamily;

  private final Cache<TenantIdAndFormId, PersistedForm> formsByTenantIdAndIdCache;

  public DbFormState(
      final ZeebeDb<ZbColumnFamilies> zeebeDb,
      final TransactionContext transactionContext,
      final EngineConfiguration config) {
    tenantIdKey = new DbString();
    dbFormKey = new DbLong();
    tenantAwareFormKey = new DbTenantAwareKey<>(tenantIdKey, dbFormKey, PlacementType.PREFIX);
    fkFormKey = new DbForeignKey<>(tenantAwareFormKey, ZbColumnFamilies.FORMS);
    dbPersistedForm = new PersistedForm();
    formsByKey =
        zeebeDb.createColumnFamily(
            ZbColumnFamilies.FORMS, transactionContext, tenantAwareFormKey, dbPersistedForm);

    dbFormId = new DbString();
    formVersion = new DbLong();
    idAndVersionKey = new DbCompositeKey<>(dbFormId, formVersion);
    tenantAwareIdAndVersionKey =
        new DbTenantAwareKey<>(tenantIdKey, idAndVersionKey, PlacementType.PREFIX);
    formByIdAndVersionColumnFamily =
        zeebeDb.createColumnFamily(
            ZbColumnFamilies.FORM_BY_ID_AND_VERSION,
            transactionContext,
            tenantAwareIdAndVersionKey,
            dbPersistedForm);

    dbDeploymentKey = new DbLong();
    tenantAwareFormIdAndDeploymentKey =
        new DbTenantAwareKey<>(
            tenantIdKey, new DbCompositeKey<>(dbFormId, dbDeploymentKey), PlacementType.PREFIX);
    formKeyByFormIdAndDeploymentKeyColumnFamily =
        zeebeDb.createColumnFamily(
            ZbColumnFamilies.FORM_KEY_BY_FORM_ID_AND_DEPLOYMENT_KEY,
            transactionContext,
            tenantAwareFormIdAndDeploymentKey,
            fkFormKey);

    versionManager =
        new VersionManager(
            DEFAULT_VERSION_VALUE, zeebeDb, ZbColumnFamilies.FORM_VERSION, transactionContext);

    formsByTenantIdAndIdCache =
        CacheBuilder.newBuilder().maximumSize(config.getFormCacheCapacity()).build();
  }

  @Override
  public void storeFormInFormColumnFamily(final FormRecord record) {
    tenantIdKey.wrapString(record.getTenantId());
    dbFormKey.wrapLong(record.getFormKey());
    dbPersistedForm.wrap(record);
    formsByKey.upsert(tenantAwareFormKey, dbPersistedForm);
    formsByTenantIdAndIdCache.put(
        new TenantIdAndFormId(record.getTenantId(), record.getFormIdBuffer()),
        dbPersistedForm.copy());
  }

  @Override
  public void storeFormInFormByIdAndVersionColumnFamily(final FormRecord record) {
    tenantIdKey.wrapString(record.getTenantId());
    dbFormId.wrapString(record.getFormId());
    formVersion.wrapLong(record.getVersion());
    dbPersistedForm.wrap(record);
    formByIdAndVersionColumnFamily.upsert(tenantAwareIdAndVersionKey, dbPersistedForm);
  }

  @Override
  public void storeFormInFormKeyByFormIdAndDeploymentKeyColumnFamily(final FormRecord record) {
    tenantIdKey.wrapString(record.getTenantId());
    dbFormKey.wrapLong(record.getFormKey());
    dbFormId.wrapString(record.getFormId());
    dbDeploymentKey.wrapLong(record.getDeploymentKey());
    formKeyByFormIdAndDeploymentKeyColumnFamily.upsert(
        tenantAwareFormIdAndDeploymentKey, fkFormKey);
  }

  @Override
  public void updateLatestVersion(final FormRecord record) {
    versionManager.addResourceVersion(
        record.getFormId(), record.getVersion(), record.getTenantId());
  }

  @Override
  public void deleteFormInFormsColumnFamily(final FormRecord record) {
    tenantIdKey.wrapString(record.getTenantId());
    dbFormKey.wrapLong(record.getFormKey());
    formsByKey.deleteExisting(tenantAwareFormKey);
    formsByTenantIdAndIdCache.invalidate(
        new TenantIdAndFormId(record.getTenantId(), record.getFormIdBuffer()));
  }

  @Override
  public void deleteFormInFormByIdAndVersionColumnFamily(final FormRecord record) {
    tenantIdKey.wrapString(record.getTenantId());
    dbFormId.wrapString(record.getFormId());
    formVersion.wrapLong(record.getVersion());
    formByIdAndVersionColumnFamily.deleteExisting(tenantAwareIdAndVersionKey);
  }

  @Override
  public void deleteFormInFormVersionColumnFamily(final FormRecord record) {
    versionManager.deleteResourceVersion(
        record.getFormId(), record.getVersion(), record.getTenantId());
  }

  @Override
  public void deleteFormInFormKeyByFormIdAndDeploymentKeyColumnFamily(final FormRecord record) {
    tenantIdKey.wrapString(record.getTenantId());
    dbFormId.wrapString(record.getFormId());
    dbDeploymentKey.wrapLong(record.getDeploymentKey());
    formKeyByFormIdAndDeploymentKeyColumnFamily.deleteIfExists(tenantAwareFormIdAndDeploymentKey);
  }

  @Override
  public Optional<PersistedForm> findLatestFormById(
      final DirectBuffer formId, final String tenantId) {
    tenantIdKey.wrapString(tenantId);
    final Optional<PersistedForm> cachedForm = getFormFromCache(tenantId, formId);
    if (cachedForm.isPresent()) {
      return cachedForm;
    }

    final PersistedForm persistedForm = getPersistedFormById(formId, tenantId);
    if (persistedForm == null) {
      return Optional.empty();
    }
    formsByTenantIdAndIdCache.put(new TenantIdAndFormId(tenantId, formId), persistedForm);
    return Optional.of(persistedForm);
  }

  @Override
  public Optional<PersistedForm> findFormByKey(final long formKey, final String tenantId) {
    tenantIdKey.wrapString(tenantId);
    dbFormKey.wrapLong(formKey);
    return Optional.ofNullable(formsByKey.get(tenantAwareFormKey)).map(PersistedForm::copy);
  }

  @Override
  public Optional<PersistedForm> findFormByIdAndDeploymentKey(
      final DirectBuffer formId, final long deploymentKey, final String tenantId) {
    tenantIdKey.wrapString(tenantId);
    dbFormId.wrapBuffer(formId);
    dbDeploymentKey.wrapLong(deploymentKey);
    return Optional.ofNullable(
            formKeyByFormIdAndDeploymentKeyColumnFamily.get(tenantAwareFormIdAndDeploymentKey))
        .flatMap(key -> findFormByKey(key.inner().wrappedKey().getValue(), tenantId));
  }

  @Override
  public int getNextFormVersion(final String formId, final String tenantId) {
    return (int) versionManager.getHighestResourceVersion(formId, tenantId) + 1;
  }

  @Override
  public void clearCache() {
    formsByTenantIdAndIdCache.invalidateAll();
    versionManager.clear();
  }

  private PersistedForm getPersistedFormById(final DirectBuffer formId, final String tenantId) {
    dbFormId.wrapBuffer(formId);
    final long latestVersion = versionManager.getLatestResourceVersion(formId, tenantId);
    formVersion.wrapLong(latestVersion);
    final PersistedForm persistedForm =
        formByIdAndVersionColumnFamily.get(tenantAwareIdAndVersionKey);
    if (persistedForm == null) {
      return null;
    }
    return persistedForm.copy();
  }

  private Optional<PersistedForm> getFormFromCache(
      final String tenantId, final DirectBuffer formId) {
    return Optional.ofNullable(
        formsByTenantIdAndIdCache.getIfPresent(new TenantIdAndFormId(tenantId, formId)));
  }

  private record TenantIdAndFormId(String tenantId, DirectBuffer formId) {}
}
