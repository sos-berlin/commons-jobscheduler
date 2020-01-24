package com.sos.JSHelper.Logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.log4j.SimpleLayout;
import org.junit.Test;

import com.sos.JSHelper.interfaces.IJobSchedulerLoggingAppender;

/** @author KB */
public class JobSchedulerLog4JAppenderTest {

    private static final Logger LOGGER = LogManager.getLogger(JobSchedulerLog4JAppenderTest.class);
    public void testSubAppendLoggingEvent() {
        IJobSchedulerLoggingAppender objJSAppender = null;
//        Appender objStdoutAppender = LOGGER.getAppender("stdout");
//        if (objStdoutAppender instanceof IJobSchedulerLoggingAppender) {
//            objJSAppender = (IJobSchedulerLoggingAppender) objStdoutAppender;
//            LOGGER.info("JobSchedulerLog4JAppender is configured as log4j-appender");
//        }
//        if (objJSAppender == null) {
//            SimpleLayout layout = new SimpleLayout();
//            Appender consoleAppender = new JobSchedulerLog4JAppender(layout);
//            LOGGER.addAppender(consoleAppender);
//            LOGGER.setLevel(Level.DEBUG);
//            LOGGER.debug("Log4J configured programmatically");
//        }
        LOGGER.info("User-Dir : " + System.getProperty("user.dir"));
    }

    @Test
    public void testBufferedLog4jAppender() {
        IJobSchedulerLoggingAppender objJSAppender = null;
//        Appender objStdoutAppender = LOGGER.getAppender("buffered");
//        if (objStdoutAppender instanceof BufferedJobSchedulerLog4JAppender) {
//            objJSAppender = (IJobSchedulerLoggingAppender) objStdoutAppender;
//            LOGGER.info("JobSchedulerLog4JAppender is configured as buffered log4j-appender");
//        }
//        if (objJSAppender == null) {
//            SimpleLayout layout = new SimpleLayout();
//            Appender consoleAppender = new JobSchedulerLog4JAppender(layout);
//            LOGGER.addAppender(consoleAppender);
//            LOGGER.setLevel(Level.DEBUG);
//            LOGGER.debug("Log4J configured programmatically");
//        }
        LOGGER.info("User-Dir : " + System.getProperty("user.dir"));
    }

}