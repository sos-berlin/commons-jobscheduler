package sos.scheduler.process;

import java.util.Properties;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Job_impl;
import sos.util.SOSArguments;
import sos.util.SOSLogger;

/** @author andreas pueschel
 * @deprecated use sos.scheduler.managed.configuration.ConfigurationJob */
@Deprecated
public class ProcessJob extends Job_impl {

    protected String application = new String("");
    private SOSConnection sosConnection = null;
    private SOSConnectionSettings connectionSettings = null;
    private SOSSettings jobSettings = null;
    private Properties jobProperties = null;
    private int jobId = 0;
    private String jobName = null;
    private String jobTitle = null;

    @Override
    public boolean spooler_init() {
        try {
            boolean rc = super.spooler_init();
            if (!rc) {
                return false;
            }
            try {
                this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
                this.setJobProperties(jobSettings.getSection("spooler"));
                if (this.getJobProperties().isEmpty()) {
                    throw new Exception("no settings found in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (this.getJobProperties().getProperty("db") == null || this.getJobProperties().getProperty("db").isEmpty()) {
                    throw new Exception("no settings found for entry [db] in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (this.getJobProperties().getProperty("db_class") == null || this.getJobProperties().getProperty("db_class").isEmpty()) {
                    throw new Exception("no settings found for entry [db_class] in section [spooler] of configuration file: " + spooler.ini_path());
                }
                spooler_log.debug6("connecting to database...");
                this.setConnection(getSchedulerConnection(this.getJobSettings()));
                this.getConnection().connect();
                this.setConnectionSettings(new SOSConnectionSettings(this.getConnection(), "SETTINGS"));
                spooler_log.debug6("..successfully connected to Job Scheduler database.");
            } catch (Exception e) {
                spooler_log.info("connect to database failed: " + e.getMessage());
                spooler_log.info("running without database...");
            }
            if (spooler_job != null) {
                setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
            }
            if (spooler_task != null) {
                this.setJobId(spooler_task.id());
            }
            if (spooler_job != null) {
                this.setJobName(spooler_job.name());
            }
            if (spooler_job != null) {
                this.setJobTitle(spooler_job.title());
            }
            this.getSettings();
            return true;
        } catch (Exception e) {
            spooler_log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public void spooler_exit() {
        try {
            try {
                if (this.getConnection() != null) {
                    spooler_log.debug6("spooler_exit(): disconnecting.. ..");
                    this.getConnection().disconnect();
                    this.setConnection(null);
                }
            } catch (Exception e) {
                spooler_log.warn("spooler_exit(): disconnect failed: " + e.toString());
            }
            spooler_log.info("Job " + this.getJobName() + " terminated.");
        } catch (Exception e) {
            // no errror processing at job level
        }
    }

    public SOSSettings getJobSettings() {
        return jobSettings;
    }

    public void setJobSettings(final SOSSettings jobSettings) {
        this.jobSettings = jobSettings;
    }

    public SOSConnection getConnection() {
        return sosConnection;
    }

    public void setConnection(final SOSConnection sosConnection) {
        this.sosConnection = sosConnection;
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
        this.jobName = jobName;
    }

    protected String getJobTitle() {
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
        return getSchedulerConnection(schedulerSettings, null);
    }

    public static SOSConnection getSchedulerConnection(final SOSSettings schedulerSettings, final SOSLogger log) throws Exception {
        String dbProperty = schedulerSettings.getSection("spooler").getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
        dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
        if (dbProperty.endsWith("-password=")) {
            dbProperty = dbProperty.substring(0, dbProperty.length() - 10);
        }
        SOSArguments arguments = new SOSArguments(dbProperty);
        SOSConnection conn;
        return SOSConnection.createInstance(schedulerSettings.getSection("spooler").getProperty("db_class"), arguments.asString("-class=", ""),
                arguments.asString("-url=", ""), arguments.asString("-user=", ""), arguments.asString("-password=", ""));
    }

    private boolean getSettings() {
        try {
            if (spooler_job == null) {
                return false;
            }
            setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
            if (getJobProperties().isEmpty()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            spooler_log.error(e.getMessage());
            return false;
        }
    }

}