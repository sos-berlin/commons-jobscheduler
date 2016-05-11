package com.sos.JSHelper.Exceptions;

public class FormatPatternException extends JobSchedulerException {

    private static final long serialVersionUID = -5301651332260978502L;

    public FormatPatternException(String pstrMessage) {
        super(pstrMessage);
        this.Message(pstrMessage);
        this.Status(JobSchedulerException.PENDING);
    }

    public FormatPatternException() {
        this("exception 'FormatPatternException' raised ...");
    }

    @Override
    public String ExceptionText() {
        return super.ExceptionText();
    }

}