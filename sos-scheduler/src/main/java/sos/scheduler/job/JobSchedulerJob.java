package sos.scheduler.job;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Job_impl;
import sos.spooler.Spooler;
import sos.spooler.Task;
import sos.spooler.Variable_set;
import sos.util.SOSArguments;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author Andreas Liebert */
public class JobSchedulerJob extends Job_impl {

    public static final String HIBERNATE_DEFAULT_FILE_NAME_SCHEDULER = "hibernate.cfg.xml";
    public static final String HIBERNATE_DEFAULT_FILE_NAME_REPORTING = "reporting.hibernate.cfg.xml";

    public static final String SCHEDULER_PARAM_PROXY_URL = "sos.proxy_url";
    public static final String SCHEDULER_PARAM_HIBERNATE_SCHEDULER = "sos.hibernate_configuration_scheduler";
    public static final String SCHEDULER_PARAM_HIBERNATE_REPORTING = "sos.hibernate_configuration_reporting";
    public static final String SCHEDULER_PARAM_USE_NOTIFICATION = "sos.use_notification";

    protected String application = new String("");
    protected SOSSchedulerLogger sosLogger = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJob.class);
    private SOSConnection sosConnection = null;
    private SOSConnectionSettings connectionSettings = null;
    private SOSSettings jobSettings = null;
    private Properties jobProperties = null;
    private int jobId = 0;
    private String jobName = null;
    private String jobFolder = null;
    private String jobTitle = null;

    public SOSConnection connectToJSDataBase() {
        try {
            boolean isUniversalAgent = false;
            try {
                this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            } catch (Exception e) {
                isUniversalAgent = true;
            }
            if (!isUniversalAgent) {
                this.setJobProperties(jobSettings.getSection("spooler"));
                if (this.getJobProperties().isEmpty()) {
                    throw new JobSchedulerException("no settings found in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (this.getJobProperties().getProperty("db") == null || this.getJobProperties().getProperty("db").isEmpty()) {
                    throw new JobSchedulerException("no settings found for entry [db] in section [spooler] of configuration file: " + spooler
                            .ini_path());
                }
                if (this.getJobProperties().getProperty("db_class") == null || this.getJobProperties().getProperty("db_class").isEmpty()) {
                    throw new JobSchedulerException("no settings found for entry [db_class] in section [spooler] of configuration file: " + spooler
                            .ini_path());
                }
                if (this.getLogger() != null) {
                    sosLogger.debug6("connecting to database...");
                } else {
                    LOGGER.debug("connecting to database...");
                }
                this.setConnection(getSchedulerConnection(this.getJobSettings()));
                this.getConnection().connect();
                this.setConnectionSettings(new SOSConnectionSettings(this.getConnection(), "SETTINGS"));
                if (this.getLogger() != null) {
                    this.getLogger().debug6("..successfully connected to JobScheduler database.");
                } else {
                    LOGGER.debug("..successfully connected to JobScheduler database.");
                }
            }
        } catch (Exception e) {
            spooler_log.info("connect to database failed: ");
            spooler_log.info("running without database...");
            LOGGER.info(e.getMessage(), e);
        }
        return sosConnection;
    }

    @Override
    public boolean spooler_init() {
        try {
            boolean rc = super.spooler_init();
            if (!rc) {
                return false;
            }
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            if (spooler_job != null && getJobSettings() != null) {
                setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
            }
            if (spooler_task != null) {
                this.setJobId(spooler_task.id());
            }
            if (spooler_job != null) {
                this.setJobName(spooler_job.name());
            }
            if (spooler_job != null) {
                this.setJobFolder(spooler_job.folder_path());
            }
            if (spooler_job != null) {
                this.setJobTitle(spooler_job.title());
            }
            this.getSettings();
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
                spooler_log.warn("spooler_exit(): disconnect failed: ");
                LOGGER.warn(e.getMessage(), e);
            }
            spooler_log.info("Job " + this.getJobName() + " terminated.");
        } catch (Exception e) {
            // no error processing at job level
        }
    }

    public SOSSettings getJobSettings() {
        return jobSettings;
    }

    public void setJobSettings(final SOSSettings jobSettings) {
        this.jobSettings = jobSettings;
    }

    public SOSConnection getConnection() {
        if (sosConnection == null) {
            connectToJSDataBase();
        }
        return sosConnection;
    }

    public void setConnection(final SOSConnection psosConnection) {
        if (sosConnection != null && !sosConnection.equals(psosConnection)) {
            try {
                sosConnection.disconnect();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            sosConnection = null;
        }
        sosConnection = psosConnection;
    }

    public SOSSchedulerLogger getLogger() {
        return sosLogger;
    }

    public void setLogger(final SOSSchedulerLogger sosLogger1) {
        sosLogger = sosLogger1;
    }

    public Properties getJobProperties() {
        return jobProperties;
    }

    public void setJobProperties(final Properties jobProperties) {
        this.jobProperties = jobProperties;
    }

    public SOSConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public void setConnectionSettings(final SOSConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    protected int getJobId() {
        return jobId;
    }

    protected void setJobId(final int jobId) {
        this.jobId = jobId;
    }

    protected String getJobName() {
        return jobName;
    }

    protected void setJobName(final String jobName) {
        this.jobName = jobName.replaceFirst(".*/([^/]+)$", "$1");
    }

    protected String getJobFolder() {
        return jobFolder;
    }

    protected void setJobFolder(final String jobFolder) {
        this.jobFolder = jobFolder;
    }

    protected String getJobTitle() {
        if (jobTitle == null) {
            jobTitle = spooler_task.job().title();
        }
        return jobTitle;
    }

    protected void setJobTitle(final String jobTitle) {
        this.jobTitle = jobTitle;
    }

    protected void setApplication(final String application) {
        this.application = application;
    }

    protected String getApplication() {
        return application;
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

    private boolean getSettings() {
        try {

            if (spooler_job == null) {
                return false;
            }
            Optional<File> schedulerIniFile = Optional.empty();
            try {
                schedulerIniFile = Optional.of(new File(spooler.ini_path()));
            } catch (Exception e) {
                // no error handling here
            }

            if (!schedulerIniFile.isPresent() || !schedulerIniFile.get().canRead()) {
                spooler_log.debug("No ini file available. Continuing without settings.");
                return false;
            }
            setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
            if (getJobProperties().isEmpty()) {
                return false;
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
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    protected URL createURL(final String fileName) throws Exception {
        URL url = null;
        try {
            url = new URL(fileName);
        } catch (MalformedURLException ex) {
            try {
                File f = new File(fileName);
                String path = f.getCanonicalPath();
                if (fileName.startsWith("/")) {
                    path = fileName;
                }
                String fs = System.getProperty("file.separator");
                if (fs.length() == 1) {
                    char sep = fs.charAt(0);
                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }
                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }
                if (!path.startsWith("file://")) {
                    path = "file://" + path;
                }
                url = new URL(path);
            } catch (MalformedURLException e) {
                throw new Exception("error in createURL(): " + e.getMessage());
            }
        }
        return url;
    }

    protected URI createURI(final String fileName) throws Exception {
        URI uri = null;
        try {
            uri = new URI(fileName);
        } catch (Exception e) {
            try {
                File f = new File(fileName);
                String path = f.getCanonicalPath();
                if (fileName.startsWith("/")) {
                    path = fileName;
                }
                String fs = System.getProperty("file.separator");
                if (fs.length() == 1) {
                    char sep = fs.charAt(0);
                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }
                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }
                if (!path.startsWith("file://")) {
                    path = "file://" + path;
                }
                uri = new URI(path);
            } catch (Exception ex) {
                throw new Exception("error in createURI(): " + e.getMessage(), e);
            }
        }
        return uri;
    }

    protected File createFile(final String fileName) throws Exception {
        try {
            if (fileName == null || fileName.isEmpty()) {
                throw new Exception("empty file name provided");
            }
            if (fileName.startsWith("file://")) {
                return new File(createURI(fileName));
            } else {
                return new File(fileName);
            }
        } catch (Exception e) {
            throw new Exception("error in createFile() [" + fileName + "]: " + e.getMessage(), e);
        }
    }

}