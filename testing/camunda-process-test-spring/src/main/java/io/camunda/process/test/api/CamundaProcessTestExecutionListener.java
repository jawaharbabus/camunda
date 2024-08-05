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

import io.camunda.process.test.impl.assertions.CamundaDataSource;
import io.camunda.process.test.impl.configuration.CamundaContainerRuntimeConfiguration;
import io.camunda.process.test.impl.containers.OperateContainer;
import io.camunda.process.test.impl.extension.CamundaProcessTestContextImpl;
import io.camunda.process.test.impl.proxy.CamundaProcessTestContextProxy;
import io.camunda.process.test.impl.proxy.ZeebeClientProxy;
import io.camunda.process.test.impl.runtime.CamundaContainerRuntime;
import io.camunda.process.test.impl.runtime.CamundaContainerRuntimeBuilder;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.spring.client.event.ZeebeClientClosingEvent;
import io.camunda.zeebe.spring.client.event.ZeebeClientCreatedEvent;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * A Spring test execution listener that provides the runtime for process tests.
 *
 * <p>Before each test method:
 *
 * <ul>
 *   <li>Start the runtime
 *   <li>Create a {@link ZeebeClient} to inject in the test class
 *   <li>Create a {@link CamundaProcessTestContext} to inject in the test class
 *   <li>Publish a {@link ZeebeClientCreatedEvent}
 * </ul>
 *
 * <p>After each test method:
 *
 * <ul>
 *   <li>Publish a {@link ZeebeClientClosingEvent}
 *   <li>Close created {@link ZeebeClient}s
 *   <li>Stop the runtime
 * </ul>
 */
public class CamundaProcessTestExecutionListener implements TestExecutionListener, Ordered {

  private final CamundaContainerRuntimeBuilder containerRuntimeBuilder;
  private final List<ZeebeClient> createdClients = new ArrayList<>();

  private CamundaContainerRuntime containerRuntime;
  private ZeebeClient client;

  public CamundaProcessTestExecutionListener() {
    this(CamundaContainerRuntime.newBuilder());
  }

  CamundaProcessTestExecutionListener(
      final CamundaContainerRuntimeBuilder containerRuntimeBuilder) {
    this.containerRuntimeBuilder = containerRuntimeBuilder;
  }

  @Override
  public void beforeTestMethod(final TestContext testContext) throws Exception {
    // create runtime
    containerRuntime = buildRuntime(testContext);
    containerRuntime.start();

    final CamundaProcessTestContext camundaProcessTestContext =
        new CamundaProcessTestContextImpl(
            containerRuntime.getZeebeContainer(), createdClients::add);

    client = createClient(testContext, camundaProcessTestContext);

    // fill proxies
    testContext.getApplicationContext().getBean(ZeebeClientProxy.class).setZeebeClient(client);
    testContext
        .getApplicationContext()
        .getBean(CamundaProcessTestContextProxy.class)
        .setContext(camundaProcessTestContext);

    // publish Zeebe client
    testContext.getApplicationContext().publishEvent(new ZeebeClientCreatedEvent(this, client));

    // initialize assertions
    final CamundaDataSource dataSource = createDataSource(containerRuntime);
    CamundaAssert.initialize(dataSource);
  }

  @Override
  public void afterTestMethod(final TestContext testContext) throws Exception {
    // reset assertions
    CamundaAssert.reset();

    // close Zeebe clients
    testContext.getApplicationContext().publishEvent(new ZeebeClientClosingEvent(this, client));

    createdClients.forEach(ZeebeClient::close);

    // clean up proxies
    testContext.getApplicationContext().getBean(ZeebeClientProxy.class).removeZeebeClient();
    testContext
        .getApplicationContext()
        .getBean(CamundaProcessTestContextProxy.class)
        .removeContext();

    // close runtime
    containerRuntime.close();
  }

  private CamundaContainerRuntime buildRuntime(final TestContext testContext) {
    final CamundaContainerRuntimeConfiguration runtimeConfiguration =
        testContext.getApplicationContext().getBean(CamundaContainerRuntimeConfiguration.class);

    containerRuntimeBuilder
        .withZeebeDockerImageVersion(runtimeConfiguration.getCamundaVersion())
        .withOperateDockerImageVersion(runtimeConfiguration.getCamundaVersion())
        .withTasklistDockerImageVersion(runtimeConfiguration.getCamundaVersion())
        .withZeebeDockerImageName(runtimeConfiguration.getZeebeDockerImageName())
        .withZeebeEnv(runtimeConfiguration.getZeebeEnvVars());

    runtimeConfiguration
        .getZeebeExposedPorts()
        .forEach(containerRuntimeBuilder::withZeebeExposedPort);

    return containerRuntimeBuilder.build();
  }

  private static ZeebeClient createClient(
      final TestContext testContext, final CamundaProcessTestContext camundaProcessTestContext) {
    return camundaProcessTestContext.createClient(
        builder -> {
          if (hasJsonMapper(testContext)) {
            final JsonMapper jsonMapper =
                testContext.getApplicationContext().getBean(JsonMapper.class);
            builder.withJsonMapper(jsonMapper);
          }
        });
  }

  private static boolean hasJsonMapper(final TestContext testContext) {
    return testContext.getApplicationContext().getBeanNamesForType(JsonMapper.class).length > 0;
  }

  private CamundaDataSource createDataSource(final CamundaContainerRuntime containerRuntime) {
    final OperateContainer operateContainer = containerRuntime.getOperateContainer();
    final String operateApiEndpoint =
        "http://" + operateContainer.getHost() + ":" + operateContainer.getRestApiPort();
    return new CamundaDataSource(operateApiEndpoint);
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }
}
