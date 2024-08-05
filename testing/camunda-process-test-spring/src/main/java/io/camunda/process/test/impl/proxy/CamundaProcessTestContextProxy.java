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
package io.camunda.process.test.impl.proxy;

import io.camunda.process.test.api.CamundaProcessTestContext;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Dynamic proxy to delegate to a {@link CamundaProcessTestContext} which allows to swap the context
 * object under the hood.
 */
public class CamundaProcessTestContextProxy extends AbstractInvocationHandler {

  private CamundaProcessTestContext delegate;

  public void setContext(final CamundaProcessTestContext camundaProcessTestContext) {
    delegate = camundaProcessTestContext;
  }

  public void removeContext() {
    delegate = null;
  }

  @Override
  protected Object handleInvocation(
      final Object proxy, final Method method, @Nullable final Object[] args) throws Throwable {
    if (delegate == null) {
      throw new RuntimeException(
          "Cannot invoke "
              + method
              + " on CamundaProcessTestContext, as CamundaProcessTestContext is currently not initialized. Maybe you run outside of a testcase?");
    }
    return method.invoke(delegate, args);
  }
}
