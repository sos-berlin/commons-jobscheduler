package com.sos.scheduler;

import java.util.ArrayList;

//import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.core.Layout;

import sos.util.SOSSchedulerLogger;

/** @author SS */
public class BufferedJobSchedulerLog4JAppender extends JobSchedulerLog4JAppender {

//    private ArrayList<LoggingEvent> logEvents;

    public BufferedJobSchedulerLog4JAppender() {
        super();
    }

    public BufferedJobSchedulerLog4JAppender(Layout pobjLayout) {
        super(pobjLayout);
    }

    @Override
    public void setSchedulerLogger(SOSSchedulerLogger pobjSchedulerLogger) {
        super.setSchedulerLogger(pobjSchedulerLogger);
//        for (int i = 0; i < logEvents.size(); i++) {
//            super.subAppend(logEvents.get(i));
//        }
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
//        logEvents = new ArrayList<LoggingEvent>();
    }

//    @Override
//    protected void subAppend(LoggingEvent event) {
//        if (hasLogger()) {
//            super.subAppend(event);
//        } else {
//            logEvents.add(event);
//        }
//    }

}