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
package io.camunda.zeebe.model.bpmn.instance.zeebe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants;
import io.camunda.zeebe.model.bpmn.instance.BpmnModelElementInstanceTest;
import io.camunda.zeebe.model.bpmn.instance.CallActivity;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;

public class ZeebeCalledElementTest extends BpmnModelElementInstanceTest {

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(BpmnModelConstants.ZEEBE_NS, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Collections.emptyList();
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
        new AttributeAssumption(BpmnModelConstants.ZEEBE_NS, "processId", false, false),
        new AttributeAssumption(
            BpmnModelConstants.ZEEBE_NS, "propagateAllChildVariables", false, false, true),
        new AttributeAssumption(
            BpmnModelConstants.ZEEBE_NS, "propagateAllParentVariables", false, false, true),
        new AttributeAssumption(
            BpmnModelConstants.ZEEBE_NS, "bindingType", false, false, ZeebeBindingType.latest));
  }

  @Test
  public void shouldReadValidBindingTypeFromXml() {
    // given
    final BpmnModelInstance modelInstance =
        Bpmn.createExecutableProcess()
            .startEvent()
            .callActivity("callActivity", c -> c.zeebeBindingType(ZeebeBindingType.deployment))
            .done();
    final String modelXml = Bpmn.convertToString(modelInstance);

    // when
    final CallActivity callActivity =
        Bpmn.readModelFromStream(new ByteArrayInputStream(modelXml.getBytes()))
            .getModelElementById("callActivity");
    final ZeebeCalledElement calledElement =
        callActivity.getSingleExtensionElement(ZeebeCalledElement.class);

    // then
    assertThat(calledElement.getBindingType()).isEqualTo(ZeebeBindingType.deployment);
  }

  @Test
  public void shouldThrowExceptionForInvalidBindingTypeInXml() {
    // given
    final BpmnModelInstance modelInstance =
        Bpmn.createExecutableProcess()
            .startEvent()
            .callActivity("callActivity", c -> c.zeebeBindingType(ZeebeBindingType.deployment))
            .done();
    final String modelXml =
        Bpmn.convertToString(modelInstance)
            .replace("bindingType=\"deployment\"", "bindingType=\"foo\"");

    // when
    final CallActivity callActivity =
        Bpmn.readModelFromStream(new ByteArrayInputStream(modelXml.getBytes()))
            .getModelElementById("callActivity");
    final ZeebeCalledElement calledElement =
        callActivity.getSingleExtensionElement(ZeebeCalledElement.class);

    // then
    assertThatThrownBy(calledElement::getBindingType)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "No enum constant io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeBindingType.foo");
  }
}
