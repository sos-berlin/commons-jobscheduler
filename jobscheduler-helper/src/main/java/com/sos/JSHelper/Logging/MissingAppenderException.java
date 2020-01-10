package com.sos.JSHelper.Logging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class MissingAppenderException extends JobSchedulerException {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    public MissingAppenderException(String pstrMessage) {
        super(pstrMessage);
        this.message(pstrMessage);
        this.setStatus(JobSchedulerException.PENDING);
        LOGGER.error(this.getExceptionText());
    }

    public MissingAppenderException() {
        this("exception 'MissingAppenderException' raised ...");
        LOGGER.error(this.getExceptionText());
    }

    public String getExceptionText() {
        return super.getExceptionText();
    }

}