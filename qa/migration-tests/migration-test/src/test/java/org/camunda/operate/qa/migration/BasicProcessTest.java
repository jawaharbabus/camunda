/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.operate.qa.migration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;
import org.camunda.operate.entities.EventEntity;
import org.camunda.operate.entities.FlowNodeInstanceEntity;
import org.camunda.operate.entities.IncidentEntity;
import org.camunda.operate.entities.OperationEntity;
import org.camunda.operate.entities.SequenceFlowEntity;
import org.camunda.operate.entities.UserEntity;
import org.camunda.operate.entities.VariableEntity;
import org.camunda.operate.entities.listview.FlowNodeInstanceForListViewEntity;
import org.camunda.operate.entities.listview.VariableForListViewEntity;
import org.camunda.operate.entities.meta.ImportPositionEntity;
import org.camunda.operate.qa.migration.util.AbstractMigrationTest;
import org.camunda.operate.qa.migration.v100.BasicProcessDataGenerator;
import org.camunda.operate.schema.indices.UserIndex;
import org.camunda.operate.schema.templates.EventTemplate;
import org.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import org.camunda.operate.schema.templates.IncidentTemplate;
import org.camunda.operate.schema.templates.ListViewTemplate;
import org.camunda.operate.schema.templates.SequenceFlowTemplate;
import org.camunda.operate.schema.templates.VariableTemplate;
import org.camunda.operate.util.ElasticsearchUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.operate.schema.templates.ListViewTemplate.ACTIVITIES_JOIN_RELATION;
import static org.camunda.operate.schema.templates.ListViewTemplate.JOIN_RELATION;
import static org.camunda.operate.schema.templates.ListViewTemplate.VARIABLES_JOIN_RELATION;
import static org.camunda.operate.schema.templates.ListViewTemplate.PROCESS_INSTANCE_JOIN_RELATION;
import static org.camunda.operate.util.CollectionUtil.chooseOne;
import static org.camunda.operate.util.ThreadUtil.sleepFor;
import static org.camunda.operate.util.ElasticsearchUtil.joinWithAnd;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public class BasicProcessTest extends AbstractMigrationTest {

  private String bpmnProcessId = BasicProcessDataGenerator.PROCESS_BPMN_PROCESS_ID;
  private Set<String> processInstanceIds;

  @Before
  public void findProcessInstanceIds() {
    assumeThatProcessIsUnderTest(bpmnProcessId);
    if (processInstanceIds == null) {
      sleepFor(5_000);
      SearchRequest searchRequest = new SearchRequest(listViewTemplate.getAlias());
      // Process instances list
      searchRequest.source()
          .query(joinWithAnd(termQuery(JOIN_RELATION, PROCESS_INSTANCE_JOIN_RELATION), termQuery(ListViewTemplate.BPMN_PROCESS_ID, bpmnProcessId)));
      try {
        processInstanceIds = ElasticsearchUtil.scrollIdsToSet(searchRequest, esClient);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      assertThat(processInstanceIds).hasSize(BasicProcessDataGenerator.PROCESS_INSTANCE_COUNT);
    }
  }

  @Test
  public void testImportPositions() {
    List<ImportPositionEntity> importPositions = entityReader.getEntitiesFor(importPositionIndex.getAlias(), ImportPositionEntity.class);
    assertThat(importPositions.isEmpty()).describedAs("There should exists at least 1 ImportPosition").isFalse();
  }

  @Test
  public void testEvents() {
    SearchRequest searchRequest = new SearchRequest(eventTemplate.getAlias());
    searchRequest.source().query(termsQuery(EventTemplate.PROCESS_INSTANCE_KEY, processInstanceIds));
    List<EventEntity> events = entityReader.searchEntitiesFor(searchRequest, EventEntity.class);
    assertThat(events.isEmpty()).isFalse();
    assertThat(events.stream().filter(e -> e.getMetadata() != null).count()).describedAs("At least one event has metadata").isGreaterThan(0);
    assertThat(events.stream().allMatch(e -> e.getEventSourceType()!= null)).describedAs("All events have a EventSourceType").isTrue();
    assertThat(events.stream().allMatch(e -> e.getEventType() != null)).describedAs("All events have a EventType").isTrue();
  }

  @Test
  public void testSequenceFlows() {
    SearchRequest searchRequest = new SearchRequest(sequenceFlowTemplate.getAlias());
    searchRequest.source().query(termsQuery(SequenceFlowTemplate.PROCESS_INSTANCE_KEY, processInstanceIds));
    List<SequenceFlowEntity> sequenceFlows = entityReader.searchEntitiesFor(searchRequest, SequenceFlowEntity.class);
    assertThat(sequenceFlows.size()).isEqualTo(BasicProcessDataGenerator.PROCESS_INSTANCE_COUNT * 2);
  }

  @Test
  public void testFlowNodeInstances() {
    SearchRequest searchRequest = new SearchRequest(flowNodeInstanceTemplate.getAlias());
    searchRequest.source().query(termsQuery(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY, processInstanceIds));
    List<FlowNodeInstanceEntity> activityInstances = entityReader.searchEntitiesFor(searchRequest, FlowNodeInstanceEntity.class);
    assertThat(activityInstances.size()).isEqualTo(BasicProcessDataGenerator.PROCESS_INSTANCE_COUNT * 3);
    assertThat(activityInstances.stream().allMatch( a -> a.getType() != null)).as("All flow node instances have a type").isTrue();
    assertThat(activityInstances.stream().allMatch( a -> a.getState()!= null)).as("All flow node instances have a state").isTrue();
  }

  @Test
  public void testVariables() {
    SearchRequest searchRequest = new SearchRequest(variableTemplate.getAlias());
    searchRequest.source().query(termsQuery(VariableTemplate.PROCESS_INSTANCE_KEY, processInstanceIds));
    List<VariableEntity> variableEntities = entityReader.searchEntitiesFor(searchRequest, VariableEntity.class);
    assertThat(variableEntities.size()).isEqualTo(BasicProcessDataGenerator.PROCESS_INSTANCE_COUNT * 4);
  }

  @Test
  public void testOperations() {
    //TODO narrow down the search criteria
    List<OperationEntity> operations = entityReader.getEntitiesFor(operationTemplate.getAlias(), OperationEntity.class);
    assertThat(operations.size()).describedAs("At least one operation is active").isGreaterThan(0);
  }

  @Test
  public void testListViews() {
    SearchRequest searchRequest = new SearchRequest(listViewTemplate.getAlias());
    int processInstancesCount = BasicProcessDataGenerator.PROCESS_INSTANCE_COUNT;

    //  Variables list
    searchRequest.source().query(joinWithAnd(termQuery(JOIN_RELATION, VARIABLES_JOIN_RELATION),
        termsQuery(ListViewTemplate.PROCESS_INSTANCE_KEY, processInstanceIds)));
    List<VariableForListViewEntity> variablesList = entityReader.searchEntitiesFor(searchRequest, VariableForListViewEntity.class);
    assertThat(variablesList.size()).isEqualTo(processInstancesCount * 4);

    // Activity instances list
    searchRequest.source().query(joinWithAnd(termQuery(JOIN_RELATION, ACTIVITIES_JOIN_RELATION),
        termsQuery(ListViewTemplate.PROCESS_INSTANCE_KEY, processInstanceIds)));
    List<FlowNodeInstanceForListViewEntity> activitiesList = entityReader.searchEntitiesFor(searchRequest, FlowNodeInstanceForListViewEntity.class);
    assertThat(activitiesList.size()).isEqualTo(processInstancesCount * 3);
  }

  @Test
  public void testIncidents() {
    SearchRequest searchRequest = new SearchRequest(incidentTemplate.getAlias());
    searchRequest.source().query(termsQuery(IncidentTemplate.PROCESS_INSTANCE_KEY, processInstanceIds));
    List<IncidentEntity> incidents = entityReader.searchEntitiesFor(searchRequest, IncidentEntity.class);
    assertThat(incidents.size()).isBetween(
        BasicProcessDataGenerator.INCIDENT_COUNT - (BasicProcessDataGenerator.COUNT_OF_CANCEL_OPERATION + BasicProcessDataGenerator.COUNT_OF_RESOLVE_OPERATION),
        BasicProcessDataGenerator.INCIDENT_COUNT
    );
    assertThat(incidents.stream().allMatch(i -> i.getState() != null)).describedAs("Each incident has a state").isTrue();
    assertThat(incidents.stream().allMatch(i -> i.getErrorType() != null)).describedAs("Each incident has an errorType").isTrue();
    IncidentEntity randomIncident = chooseOne(incidents);
    assertThat(randomIncident.getErrorMessageHash()).isNotNull();
  }

  @Test
  public void testUsers() {
    final List<UserEntity> users = entityReader.getEntitiesFor(userIndex.getAlias(), UserEntity.class);
    assertThat(users.size()).isEqualTo(2);
    assertThat(users).extracting(UserIndex.USERNAME).contains("demo", "act");
  }

}
