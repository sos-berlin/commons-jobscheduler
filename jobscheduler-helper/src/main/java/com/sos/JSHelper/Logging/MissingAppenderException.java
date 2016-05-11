package com.sos.JSHelper.Logging;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class MissingAppenderException extends JobSchedulerException {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getRootLogger();

    public MissingAppenderException(String pstrMessage) {
        super(pstrMessage);
        this.Message(pstrMessage);
        this.Status(JobSchedulerException.PENDING);
        LOGGER.error(this.ExceptionText());
    }

    public MissingAppenderException() {
        this("exception 'MissingAppenderException' raised ...");
        LOGGER.error(this.ExceptionText());
    }

    public String ExceptionText() {
        return super.ExceptionText();
    }

}