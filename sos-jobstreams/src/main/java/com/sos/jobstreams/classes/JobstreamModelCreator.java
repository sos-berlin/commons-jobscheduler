package com.sos.jobstreams.classes;

import java.sql.SQLException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jobstreams.resolver.JSConditionResolver;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JobstreamModelCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobstreamModelCreator.class);
    private EventHandlerSettings eventHandlerSettings;
    private SchedulerXmlCommandExecutor schedulerXmlCommandExecutor;
    private JobstreamModelCreatorThread jobstreamModelCreatorThread;
    private String identifier;

    public JobstreamModelCreator(EventHandlerSettings eventHandlerSettings, SchedulerXmlCommandExecutor schedulerXmlCommandExecutor) {
        super();
        this.eventHandlerSettings = eventHandlerSettings;
        this.schedulerXmlCommandExecutor = schedulerXmlCommandExecutor;
    }

    static class JobstreamModelCreatorThread extends Thread {

        private SOSHibernateFactory reportingFactory;
        private EventHandlerSettings eventHandlerSettings;
        private SchedulerXmlCommandExecutor schedulerXmlCommandExecutor;
        public JSConditionResolver result;
        private String identifier;

        public JobstreamModelCreatorThread(SOSHibernateFactory reportingFactory, EventHandlerSettings eventHandlerSettings,
                SchedulerXmlCommandExecutor schedulerXmlCommandExecutor) {
            super();
            this.reportingFactory = reportingFactory;
            this.eventHandlerSettings = eventHandlerSettings;
            this.schedulerXmlCommandExecutor = schedulerXmlCommandExecutor;
        }

        @Override
        public void run() {
            MDC.put("plugin", identifier);

            LOGGER.debug("start initialization of jobstream model." + Constants.testDelay);
            JSConditionResolver j = new JSConditionResolver(schedulerXmlCommandExecutor, eventHandlerSettings);
            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = reportingFactory.openStatelessSession("JobstreamModelCreatorThread");
                j.reInitPartial(sosHibernateSession);
                if (Constants.testDelay == null) {
                    LOGGER.debug("testDelay is null.");
                    sleep(10000);
                } else {
                    sleep(Constants.testDelay * 1000);
                }
                LOGGER.debug("new model is now available");
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    LOGGER.info("Init closed");
                } else {
                    LOGGER.error(e.getMessage(), e);
                }
                j = null;
            } finally {
                if (sosHibernateSession != null) {
                    sosHibernateSession.close();
                }
            }

            result = j;
        }

        public void setIdentifier(String _identifier) {
            this.identifier = _identifier;

        }
    }

    public void createResolver(SOSHibernateFactory reportingFactory) {
        LOGGER.debug("create new jobstream model....");
        if (jobstreamModelCreatorThread != null) {
            LOGGER.debug("interrupting running thread");

            jobstreamModelCreatorThread.result = null;
            jobstreamModelCreatorThread.interrupt();
        }
        jobstreamModelCreatorThread = new JobstreamModelCreatorThread(reportingFactory, eventHandlerSettings, schedulerXmlCommandExecutor);
        jobstreamModelCreatorThread.setIdentifier(identifier);
        LOGGER.debug("start thread");
        jobstreamModelCreatorThread.start();
    }

    public boolean haveNewModel() {
        LOGGER.debug("Checking for new model");
        if (jobstreamModelCreatorThread == null) {
            LOGGER.debug("jobstreamModelCreatorThread is null");
        } else {
            LOGGER.debug("jobstreamModelCreatorThread is not null");
            LOGGER.debug(jobstreamModelCreatorThread.getState().toString());

            if (jobstreamModelCreatorThread.result == null) {
                LOGGER.debug("jobstreamModelCreatorThread.result is null");
            } else {
                LOGGER.debug("jobstreamModelCreatorThread.result is not null");
            }
        }

        return (jobstreamModelCreatorThread != null && jobstreamModelCreatorThread.getState() == Thread.State.TERMINATED
                && jobstreamModelCreatorThread.result != null);
    }

    public void reset() {
        jobstreamModelCreatorThread = null;
    }

    public JSConditionResolver getResult() {
        if (jobstreamModelCreatorThread == null) {
            return null;
        }
        return jobstreamModelCreatorThread.result;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}