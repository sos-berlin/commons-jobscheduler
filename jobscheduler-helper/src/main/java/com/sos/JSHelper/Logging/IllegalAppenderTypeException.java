package com.sos.JSHelper.Logging;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class IllegalAppenderTypeException extends JobSchedulerException {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getRootLogger();

    public IllegalAppenderTypeException(String pstrMessage) {
        super(pstrMessage);
        this.message(pstrMessage);
        this.setStatus(JobSchedulerException.PENDING);
        LOGGER.error(this.getExceptionText());
    }

    public IllegalAppenderTypeException() {
        this("exception 'IllegalAppenderTypeException' raised ...");
        LOGGER.error(this.getExceptionText());
    }

    public String getExceptionText() {
        return super.getExceptionText();
    }

}