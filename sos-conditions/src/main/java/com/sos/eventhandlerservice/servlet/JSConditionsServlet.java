package com.sos.eventhandlerservice.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.classes.event.EventHandlerSettings;

public class JSConditionsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionsServlet.class);
    private JobSchedulerConditionsEventHandler eventHandler;

    public JSConditionsServlet() {
        super();
    }

    public void init() throws ServletException {
        final String METHOD = "init";
        LOGGER.info(METHOD);
        logThreadInfo();
        String host = "localhost";
        String port = "40444";
        Path hibernateFile = Paths.get("xxxx");
       // String configDir = baseDir + schedulerId + "/config";

        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setHttpHost(host);
        settings.setHttpPort(port);
        settings.setHibernateConfigurationReporting(hibernateFile);
        
        eventHandler = new JobSchedulerConditionsEventHandler();
        eventHandler.setIdentifier("JobSchedulerCondtions");
        try {
            eventHandler.onPrepare(settings);
            eventHandler.onActivate(null);
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

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("REQUEST....");
    }

    public void destroy() {
        LOGGER.info("destroy");
        eventHandler.close();
        eventHandler.awaitEnd();
    }

    private void logThreadInfo() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        String jvmName = runtimeBean.getName();
        long pid = Long.valueOf(jvmName.split("@")[0]);
        int peakThreadCount = bean.getPeakThreadCount();

        LOGGER.info("JVM Name = " + jvmName);
        LOGGER.info("JVM PID  = " + pid);
        LOGGER.info("Peak Thread Count = " + peakThreadCount);
    }

    private JSConditionsSettings getSettings() throws Exception {
        String method = "getSettings";

        JSConditionsSettings jsConditionsSettings = new JSConditionsSettings();
        String conditionsConfiguration = getInitParameter("conditions_configuration");

        String jettyBase = System.getProperty("jetty.base");

        Path pathToConfigurationFile;

        if (conditionsConfiguration.contains("..")) {
            pathToConfigurationFile = Paths.get(jettyBase, conditionsConfiguration);
        } else {
            pathToConfigurationFile = Paths.get(conditionsConfiguration);
        }

        String canonicalPathToConfigurationFile = pathToConfigurationFile.toFile().getCanonicalPath();
        LOGGER.info(String.format("[%s][order_configuration][%s]%s", method, pathToConfigurationFile, canonicalPathToConfigurationFile));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(canonicalPathToConfigurationFile)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the order configuration: %s", method, canonicalPathToConfigurationFile, ex
                    .toString()), ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));

        jsConditionsSettings.setJobschedulerUrl(conf.getProperty("jobscheduler_url"));
        String hibernateConfiguration = conf.getProperty("hibernate_configuration").trim();
        Path hibernateConfigurationFileName;
        if (hibernateConfiguration.contains("..")) {
            hibernateConfigurationFileName = Paths.get(jettyBase, hibernateConfiguration);
        } else {
            hibernateConfigurationFileName = Paths.get(hibernateConfiguration);
        }

        jsConditionsSettings.setHibernateConfigurationFile(hibernateConfigurationFileName);

        return jsConditionsSettings;

    }

}
