package com.sos.JSHelper.Logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Test;

import com.sos.JSHelper.interfaces.IJobSchedulerLoggingAppender;
import com.sos.scheduler.BufferedJobSchedulerLog4JAppender;
import com.sos.scheduler.JobSchedulerLog4JAppender;

/** @author KB */
public class JobSchedulerLog4JAppenderTest {

    public void testSubAppendLoggingEvent() {
        Logger logger = null;
        logger = Logger.getRootLogger();
        IJobSchedulerLoggingAppender objJSAppender = null;
        Appender objStdoutAppender = logger.getAppender("stdout");
        if (objStdoutAppender instanceof IJobSchedulerLoggingAppender) {
            objJSAppender = (IJobSchedulerLoggingAppender) objStdoutAppender;
            logger.info("JobSchedulerLog4JAppender is configured as log4j-appender");
        }
        if (objJSAppender == null) {
            SimpleLayout layout = new SimpleLayout();
            Appender consoleAppender = new JobSchedulerLog4JAppender(layout);
            logger.addAppender(consoleAppender);
            logger.setLevel(Level.DEBUG);
            logger.debug("Log4J configured programmatically");
        }
        logger.info("User-Dir : " + System.getProperty("user.dir"));
    }

    @Test
    public void testBufferedLog4jAppender() {
        Logger logger = null;
        logger = Logger.getRootLogger();
        IJobSchedulerLoggingAppender objJSAppender = null;
        Appender objStdoutAppender = logger.getAppender("buffered");
        if (objStdoutAppender instanceof BufferedJobSchedulerLog4JAppender) {
            objJSAppender = (IJobSchedulerLoggingAppender) objStdoutAppender;
            logger.info("JobSchedulerLog4JAppender is configured as buffered log4j-appender");
        }
        if (objJSAppender == null) {
            SimpleLayout layout = new SimpleLayout();
            Appender consoleAppender = new JobSchedulerLog4JAppender(layout);
            logger.addAppender(consoleAppender);
            logger.setLevel(Level.DEBUG);
            logger.debug("Log4J configured programmatically");
        }
        logger.info("User-Dir : " + System.getProperty("user.dir"));
    }

}