/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.service.transformers.filter;

import static io.camunda.search.clients.query.SearchQueryBuilders.and;
import static io.camunda.search.clients.query.SearchQueryBuilders.longTerms;
import static io.camunda.search.clients.query.SearchQueryBuilders.stringTerms;
import static io.camunda.search.clients.query.SearchQueryBuilders.term;

import io.camunda.search.clients.query.SearchQuery;
import io.camunda.service.search.filter.UserTaskFilter;
import io.camunda.service.transformers.ServiceTransformers;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserTaskFilterTransformer implements FilterTransformer<UserTaskFilter> {

  private final ServiceTransformers transformers;

  public UserTaskFilterTransformer(final ServiceTransformers transformers) {
    this.transformers = transformers;
  }

  @Override
  public SearchQuery toSearchQuery(final UserTaskFilter filter) {
    final var userTaskKeysQuery = getUserTaskKeysQuery(filter.keys());

    final var processInstanceKeysQuery = getProcessInstanceKeysQuery(filter.processInstanceKeys());
    final var processDefinitionKeyQuery =
        getProcessDefinitionKeyQuery(filter.processDefinitionKeys());
    final var bpmnProcessDefinitionIdQuery = getBpmnProcessIdQuery(filter.bpmnProcessIds());
    final var elementIdQuery = getElementIdQuery(filter.elementIds());

    final var candidateUsersQuery = getCandidateUsersQuery(filter.candidateUsers());
    final var candidateGroupsQuery = getCandidateGroupsQuery(filter.candidateGroups());

    final var assigneesQuery = getAssigneesQuery(filter.assignees());
    final var stateQuery = getStateQuery(filter.states());
    final var tenantQuery = getTenantQuery(filter.tenantIds());

    // Temporary internal condition - in order to bring only Zeebe User Tasks from Tasklist Indices
    final var userTaksImplementationQuery = getUserTasksImplementationOnly();

    return and(
        userTaskKeysQuery,
        bpmnProcessDefinitionIdQuery,
        candidateUsersQuery,
        candidateGroupsQuery,
        assigneesQuery,
        stateQuery,
        processInstanceKeysQuery,
        processDefinitionKeyQuery,
        tenantQuery,
        userTaksImplementationQuery,
        elementIdQuery);
  }

  @Override
  public List<String> toIndices(final UserTaskFilter filter) {
    if (filter != null && filter.states() != null && !filter.states().isEmpty()) {
      if (Objects.equals(filter.states().getFirst(), "CREATED") && filter.states().size() == 1) {
        return Arrays.asList("tasklist-task-8.5.0_"); // Not necessary to visit alias in this case
      }
    }
    return Arrays.asList("tasklist-task-8.5.0_alias");
  }

  private SearchQuery getProcessInstanceKeysQuery(final List<Long> processInstanceKeys) {
    return longTerms("processInstanceId", processInstanceKeys);
  }

  private SearchQuery getProcessDefinitionKeyQuery(final List<Long> processDefinitionIds) {
    return longTerms("processDefinitionId", processDefinitionIds);
  }

  private SearchQuery getUserTaskKeysQuery(final List<Long> userTaskKeys) {
    return longTerms("key", userTaskKeys);
  }

  private SearchQuery getUserTasksImplementationOnly() {
    return term("implementation", "ZEEBE_USER_TASK");
  }

  private SearchQuery getCandidateUsersQuery(final List<String> candidateUsers) {
    return stringTerms("candidateUsers", candidateUsers);
  }

  private SearchQuery getCandidateGroupsQuery(final List<String> candidateGroups) {
    return stringTerms("candidateGroups", candidateGroups);
  }

  private SearchQuery getAssigneesQuery(final List<String> assignee) {
    return stringTerms("assignee", assignee);
  }

  private SearchQuery getStateQuery(final List<String> state) {
    return stringTerms("state", state);
  }

  private SearchQuery getTenantQuery(final List<String> tenant) {
    return stringTerms("tenantId", tenant);
  }

  private SearchQuery getBpmnProcessIdQuery(final List<String> bpmnProcessId) {
    return stringTerms("bpmnProcessId", bpmnProcessId);
  }

  private SearchQuery getElementIdQuery(final List<String> taskDefinitionId) {
    return stringTerms("flowNodeBpmnId", taskDefinitionId);
  }
}
