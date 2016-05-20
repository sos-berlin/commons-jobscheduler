package com.sos.JSHelper.Exceptions;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.SOSMsgJsh;

/** @author KB */
public class ParametersMissingButRequiredException extends JobSchedulerException {

    private static final Logger LOGGER = Logger.getLogger(ParametersMissingButRequiredException.class);
    private static final long serialVersionUID = -6325645157747517913L;

    public ParametersMissingButRequiredException(final String pstrApplSystem, final String pstrApplDocuUrl) {
        super(new SOSMsgJsh("SOSVfs_E_278").getFullMessage(pstrApplSystem, pstrApplDocuUrl));
        this.setStatus(JobSchedulerException.ERROR);
        this.setCategory(CategoryOptions);
        this.setType(TypeOptionMissing);
        this.eMailSubject(this.getMessage());
        LOGGER.error(this.getExceptionText());
    }

}