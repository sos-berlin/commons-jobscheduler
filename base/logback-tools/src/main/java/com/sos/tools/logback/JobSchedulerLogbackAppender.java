package com.sos.tools.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import sos.spooler.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An appender to hide JobSchedulers spooler_log object behind logback.
 *
 * The log levels used in logback are delegated to the following JobScheduler log levels.
 * <p>
 *     <table>
 *          <tr>
 *             <th width="10" align="left">logback</th>
 *             <th width="10" align="left">JobScheduler</th>
 *             <th width="80"/>
 *         </tr>
 *         <tr>
 *             <td>trace</td>
 *             <td>debug3</td>
 *         </tr>
 *         <tr>
 *             <td>debug</td>
 *             <td>debug</td>
 *         </tr>
 *         <tr>
 *             <td>info</td>
 *             <td>info</td>
 *         </tr>
 *         <tr>
 *             <td>warn</td>
 *             <td>warn</td>
 *         </tr>
 *         <tr>
 *             <td>error</td>
 *             <td>error</td>
 *         </tr>
 *     </table>
 * </p>
 *
 * <p>
 *     <b>
 *      Please note that messages will be logged only if its log level is higher or equal the log level specified
 *      in factory.ini.
 *     </b>
 * </p>
 * @author stefan.schaedlich@sos-berlin.com
 * at 10.05.13 15:40
 */
public class JobSchedulerLogbackAppender<E> extends ConsoleAppender<ILoggingEvent> {

    private Log jobSchedulerLogger = null;
    private ByteArrayOutputStream outputStream;

    @Override
    public void start() {
        super.start();

        // to redirect the console output to an output stream
        outputStream = new ByteArrayOutputStream();
        try {
            encoder.init(outputStream);
        } catch (IOException e) {
            addError("Error to initialize encoder in Appender [" + name + "].");
        }

    }

    @Override
    protected void append(ILoggingEvent event) {

        // The presence of the spooler logger can not be tested in start(), because it is set dynamically.
        if (!hasLogger()) {
            addError("No JobschedulerLogger (spooler_log object) set for the appender named ["+ name +"].");
            return;
        }

        // Apply the encoder an get the formatted message from outputstream.
        String messageText = "";
        try {
            encoder.doEncode(event);
            outputStream.close();
            messageText = outputStream.toString();
            outputStream.reset();
        } catch (IOException e) {
            addError("Error applying the encoder of the appender [" + name + "].");
        }

        // Redirect the message to JobScheduler
        try {
            int level = event.getLevel().toInt();
            switch (level) {
                case Level.TRACE_INT:
                    jobSchedulerLogger.debug3(messageText);
                    break;
                case Level.DEBUG_INT:
                    jobSchedulerLogger.debug(messageText);
                    break;
                case Level.WARN_INT:
                    jobSchedulerLogger.warn(messageText);
                    break;
                case Level.INFO_INT:
                    jobSchedulerLogger.info(messageText);
                    break;
                case Level.ERROR_INT:
                    jobSchedulerLogger.error(messageText);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
        }
    }

    public void setSchedulerLogger(Log schedulerLogger) {
        this.jobSchedulerLogger = schedulerLogger;
    }

    public boolean hasLogger() {
        return (jobSchedulerLogger != null) ? true : false;
    }
}
