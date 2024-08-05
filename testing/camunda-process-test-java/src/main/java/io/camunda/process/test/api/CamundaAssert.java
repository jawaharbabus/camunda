/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.process.test.api;

import io.camunda.process.test.api.assertions.ProcessInstanceAssert;
import io.camunda.process.test.impl.assertions.CamundaDataSource;
import io.camunda.process.test.impl.assertions.ProcessInstanceAssertj;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import java.time.Duration;
import org.awaitility.Awaitility;

/**
 * The entry point for all assertions.
 *
 * <p>Example usage:
 *
 * <pre>
 *   &#064;Test
 *   void shouldWork() {
 *     // given
 *     final ProcessInstanceEvent processInstance =
 *         zeebeClient
 *             .newCreateInstanceCommand()
 *             .bpmnProcessId("process")
 *             .latestVersion()
 *             .send()
 *             .join();
 *
 *     // when
 *
 *     // then
 *     CamundaAssert.assertThat(processInstance)
 *         .isCompleted()
 *         .hasCompletedElements("A", "B");
 *   }
 * }
 * </pre>
 */
public class CamundaAssert {

  /** The default time how long an assertion waits until the expected state is reached. */
  public static final Duration DEFAULT_ASSERTION_TIMEOUT = Duration.ofSeconds(10);

  /** The default time between two assertion attempts until the expected state is reached. */
  public static final Duration DEFAULT_ASSERTION_INTERVAL = Duration.ofMillis(100);

  private static final ThreadLocal<CamundaDataSource> DATA_SOURCE = new ThreadLocal<>();

  static {
    Awaitility.setDefaultTimeout(DEFAULT_ASSERTION_TIMEOUT);
    Awaitility.setDefaultPollInterval(DEFAULT_ASSERTION_INTERVAL);
  }

  // ======== Configuration options ========

  /**
   * Configures the time how long an assertion waits until the expected state is reached.
   *
   * @param assertionTimeout the maximum time of an assertion
   * @see #DEFAULT_ASSERTION_TIMEOUT
   */
  public static void setAssertionTimeout(final Duration assertionTimeout) {
    Awaitility.setDefaultTimeout(assertionTimeout);
  }

  /**
   * Configures the time between two assertion attempts until the expected state is reached.
   *
   * @param assertionInterval time between two assertion attempts
   * @see #DEFAULT_ASSERTION_INTERVAL
   */
  public static void setAssertionInterval(final Duration assertionInterval) {
    Awaitility.setDefaultPollInterval(assertionInterval);
  }

  // ======== Assertions ========

  /**
   * To verify a process instance.
   *
   * @param processInstanceEvent the event of the process instance to verify
   * @return the assertion object
   */
  public static ProcessInstanceAssert assertThat(final ProcessInstanceEvent processInstanceEvent) {
    return new ProcessInstanceAssertj(
        getDataSource(), processInstanceEvent.getProcessInstanceKey());
  }

  /**
   * To verify a process instance.
   *
   * @param processInstanceResult the result of the process instance to verify
   * @return the assertion object
   */
  public static ProcessInstanceAssert assertThat(
      final ProcessInstanceResult processInstanceResult) {
    return new ProcessInstanceAssertj(
        getDataSource(), processInstanceResult.getProcessInstanceKey());
  }

  // ======== Internal ========

  private static CamundaDataSource getDataSource() {
    if (DATA_SOURCE.get() == null) {
      throw new IllegalStateException(
          "No data source is set. Maybe you run outside of a testcase?");
    }
    return DATA_SOURCE.get();
  }

  /**
   * Initializes the assertions by setting the data source. Must be called before the assertions are
   * used.
   */
  static void initialize(final CamundaDataSource dataSource) {
    CamundaAssert.DATA_SOURCE.set(dataSource);
  }

  /**
   * Resets the assertions by removing the data source. Must call {@link
   * #initialize(CamundaDataSource)} before the assertions can be used again.
   */
  static void reset() {
    CamundaAssert.DATA_SOURCE.remove();
  }
}
