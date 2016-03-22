package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JobChainNodeAction;

public class JSCmdJobChainNodeModify extends JobChainNodeModify {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdJobChainNodeModify";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdJobChainNodeModify.class);

    public JSCmdJobChainNodeModify(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setJobChainIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJobChain(value);
        }
    }

    public void setStateIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setState(value);
        }
    }

    public void setActionIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            JobChainNodeAction jobChainNodeAction = JobChainNodeAction.fromValue(value);
            super.setAction(jobChainNodeAction);
        }
    }
}
