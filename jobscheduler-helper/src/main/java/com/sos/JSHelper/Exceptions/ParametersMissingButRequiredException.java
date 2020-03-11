package com.sos.JSHelper.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.SOSMsgJsh;

public class ParametersMissingButRequiredException extends JobSchedulerException {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParametersMissingButRequiredException.class);
    private static final long serialVersionUID = -6325645157747517913L;

    public ParametersMissingButRequiredException(final String pstrApplSystem, final String pstrApplDocuUrl) {
        super(new SOSMsgJsh("SOSVfs_E_278").getFullMessage(pstrApplSystem, pstrApplDocuUrl));
        LOGGER.error(getExceptionText());
    }

}