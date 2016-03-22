package sos.scheduler.managed;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.connection.SOSConnection;
import sos.marshalling.SOSImport;
import sos.settings.SOSConnectionSettings;
import sos.util.SOSArguments;
import sos.util.SOSStandardLogger;

/** @author Andreas Liebert */
public class ManagedJobChainImport extends SOSImport {

    private static final Logger LOGGER = Logger.getLogger(ManagedJobChainImport.class);
    private static SOSConnection conn;
    private static SOSStandardLogger sosLogger = null;
    private int workflow = -1;
    private boolean jobExists = false;
    private boolean modelExists = true;
    private String modelId = "";

    public ManagedJobChainImport(SOSConnection conn, String file_name, String package_id, String package_element, String package_value, SOSStandardLogger log) {
        super(conn, file_name, package_id, package_element, package_value, log);
    }

    public static void showUsage() {
        LOGGER.info("usage:ManagedJobChainImport ");
        LOGGER.info("Argumente:");
        LOGGER.info("     -file=           Importdatei");
        LOGGER.info("     -v=              Loglevel (optional)");
        LOGGER.info("     -log=            LogDatei (optional)");
        LOGGER.info("     -settings=       factory.ini Datei (default:../config/factory.ini)");
        LOGGER.info("     -package=        zu importierende Pakete (package[+package[+...]] default: alle)");
        LOGGER.info("     -jobchain=       zu importierende jochains (jobchain[+jobchain[+...]] default: alle)");
    }

    public static void main(String[] args) {
        if (args.length == 0 || "-?".equals(args[0]) || "/?".equals(args[0]) || "-h".equals(args[0])) {
            showUsage();
            System.exit(0);
        }
        try {
            SOSArguments arguments = new SOSArguments(args);
            String xmlFile = "";
            String logFile = "";
            int logLevel = 0;
            String settingsFile = "";
            String packages = "";
            String jobchains = "";
            try {
                xmlFile = arguments.as_string("-file=");
                logLevel = arguments.as_int("-v=", SOSStandardLogger.INFO);
                logFile = arguments.as_string("-log=", "");
                settingsFile = arguments.as_string("-settings=", "../config/factory.ini");
                jobchains = arguments.as_string("-jobchain=", "");
                packages = arguments.as_string("-package=", "");
            } catch (Exception e1) {
                LOGGER.info(e1.getMessage());
                showUsage();
                System.exit(0);
            }
            if (logFile.length() > 0)
                sosLogger = new SOSStandardLogger(logFile, logLevel);
            else
                sosLogger = new SOSStandardLogger(logLevel);
            if (packages.length() > 0 && jobchains.length() > 0) {
                LOGGER.info("jobchain und package dürfen nicht zusammen angegeben werden.");
                showUsage();
                System.exit(0);
            }
            ManagedJobExport.setSosLogger(sosLogger);
            conn = ManagedJobExport.getDBConnection(settingsFile);
            conn.connect();
            conn.setAutoCommit(false);
            if (packages != null && packages.length() > 0) {
                doMultipleImport(conn, xmlFile, "PACKAGE", packages);
            } else if (jobchains != null && jobchains.length() > 0) {
                doMultipleImport(conn, xmlFile, "NAME", jobchains);
            } else {
                ManagedJobChainImport imp = new ManagedJobChainImport(conn, xmlFile, null, null, null, sosLogger);
                imp.setUpdate(false);
                imp.setHandler(JobSchedulerManagedObject.getTableManagedModels(), "key_handler_MANAGED_MODELS", "", "NAME");
                imp.setHandler(JobSchedulerManagedObject.getTableManagedJobs(), "key_handler_MANAGED_JOBS", "rec_handler_MANAGED_JOBS", null);
                imp.setHandler(JobSchedulerManagedObject.getTableManagedOrders(), "key_handler_MANAGED_ORDERS", null, null);
                imp.doImport(conn, xmlFile);
            }
            conn.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

    private static void doMultipleImport(SOSConnection conn, String xmlFile, String field, String identifiers) throws Exception {
        String[] identArray = identifiers.split("\\+");
        for (int i = 0; i < identArray.length; i++) {
            String identifier = identArray[i];
            ManagedJobChainImport imp = new ManagedJobChainImport(conn, xmlFile, JobSchedulerManagedObject.getTableManagedModels(), field, identifier, sosLogger);
            imp.setUpdate(false);
            imp.setHandler(JobSchedulerManagedObject.getTableManagedModels(), "key_handler_MANAGED_MODELS", "", "NAME");
            imp.setHandler(JobSchedulerManagedObject.getTableManagedJobs(), "key_handler_MANAGED_JOBS", "rec_handler_MANAGED_JOBS", null);
            imp.setHandler(JobSchedulerManagedObject.getTableManagedOrders(), "key_handler_MANAGED_ORDERS", null, null);
            imp.doImport(conn, xmlFile);
        }
    }

    public HashMap key_handler_MANAGED_JOBS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings(), sosLogger);
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_jobs.id");
        keys.put("ID", String.valueOf(key));
        return keys;
    }

    public HashMap key_handler_MANAGED_MODELS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings(), sosLogger);
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_models.id");
        keys.put("ID", String.valueOf(key));
        modelId = String.valueOf(key);
        return keys;
    }

    public HashMap key_handler_MANAGED_ORDERS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings(), sosLogger);
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_orders.id");
        keys.put("ID", String.valueOf(key));
        return keys;
    }

    public HashMap rec_handler_MANAGED_JOBS(HashMap keys, HashMap record, String record_identifier) throws Exception {
        record.put("MODEL", modelId);
        return record;
    }

}
