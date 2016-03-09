/**
 * 
 */
package com.sos.JSHelper.Exceptions;

import com.sos.JSHelper.Basics.SOSMsgJsh;

/** @author KB */
public class ParametersMissingButRequiredException extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6325645157747517913L;

    public ParametersMissingButRequiredException(final String pstrApplSystem, final String pstrApplDocuUrl) {
        super(new SOSMsgJsh("SOSVfs_E_278").getFullMessage(pstrApplSystem, pstrApplDocuUrl));
        this.Status(JobSchedulerException.ERROR);
        this.Category(CategoryOptions);
        this.Typ(TypeOptionMissing);
        this.eMailSubject(this.getMessage());
        System.err.println(this.ExceptionText());
        // this.printStackTrace(System.err);
    }
}
