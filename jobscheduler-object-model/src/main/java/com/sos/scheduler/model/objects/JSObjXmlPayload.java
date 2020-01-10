package com.sos.scheduler.model.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjXmlPayload extends XmlPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjXmlPayload.class);

    public JSObjXmlPayload(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        try {
            objFactory = schedulerObjectFactory;
        } catch (Exception e) {
            LOGGER.error("Could not instantiate Order.", e);
            throw new JobSchedulerException("Could not instantiate Payload.", e);
        }
        objFactory = schedulerObjectFactory;
    }

}