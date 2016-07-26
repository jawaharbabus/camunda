package org.camunda.tngp.broker.wf.runtime.bpmn.handler;

import org.camunda.tngp.bpmn.graph.FlowElementVisitor;
import org.camunda.tngp.bpmn.graph.ProcessGraph;
import org.camunda.tngp.broker.wf.repository.WfTypeCacheService;
import org.camunda.tngp.broker.wf.runtime.bpmn.event.BpmnActivityEventReader;
import org.camunda.tngp.broker.wf.runtime.bpmn.event.BpmnEventReader;
import org.camunda.tngp.broker.wf.runtime.bpmn.event.BpmnFlowElementEventReader;
import org.camunda.tngp.broker.wf.runtime.bpmn.event.BpmnProcessEventReader;
import org.camunda.tngp.graph.bpmn.BpmnAspect;
import org.camunda.tngp.log.LogReader;
import org.camunda.tngp.log.LogWriter;
import org.camunda.tngp.log.idgenerator.IdGenerator;
import org.camunda.tngp.taskqueue.data.BpmnActivityEventDecoder;
import org.camunda.tngp.taskqueue.data.BpmnFlowElementEventDecoder;
import org.camunda.tngp.taskqueue.data.BpmnProcessEventDecoder;

import uk.co.real_logic.agrona.collections.Int2ObjectHashMap;

public class BpmnEventHandler
{

    protected BpmnEventReader eventReader = new BpmnEventReader();
    protected final LogReader logReader;
    protected final LogWriter logWriter;
    protected final IdGenerator idGenerator;

    protected final Int2ObjectHashMap<BpmnFlowElementEventHandler> flowElementEventHandlers = new Int2ObjectHashMap<>();
    protected final Int2ObjectHashMap<BpmnProcessEventHandler> processEventHandlers = new Int2ObjectHashMap<>();
    protected final Int2ObjectHashMap<BpmnActivityInstanceEventHandler> activityEventHandlers = new Int2ObjectHashMap<>();

    protected final WfTypeCacheService processCache;
    protected FlowElementVisitor flowElementVisitor = new FlowElementVisitor();

    public BpmnEventHandler(WfTypeCacheService processCache, LogReader logReader, LogWriter logWriter, IdGenerator idGenerator)
    {
        this.processCache = processCache;
        this.logReader = logReader;
        this.logWriter = logWriter;
        this.idGenerator = idGenerator;
    }

    public void addFlowElementHandler(BpmnFlowElementEventHandler handler)
    {
        flowElementEventHandlers.put(handler.getHandledBpmnAspect().value(), handler);
    }

    public void addProcessHandler(BpmnProcessEventHandler handler)
    {
        processEventHandlers.put(handler.getHandledBpmnAspect().value(), handler);
    }

    public void addActivityHandler(BpmnActivityInstanceEventHandler handler)
    {
        activityEventHandlers.put(handler.getHandledBpmnAspect().value(), handler);
    }

    public int doWork()
    {
        int workCount = 0;

        final boolean hasNext = logReader.read(eventReader);
        if (hasNext)
        {
            handleBpmnEvent(eventReader);
            workCount++;
        }

        return workCount;
    }

    protected void handleBpmnEvent(BpmnEventReader eventReader)
    {
        final int bpmnEventType = eventReader.templateId();

        switch (bpmnEventType)
        {
            case BpmnProcessEventDecoder.TEMPLATE_ID:
                handleBpmnProcessEvent(eventReader.processEvent());
                break;
            case BpmnFlowElementEventDecoder.TEMPLATE_ID:
                handleBpmnFlowElementEvent(eventReader.flowElementEvent());
                break;
            case BpmnActivityEventDecoder.TEMPLATE_ID:
                handleBpmnActivityEvent(eventReader.activityEvent());
                break;
            default:
                throw new RuntimeException("No handler for event of type " + bpmnEventType);
        }
    }

    protected void handleBpmnFlowElementEvent(BpmnFlowElementEventReader flowElementEventReader)
    {
        final ProcessGraph process = processCache.getProcessGraphByTypeId(flowElementEventReader.processId());

        flowElementVisitor.init(process).moveToNode(flowElementEventReader.flowElementId());

        final BpmnAspect bpmnAspect = flowElementVisitor.aspectFor(flowElementEventReader.event());
        final BpmnFlowElementEventHandler handler = flowElementEventHandlers.get(bpmnAspect.value());

        System.out.println("Handling event of type " + flowElementEventReader.event());

        handler.handle(flowElementEventReader, process, logWriter, idGenerator);
    }

    protected void handleBpmnProcessEvent(BpmnProcessEventReader processEventReader)
    {
        final ProcessGraph process = processCache.getProcessGraphByTypeId(processEventReader.processId());

        // TODO: move to process node; how to find it? Do we need to store the node id (!= process id) in the process event?
        flowElementVisitor.init(process).moveToNode(0);

        final BpmnAspect bpmnAspect = flowElementVisitor.aspectFor(processEventReader.event());
        final BpmnProcessEventHandler handler = processEventHandlers.get(bpmnAspect.value());

        System.out.println("Handling event of type " + processEventReader.event());

        handler.handle(processEventReader, process, logWriter, idGenerator);
    }

    protected void handleBpmnActivityEvent(BpmnActivityEventReader activityEventReader)
    {
        final ProcessGraph process = processCache.getProcessGraphByTypeId(activityEventReader.processId());

        flowElementVisitor.init(process).moveToNode(activityEventReader.flowElementId());

        final BpmnAspect bpmnAspect = flowElementVisitor.aspectFor(activityEventReader.event());
        final BpmnActivityInstanceEventHandler handler = activityEventHandlers.get(bpmnAspect.value());

        System.out.println("Handling event of type " + activityEventReader.event());

        handler.handle(activityEventReader, process, logWriter, idGenerator);
    }

    public void setEventReader(BpmnEventReader eventReader)
    {
        this.eventReader = eventReader;
    }

    public void setFlowElementVisitor(FlowElementVisitor flowElementVisitor)
    {
        this.flowElementVisitor = flowElementVisitor;
    }
}
