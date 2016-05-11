package com.sos.JSHelper.Exceptions;

/** @author Rainer Buhl */
public class NoNewDataException extends JobSchedulerException {

    private static final long serialVersionUID = -8636032062881811019L;

    public NoNewDataException(String pstrMessage) {
        super(pstrMessage);
        this.Status(JobSchedulerException.WARNING);
    }

    public NoNewDataException() {
        this("No new Data available");
    }

}