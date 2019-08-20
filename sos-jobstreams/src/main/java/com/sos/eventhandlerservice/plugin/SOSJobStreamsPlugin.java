package com.sos.eventhandlerservice.plugin;

import javax.inject.Inject;

import com.sos.jitl.classes.plugin.JobSchedulerEventPlugin;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class SOSJobStreamsPlugin extends JobSchedulerEventPlugin {

    private final JobSchedulerJobStreamsEventHandler eventHandler;

    @Inject
    public SOSJobStreamsPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables, EventPublisher eventBus) {
        super(scheduler, xmlCommandExecutor, variables);
        setIdentifier("jobstreams");

        eventHandler = new JobSchedulerJobStreamsEventHandler(xmlCommandExecutor, eventBus);
        eventHandler.setIdentifier(getIdentifier());
    }

    @Override
    public void onPrepare() {
        // do nothing
    }

    @Override
    public void onActivate() {
        super.executeOnActivate(eventHandler);
    }

    @Override
    public void close() {
        super.executeClose(eventHandler);
    }

}