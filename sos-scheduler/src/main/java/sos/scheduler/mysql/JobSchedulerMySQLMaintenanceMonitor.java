package sos.scheduler.mysql;

import java.util.ArrayList;
import java.util.HashMap;

import sos.connection.SOSConnection;
import sos.connection.SOSConnectionVersionLimiter;
import sos.connection.SOSMySQLConnection;
import sos.scheduler.job.JobSchedulerJob;
import sos.util.SOSSchedulerLogger;

/** This job takes care of maintenance tasks for a MySQL database. It performs
 * the operations ANALYZE TABLE, OPTIMIZE TABLE and REPAIR TABLE (with the QUICK
 * option) for all or for selected tables. These operations are important for
 * the performance of InnoDB tables, but can only be used starting with MySQL
 * version 4.0.13
 *
 * @author Andreas Liebert */
public class JobSchedulerMySQLMaintenanceMonitor extends JobSchedulerJob {

    public boolean spooler_process() throws Exception {
        boolean new_connection = false;
        SOSConnection connection = this.getConnection();
        String database = "";
        String[] operations = null;
        String[] tables = null;
        String[] attributes = null;
        this.setLogger(new SOSSchedulerLogger(spooler_log));
        try {
            if (getJobProperties().getProperty("config") != null) {
                connection = sos.connection.SOSConnection.createInstance(getJobProperties().getProperty("config"));
                connection.connect();
                new_connection = true;
            } else if (getJobProperties().getProperty("database") != null) {
                database = getJobProperties().getProperty("database").toString();
                if (database != null && !database.isEmpty()) {
                    connection.execute("USE " + database);
                }
                getLogger().info("database changed to: " + database);
            }
            if (!(connection instanceof SOSMySQLConnection)) {
                getLogger().warn("This Job only works with MySQL databases.");
                return false;
            }
            SOSConnectionVersionLimiter limiter = new SOSConnectionVersionLimiter();
            limiter.setExcludedThroughVersion(3, 999);
            for (int i = 0; i < 13; i++) {
                limiter.addExcludedVersion("4.0." + i);
            }
            limiter.check(connection, getLogger());
            if (getJobProperties().getProperty("operations") != null) {
                operations = getJobProperties().getProperty("operations").toString().split(",");
            }
            if (operations == null) {
                operations = new String[3];
                operations[0] = "ANALYZE";
                operations[1] = "OPTIMIZE";
                operations[2] = "REPAIR@QUICK";
            }
            if (getJobProperties().getProperty("tables") != null) {
                tables = getJobProperties().getProperty("tables").split(",");
            }
            if (tables == null) {
                ArrayList result = connection.getArrayValue("SHOW TABLES");
                if (result != null && !result.isEmpty()) {
                    tables = new String[result.size()];
                    for (int i = 0; i < result.size(); i++) {
                        tables[i] = result.get(i).toString();
                    }
                }
            }
            if (tables == null) {
                throw new Exception("no tables found for this database");
            }
        } catch (Exception e) {
            this.getLogger().error("Error reading Settings: " + e);
            return false;
        }
        try {
            for (int i = 0; i < tables.length; i++) {
                attributes = tables[i].split(":");
                if (attributes == null || attributes.length == 1) {
                    attributes = new String[operations.length + 1];
                    attributes[0] = tables[i];
                    for (int j = 0; j < operations.length; j++) {
                        attributes[j + 1] = operations[j];
                    }
                }
                for (int j = 1; j < attributes.length; j++) {
                    String statement_suffix = "";
                    String statement;
                    attributes[j] = attributes[j].trim();
                    if (attributes[j].indexOf("@") > 0) {
                        statement_suffix = " " + attributes[j].substring(attributes[j].indexOf("@") + 1);
                        attributes[j] = attributes[j].substring(0, attributes[j].indexOf("@"));
                    } else {
                        statement_suffix = "";
                    }
                    if ("ANALYZE".equalsIgnoreCase(attributes[j])) {
                        statement = "ANALYZE TABLE " + attributes[0] + statement_suffix;
                    } else if ("OPTIMIZE".equalsIgnoreCase(attributes[j])) {
                        statement = "OPTIMIZE TABLE " + attributes[0] + statement_suffix;
                    } else if ("REPAIR".equalsIgnoreCase(attributes[j])) {
                        statement = "REPAIR TABLE " + attributes[0] + statement_suffix;
                    } else if ("NOP".equalsIgnoreCase(attributes[j])) {
                        statement = "";
                    } else {
                        getLogger().error("illegal operation was given for table [" + attributes[0] + "]: " + attributes[j]);
                        return false;
                    }
                    if (!statement.isEmpty()) {
                        getLogger().debug3("processing statement: " + statement);
                        HashMap result = connection.getSingle(statement);
                        if (result == null || result.isEmpty()) {
                            getLogger().error("database returned no result for statement: " + statement);
                            return false;
                        }
                        getLogger().info("table: " + result.get("table").toString() + ", operation: " + result.get("op").toString() + ", Message Type: "
                                + result.get("msg_type").toString() + ", Message Text: " + result.get("msg_text").toString());
                    }
                }
            }
            if (getJobProperties().getProperty("flush_tables") != null && "yes".equalsIgnoreCase(getJobProperties().getProperty("flush_tables").toString())) {
                getLogger().info("tables will be flushed to publish optimizations");
                connection.execute("FLUSH TABLES");
            }
        } catch (Exception e) {
            getLogger().error("maintenance monitor failed: " + e);
            return false;
        } finally {
            if (new_connection && (connection != null)) {
                connection.disconnect();
                connection = null;
            }
        }
        return false;
    }

}