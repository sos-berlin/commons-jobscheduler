/**
 * 
 */
package com.sos.JSHelper.Options;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSValidationError {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());

    private String strErrorMessage = "";
    private JobSchedulerException objException = null;

    /**
	 * 
	 */
    public SOSValidationError() {
        this.setErrorMessage("");
    }

    public void setErrorMessage(final String string) {
        strErrorMessage = "";
    }

    public String getErrorMessage() {
        return strErrorMessage;
    }

    public SOSValidationError(final String pstrErrorMessage) {
        strErrorMessage = pstrErrorMessage;
    }

    public JobSchedulerException getException() {
        return objException;
    }

    public void setException(final JobSchedulerException objException) {
        this.objException = objException;
    }
}
