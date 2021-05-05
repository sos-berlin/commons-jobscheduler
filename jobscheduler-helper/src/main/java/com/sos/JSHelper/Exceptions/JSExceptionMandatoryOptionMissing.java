package com.sos.JSHelper.Exceptions;

public class JSExceptionMandatoryOptionMissing extends JobSchedulerException {

    private static final long serialVersionUID = 1L;

    public JSExceptionMandatoryOptionMissing(String msg) {
        super(msg);
        message(msg);
    }

    public JSExceptionMandatoryOptionMissing() {
        this("exception 'JSExceptionMandatoryOptionMissing' raised ...");
    }

    public String getExceptionText() {
        return super.getExceptionText();
    }

}