package com.sos.JSHelper.Options;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author KB */
public class SOSOptionTimeTest {

    private final String conClassName = "SOSOptionTimeTest";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionTimeTest.class);
    private SOSOptionTime objTime = null;

    @Before
    public void setUp() throws Exception {
        objTime = new SOSOptionTime(null, conClassName, conClassName, conClassName, conClassName, false);
    }

    @Test
    public final void testGetTimeAsSeconds() {
        objTime.setValue("10");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.setValue("1:50");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.setValue("1:30:45");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.setValue("13045");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.setValue("48:30:45");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.setValue("99:99:99");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.value(17);
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
    }

}