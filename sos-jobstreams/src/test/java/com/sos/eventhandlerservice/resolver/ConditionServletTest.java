package com.sos.eventhandlerservice.resolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.servlet.JobSchedulerConditionsEventHandler;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.plugin.PluginMailer;

public class ConditionServletTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionServletTest.class);

    public static void main(String[] args) throws Exception {

        final String METHOD = "init";
        LOGGER.info(METHOD);
        String host = "localhost";
        String port = "40444";
        Path hibernateFile = Paths.get("D:\\documents\\sos-berlin.com\\scheduler_joc_cockpit\\config\\reporting.hibernate.cfg.xml");
        // String configDir = baseDir + schedulerId + "/config";

        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setHttpHost(host);
        settings.setHttpPort(port);
        settings.setHibernateConfigurationReporting(hibernateFile);
        settings.setConfigDirectory(Paths.get("src/test/resources/config"));
        settings.setSchedulerId("scheduler_joc_cockpit");
        settings.setJocUrl("http://localhost:4446");

        JobSchedulerConditionsEventHandler eventHandler = new JobSchedulerConditionsEventHandler();
        eventHandler.setIdentifier("JobSchedulerConditions");
        try {
            eventHandler.onPrepare(settings);
            PluginMailer mailer = new PluginMailer(eventHandler.getIdentifier(), new HashMap<>());
            eventHandler.onActivate(mailer);
        } catch (Exception e) {
            throw e;
        } finally {
        }
        try {
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", METHOD, e.toString()), e);
            throw new ServletException(String.format("[%s]%s", METHOD, e.toString()), e);
        }
    }

}
