package com.sos.JSHelper.Exceptions;

/** @author kb */
public class JSExceptionMandatoryOptionMissing extends JobSchedulerException {

    private static final long serialVersionUID = 1L;

    public JSExceptionMandatoryOptionMissing(String pstrMessage) {
        super(pstrMessage);
        this.message(pstrMessage);
        this.setStatus(JobSchedulerException.PENDING);
        this.setCategory(CategoryOptions);
        this.setType(TypeOptionMissing);
        this.eMailSubject("Mandatory Option missing.");
    }

    public JSExceptionMandatoryOptionMissing() {
        this("exception 'JSExceptionMandatoryOptionMissing' raised ...");
    }

    public String getExceptionText() {
        return super.getExceptionText();
    }

}