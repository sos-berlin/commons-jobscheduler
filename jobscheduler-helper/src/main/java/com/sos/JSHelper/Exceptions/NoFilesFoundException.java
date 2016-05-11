package com.sos.JSHelper.Exceptions;

/** @author Ghassan Beydoun */
public class NoFilesFoundException extends JobSchedulerException {

    private static final long serialVersionUID = 1203178426931970420L;

    public NoFilesFoundException(String pstrMessage) {
        super(pstrMessage);
        this.Status(JobSchedulerException.PENDING);
    }

    public NoFilesFoundException() {
        super("No Files found on the server.");
    }

}