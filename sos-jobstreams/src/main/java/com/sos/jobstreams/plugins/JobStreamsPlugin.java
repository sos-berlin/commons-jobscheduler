package com.sos.jobstreams.plugins;

import javax.inject.Inject;

import com.sos.jitl.eventhandler.plugin.LoopEventHandlerPlugin;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class JobStreamsPlugin extends LoopEventHandlerPlugin {

    private final JobSchedulerJobStreamsEventHandler eventHandler;

    @Inject
    public JobStreamsPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables, EventPublisher eventBus) {
        super(scheduler, xmlCommandExecutor, variables);
        setIdentifier("jobstreams");

        eventHandler = new JobSchedulerJobStreamsEventHandler(xmlCommandExecutor, eventBus);
        eventHandler.setPeriodBegin(getJobSchedulerVariable("sos.jobstream_period_begin"));
        eventHandler.setIdentifier(getIdentifier());
    }

    @Override
    public void onPrepare() {
        // do nothing
    }

    @Override
    public void onActivate() {
        super.onActivate(eventHandler);
    }

}