package com.sos.JSHelper.Options;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/** @author KB */
public class SOSOptionTimeTest {

    private final String conClassName = "SOSOptionTimeTest";
    private static final Logger LOGGER = Logger.getLogger(SOSOptionTimeTest.class);
    private SOSOptionTime objTime = null;

    @Before
    public void setUp() throws Exception {
        objTime = new SOSOptionTime(null, conClassName, conClassName, conClassName, conClassName, false);
    }

    @Test
    public final void testGetTimeAsSeconds() {
        objTime.Value("10");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.Value("1:50");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.Value("1:30:45");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.Value("13045");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.Value("48:30:45");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.Value("99:99:99");
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
        objTime.value(17);
        LOGGER.info("time in seconds : " + objTime.getTimeAsSeconds());
    }

}