/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.tasklist.webapp.service;

import static io.camunda.zeebe.client.api.command.CommandWithTenantStep.DEFAULT_TENANT_IDENTIFIER;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.Metrics;
import io.camunda.tasklist.entities.TaskEntity;
import io.camunda.tasklist.entities.TaskImplementation;
import io.camunda.tasklist.entities.TaskState;
import io.camunda.tasklist.exceptions.NotFoundException;
import io.camunda.tasklist.exceptions.TasklistRuntimeException;
import io.camunda.tasklist.store.TaskMetricsStore;
import io.camunda.tasklist.store.TaskStore;
import io.camunda.tasklist.store.VariableStore.GetVariablesRequest;
import io.camunda.tasklist.views.TaskSearchView;
import io.camunda.tasklist.webapp.CommonUtils;
import io.camunda.tasklist.webapp.es.TaskValidator;
import io.camunda.tasklist.webapp.graphql.entity.TaskDTO;
import io.camunda.tasklist.webapp.graphql.entity.TaskQueryDTO;
import io.camunda.tasklist.webapp.graphql.entity.UserDTO;
import io.camunda.tasklist.webapp.graphql.entity.VariableDTO;
import io.camunda.tasklist.webapp.graphql.entity.VariableInputDTO;
import io.camunda.tasklist.webapp.rest.exception.ForbiddenActionException;
import io.camunda.tasklist.webapp.rest.exception.InvalidRequestException;
import io.camunda.tasklist.webapp.security.AssigneeMigrator;
import io.camunda.tasklist.webapp.security.UserReader;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.AssignUserTaskCommandStep1;
import io.camunda.zeebe.client.api.command.ClientException;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.CompleteUserTaskCommandStep1;
import io.camunda.zeebe.client.api.command.UnassignUserTaskCommandStep1;
import io.camunda.zeebe.client.protocol.rest.ProblemDetail;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private UserReader userReader;
  @Mock private ZeebeClient zeebeClient;
  @Mock private TaskStore taskStore;
  @Mock private VariableService variableService;
  @Spy private ObjectMapper objectMapper = CommonUtils.getObjectMapper();
  @Mock private Metrics metrics;
  @Mock private TaskMetricsStore taskMetricsStore;
  @Mock private AssigneeMigrator assigneeMigrator;
  @Mock private TaskValidator taskValidator;

  @InjectMocks private TaskService instance;

  @Test
  void getTasks() {
    // Given
    final var taskQuery = new TaskQueryDTO().setSearchAfter(new String[] {"123", "456"});
    final var providedTask =
        new TaskSearchView()
            .setId("123")
            .setState(TaskState.CREATED)
            .setSortValues(new String[] {"123", "456"})
            .setImplementation(TaskImplementation.JOB_WORKER);
    final var expectedTask =
        new TaskDTO()
            .setId("123")
            .setTaskState(TaskState.CREATED)
            .setSortValues(new String[] {"123", "456"})
            .setImplementation(TaskImplementation.JOB_WORKER);

    when(taskStore.getTasks(taskQuery.toTaskQuery())).thenReturn(List.of(providedTask));

    // When
    final var result = instance.getTasks(taskQuery);

    // Then
    assertThat(result).containsExactly(expectedTask);
  }

  @Test
  void searchTasksWhenSeveralSearchOptionsReturnsError() {
    // Given
    final var taskQuery =
        new TaskQueryDTO()
            .setState(TaskState.CREATED)
            .setAssigned(true)
            .setSearchAfter(new String[] {"123"})
            .setSearchAfterOrEqual(new String[] {"456"});

    // When and Then
    // When / Then
    final InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, () -> instance.getTasks(taskQuery));

    // Validate the exception message if needed
    assertEquals(
        "Only one of [searchAfter, searchAfterOrEqual, searchBefore, searchBeforeOrEqual] must be present in request.",
        exception.getMessage());
  }

  @Test
  void getTasksWithVariables() {
    // Given
    final var taskQuery = new TaskQueryDTO();
    final var providedTasks =
        List.of(
            new TaskSearchView().setId("123").setState(TaskState.CREATED),
            new TaskSearchView().setId("456").setState(TaskState.COMPLETED));
    final var expectedTasks =
        List.of(
            new TaskDTO()
                .setId("123")
                .setTaskState(TaskState.CREATED)
                .setVariables(
                    new VariableDTO[] {
                      new VariableDTO()
                          .setId("var123")
                          .setName("varA")
                          .setPreviewValue("valA")
                          .setIsValueTruncated(false)
                    }),
            new TaskDTO()
                .setId("456")
                .setTaskState(TaskState.COMPLETED)
                .setVariables(
                    new VariableDTO[] {
                      new VariableDTO()
                          .setId("var123")
                          .setName("varA")
                          .setPreviewValue("longVal")
                          .setIsValueTruncated(true)
                    }));

    when(taskStore.getTasks(taskQuery.toTaskQuery())).thenReturn(providedTasks);
    final Set<String> fieldNames = Set.of("id", "name", "previewValue", "isValueTruncated");
    when(variableService.getVariablesPerTaskId(
            List.of(
                new GetVariablesRequest()
                    .setTaskId("123")
                    .setState(TaskState.CREATED)
                    .setVarNames(List.of("varA"))
                    .setFieldNames(fieldNames),
                new GetVariablesRequest()
                    .setTaskId("456")
                    .setState(TaskState.COMPLETED)
                    .setVarNames(List.of("varA"))
                    .setFieldNames(fieldNames))))
        .thenReturn(
            Map.of(
                "123",
                List.of(
                    new VariableDTO()
                        .setId("var123")
                        .setName("varA")
                        .setPreviewValue("valA")
                        .setIsValueTruncated(false)),
                "456",
                List.of(
                    new VariableDTO()
                        .setId("var123")
                        .setName("varA")
                        .setPreviewValue("longVal")
                        .setIsValueTruncated(true))));

    // When
    final var result = instance.getTasks(taskQuery, Set.of("varA"), false);

    // Then
    assertThat(result).containsAll(expectedTasks);
  }

  @Test
  void getTasksWithoutVariables() {
    // Given
    final var taskQuery = new TaskQueryDTO();
    final var providedTasks =
        List.of(
            new TaskSearchView().setId("123").setState(TaskState.CREATED),
            new TaskSearchView().setId("456").setState(TaskState.COMPLETED));
    final var expectedTasks =
        List.of(
            new TaskDTO().setId("123").setTaskState(TaskState.CREATED),
            new TaskDTO().setId("456").setTaskState(TaskState.COMPLETED));

    when(taskStore.getTasks(taskQuery.toTaskQuery())).thenReturn(providedTasks);
    // When
    final var result = instance.getTasks(taskQuery, emptySet(), false);

    // Then
    assertThat(result).containsAll(expectedTasks);
    verify(variableService, never()).getVariablesPerTaskId(any());
  }

  private static Stream<Arguments> getTasksTestData() {
    final var arr = new String[] {"a", "b"};
    return Stream.of(
        Arguments.of(null, null, arr, arr),
        Arguments.of(arr, null, null, arr),
        Arguments.of(null, arr, arr, null),
        Arguments.of(null, arr, arr, arr),
        Arguments.of(arr, arr, arr, arr));
  }

  @ParameterizedTest
  @MethodSource("getTasksTestData")
  void getTasksWhenMoreThanOneSearchQueryProvided(
      final String[] searchBefore,
      final String[] searchBeforeOrEqual,
      final String[] searchAfter,
      final String[] searchAfterOrEqual) {
    // Given
    final var taskQuery =
        new TaskQueryDTO()
            .setSearchBefore(searchBefore)
            .setSearchBeforeOrEqual(searchBeforeOrEqual)
            .setSearchAfter(searchAfter)
            .setSearchAfterOrEqual(searchAfterOrEqual);

    // When - Then
    assertThatThrownBy(() -> instance.getTasks(taskQuery))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage(
            "Only one of [searchAfter, searchAfterOrEqual, searchBefore, searchBeforeOrEqual] must be present in request.");

    verifyNoInteractions(taskStore);
  }

  @Test
  void getTask() {
    // Given
    final String taskId = "123";
    final var providedTask = new TaskEntity().setId(taskId).setState(TaskState.CREATED);
    final var expectedTask =
        new TaskDTO()
            .setId(taskId)
            .setTaskState(TaskState.CREATED)
            .setTenantId(DEFAULT_TENANT_IDENTIFIER);
    when(taskStore.getTask(taskId)).thenReturn(providedTask);

    // When
    final var result = instance.getTask(taskId);

    // Then
    assertThat(result).isEqualTo(expectedTask);
  }

  private static Stream<Arguments> assignTaskTestData() {
    final var userA = "userA";
    final var userB = "userB";
    return Stream.of(
        Arguments.of(null, true, userA, new UserDTO().setUserId(userA), userA),
        Arguments.of(false, false, userA, new UserDTO().setUserId(userB).setApiUser(true), userA),
        Arguments.of(true, true, null, new UserDTO().setUserId(userB), userB),
        Arguments.of(true, true, "", new UserDTO().setUserId(userB), userB));
  }

  @ParameterizedTest
  @MethodSource("assignTaskTestData")
  void assignTask(
      final Boolean providedAllowOverrideAssignment,
      final boolean expectedAllowOverrideAssignment,
      final String providedAssignee,
      final UserDTO user,
      final String expectedAssignee) {

    // Given
    final var taskId = "123";
    final var taskBefore = mock(TaskEntity.class);
    when(taskStore.getTask(taskId)).thenReturn(taskBefore);
    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.JOB_WORKER);
    when(userReader.getCurrentUser()).thenReturn(user);
    final var assignedTask = new TaskEntity().setAssignee(expectedAssignee);
    when(taskStore.persistTaskClaim(taskBefore, expectedAssignee)).thenReturn(assignedTask);

    // When
    final var result =
        instance.assignTask(taskId, providedAssignee, providedAllowOverrideAssignment);

    // Then
    verify(taskValidator).validateCanAssign(taskBefore, expectedAllowOverrideAssignment);
    assertThat(result).isEqualTo(TaskDTO.createFrom(assignedTask, objectMapper));
  }

  @Test
  public void assignTaskByApiUser() {
    // given
    final var taskId = "123";
    when(userReader.getCurrentUser()).thenReturn(new UserDTO().setUserId("userA").setApiUser(true));

    // when - then
    verifyNoInteractions(taskStore, taskValidator);
    assertThatThrownBy(() -> instance.assignTask(taskId, "", true))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage("Assignee must be specified");
  }

  @Test
  public void assignTaskToEmptyUser() {
    // given
    final var taskId = "123";
    when(userReader.getCurrentUser()).thenReturn(new UserDTO().setUserId("userA").setApiUser(true));

    // when - then
    verifyNoInteractions(taskStore, taskValidator);
    assertThatThrownBy(() -> instance.assignTask(taskId, "", true))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage("Assignee must be specified");
  }

  @Test
  public void assignTaskToInvalidTask() {
    // given
    final var taskId = "123";
    when(userReader.getCurrentUser()).thenReturn(new UserDTO().setUserId("userA").setApiUser(true));
    when(taskStore.getTask(taskId))
        .thenThrow(new NotFoundException("task with id " + taskId + " was not found "));

    // when - then
    verifyNoInteractions(taskStore, taskValidator);
    assertThatThrownBy(() -> instance.assignTask(taskId, "userA", true))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("task with id %s was not found ", taskId);
  }

  @Test
  public void unassignTaskToInvalidTask() {
    // given
    final var taskId = "123";
    when(taskStore.getTask(taskId))
        .thenThrow(new NotFoundException("task with id " + taskId + " was not found "));

    // when - then
    verifyNoInteractions(taskStore, taskValidator);
    assertThatThrownBy(() -> instance.unassignTask(taskId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("task with id %s was not found ", taskId);
  }

  @Test
  public void assignTaskWhenUserTriesToAssignTaskToAnotherAssignee() {
    // given
    final var taskId = "123";
    when(userReader.getCurrentUser()).thenReturn(new UserDTO().setUserId("userA"));

    // when - then
    verifyNoInteractions(taskStore, taskValidator);
    assertThatThrownBy(() -> instance.assignTask(taskId, "userB", true))
        .isInstanceOf(ForbiddenActionException.class)
        .hasMessage("User doesn't have the permission to assign another user to this task");
  }

  @Test
  void unassignTask() {
    // Given
    final var taskId = "123";
    final var taskBefore = mock(TaskEntity.class);
    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.JOB_WORKER);
    when(taskStore.getTask(taskId)).thenReturn(taskBefore);
    final var unassignedTask = new TaskEntity().setId(taskId).setState(TaskState.CREATED);
    when(taskStore.persistTaskUnclaim(taskBefore)).thenReturn(unassignedTask);

    // When
    final var result = instance.unassignTask(taskId);

    // Then
    verify(taskValidator).validateCanUnassign(taskBefore);
    assertThat(result).isEqualTo(TaskDTO.createFrom(unassignedTask, objectMapper));
  }

  @Test
  void completeTask() {
    // Given
    final var taskId = "123";
    final var variables = List.of(new VariableInputDTO().setName("a").setValue("1"));
    final Map<String, Object> variablesMap = Map.of("a", 1);

    final var mockedUser = mock(UserDTO.class);
    when(userReader.getCurrentUser()).thenReturn(mockedUser);
    final var taskBefore = mock(TaskEntity.class);
    when(taskStore.getTask(taskId)).thenReturn(taskBefore);
    final var completedTask =
        new TaskEntity()
            .setId(taskId)
            .setState(TaskState.COMPLETED)
            .setAssignee("demo")
            .setCompletionTime(OffsetDateTime.now());
    when(taskStore.persistTaskCompletion(taskBefore)).thenReturn(completedTask);

    // mock zeebe command
    final var mockedJobCommandStep1 = mock(CompleteJobCommandStep1.class);
    when(zeebeClient.newCompleteCommand(123)).thenReturn(mockedJobCommandStep1);
    final var mockedJobCommandStep2 = mock(CompleteJobCommandStep1.class);
    when(mockedJobCommandStep1.variables(variablesMap)).thenReturn(mockedJobCommandStep2);
    final var mockedZeebeFuture = mock(ZeebeFuture.class);
    when(mockedJobCommandStep2.send()).thenReturn(mockedZeebeFuture);
    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.JOB_WORKER);
    // When
    final var result = instance.completeTask(taskId, variables, true);

    // Then
    verify(taskValidator).validateCanComplete(taskBefore);
    verify(variableService).persistTaskVariables(taskId, variables, true);
    verify(variableService).deleteDraftTaskVariables(taskId);
    assertThat(result).isEqualTo(TaskDTO.createFrom(completedTask, objectMapper));
  }

  @Test
  void completeZeebeUserTask() {
    // Given
    final var taskId = "123";
    final var variables = List.of(new VariableInputDTO().setName("a").setValue("1"));
    final Map<String, Object> variablesMap = Map.of("a", 1);

    final var mockedUser = mock(UserDTO.class);
    when(userReader.getCurrentUser()).thenReturn(mockedUser);
    final var taskBefore = mock(TaskEntity.class);
    when(taskStore.getTask(taskId)).thenReturn(taskBefore);
    final var completedTask =
        new TaskEntity()
            .setId(taskId)
            .setState(TaskState.COMPLETED)
            .setAssignee("demo")
            .setCompletionTime(OffsetDateTime.now());
    when(taskStore.persistTaskCompletion(taskBefore)).thenReturn(completedTask);

    // mock zeebe command
    final var mockedJobCommandStep1 = mock(CompleteUserTaskCommandStep1.class);
    when(zeebeClient.newUserTaskCompleteCommand(123)).thenReturn(mockedJobCommandStep1);
    final var mockedJobCommandStep2 = mock(CompleteUserTaskCommandStep1.class);
    when(mockedJobCommandStep1.variables(variablesMap)).thenReturn(mockedJobCommandStep2);
    final var mockedZeebeFuture = mock(ZeebeFuture.class);
    when(mockedJobCommandStep2.send()).thenReturn(mockedZeebeFuture);
    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.ZEEBE_USER_TASK);
    // When
    final var result = instance.completeTask(taskId, variables, true);

    // Then
    verify(taskValidator).validateCanComplete(taskBefore);
    verify(variableService).persistTaskVariables(taskId, variables, true);
    verify(variableService).deleteDraftTaskVariables(taskId);
    assertThat(result).isEqualTo(TaskDTO.createFrom(completedTask, objectMapper));
  }

  @Test
  void unassignZeebeUserTask() {
    // Given
    final var taskId = 123L;
    final var taskBefore = mock(TaskEntity.class);

    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.ZEEBE_USER_TASK);
    when(taskBefore.getKey()).thenReturn(taskId);
    when(taskStore.getTask(String.valueOf(taskId))).thenReturn(taskBefore);
    final var unassignedTask = new TaskEntity().setAssignee(null);
    when(taskStore.persistTaskUnclaim(taskBefore)).thenReturn(unassignedTask);
    when(zeebeClient.newUserTaskUnassignCommand(Long.valueOf(taskId)))
        .thenReturn(mock(UnassignUserTaskCommandStep1.class));
    when(zeebeClient.newUserTaskUnassignCommand(Long.valueOf(taskId)).send())
        .thenReturn(mock(ZeebeFuture.class));
    final var result = instance.unassignTask(String.valueOf(taskId));

    // Then
    verify(taskValidator).validateCanUnassign(taskBefore);
    assertThat(result).isEqualTo(TaskDTO.createFrom(unassignedTask, objectMapper));
  }

  @Test
  void unassignZeebeUserTaskException() {
    // Given
    final var taskId = 123L;
    final var taskBefore = mock(TaskEntity.class);

    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.ZEEBE_USER_TASK);
    when(taskBefore.getKey()).thenReturn(taskId);
    when(taskStore.getTask(String.valueOf(taskId))).thenReturn(taskBefore);
    final var unassignedTask = new TaskEntity().setAssignee(null);
    when(taskStore.persistTaskUnclaim(taskBefore)).thenReturn(unassignedTask);
    when(zeebeClient.newUserTaskUnassignCommand(Long.valueOf(taskId)))
        .thenReturn(mock(UnassignUserTaskCommandStep1.class));
    when(zeebeClient.newUserTaskUnassignCommand(Long.valueOf(taskId)).send())
        .thenThrow(new ClientException("reason for error"));

    // Then
    assertThatThrownBy(() -> instance.unassignTask(String.valueOf(taskId)))
        .isInstanceOf(TasklistRuntimeException.class)
        .hasMessage("reason for error");
  }

  @Test
  void assignZeebeUserTaskException() {
    // Given
    final var taskId = "123";
    final var taskBefore = mock(TaskEntity.class);
    final var user = mock(UserDTO.class);
    final var problemDetail = new ProblemDetail();
    final var providedAssignee = "expectedAssignee";
    final var providedAllowOverrideAssignment = false;

    problemDetail.setDetail("detail");

    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.ZEEBE_USER_TASK);
    when(taskStore.getTask(taskId)).thenReturn(taskBefore);
    when(userReader.getCurrentUser()).thenReturn(user);
    when(user.getUserId()).thenReturn(providedAssignee);
    when(zeebeClient.newUserTaskAssignCommand(Long.valueOf(taskId)))
        .thenReturn(mock(AssignUserTaskCommandStep1.class));
    when(zeebeClient.newUserTaskAssignCommand(Long.valueOf(taskId)).assignee(any()))
        .thenReturn(mock(AssignUserTaskCommandStep1.class));
    when(zeebeClient.newUserTaskAssignCommand(Long.valueOf(taskId)).assignee(any()).send())
        .thenThrow(new ClientException("reason for error"));

    // Then
    assertThatThrownBy(
            () -> instance.assignTask(taskId, providedAssignee, providedAllowOverrideAssignment))
        .isInstanceOf(TasklistRuntimeException.class)
        .hasMessage("reason for error");
  }

  @ParameterizedTest
  @MethodSource("assignTaskTestData")
  void assignZeebeUserTask(
      final Boolean providedAllowOverrideAssignment,
      final boolean expectedAllowOverrideAssignment,
      final String providedAssignee,
      final UserDTO user,
      final String expectedAssignee) {
    // Given
    final var taskId = "123";
    final var taskBefore = mock(TaskEntity.class);

    when(taskBefore.getImplementation()).thenReturn(TaskImplementation.ZEEBE_USER_TASK);
    when(taskStore.getTask(taskId)).thenReturn(taskBefore);
    when(userReader.getCurrentUser()).thenReturn(user);
    final var assignedTask = new TaskEntity().setAssignee(expectedAssignee);
    when(taskStore.persistTaskClaim(taskBefore, expectedAssignee)).thenReturn(assignedTask);
    when(zeebeClient.newUserTaskAssignCommand(Long.valueOf(taskId)))
        .thenReturn(mock(AssignUserTaskCommandStep1.class));
    when(zeebeClient.newUserTaskAssignCommand(Long.valueOf(taskId)).assignee(any()))
        .thenReturn(mock(AssignUserTaskCommandStep1.class));
    when(zeebeClient.newUserTaskAssignCommand(Long.valueOf(taskId)).assignee(any()).send())
        .thenReturn(mock(ZeebeFuture.class));
    final var result =
        instance.assignTask(taskId, providedAssignee, providedAllowOverrideAssignment);

    // Then
    verify(taskValidator).validateCanAssign(taskBefore, expectedAllowOverrideAssignment);
    assertThat(result).isEqualTo(TaskDTO.createFrom(assignedTask, objectMapper));
  }
}
