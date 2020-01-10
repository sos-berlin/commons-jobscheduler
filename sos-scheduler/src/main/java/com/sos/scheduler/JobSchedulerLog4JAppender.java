package com.sos.scheduler;

import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.spi.LoggingEvent;

import com.sos.JSHelper.interfaces.IJobSchedulerLoggingAppender;

import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;

/** @author KB */
public class JobSchedulerLog4JAppender /* extends ConsoleAppender*/ implements IJobSchedulerLoggingAppender {

    private static final String CLASSNAME = "JobSchedulerLog4JAppender";
    private SOSSchedulerLogger sosLogger = null;

    public JobSchedulerLog4JAppender() {
        super();
    }

    public JobSchedulerLog4JAppender(final Layout pobjLayout) {
//        super(pobjLayout);
    }

    public void setSchedulerLogger(final SOSSchedulerLogger pobjSchedulerLogger) {
        sosLogger = pobjSchedulerLogger;
    }

    @Override
    public void activateOptions() {
//        super.activateOptions();
    }

//    @Override
//    protected void subAppend(final LoggingEvent event) {
//        if (!hasLogger()) {
//            super.subAppend(event);
//        } else {
//            String strMsg = getLayout().format(event);
//
//            Level lL = event.getLevel();
//            int intL = lL.toInt();
//            int intSOSLevel = 0;
//            switch (intL) {
//            case Level.DEBUG_INT:
//            case Level.TRACE_INT:
//                intSOSLevel = SOSLogger.DEBUG;
//                break;
//            case Level.WARN_INT:
//                intSOSLevel = SOSLogger.WARN;
//                break;
//            case Level.INFO_INT:
//                intSOSLevel = SOSLogger.INFO;
//                break;
//            case Level.ERROR_INT:
//                intSOSLevel = SOSLogger.ERROR;
//                break;
//            case Level.FATAL_INT:
//                intSOSLevel = SOSLogger.ERROR;
//                break;
//            default:
//                intSOSLevel = SOSLogger.INFO;
//                break;
//            }
//            if (!strMsg.trim().isEmpty() && isAllowedLogger(event)) {
//                if (hasLogger()) {
//                    sosLogger.log(intSOSLevel, strMsg);
//                } else {
//                    System.out.print(CLASSNAME + " (system.out): " + strMsg);
//                }
//            }
//        }
//    }

//    public static boolean isAllowedLogger(LoggingEvent event) {
//        if (event.getLoggerName().startsWith("com.sos.scheduler.engine.")) {
//            return false;
//        }
//        return true;
//    }

    @Override
    public boolean hasLogger() {
        return sosLogger != null;
    }

}