package com.sos.scheduler.model.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author oh */
public class JSCommandOKException extends JobSchedulerException {

    private static final long serialVersionUID = 7094590460479639416L;

    public JSCommandOKException() {
        //
    }

    public JSCommandOKException(String pstrMessage) {
        super(pstrMessage);
        // TODO Auto-generated constructor stub
    }

    public JSCommandOKException(String pstrMessage, Exception e) {
        super(pstrMessage, e);
        // TODO Auto-generated constructor stub
    }
}
