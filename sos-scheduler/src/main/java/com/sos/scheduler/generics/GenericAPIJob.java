package com.sos.scheduler.generics;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

public class GenericAPIJob extends JSJobUtilitiesClass<GenericAPIJobOptions> {

    private static final Logger LOGGER = Logger.getLogger(GenericAPIJob.class);

    public GenericAPIJob() {
        super(new GenericAPIJobOptions());
    }

    @Override
    public GenericAPIJobOptions Options() {
        if (objOptions == null) {
            objOptions = new GenericAPIJobOptions();
        }
        return objOptions;
    }

    public GenericAPIJob Execute() throws Exception {
        final String conMethodName = "GenericAPIJob::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().toString());
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
        }
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }


}