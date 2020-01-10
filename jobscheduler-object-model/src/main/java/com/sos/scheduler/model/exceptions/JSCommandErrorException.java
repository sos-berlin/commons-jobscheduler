package com.sos.scheduler.model.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author oh */
public class JSCommandErrorException extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6924451147571494210L;
    @SuppressWarnings("unused")
    private final String conClassName = "JSCommandErrorException";

    public JSCommandErrorException() {
        //
    }

    /** \brief JSCommandErrorException
     *
     * \details
     *
     * @param pstrMessage */
    public JSCommandErrorException(String pstrMessage) {
        super(pstrMessage);
        // TODO Auto-generated constructor stub
    }

    /** \brief JSCommandErrorException
     *
     * \details
     *
     * @param pstrMessage
     * @param e */
    public JSCommandErrorException(String pstrMessage, Exception e) {
        super(pstrMessage, e);
        // TODO Auto-generated constructor stub
    }
}
