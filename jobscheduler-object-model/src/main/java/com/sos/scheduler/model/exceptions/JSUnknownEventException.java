package com.sos.scheduler.model.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JSUnknownEventException extends JobSchedulerException {

    private static final long serialVersionUID = 162451490637611372L;

    public JSUnknownEventException() {
        //
    }

    public JSUnknownEventException(String pstrMessage) {
        super(pstrMessage);
        // TODO Auto-generated constructor stub
    }

    public JSUnknownEventException(String pstrMessage, Exception e) {
        super(pstrMessage, e);
        // TODO Auto-generated constructor stub
    }
}
