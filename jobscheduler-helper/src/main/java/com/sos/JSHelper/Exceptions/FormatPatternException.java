package com.sos.JSHelper.Exceptions;

public class FormatPatternException extends JobSchedulerException {

    private static final long serialVersionUID = -5301651332260978502L;

    public FormatPatternException(String pstrMessage) {
        super(pstrMessage);
        this.message(pstrMessage);
    }

    public FormatPatternException() {
        this("exception 'FormatPatternException' raised ...");
    }

    @Override
    public String getExceptionText() {
        return super.getExceptionText();
    }

}