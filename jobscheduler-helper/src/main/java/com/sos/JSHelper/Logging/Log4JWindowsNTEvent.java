package com.sos.JSHelper.Logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.nt.NTEventLogAppender;

public class Log4JWindowsNTEvent {

    public static void main(String[] args) {

        // specify pattern for layout
        PatternLayout myLayout = new PatternLayout("%-4r [%t] %-5p %c %x - %m%n");

        // get a logger instance called "Log4JWindowsEvent"
        Logger myLogger = Logger.getLogger("Log4JWindowsNTEvent");

        String mySource = "the source";

        // create an NTEventLogAppender
        NTEventLogAppender eventLogAppender = new NTEventLogAppender(mySource, myLayout);
        // create a console appender
        ConsoleAppender consoleAppender = new ConsoleAppender(myLayout);

        // associate the appenders to the logger
        myLogger.addAppender(consoleAppender);
        myLogger.addAppender(eventLogAppender);

        // log events using the logger object
        // note that since we have associated a
        // level of WARN for our logger, we will not see INFO
        // and DEBUG STATEMENTS
        // remember: DEBUG < INFO < WARN < ERROR < FATAL
        // myLogger.setLevel(Level.WARN);
        myLogger.setLevel(Level.INFO);

        // log a fatal event
        myLogger.info("info: I have died of thirst");
        myLogger.warn("warn: I have died of thirst");
        myLogger.error("error: I have died of thirst");
        myLogger.fatal("fatal: I have died of thirst");

    }
}
