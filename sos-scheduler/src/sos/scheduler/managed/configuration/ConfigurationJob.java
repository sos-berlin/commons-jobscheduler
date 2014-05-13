package sos.scheduler.managed.configuration;


import java.util.Properties;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Job_impl;
import sos.util.SOSArguments;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;

/**
 *<p>ConfigurationJob is a base class for order jobs with extended node configurations.</p>  
 * <p>was: ProcessJobs</p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 
 */

public class ConfigurationJob extends Job_impl {

    
    /** Protokollierung */
    private SOSSchedulerLogger sosLogger = null;
    
    /** Verbindung zur Datenbank */
    private SOSConnection sosConnection = null;
    
    /** Einstellungen aus der Datenbank */
    private SOSConnectionSettings connectionSettings = null; 
    
    /** Einstellungen des Jobs */
    private SOSSettings jobSettings = null;

    /** Job-Einstellungen */
    private Properties jobProperties = null;

    /** Attribut jobId: ID der Task im Scheduler */
    private int jobId = 0;

    /** Attribut jobName: Job-Name im Scheduler */
    private String jobName = null;

    /** Attribut jobTitle: Job-Title im Scheduler */
    private String jobTitle = null;

    /** Attribut application: Name der Applikation für Einstellungen */
    protected String application = new String("");
    
    
    /**
     * Initialisierung
     * 
     * @see sos.spooler.Job_impl#spooler_init()
     */
    public boolean spooler_init() {
        
        try {
            boolean rc = super.spooler_init();
            if (!rc) return false;
            this.setLogger(new SOSSchedulerLogger(spooler_log));            

            try { // to initialize database connection
                
                this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
                this.setJobProperties(this.jobSettings.getSection("spooler"));

                if (this.getJobProperties().isEmpty()) 
                    throw new Exception("no settings found in section [spooler] of configuration file: " + spooler.ini_path());

                if (this.getJobProperties().getProperty("db") == null || this.getJobProperties().getProperty("db").length() == 0) 
                    throw new Exception("no settings found for entry [db] in section [spooler] of configuration file: " + spooler.ini_path());
                
                if (this.getJobProperties().getProperty("db_class") == null || this.getJobProperties().getProperty("db_class").length() == 0) 
                    throw new Exception("no settings found for entry [db_class] in section [spooler] of configuration file: " + spooler.ini_path());
                
                if (this.getLogger() != null) sosLogger.debug6("connecting to database...");

                this.setConnection(getSchedulerConnection(this.getJobSettings(), this.getLogger()));
                this.getConnection().connect();
                
                this.setConnectionSettings(new SOSConnectionSettings(this.getConnection(), "SETTINGS", this.getLogger()));
                
                if (this.getLogger() != null) this.getLogger().debug6("..successfully connected to Job Scheduler database.");
            }
            catch (Exception e) {
                spooler_log.info("connect to database failed: " + e.getMessage());
                spooler_log.info("running without database...");
            }

            if (spooler_job!=null) setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
            
            if (spooler_task != null) this.setJobId(spooler_task.id());
            if (spooler_job != null) this.setJobName(spooler_job.name());
            if (spooler_job != null) this.setJobTitle(spooler_job.title());
            this.getSettings();
            
            return true;
        }
        catch (Exception e) {
            spooler_log.error(e.getMessage());
            return false;
        }
    }
    
    
    /**
     * Cleanup
     * 
     * @see sos.spooler.Job_impl#spooler_exit()
     */
    public void spooler_exit() {

        try {
            try { // to close the database connection
                if (this.getConnection() != null) {
                    spooler_log.debug6("spooler_exit(): disconnecting.. ..");
                    this.getConnection().disconnect();
                    this.setConnection(null);
                }
            }
            catch (Exception e) {
                spooler_log.warn("spooler_exit(): disconnect failed: " + e.toString());
            }

            spooler_log.info("Job " + this.getJobName() + " terminated.");
        } catch (Exception e) {} // no errror processing at job level
    }
    
    
    /**
     * @return Returns the jobSettings.
     */
    public SOSSettings getJobSettings() {
        return this.jobSettings;
    }
    
    
    /**
     * @param jobSettings
     *            The jobSettings to set.
     */
    public void setJobSettings(SOSSettings jobSettings) {
        this.jobSettings = jobSettings;
    }
    
    
    /**
     * @return Returns the sosConnection.
     */
    public SOSConnection getConnection() {
        return this.sosConnection;
    }
    
    
    /**
     * @param sosConnection
     *            The sosConnection to set.
     */
    public void setConnection(SOSConnection sosConnection) {
        this.sosConnection = sosConnection;
    }
    
    
    /**
     * @return Returns the sosLogger.
     */
    public SOSSchedulerLogger getLogger() {
        return sosLogger;
    }
    
    
    /**
     * @param sosLogger
     *            The sosLogger to set.
     */
    public void setLogger(SOSSchedulerLogger sosLogger) {
        this.sosLogger = sosLogger;
    }
    
    
    /**
     * @return Returns the jobProperties.
     */
    public Properties getJobProperties() {
        return this.jobProperties;
    }
    
    
    /**
     * @param jobProperties
     *            The jobProperties to set.
     */
    public void setJobProperties(Properties jobProperties) {
        this.jobProperties = jobProperties;
    }

    
    /**
     * @return Returns the connectionSettings.
     */
    public SOSConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    
    
    /**
     * @param connectionSettings The connectionSettings to set.
     */
    public void setConnectionSettings(SOSConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }
    
    
    /**
     * @return Returns the jobId.
     */
    protected int getJobId() {
        return jobId;
    }

    
    /**
     * @param jobId The jobId to set.
     */
    protected void setJobId(int jobId) {
        this.jobId = jobId;
    }

    
    /**
     * @return Returns the jobName.
     */
    protected String getJobName() {
        return jobName;
    }

    
    /**
     * @param jobName The jobName to set.
     */
    protected void setJobName(String jobName) {
        this.jobName = jobName;
    }

    
    /**
     * @return Returns the jobTitle.
     */
    protected String getJobTitle() {
        return jobTitle;
    }

    
    /**
     * @param jobTitle The jobTitle to set.
     */
    protected void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    
    /**
     * @param application The application to set.
     */
    protected void setApplication(String application) {
        this.application = application;
    }

    
    /**
     * @return Returns the application.
     */
    protected String getApplication() {
        return application;
    }

    
    /**
     * Gibt die Settings des Job Schedulers als SOSSettings Objekt zurück
     * @param factoryIni Pfad zur Datei factory.ini (wird von der Methode ini_path()
     * des spooler Objekts geliefert)
     * @throws Exception
     */
    public static SOSSettings getSchedulerSettings(String factoryIni) throws Exception{
        SOSSettings schedulerSettings = new SOSProfileSettings(factoryIni);
        return schedulerSettings;
    }
    
    
    /**
     * Gibt ein SOSConnection Objekt mit der Datenbankverbindung des Job Schedulers
     * zurück. Die Verbindung muss noch mit connect() geöffnet und disconnect() wieder
     * geschlossen werden.
     * @param schedulerSettings Settings des Schedulers       
     * @throws Exception
     */
    public static SOSConnection getSchedulerConnection(SOSSettings schedulerSettings) throws Exception{
        return getSchedulerConnection(schedulerSettings, null);
    }
    
    
    /**
     * Gibt ein SOSConnection Objekt mit der Datenbankverbindung des Job Schedulers
     * zurück. Die Verbindung muss mit connect() geöffnet und disconnect() wieder
     * geschlossen werden.
     * 
     * @param schedulerSettings Settings des Schedulers
     * @param log SOSLogger, der von der Datenbankverbindung genutzt werden soll      
     * @throws Exception
     */
    public static SOSConnection getSchedulerConnection(SOSSettings schedulerSettings, SOSLogger log) throws Exception {
        
        String dbProperty = schedulerSettings.getSection("spooler").getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
        dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
        if (dbProperty.endsWith("-password=")) dbProperty=dbProperty.substring(0, dbProperty.length()-10);
        SOSArguments arguments = new SOSArguments(dbProperty);

        SOSConnection conn;
        if (log!=null){
            conn = SOSConnection.createInstance(  
                                            schedulerSettings.getSection("spooler").getProperty("db_class"),
                                            arguments.as_string("-class=", ""),
                                            arguments.as_string("-url=", ""),
                                            arguments.as_string("-user=", ""),
                                            arguments.as_string("-password=", ""),
                                            log );
        } else{
            conn = SOSConnection.createInstance(  
                    schedulerSettings.getSection("spooler").getProperty("db_class"),
                    arguments.as_string("-class=", ""),
                    arguments.as_string("-url=", ""),
                    arguments.as_string("-user=", ""),
                    arguments.as_string("-password=", ""));
        }
        
        return conn;                            
    }
    

    /**
     * Einstellungen lesen
     */
    private boolean getSettings() {
        
        try {
            if (spooler_job == null) return false;
            
            setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));

            if (getJobProperties().isEmpty()) return false;

            return true;

        } catch (Exception e) {
            spooler_log.error(e.getMessage());
            return false;
        }
    }

}
