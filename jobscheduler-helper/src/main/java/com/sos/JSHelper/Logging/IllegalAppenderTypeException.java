package com.sos.JSHelper.Logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class IllegalAppenderTypeException extends JobSchedulerException {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

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