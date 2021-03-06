package com.sos.jobstreams.resolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobstreams.plugins.JobSchedulerJobStreamsEventHandler;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.eventhandler.plugin.notifier.Mailer;
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;

public class ConditionServletTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionServletTest.class);

    public static void main(String[] args) throws Exception {

        final String METHOD = "init";
        System.out.println(ZoneId.systemDefault());

        LOGGER.info(METHOD);
        String host = "localhost";
        String port = "40444";
        Path hibernateFile = Paths.get("src/test/resources/reporting.hibernate.cfg.xml");
        // String configDir = baseDir + schedulerId + "/config";

        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setHttpHost(host);
        settings.setHttpPort(port);

        settings.setHibernateConfigurationReporting(hibernateFile);
        settings.setConfigDirectory(Paths.get("src/test/resources/config"));
        settings.setSchedulerId("scheduler_joc_cockpit");
        settings.setJocUrl("http://localhost:4446");

        JobSchedulerJobStreamsEventHandler eventHandler = new JobSchedulerJobStreamsEventHandler();
        eventHandler.setIdentifier("JobSchedulerConditions");
        try {
            eventHandler.onPrepare(settings);
            HashMap<String,String>mailSettings = new HashMap<String,String>();
            mailSettings.put("to","uwe.risse@sos-berlin.com");
            mailSettings.put("smtp","mail.sos-berlin.com");
            mailSettings.put("from","jobstream@sos-berlin.com");
            mailSettings.put("mail_on_error","1");
            mailSettings.put("mail.smtp.port","25");
            Mailer mailer = new Mailer(eventHandler.getIdentifier(), mailSettings);
            eventHandler.onActivate(new Notifier(mailer,ConditionServletTest.class));
        } catch (Exception e) {
            throw e;
        } finally {
        }
        try {
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", METHOD, e.toString()), e);
            throw new Exception(String.format("[%s]%s", METHOD, e.toString()), e);
        }
    }

}
