package com.sos.JSHelper.Logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.nt.NTEventLogAppender;

public class Log4JWindowsNTEvent {

    public static void main(String[] args) {
        PatternLayout myLayout = new PatternLayout("%-4r [%t] %-5p %c %x - %m%n");
        Logger myLogger = Logger.getLogger("Log4JWindowsNTEvent");
        String mySource = "the source";
        NTEventLogAppender eventLogAppender = new NTEventLogAppender(mySource, myLayout);
        ConsoleAppender consoleAppender = new ConsoleAppender(myLayout);
        myLogger.addAppender(consoleAppender);
        myLogger.addAppender(eventLogAppender);
        myLogger.setLevel(Level.INFO);
        myLogger.info("info: I have died of thirst");
        myLogger.warn("warn: I have died of thirst");
        myLogger.error("error: I have died of thirst");
        myLogger.fatal("fatal: I have died of thirst");
    }

}