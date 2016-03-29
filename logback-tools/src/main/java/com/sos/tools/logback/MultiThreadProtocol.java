package com.sos.tools.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.base.Strings;
import com.sos.tools.logback.db.LoggingEventDBItem;
import com.sos.tools.logback.db.LoggingEventPropertyDBLayer;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/** Class for logging of multiple threads / JVM in one file
 *
 * This class works with the MDC feature of logback combined with the
 * SiftingAppender to write to log files with concurrent threads.
 *
 * The logfiles will be created by using a fileIndicator in its names:
 * 
 * <pre>
 * {@code
 * <appender name="SIFT-JOBNET" class="ch.qos.logback.classic.sift.SiftingAppender">
 *    <discriminator>
 *      <key>fileIndicator</key>
 *      <defaultValue>unknown</defaultValue>
 *    </discriminator>
 *    <sift>
 *      <appender name="JOBNET-${fileIndicator}" class="ch.qos.logback.core.FileAppender">
 *        <file>${user.dir}/logs/jobnet-${fileIndicator}.log</file>
 *        <prudent>true</prudent>
 *        <append>true</append>
 *        <layout class="ch.qos.logback.classic.PatternLayout">
 *          <pattern>%d [%class:%line] [${itemIndicator}] %level %mdc %logger{35} - %message%n</pattern>
 *        </layout>
 *      </appender>
 *    </sift>
 *  </appender>
 * }
 * </pre>
 *
 * If the application is configured with a different logging api than this class
 * will log a warning and the protocol file will NOT be created.
 *
 * The distinction of the log files will be realized with the first parameter
 * <i>fileIndicator</i>. The second parameter <i>itemIndicator</i> can be used
 * in the layout pattern to identify log entries for a specific item of the log.
 *
 * The logger which is specified by the third parameter <i>loggerName</i> must
 * be present in your logback configuration, too:
 * 
 * <pre>
 * {@code
 * <logger name="SOSJobnet" level="TRACE" additivity="false">
 *   <appender-ref ref="SIFT-JOBNET" />
 * </logger>
 * }
 * </pre>
 *
 * @author stefan schaedlich */
public class MultiThreadProtocol implements org.slf4j.Logger {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MultiThreadProtocol.class);
    private Logger threadLogger;
    private final String item;
    private final String itemIndicator;

    public MultiThreadProtocol(String fileIndicator, String item, String loggerName) {
        this.item = item;
        this.itemIndicator = getItemIndicator(fileIndicator, item);
        if (LogbackHelper.isLogger(loggerName)) {
            this.threadLogger = (Logger) LoggerFactory.getLogger(loggerName);
            SiftingAppender appender = hasSiftingappender(threadLogger);
            if (appender != null) {
                logger.info("The appender for the jobnet protocol is " + appender.getName());
                MDC.put("fileIndicator", fileIndicator);
                MDC.put("item", item);
                MDC.put("itemIndicator", itemIndicator);
            } else {
                logger.warn("No sifting appender is configured in logback configuration - no protocol file for fileIndicator=" + fileIndicator
                        + ", item=" + item + " will be created.");
            }
        } else {
            logger.warn("Logger '" + loggerName + "' not defined - no logback protocol will be created.");
        }
    }

    private SiftingAppender hasSiftingappender(Logger logger) {
        SiftingAppender result = null;
        for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
            Appender<ILoggingEvent> appender = index.next();
            if (appender instanceof SiftingAppender) {
                result = (SiftingAppender) appender;
                break;
            }
        }
        return result;
    }

    private String formatMessage(String message) {
        return "[" + Strings.padEnd(item, 35, ' ') + "] " + message;
    }

    private static String getItemIndicator(String forUUID, String forItem) {
        return forItem + "." + forUUID;
    }

    public static String getProtocolAsText(File configurationFile, String forUUID, String forItem, String forLogger) {
        LoggingEventPropertyDBLayer loggingDBLayer = new LoggingEventPropertyDBLayer(configurationFile);
        String itemIndicator = getItemIndicator(forUUID, forItem);
        List<LoggingEventDBItem> records = loggingDBLayer.getProtocol("itemIndicator", itemIndicator, forLogger);
        String result = loggingDBLayer.asText(records);
        return result;
    }

    public static String getProtocolAsText(File configurationFile, String forUUID, String forLogger) {
        LoggingEventPropertyDBLayer loggingDBLayer = new LoggingEventPropertyDBLayer(configurationFile);
        List<LoggingEventDBItem> records = loggingDBLayer.getProtocol("fileIndicator", forUUID, forLogger);
        String result = loggingDBLayer.asText(records);
        return result;
    }

    public org.slf4j.Logger getLogger() {
        return threadLogger;
    }

    @Override
    public String getName() {
        return threadLogger != null ? threadLogger.getName() : "";
    }

    @Override
    public boolean isTraceEnabled() {
        return threadLogger != null ? threadLogger.isTraceEnabled() : false;
    }

    @Override
    public void trace(String message) {
        if (threadLogger != null) {
            threadLogger.trace(formatMessage(message));
        }
    }

    @Override
    public void trace(String message, Throwable error) {
        if (threadLogger != null) {
            threadLogger.trace(formatMessage(message), error);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return threadLogger != null ? threadLogger.isTraceEnabled(marker) : false;
    }

    @Override
    public void trace(Marker marker, String message) {
        if (threadLogger != null) {
            threadLogger.trace(marker, formatMessage(message));
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.trace(marker, format, arg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.trace(marker, format, arg1, arg2);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.trace(marker, format, argArray);
        }
    }

    @Override
    public void trace(Marker marker, String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.trace(marker, formatMessage(message), t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return threadLogger != null ? threadLogger.isDebugEnabled() : false;
    }

    @Override
    public void trace(String format, Object obj1) {
        if (threadLogger != null) {
            threadLogger.trace(format, obj1);
        }
    }

    @Override
    public void trace(String format, Object obj1, Object obj2) {
        if (threadLogger != null) {
            threadLogger.trace(format, obj1, obj2);
        }
    }

    @Override
    public void trace(String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.trace(format, argArray);
        }
    }

    @Override
    public void error(String message) {
        if (threadLogger != null) {
            threadLogger.error(formatMessage(message));
        }
    }

    @Override
    public void error(String message, Throwable error) {
        if (threadLogger != null) {
            threadLogger.error(formatMessage(message), error);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return threadLogger != null ? threadLogger.isErrorEnabled(marker) : false;
    }

    @Override
    public void error(Marker marker, String message) {
        if (threadLogger != null) {
            threadLogger.error(marker, formatMessage(message));
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.error(marker, format, arg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.error(marker, format, arg1, arg2);
        }
    }

    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.error(marker, format, argArray);
        }
    }

    @Override
    public void error(Marker marker, String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.error(marker, formatMessage(message), t);
        }
    }

    @Override
    public void error(String format, Object obj1) {
        if (threadLogger != null) {
            threadLogger.error(format, obj1);
        }
    }

    @Override
    public void error(String format, Object obj1, Object obj2) {
        if (threadLogger != null) {
            threadLogger.error(format, obj1, obj2);
        }
    }

    @Override
    public void error(String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.error(format, argArray);
        }
    }

    @Override
    public void debug(String message) {
        if (threadLogger != null) {
            threadLogger.debug(formatMessage(message));
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.debug(format, arg);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.debug(format, arg1, arg2);
        }
    }

    @Override
    public void debug(String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.debug(format, argArray);
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.debug(formatMessage(message), t);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return threadLogger != null ? threadLogger.isDebugEnabled() : false;
    }

    @Override
    public void debug(Marker marker, String message) {
        if (threadLogger != null) {
            threadLogger.debug(marker, formatMessage(message));
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.debug(marker, format, arg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.debug(marker, format, arg1, arg2);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.debug(marker, format, argArray);
        }
    }

    @Override
    public void debug(Marker marker, String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.debug(marker, formatMessage(message), t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return threadLogger != null ? threadLogger.isInfoEnabled() : false;
    }

    @Override
    public void info(String message) {
        if (threadLogger != null) {
            threadLogger.info(formatMessage(message));
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.info(format, arg);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.info(format, arg1, arg2);
        }
    }

    @Override
    public void info(String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.info(format, argArray);
        }
    }

    @Override
    public void info(String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.info(formatMessage(message), t);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return threadLogger != null ? threadLogger.isInfoEnabled(marker) : false;
    }

    @Override
    public void info(Marker marker, String message) {
        if (threadLogger != null) {
            threadLogger.info(marker, formatMessage(message));
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.info(marker, format, arg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.info(marker, format, arg1, arg2);
        }
    }

    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.info(marker, format, argArray);
        }
    }

    @Override
    public void info(Marker marker, String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.info(marker, formatMessage(message), t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return threadLogger != null ? threadLogger.isWarnEnabled() : false;
    }

    @Override
    public void warn(String message) {
        if (threadLogger != null) {
            threadLogger.warn(formatMessage(message));
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.warn(format, arg);
        }
    }

    @Override
    public void warn(String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.warn(format, argArray);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.warn(format, arg1, arg2);
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.warn(formatMessage(message), t);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return threadLogger != null ? threadLogger.isWarnEnabled(marker) : false;
    }

    @Override
    public void warn(Marker marker, String message) {
        if (threadLogger != null) {
            threadLogger.warn(marker, formatMessage(message));
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (threadLogger != null) {
            threadLogger.warn(marker, format, arg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (threadLogger != null) {
            threadLogger.warn(marker, format, arg1, arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        if (threadLogger != null) {
            threadLogger.warn(marker, format, argArray);
        }
    }

    @Override
    public void warn(Marker marker, String message, Throwable t) {
        if (threadLogger != null) {
            threadLogger.warn(marker, formatMessage(message), t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return threadLogger != null ? threadLogger.isErrorEnabled() : false;
    }

}
