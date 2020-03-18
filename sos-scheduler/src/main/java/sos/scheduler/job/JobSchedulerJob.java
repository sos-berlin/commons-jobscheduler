package sos.scheduler.job;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Job;
import sos.spooler.Job_impl;
import sos.spooler.Spooler;
import sos.spooler.Task;
import sos.spooler.Variable_set;
import sos.util.SOSArguments;
import sos.util.SOSString;

/** @author Andreas Liebert */
public class JobSchedulerJob extends Job_impl {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJob.class);

    public static final String HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER = "hibernate.cfg.xml";
    public static final String HIBERNATE_DEFAULT_FILE_NAME_REPORTING = "reporting.hibernate.cfg.xml";

    public static final String SCHEDULER_PARAM_PROXY_URL = "sos.proxy_url";
    public static final String SCHEDULER_PARAM_HIBERNATE_SCHEDULER = "sos.hibernate_configuration_scheduler";
    public static final String SCHEDULER_PARAM_HIBERNATE_REPORTING = "sos.hibernate_configuration_reporting";
    public static final String SCHEDULER_PARAM_USE_NOTIFICATION = "sos.use_notification";

    private SOSConnection sosConnection = null;
    private SOSConnectionSettings connectionSettings = null;
    private SOSSettings jobSettings = null;
    private Properties jobProperties = null;
    private String application = new String("");
    private String taskJobName = null;
    private String jobName = null;
    private String jobFolder = null;
    private String jobTitle = null;
    private int jobId = 0;

    @Override
    public boolean spooler_init() {
        try {
            boolean rc = super.spooler_init();
            if (!rc) {
                return false;
            }
            if (spooler_task != null) {
                setJobId(spooler_task.id());
            }
            if (spooler_job != null) {
                String jobName = spooler_job.name();

                setJobName(jobName);
                setJobFolder(spooler_job.folder_path());
                setJobTitle(spooler_job.title());

                setSettings(jobName);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    @Override
    public void spooler_exit() {
        try {
            try {
                if (sosConnection != null) {
                    spooler_log.debug6("spooler_exit(): disconnecting.. ..");
                    sosConnection.disconnect();
                    sosConnection = null;
                }
            } catch (Exception e) {
                sosConnection = null;
                spooler_log.warn("spooler_exit(): disconnect failed: " + e.toString());
                LOGGER.warn(e.toString(), e);
            }
            spooler_log.info("Job " + getJobName() + " terminated.");
        } catch (Exception e) {
            // no error processing at job level
        }
    }

    private SOSConnection connectToJSDataBase() {
        try {
            boolean isUniversalAgent = false;
            try {
                setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            } catch (Exception e) {
                isUniversalAgent = true;
            }
            if (!isUniversalAgent) {
                setJobProperties(jobSettings.getSection("spooler"));
                if (getJobProperties().isEmpty()) {
                    throw new JobSchedulerException("no settings found in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (getJobProperties().getProperty("db") == null || this.getJobProperties().getProperty("db").isEmpty()) {
                    throw new JobSchedulerException("no settings found for entry [db] in section [spooler] of configuration file: " + spooler
                            .ini_path());
                }
                if (getJobProperties().getProperty("db_class") == null || this.getJobProperties().getProperty("db_class").isEmpty()) {
                    throw new JobSchedulerException("no settings found for entry [db_class] in section [spooler] of configuration file: " + spooler
                            .ini_path());
                }
                LOGGER.debug("connecting to database...");

                setConnection(getSchedulerConnection(this.getJobSettings()));
                getConnection().connect();
                setConnectionSettings(new SOSConnectionSettings(this.getConnection(), "SETTINGS"));
                LOGGER.debug("..successfully connected to JobScheduler database.");

            }
        } catch (Exception e) {
            spooler_log.info("connect to database failed: ");
            spooler_log.info("running without database...");
            LOGGER.info(e.getMessage(), e);
        }
        return sosConnection;
    }

    private void setConnection(final SOSConnection val) {
        if (sosConnection != null && !sosConnection.equals(val)) {
            try {
                sosConnection.disconnect();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            sosConnection = null;
        }
        sosConnection = val;
    }

    public SOSConnection getConnection() {
        if (sosConnection == null) {
            connectToJSDataBase();
        }
        return sosConnection;
    }

    public SOSSettings getJobSettings() {
        return jobSettings;
    }

    public void setJobSettings(final SOSSettings val) {
        jobSettings = val;
    }

    public Properties getJobProperties() {
        return jobProperties;
    }

    public void setJobProperties(final Properties val) {
        jobProperties = val;
    }

    public SOSConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public void setConnectionSettings(final SOSConnectionSettings val) {
        connectionSettings = val;
    }

    protected int getJobId() {
        return jobId;
    }

    protected void setJobId(final int val) {
        jobId = val;
    }

    protected String getJobName() {
        return jobName;
    }

    protected void setJobName(final String val) {
        jobName = val.replaceFirst(".*/([^/]+)$", "$1");
    }

    protected String getJobFolder() {
        return jobFolder;
    }

    protected void setJobFolder(final String val) {
        jobFolder = val;
    }

    protected String getJobTitle() {
        if (jobTitle == null) {
            jobTitle = spooler_task.job().title();
        }
        return jobTitle;
    }

    protected void setJobTitle(final String val) {
        jobTitle = val;
    }

    protected void setApplication(final String val) {
        application = val;
    }

    protected String getApplication() {
        return application;
    }

    public String getTaskJobName() {
        if (taskJobName == null && spooler_task != null) {
            Job job = spooler_task.job();
            if (job != null) {
                taskJobName = job.name();
            }
        }
        return taskJobName;
    }

    public static SOSSettings getSchedulerSettings(final String factoryIni) throws Exception {
        SOSSettings schedulerSettings = new SOSProfileSettings(factoryIni);
        return schedulerSettings;
    }

    public static SOSConnection getSchedulerConnection(final SOSSettings schedulerSettings) throws Exception {
        String dbProperty = schedulerSettings.getSection("spooler").getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
        dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
        if (dbProperty.endsWith("-password=")) {
            dbProperty = dbProperty.substring(0, dbProperty.length() - 10);
        }
        SOSArguments arguments = new SOSArguments(dbProperty);
        return SOSConnection.createInstance(schedulerSettings.getSection("spooler").getProperty("db_class"), arguments.asString("-class=", ""),
                arguments.asString("-url=", ""), arguments.asString("-user=", ""), arguments.asString("-password=", ""));
    }

    public Path getHibernateConfigurationScheduler() {
        return getHibernateConfigurationScheduler(spooler);
    }

    public static Path getHibernateConfigurationScheduler(Spooler spooler) {
        Variable_set vs = spooler.variables();
        if (vs != null) {
            String var = vs.value(SCHEDULER_PARAM_HIBERNATE_SCHEDULER);
            if (!SOSString.isEmpty(var)) {
                return Paths.get(var);
            }
        }
        return Paths.get(spooler.directory() + "/config").resolve(HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER);
    }

    public Path getHibernateConfigurationReporting() {
        return getHibernateConfigurationReporting(spooler, spooler_task);
    }

    public static Path getHibernateConfigurationReporting(Spooler spooler, Task task) {
        Path configDir = null;
        boolean isAgent = !SOSString.isEmpty(task.agent_url());
        if (isAgent) {
            configDir = Paths.get(System.getenv("SCHEDULER_DATA")).resolve("config");
        } else {
            Variable_set vs = spooler.variables();
            if (vs != null) {
                String var = vs.value(SCHEDULER_PARAM_HIBERNATE_REPORTING);
                if (!SOSString.isEmpty(var)) {
                    return Paths.get(var);
                }
            }
            configDir = Paths.get(spooler.directory() + "/config");
        }
        Path configFile = configDir.resolve(HIBERNATE_DEFAULT_FILE_NAME_REPORTING);
        if (Files.exists(configFile)) {
            return configFile;
        }
        if (isAgent) {
            throw new JobSchedulerException("no hibernate configuration file found on agent file system!");
        }
        return getHibernateConfigurationScheduler(spooler);
    }

    private void setSettings(String jobName) {
        try {
            Optional<File> schedulerIniFile = Optional.empty();
            try {
                schedulerIniFile = Optional.of(new File(spooler.ini_path()));
            } catch (Exception e) {
                // no error handling here
            }

            if (!schedulerIniFile.isPresent() || !schedulerIniFile.get().canRead()) {
                spooler_log.debug("No ini file available. Continuing without settings.");
                return;
            }
            setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            setJobProperties(getJobSettings().getSection("job " + jobName));
            if (getJobProperties().isEmpty()) {
                return;
            }
            if (getJobProperties().getProperty("delay_after_error") != null) {
                String[] delays = getJobProperties().getProperty("delay_after_error").toString().split(";");
                if (delays.length > 0) {
                    spooler_job.clear_delay_after_error();
                }
                for (String delay2 : delays) {
                    String[] delay = delay2.split(":");
                    spooler_job.set_delay_after_error(Integer.parseInt(delay[0]), delay[1]);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
}