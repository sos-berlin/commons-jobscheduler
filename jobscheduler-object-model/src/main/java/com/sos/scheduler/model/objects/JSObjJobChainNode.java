package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JobChain.JobChainNode;

/** @author oh */
public class JSObjJobChainNode extends JobChainNode {

    public JSObjJobChainNode(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
