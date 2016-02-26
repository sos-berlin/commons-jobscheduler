package sos.scheduler.managed;

import java.util.Properties;

import org.apache.log4j.Logger;

import sos.connection.SOSConnection;
import sos.marshalling.SOSExport;
import sos.settings.SOSProfileSettings;
import sos.util.SOSArguments;
import sos.util.SOSStandardLogger;

/** @author Andreas Liebert */
public class ManagedJobExport {

    private static final Logger LOGGER = Logger.getLogger(ManagedJobExport.class);
    private static SOSConnection conn;
    private static SOSStandardLogger sosLogger = null;

    public static void main(String[] args) {
        if (args.length == 0 || "-?".equals(args[0]) || "-h".equals(args[0])) {
            showUsage();
            System.exit(0);
        }
        try {
            SOSArguments arguments = new SOSArguments(args);
            String xmlFile = "";
            String logFile = "";
            int logLevel = 0;
            String settingsFile = "";
            int jobID = 0;
            try {
                xmlFile = arguments.as_string("-file=", "job_export.xml");
                logLevel = arguments.as_int("-v=", SOSStandardLogger.INFO);
                logFile = arguments.as_string("-log=", "");
                jobID = arguments.as_int("-job=");
                settingsFile = arguments.as_string("-settings=", "../config/factory.ini");
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
                showUsage();
                System.exit(0);
            }
            if (logFile.length() > 0) {
                sosLogger = new SOSStandardLogger(logFile, logLevel);
            } else {
                sosLogger = new SOSStandardLogger(logLevel);
            }
            getDBConnection(settingsFile);
            conn.connect();
            arguments.check_all_used();
            export(xmlFile, jobID);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

    protected static SOSConnection getDBConnection(String settingsFile) throws Exception {
        SOSProfileSettings settings = new SOSProfileSettings(settingsFile);
        Properties props = settings.getSection("spooler");
        if (props.isEmpty()) {
            throw new Exception("no settings found in section [spooler] of configuration file: " + settingsFile);
        }
        if (props.getProperty("db") == null || props.getProperty("db").isEmpty()) {
            throw new Exception("no settings found for entry [db] in section [spooler] of configuration file: " + settingsFile);
        }
        if (props.getProperty("db_class") == null || props.getProperty("db_class").isEmpty()) {
            throw new Exception("no settings found for entry [db_class] in section [spooler] of configuration file: " + settingsFile);
        }
        if (sosLogger != null) {
            sosLogger.debug6("connecting to database.. .");
        }
        String dbProperty = props.getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
        dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
        SOSArguments dbArguments = new SOSArguments(dbProperty);
        conn = SOSConnection.createInstance(props.getProperty("db_class"), dbArguments.as_string("-class=", ""), dbArguments.as_string("-url=", ""), dbArguments.as_string("-user=", ""), dbArguments.as_string("-password=", ""), sosLogger);
        return conn;
    }

    public static void showUsage() {
        LOGGER.info("usage:ManagedJobExport ");
        LOGGER.info("Argumente:");
        LOGGER.info("     -job=            ID des zu kopierenden jobs");
        LOGGER.info("     -v=              Loglevel (optional)");
        LOGGER.info("     -log=            LogDatei (optional)");
        LOGGER.info("     -settings=       factory.ini Datei (default:../config/factory.ini)");
        LOGGER.info("     -file=           Exportdatei (default:job_export.xml)");
    }

    private static void export(String xmlFile, int jobID) throws Exception {
        String selManagedJobs = "SELECT * FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"ID\"=" + jobID;
        String selJobTypes = "SELECT * FROM " + JobSchedulerManagedObject.getTableManagedJobTypes() + " WHERE \"TYPE\"='?'";
        String selSettings = "SELECT * FROM " + JobSchedulerManagedObject.getTableSettings()
                + " WHERE \"APPLICATION\" IN ('job_type/local/?', 'job_type/global/?', 'job_type/mixed/?')";
        SOSExport export = new SOSExport(conn, xmlFile, "DOCUMENT", sosLogger);
        int job = export.query(JobSchedulerManagedObject.getTableManagedJobs(), "ID", selManagedJobs);
        int jobTypes = export.query(JobSchedulerManagedObject.getTableManagedJobTypes(), "TYPE", selJobTypes, "JOB_TYPE", job);
        int settings = export.query(JobSchedulerManagedObject.getTableSettings(), "APPLICATION,SECTION,NAME", selSettings, "TYPE,TYPE,TYPE", jobTypes);
        export.doExport();
    }

    public static SOSStandardLogger getSosLogger() {
        return sosLogger;
    }

    public static void setSosLogger(SOSStandardLogger sosLogger) {
        ManagedJobExport.sosLogger = sosLogger;
    }
    
}
