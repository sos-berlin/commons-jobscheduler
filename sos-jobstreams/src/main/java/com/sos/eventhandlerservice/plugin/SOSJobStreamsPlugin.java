package com.sos.eventhandlerservice.plugin;

import javax.inject.Inject;

import com.sos.eventhandlerservice.servlet.JobSchedulerConditionsEventHandler;
import com.sos.jitl.classes.plugin.JobSchedulerEventPlugin;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class SOSJobStreamsPlugin extends JobSchedulerEventPlugin {

    private final JobSchedulerConditionsEventHandler eventHandler;

    @Inject
    public SOSJobStreamsPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables, EventPublisher eventBus) {
        super(scheduler, xmlCommandExecutor, variables);
        setIdentifier("conditions");

        eventHandler = new JobSchedulerConditionsEventHandler(xmlCommandExecutor, eventBus);
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