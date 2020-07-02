/**
 * 
 */
package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSValidationError {


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
