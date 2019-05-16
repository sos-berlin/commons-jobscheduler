package com.sos.eventhandlerservice.servlet;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.classes.ConditionCustomEvent;
import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.JobSchedulerEvent;
import com.sos.eventhandlerservice.classes.TaskEndEvent;
import com.sos.eventhandlerservice.db.FilterConsumedInConditions;
import com.sos.eventhandlerservice.resolver.JSConditionResolver;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventType;
import com.sos.jitl.classes.event.JobSchedulerPluginEventHandler;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.reporting.db.DBLayer;

public class JobSchedulerConditionsEventHandler extends JobSchedulerPluginEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerConditionsEventHandler.class);
    private SOSHibernateFactory reportingFactory;
    private int waitInterval = 2;
    JSConditionResolver conditionResolver;

    public JobSchedulerConditionsEventHandler() {
        super();
    }

    @Override
    public void onActivate(PluginMailer mailer) {
        super.onActivate(mailer);

        String method = "onActivate";
        SOSHibernateSession reportingSession = null;
        try {
            createReportingFactory(getSettings().getHibernateConfigurationReporting());

            reportingSession = reportingFactory.openStatelessSession();
            File f = new File("src/test/resources/config/private/private.conf");
            conditionResolver = new JSConditionResolver(reportingSession, f);
            conditionResolver.init();

            EventType[] observedEventTypes = new EventType[] { EventType.TaskEnded, EventType.VariablesCustomEvent };
            start(observedEventTypes);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (reportingSession != null) {
                reportingSession.close();
            }
            wait(waitInterval);
        }
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        String method = "onEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

        // execute(false, eventId, null);
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = "onNonEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

        execute(true, eventId, events);
    }

    @Override
    public void onEnded() {
        closeRestApiClient();
        closeReportingFactory();
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        LOGGER.debug(String.format("%s: onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        boolean resolveInConditions = false;

        try {
            for (JsonValue entry : events) {
                if (entry != null) {
                    System.out.println(entry.toString());
                    JobSchedulerEvent jobSchedulerEvent = new JobSchedulerEvent((JsonObject) entry);
                    switch (jobSchedulerEvent.getType()) {
                    case "TaskEnded":
                        TaskEndEvent taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        conditionResolver.resolveOutConditions(taskEndEvent, "scheduler_joc_cockpit", taskEndEvent.getJobPath());
                        resolveInConditions = true;
                        break;

                    case "VariablesCustomEvent":
                        // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}
                        ConditionCustomEvent customEvent = new ConditionCustomEvent((JsonObject) entry);
                        switch (customEvent.getKey()) {
                        case "CheckConditions":
                            resolveInConditions = true;
                        case "InitConditionResolver":
                            conditionResolver.reInit();
                            resolveInConditions = true;
                            break;
                        case "ResetConditionResolver":

                            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                            filterConsumedInConditions.setSession("now");
                            filterConsumedInConditions.setWorkflow(customEvent.getWorkflow());
                            filterConsumedInConditions.setJob(customEvent.getJob());

                            conditionResolver.removeConsumedInconditions(filterConsumedInConditions);
                            break;
                        }
                        break;
                    }
                }
            }
            if (resolveInConditions) {

                final long timeStart = System.currentTimeMillis();
                List<String> listOfWorkflows = conditionResolver.resolveInConditions();
                for (String workflow : listOfWorkflows) {
                    notifyJoc(workflow);
                }
                final long timeEnd = System.currentTimeMillis();
                System.out.println("Resolving all InConditions: " + (timeEnd - timeStart) + " ms.");
            }
        } catch (

        Exception e) {
            e.printStackTrace();
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        }

        wait(waitInterval);

    }

    private void closeReportingFactory() {
        if (reportingFactory != null) {
            reportingFactory.close();
            reportingFactory = null;
        }
    }

    private void notifyJoc(String workflow) {
        // TODO: CustomEvent schicken.
    }

    private void createReportingFactory(Path configFile) throws Exception {
        reportingFactory = new SOSHibernateFactory(configFile);
        reportingFactory.setIdentifier("reporting");
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
        reportingFactory.addClassMapping(Constants.getConditionsClassMapping());
        reportingFactory.build();
    }

}
