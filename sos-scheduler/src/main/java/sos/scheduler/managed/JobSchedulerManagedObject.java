package sos.scheduler.managed;

import java.util.HashMap;

import sos.connection.SOSConnection;
import sos.spooler.Job_chain;
import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSStandardLogger;

/** @author Andreas Püschel */
public class JobSchedulerManagedObject {

    private static final String conParameterDATABASE_CONNECTION = "database_connection";
    private static final String conParameterSCHEDULER_MANAGED_JOBS_VERSION = "scheduler_managed_jobs_version";
    private static final String conParameterCOMMAND = "command";
    private static final String conParameterSCHEDULER_ORDER_COMMAND = "scheduler_order_command";
    private static String tableLiveObjects = "LIVE_OBJECTS";
    private static String tableLiveObjectHistory = "LIVE_OBJECT_HISTORY";
    private static String tableLiveObjectMetadata = "LIVE_OBJECT_METADATA";
    private static String tableLiveObjectReferences = "LIVE_OBJECT_REFERENCES";
    private static String tableLiveJobs = "LIVE_JOBS";
    private static String tableLiveJobChains = "LIVE_JOB_CHAINS";
    private static String tableLiveLocks = "LIVE_LOCKS";
    private static String tableLiveOrders = "LIVE_ORDERS";
    private static String tableLiveProcessClasses = "LIVE_PROCESS_CLASSES";
    private static String tableLiveSchedules = "LIVE_SCHEDULES";
    private static String tableManagedOrders = "SCHEDULER_MANAGED_ORDERS";
    private static String tableManagedOrderParameters = "SCHEDULER_MANAGED_ORDER_PARAMETERS";
    private static String tableManagedConnections = "SCHEDULER_MANAGED_CONNECTIONS";
    private static String tableManagedJobTypes = "SCHEDULER_MANAGED_JOB_TYPES";
    private static String tableManagedJobs = "SCHEDULER_MANAGED_JOBS";
    private static String tableManagedUserJobs = "SCHEDULER_MANAGED_USER_JOBS";
    private static String tableManagedTempUsers = "SCHEDULER_MANAGED_TEMP_USERS";
    private static String tableManagedModels = "SCHEDULER_MANAGED_MODELS";
    private static String tableManagedUserVariables = "SCHEDULER_MANAGED_USER_VARIABLES";
    private static String tableWorkflowPackages = "WORKFLOW_PACKAGES";
    private static String tableWorkflowHistory = "WORKFLOW_HISTORYS";
    private static String tableSettings = "SETTINGS";
    public static String tableManagedObjects = "SCHEDULER_MANAGED_OBJECTS";
    public static String tableManagedTree = "SCHEDULER_MANAGED_TREE";
    public static String tableManagedSubmits = "SCHEDULER_MANAGED_SUBMISSIONS";
    public static String tableManagedReferences = "SCHEDULER_MANAGED_REFERNCES";

    public static String getTableManagedOrderParameters() {
        return tableManagedOrderParameters;
    }

    public static void setTableManagedOrderParameters(final String tableManagedOrderParameters) {
        JobSchedulerManagedObject.tableManagedOrderParameters = tableManagedOrderParameters;
    }

    public static String getTableManagedOrders() {
        return tableManagedOrders;
    }

    public static void setTableManagedOrders(final String tableManagedOrders) {
        JobSchedulerManagedObject.tableManagedOrders = tableManagedOrders;
    }

    public static String getTableWorkflowHistory() {
        return tableWorkflowHistory;
    }

    public static void setTableWorkflowHistory(final String tableWorkflowHistory) {
        JobSchedulerManagedObject.tableWorkflowHistory = tableWorkflowHistory;
    }

    public static String getTableManagedModels() {
        return tableManagedModels;
    }

    public static void setTableManagedModels(final String tableManagedModels) {
        JobSchedulerManagedObject.tableManagedModels = tableManagedModels;
    }

    public static String getTableWorkflowPackages() {
        return tableWorkflowPackages;
    }

    public static void setTableWorkflowPackages(final String tableWorkflowPackages) {
        JobSchedulerManagedObject.tableWorkflowPackages = tableWorkflowPackages;
    }

    public static String getTableManagedConnections() {
        return tableManagedConnections;
    }

    public static void setTableManagedConnections(final String tableManagedConnections) {
        JobSchedulerManagedObject.tableManagedConnections = tableManagedConnections;
    }

    public static String getTableManagedJobs() {
        return tableManagedJobs;
    }

    public static void setTableManagedJobs(final String tableManagedJobs) {
        JobSchedulerManagedObject.tableManagedJobs = tableManagedJobs;
    }

    public static String getTableManagedJobTypes() {
        return tableManagedJobTypes;
    }

    public static void setTableManagedJobTypes(final String tableManagedJobTypes) {
        JobSchedulerManagedObject.tableManagedJobTypes = tableManagedJobTypes;
    }

    public static String getTableManagedUserJobs() {
        return tableManagedUserJobs;
    }

    public static void setTableManagedUserJobs(final String tableManagedUserJobs) {
        JobSchedulerManagedObject.tableManagedUserJobs = tableManagedUserJobs;
    }

    public static SOSConnection getOrderConnection(final SOSConnection connection, final Job_impl job) throws Exception {
        SOSConnection localConnection = null;
        Order order = null;
        boolean useManagedConnection = true;
        HashMap result = null;
        String spoolerId = job.spooler.id().toLowerCase();
        Variable_set taskParams = job.spooler_task.params();
        Variable_set orderPayload = null;
        String managedVersion = job.spooler.var(conParameterSCHEDULER_MANAGED_JOBS_VERSION);
        if (managedVersion == null || managedVersion.isEmpty()) {
            managedVersion = "1";
        }
        job.spooler_log.debug6("scheduler_managed_jobs_version: " + managedVersion);
        Variable_set mergedParams = job.spooler.create_variable_set();
        mergedParams.merge(taskParams);
        if (job.spooler_task.job().order_queue() != null) {
            order = job.spooler_task.order();
            orderPayload = order.params();
            mergedParams.merge(orderPayload);
        }
        String connectionName = mergedParams.var(conParameterDATABASE_CONNECTION);
        if (!mergedParams.var("db_class").isEmpty() && !mergedParams.var("db_driver").isEmpty() && !mergedParams.var("db_url").isEmpty()) {
            useManagedConnection = false;
        }
        if (job.spooler_task.job().order_queue() != null && useManagedConnection) {
            String jobChain = order.job_chain().name();
            String orderId = getOrderIdInTable(spoolerId, jobChain, order);
            job.spooler_log.debug3("Connection from payload:" + connectionName);
            if ("1".equalsIgnoreCase(managedVersion)) {
                if (connectionName == null || connectionName.isEmpty()) {
                    connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\"='" + spoolerId
                            + "' AND \"JOB_CHAIN\"='" + jobChain + "'" + " AND \"ORDER_ID\"='" + orderId + "'");
                }
                if (connectionName == null || connectionName.isEmpty()) {
                    connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders()
                            + " WHERE \"SPOOLER_ID\" IS NULL AND \"JOB_CHAIN\"='" + jobChain + "' AND " + "\"ORDER_ID\"='" + orderId + "'");
                }
            }
        }
        if ("1".equalsIgnoreCase(managedVersion) && useManagedConnection) {
            if (connectionName == null || connectionName.isEmpty()) {
                connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\"='" + spoolerId
                        + "' AND \"JOB_NAME\"='" + job.spooler_job.name() + "'");
            }
            if (connectionName == null || connectionName.isEmpty()) {
                connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\" IS NULL AND "
                        + "\"JOB_NAME\"='" + job.spooler_job.name() + "'");
            }
        }
        if (useManagedConnection) {
            if (connectionName == null || connectionName.isEmpty()) {
                throw new Exception("no database connection identifier found for managed order");
            }
            result = connection.getSingle("SELECT \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CONNECTION\" FROM "
                    + JobSchedulerManagedObject.getTableManagedConnections() + " WHERE \"CONNECTION\"='" + connectionName + "'");
        } else {
            result = new HashMap();
            result.put("class", mergedParams.var("db_class"));
            result.put("driver", mergedParams.var("db_driver"));
            result.put("url", mergedParams.var("db_url"));
            result.put("username", mergedParams.var("db_user"));
            result.put("password", mergedParams.var("db_password"));
        }
        if (result.isEmpty()) {
            throw new Exception("no connection settings found for managed connection: " + connectionName);
        }
        try {
            job.spooler_log.debug6("..creating local connection object");
            localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
                    result.get("username").toString(), result.get("password").toString(), new SOSSchedulerLogger(job.spooler_log));
        } catch (Exception e) {
            throw new Exception("error occurred establishing database connection: " + e.getMessage());
        }
        return localConnection;
    }

    public static SOSConnection getJobConnection(final SOSConnection connection, final String spoolerId, final String jobName, SOSLogger log) throws Exception {
        SOSConnection localConnection;
        String connectionName = "";
        HashMap result = null;
        if (log == null) {
            log = new SOSStandardLogger(SOSLogger.INFO);
        }
        if (connectionName == null || connectionName.isEmpty()) {
            connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\"='" + spoolerId
                    + "' AND \"JOB_NAME\"='" + jobName + "'");
        }
        if (connectionName == null || connectionName.isEmpty()) {
            connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\" IS NULL AND "
                    + "\"JOB_NAME\"='" + jobName + "'");
        }
        if (connectionName == null || connectionName.isEmpty()) {
            throw new Exception("no database connection identifier found for managed job");
        }
        result = connection.getSingle("SELECT \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CONNECTION\" FROM "
                + JobSchedulerManagedObject.getTableManagedConnections() + " WHERE \"CONNECTION\"='" + connectionName + "'");
        if (result.isEmpty()) {
            throw new Exception("no connection settings found for managed connection: " + connectionName);
        }
        try {
            if (log != null) {
                log.debug6("..creating local connection object");
                localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
                        result.get("username").toString(), result.get("password").toString(), log);
            } else {
                localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
                        result.get("username").toString(), result.get("password").toString());
            }
        } catch (Exception e) {
            throw new Exception("error occurred establishing database connection: " + e.getMessage());
        }
        return localConnection;
    }

    public static SOSConnection getOrderConnection(final SOSConnection connection, final String spoolerId, final String jobChain, final String order,
            final String databaseConnection) throws Exception {
        return getOrderConnection(connection, spoolerId, jobChain, order, databaseConnection, null);
    }

    public static SOSConnection getOrderConnection(final SOSConnection connection, final String spoolerId, final String jobChain, final String order,
            final String databaseConnection, SOSLogger log) throws Exception {
        SOSConnection localConnection;
        String connectionName = "";
        String jobChainModel = "";
        HashMap result = null;
        if (log == null) {
            log = new SOSStandardLogger(SOSLogger.INFO);
        }
        String orderId = getOrderIdInTable(spoolerId, jobChain, order);
        connectionName = databaseConnection;
        if (log != null) {
            log.debug3("Connection from payload:" + connectionName);
        }
        if (connectionName == null || connectionName.isEmpty()) {
            connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\"='" + spoolerId
                    + "' AND \"JOB_CHAIN\"='" + jobChain + "'" + " AND \"ORDER_ID\"='" + orderId + "'");
        }
        if (connectionName == null || connectionName.isEmpty()) {
            connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders()
                    + " WHERE \"SPOOLER_ID\" IS NULL AND \"JOB_CHAIN\"='" + jobChain + "' AND " + "\"ORDER_ID\"='" + orderId + "'");
        }
        if (connectionName == null || connectionName.isEmpty()) {
            jobChainModel = connection.getSingleValue("SELECT \"ID\" FROM " + getTableManagedModels() + " WHERE \"NAME\"='" + jobChain + "'");
            if (jobChainModel == null || jobChainModel.isEmpty()) {
                throw new Exception("no model found for job chain [" + jobChain + "]of managed order: " + orderId);
            }
        }
        result = connection.getSingle("SELECT \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CONNECTION\" FROM "
                + JobSchedulerManagedObject.getTableManagedConnections() + " WHERE \"CONNECTION\"='" + connectionName + "'");
        if (result.isEmpty()) {
            throw new Exception("no connection settings found for managed connection: " + connectionName);
        }
        try {
            if (log != null) {
                log.debug6("..creating local connection object");
                localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
                        result.get("username").toString(), result.get("password").toString(), log);
            } else {
                localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
                        result.get("username").toString(), result.get("password").toString());
            }
        } catch (Exception e) {
            throw new Exception("error occurred establishing database connection: " + e);
        }
        return localConnection;
    }

    public static String getJobCommand(final SOSConnection connection, final Job_impl job) throws Exception {
        String command = "";
        try {
            sos.spooler.Variable_set params = job.spooler_task.params();
            command = params.var(conParameterCOMMAND);
            if (command == null || command.isEmpty()) {
                command = params.var(conParameterSCHEDULER_ORDER_COMMAND);
            }
        } catch (Exception e) {
        }
        job.spooler_log.debug3("job command: " + command);
        if (command != null && !command.isEmpty()) {
            if (isHex(command)) {
                command = new String(fromHexString(command), "US-ASCII");
            }
            return command;
        }
        try {
            String managedVersion = job.spooler.var(conParameterSCHEDULER_MANAGED_JOBS_VERSION);
            if (managedVersion == null || managedVersion.isEmpty()) {
                managedVersion = "1";
            }
            job.spooler_log.debug6("scheduler_managed_jobs_version: " + managedVersion);
            if ("1".equalsIgnoreCase(managedVersion)) {
                if (job.spooler.id() != null && !job.spooler.id().isEmpty()) {
                    command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\"='"
                            + job.spooler.id().toLowerCase() + "'" + " AND \"JOB_NAME\"='" + job.spooler_job.name() + "'");
                }
                if (command == null || command.isEmpty()) {
                    command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\" IS NULL" + " AND \"JOB_NAME\"='"
                            + job.spooler_job.name() + "'");
                }
            }
        } catch (Exception e) {
        }
        job.spooler_log.debug3("job command: " + command);
        return command;
    }

    public static String getOrderCommand(final SOSConnection connection, final Job_impl job, String commandScript) throws Exception {
        job.spooler_log.debug9("entered getOrderCommand()...");
        Order order = job.spooler_task.order();
        job.spooler_log.debug9("order!=null: " + (order != null));
        String spoolerID = job.spooler.id().toLowerCase();
        job.spooler_log.debug9("spoolerID: " + spoolerID);
        Job_chain chain = order.job_chain();
        job.spooler_log.debug9("chain!=null: " + (chain != null));
        String jobChainName = chain.name();
        job.spooler_log.debug9("jobChainName: " + jobChainName);
        String orderID = getOrderIdInTable(spoolerID, jobChainName, order);
        String command = "";
        try {
            sos.spooler.Variable_set params = job.spooler.create_variable_set();
            if (commandScript != null && !commandScript.isEmpty()) {
                job.spooler_log.debug9("command in script tag found...");
                job.spooler_task.params().set_var(conParameterCOMMAND, commandScript);
            }
            params.merge(job.spooler_task.params());
            params.merge(order.params());
            job.spooler_log.debug9("trying to get Command from parameters...");
            command = params.var(conParameterCOMMAND);
            if (command == null || command.isEmpty()) {
                command = params.var(conParameterSCHEDULER_ORDER_COMMAND);
            }
        } catch (Exception e) {
        }
        if (command != null && !command.isEmpty() && isHex(command)) {
            command = new String(fromHexString(command), "US-ASCII");
        }
        try {
            String managedVersion = job.spooler.var(conParameterSCHEDULER_MANAGED_JOBS_VERSION);
            if (managedVersion == null || managedVersion.isEmpty()) {
                managedVersion = "1";
            }
            job.spooler_log.debug6("scheduler_managed_jobs_version: " + managedVersion);
            if ((command == null || command.isEmpty()) && "1".equalsIgnoreCase(managedVersion)) {
                job.spooler_log.debug9("trying to get Command from table " + getTableManagedOrders() + " ...");
                if (!order.id().startsWith("-")) {
                    command = connection
                            .getClob("SELECT \"COMMAND\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\"='" + job.spooler.id().toLowerCase()
                                    + "' AND \"JOB_CHAIN\"='" + order.job_chain().name() + "'" + " AND \"ORDER_ID\"='" + orderID + "'");
                } else {
                    command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\" IS NULL AND \"JOB_CHAIN\"='"
                            + order.job_chain().name() + "'" + " AND \"ORDER_ID\"='" + orderID + "'");
                }
            }
        } catch (Exception e) {
        }
        job.spooler_log.debug3("order command: " + command);
        return command;
    }

    private static String substringAfter(final String str, final String separator) {
        if (str.isEmpty()) {
            return str;
        }
        if (separator == null) {
            return "";
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    public static String getOrderIdInTable(final String spoolerID, final String jobChain, final Order order) {
        return getOrderIdInTable(spoolerID, jobChain, order.id());
    }

    public static String getOrderIdInTable(String spoolerID, final String jobChain, final String orderId) {
        if (orderId.startsWith("-")) {
            spoolerID = "";
        }
        String prefix = spoolerID + "-" + jobChain + "-";
        return substringAfter(orderId, prefix);
    }

    public static String replaceVariablesInCommand(final String command, final Variable_set vars) throws Exception {
        return replaceVariablesInCommand(command, vars, null);
    }

    public static String replaceVariablesInCommand(String command, final Variable_set vars, final SOSLogger log) throws Exception {
        String[] keys = vars.names().split(";");
        if (log != null) {
            log.debug3("doing replacements for " + keys.length + " parameters.");
        }
        for (String parameterName : keys) {
            String parameterValue = vars.var(parameterName).replaceAll("\\\\", "\\\\\\\\");
            command = command.replaceAll("(?i)(\\$|§)\\{" + parameterName + "\\}", parameterValue.replaceAll("\\[quot\\]", "'"));
        }
        return command;
    }

    public static byte[] fromHexString(final String s) throws IllegalArgumentException {
        int stringLength = s.length();
        if ((stringLength & 0x1) != 0) {
            throw new IllegalArgumentException("fromHexString requires an even number of hex characters");
        }
        byte[] b = new byte[stringLength / 2];
        for (int i = 0, j = 0; i < stringLength; i += 2, j++) {
            int high = charToNibble(s.charAt(i));
            int low = charToNibble(s.charAt(i + 1));
            b[j] = (byte) (high << 4 | low);
        }
        return b;
    }

    private static int charToNibble(final char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('a' <= c && c <= 'f') {
            return c - 'a' + 0xa;
        } else if ('A' <= c && c <= 'F') {
            return c - 'A' + 0xa;
        } else {
            throw new IllegalArgumentException("Invalid hex character: " + c);
        }
    }

    public static String toHexString(final byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte element : b) {
            int bi = 0xff & element;
            int c = '0' + bi / 16 % 16;
            if (c > '9') {
                c = 'A' + (c - '0' - 10);
            }
            sb.append((char) c);
            c = '0' + bi % 16;
            if (c > '9') {
                c = 'a' + (c - '0' - 10);
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static String getTableManagedTempUsers() {
        return tableManagedTempUsers;
    }

    public static void setTableManagedTempUsers(final String tableManagedTempUsers) {
        JobSchedulerManagedObject.tableManagedTempUsers = tableManagedTempUsers;
    }

    public static String getTableSettings() {
        return tableSettings;
    }

    public static void setTableSettings(final String tableSettings) {
        JobSchedulerManagedObject.tableSettings = tableSettings;
    }

    public static String getTableManagedUserVariables() {
        return tableManagedUserVariables;
    }

    public static void setTableManagedUserVariables(final String tableManagedUserVariables) {
        JobSchedulerManagedObject.tableManagedUserVariables = tableManagedUserVariables;
    }

    public static final boolean isHexStringChar(final char c) {
        return Character.isDigit(c) || Character.isWhitespace(c) || "0123456789abcdefABCDEF".indexOf(c) >= 0;
    }

    public static final boolean isHex(final String sampleData) {
        for (int i = 0; i < sampleData.length(); i++) {
            if (!isHexStringChar(sampleData.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getTableLiveJobChains() {
        return tableLiveJobChains;
    }

    public static void setTableLiveJobChains(final String tableLiveJobChains) {
        JobSchedulerManagedObject.tableLiveJobChains = tableLiveJobChains;
    }

    public static String getTableLiveJobs() {
        return tableLiveJobs;
    }

    public static void setTableLiveJobs(final String tableLiveJobs) {
        JobSchedulerManagedObject.tableLiveJobs = tableLiveJobs;
    }

    public static String getTableLiveLocks() {
        return tableLiveLocks;
    }

    public static void setTableLiveLocks(final String tableLiveLocks) {
        JobSchedulerManagedObject.tableLiveLocks = tableLiveLocks;
    }

    public static String getTableLiveObjectHistory() {
        return tableLiveObjectHistory;
    }

    public static void setTableLiveObjectHistory(final String tableLiveObjectHistory) {
        JobSchedulerManagedObject.tableLiveObjectHistory = tableLiveObjectHistory;
    }

    public static String getTableLiveObjectMetadata() {
        return tableLiveObjectMetadata;
    }

    public static void setTableLiveObjectMetadata(final String tableLiveObjectMetadata) {
        JobSchedulerManagedObject.tableLiveObjectMetadata = tableLiveObjectMetadata;
    }

    public static String getTableLiveObjectReferences() {
        return tableLiveObjectReferences;
    }

    public static void setTableLiveObjectReferences(final String tableLiveObjectReferences) {
        JobSchedulerManagedObject.tableLiveObjectReferences = tableLiveObjectReferences;
    }

    public static String getTableLiveObjects() {
        return tableLiveObjects;
    }

    public static void setTableLiveObjects(final String tableLiveObjects) {
        JobSchedulerManagedObject.tableLiveObjects = tableLiveObjects;
    }

    public static String getTableLiveOrders() {
        return tableLiveOrders;
    }

    public static void setTableLiveOrders(final String tableLiveOrders) {
        JobSchedulerManagedObject.tableLiveOrders = tableLiveOrders;
    }

    public static String getTableLiveProcessClasses() {
        return tableLiveProcessClasses;
    }

    public static void setTableLiveProcessClasses(final String tableLiveProcessClasses) {
        JobSchedulerManagedObject.tableLiveProcessClasses = tableLiveProcessClasses;
    }

    public static String getTableLiveSchedules() {
        return tableLiveSchedules;
    }

    public static void setTableLiveSchedules(final String tableLiveSchedules) {
        JobSchedulerManagedObject.tableLiveSchedules = tableLiveSchedules;
    }

}