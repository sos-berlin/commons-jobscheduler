package com.sos.tools.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sos.spooler.Log;

import java.util.Iterator;

/** Class to modify the JobSchedulerLogbackAppender at runtime.
 *
 * To bind the JobScheduler logger to the JobSchedulerLogbackAppender it is
 * necessary to set the log object in the appender at runtime. To do this the
 * method prepareLogbackAppender() has to be called.
 *
 * To test if JobScheduler installation is configured well to use the Appender
 * use isLogbackConfigured().
 *
 * @author stefan.schaedlich@sos-berlin.com at 18.05.13 16:42 */
public class LogbackHelper {

    private static Logger logger = LoggerFactory.getLogger(LogbackHelper.class);

    private final static String LOGGER_NAME = ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME;

    /** Bind the logger object of JobScheduler to the appender. It is required
     * that the logback configuration has a root logger referencing the
     * JobSchedulerLogbackAppender.
     *
     * @param spooler_log */
    public static void prepareLogbackAppender(Log spooler_log) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        ch.qos.logback.classic.Logger l = lc.getLogger("ROOT");
        if (l == null) {
            spooler_log.warn("No logger [" + LOGGER_NAME + "] defined - redirection to JobScheduler logback not possible.");
        } else {
            JobSchedulerLogbackAppender<?> appender = getAppenderOrNull(l);
            if (appender == null) {
                spooler_log.warn("The logger [" + l.getName() + "] has no appender reference to a JobSchedulerLogbackAppender");
            } else {
                appender.setSchedulerLogger(spooler_log);
                logger.info("Appender {} successfully connected with JobScheduler logger.", appender.getName());
            }
        }
    }

    private static JobSchedulerLogbackAppender<?> getAppenderOrNull(ch.qos.logback.classic.Logger l) {
        JobSchedulerLogbackAppender<?> result = null;
        for (Iterator<Appender<ILoggingEvent>> index = l.iteratorForAppenders(); index.hasNext();) {
            Appender<ILoggingEvent> appender = index.next();
            if (appender instanceof JobSchedulerLogbackAppender) {
                result = (JobSchedulerLogbackAppender<?>) appender;
                break;
            }
        }
        return result;
    }

    /** Test if logback classic and logback core is present in the classpath.
     *
     * @return boolean */
    public static boolean isLogbackConfigured() {
        try {
            Class.forName("ch.qos.logback.classic.LoggerContext");		// runs with
                                                                   // logback
                                                                   // classic ?
        } catch (ClassNotFoundException e) {
            logger.debug("Could not find class 'ch.qos.logback.classic.Logger' - Logback classic is not in classpath.");
            return false;
        }
        try {
            Class.forName("ch.qos.logback.core.Appender");		// runs with logback
                                                           // core ?
        } catch (ClassNotFoundException e) {
            logger.debug("Could not find class 'ch.qos.logback.core.Appender' - Logback core is not in classpath.");
            return false;
        }
        return true;
    }

    public static boolean isLogger(String loggerName) {
        boolean result = false;
        if (isLogbackConfigured()) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
                if (logger.getName().equals(loggerName)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
