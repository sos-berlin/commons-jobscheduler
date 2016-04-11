package sos.scheduler.managed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import sos.connection.SOSConnection;
import sos.scheduler.managed.db.JobSchedulerManagedDBReportJobOptions;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSArguments;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** This class executes database statements for managed orders. It can be
 * extended to create own database jobs (e.g do further processing with the
 * results of the statement).<br/>
 * In that case the executeStatements function has to be overwritten.
 *
 * @see JobSchedulerManagedDatabaseJob#executeStatements(SOSConnection, String)
 * @author andreas pueschel */
public class JobSchedulerManagedDatabaseJob extends JobSchedulerManagedJob {

    protected boolean flgColumn_names_case_sensitivity = false;
    protected boolean flgAdjust_column_names = false;
    protected JobSchedulerManagedDBReportJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerManagedDatabaseJob.class);
    private static final String PARAMETER_NAME_VALUE = "name_value";
    private static final String PARAMETER_SCHEDULER_ORDER_SCHEMA = "scheduler_order_schema";
    private static final String PARAMETER_SCHEDULER_ORDER_USER_NAME = "scheduler_order_user_name";
    private static final String PARAMETER_AUTO_COMMIT = "auto_commit";
    private static final String PARAMETER_RESULTSET_AS_PARAMETERS = "resultset_as_parameters";
    private static final String PARAMETER_RESULTSET_AS_WARNING = "resultset_as_warning";
    private static final String PARAMETER_SCHEDULER_ORDER_IS_USER_JOB = "scheduler_order_is_user_job";
    private boolean userJob = false;
    private final Random rand = new Random();
    private String ip = null;
    private String revokeUser = "";
    private String revokeUserQuoted = "";
    private boolean autoCommit = false;

    @Override
    public boolean spooler_init() {
        if (!super.spooler_init()) {
            return false;
        }
        return true;
    }

    protected boolean getBoolParam(final String pstrParamName, final boolean pflgDefaultValue) {
        boolean flgRet = pflgDefaultValue;
        if (orderPayload != null) {
            String strValue = orderPayload.var(pstrParamName);
            if (strValue != null && ("1".equals(strValue) || "true".equalsIgnoreCase(strValue))) {
                flgRet = true;
            }
        }
        return flgRet;
    }

    @Override
    public boolean spooler_process() {
        SOSConnection localConnection = null;
        try {
            objOptions = new JobSchedulerManagedDBReportJobOptions(getAllParametersAsProperties());
        } catch (Exception e2) {
            throw new JobSchedulerException(e2);
        }
        Order order = null;
        String command = "";
        orderPayload = null;
        Variable_set realOrderParams = null;
        boolean rc = true;
        boolean resultsetAsWarning = false;
        boolean resultsetAsParameters = false;
        boolean resultsetNameValue = false;
        boolean execReturnsResultSet = false;
        flgAdjust_column_names = true;
        flgColumn_names_case_sensitivity = false;
        autoCommit = false;
        try {
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            super.prepareParams();
            flgAdjust_column_names = objOptions.Adjust_column_names.value();
            flgColumn_names_case_sensitivity = objOptions.Column_names_case_sensitivity.value();
            if (orderPayload != null && orderPayload.var(PARAMETER_SCHEDULER_ORDER_IS_USER_JOB) != null
                    && "1".equals(orderPayload.var(PARAMETER_SCHEDULER_ORDER_IS_USER_JOB))) {
                userJob = true;
            }
            if (orderPayload != null
                    && orderPayload.var(PARAMETER_RESULTSET_AS_WARNING) != null
                    && ("1".equals(orderPayload.var(PARAMETER_RESULTSET_AS_WARNING)) 
                            || "true".equalsIgnoreCase(orderPayload.var(PARAMETER_RESULTSET_AS_WARNING)))) {
                resultsetAsWarning = true;
            }
            execReturnsResultSet = objOptions.exec_returns_resultset.value();
            if (orderPayload != null
                    && orderPayload.var(PARAMETER_RESULTSET_AS_PARAMETERS) != null
                    && ("1".equals(orderPayload.var(PARAMETER_RESULTSET_AS_PARAMETERS))
                            || "true".equalsIgnoreCase(orderPayload.var(PARAMETER_RESULTSET_AS_PARAMETERS)) 
                            || PARAMETER_NAME_VALUE.equalsIgnoreCase(orderPayload.var(PARAMETER_RESULTSET_AS_PARAMETERS)))) {
                resultsetAsParameters = true;
                if (PARAMETER_NAME_VALUE.equalsIgnoreCase(orderPayload.var(PARAMETER_RESULTSET_AS_PARAMETERS))) {
                    resultsetNameValue = true;
                }
            }
            if (orderPayload != null && orderPayload.var(PARAMETER_AUTO_COMMIT) != null
                    && ("1".equals(orderPayload.var(PARAMETER_AUTO_COMMIT)) || "true".equalsIgnoreCase(orderPayload.var(PARAMETER_AUTO_COMMIT)))) {
                autoCommit = true;
            }
            try {
                if (userJob) {
                    checkOldTempUsers();
                    localConnection = this.getUserConnection(orderPayload.var(PARAMETER_SCHEDULER_ORDER_USER_NAME),
                            orderPayload.var(PARAMETER_SCHEDULER_ORDER_SCHEMA));
                } else {
                    localConnection = JobSchedulerManagedObject.getOrderConnection(this.getConnection(), this);
                    localConnection.connect();
                }
            } catch (Exception e) {
                throw new JobSchedulerException("error occurred establishing database connection: " + e.getMessage(), e);
            }
            localConnection.setExecReturnsResultSet(execReturnsResultSet);
            try {
                String commandScript = getJobScript();
                getLogger().debug9("setting 'command_script' value from script tag of job: " + commandScript);
                if (orderJob) {
                    command = JobSchedulerManagedObject.getOrderCommand(this.getConnection(), this, commandScript);
                }
                if (command == null || command.isEmpty()) {
                    command = JobSchedulerManagedObject.getJobCommand(this.getConnection(), this);
                }
                if (command == null || command.isEmpty()) {
                    throw new JobSchedulerException("command is empty");
                }
            } catch (Exception e) {
                throw new JobSchedulerException("no database command found: " + e);
            }
            command = command.replaceAll("(\\$|§)\\{scheduler_order_job_name\\}", this.getJobName());
            command = command.replaceAll("(\\$|§)\\{scheduler_order_job_id\\}", Integer.toString(this.getJobId()));
            command = command.replaceAll("(\\$|§)\\{scheduler_id\\}", spooler.id());
            if (orderPayload != null) {
                command = JobSchedulerManagedObject.replaceVariablesInCommand(command, orderPayload, getLogger());
            }
            if (orderJob) {
                order = spooler_task.order();
                realOrderParams = order.params();
                command = command.replaceAll("(\\$|§)\\{scheduler_order_id\\}", order.id());
                command = command.replaceAll("(\\$|§)\\{scheduler_order_managed_id\\}", "0");
                this.getLogger().info("executing database statement(s) for managed order [" + order.id() + "]: " + command);
            } else {
                this.getLogger().info("executing database statement(s): " + command);
            }
            executeStatements(localConnection, command);
            this.getLogger().info("database statement(s) executed.");
            if ((resultsetAsWarning || resultsetAsParameters) && localConnection.getResultSet() != null) {
                String warning = "";
                int rowCount = 0;
                HashMap<String, String> result = null;
                boolean resultsetTrueReady = false;
                while (!(result = localConnection.get()).isEmpty()) {
                    if (resultsetTrueReady) {
                        break;
                    }
                    String orderParamKey = "";
                    rowCount++;
                    int columnCount = 0;
                    warning = "execution terminated with warning:";
                    boolean resultsetNameValueReady = false;
                    for (String key : result.keySet()) {
                        columnCount++;
                        if (key == null || key.isEmpty()) {
                            continue;
                        }
                        String value = result.get(key);
                        warning += " " + key + "=" + value;
                        if (resultsetAsParameters && order != null && !resultsetNameValueReady) {
                            if (resultsetNameValue) {
                                if (columnCount == 1) {
                                    orderParamKey = value;
                                }
                                if (columnCount == 2) {
                                    realOrderParams.set_var(orderParamKey, value);
                                    resultsetNameValueReady = true;
                                    if (!resultsetAsWarning) {
                                        break;
                                    }
                                }
                            } else if (rowCount == 1) {
                                realOrderParams.set_var(key, value);
                                resultsetTrueReady = true;
                            }
                        }
                    }
                }
                if (warning != null && !warning.isEmpty() && resultsetAsWarning) {
                    rc = false;
                    this.getLogger().warn(warning);
                }
            }
            if (getLogger().hasWarnings() || getLogger().hasErrors()) {
                spooler_task.end();
            }
            return rc && orderJob;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing managed order ["
                    + (order != null ? "Job Chain: " + order.job_chain().name() + ", ID:" + order.id() : "(none)") + "] : " + e);
            if (userJob) {
                try {
                    writeError(e, order);
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
            spooler_task.end();
            return false;
        } finally {
            try {
                if (localConnection != null && !userJob) {
                    localConnection.disconnect();
                }
            } catch (Exception ex) {
                // ignore this error
            }
            if (userJob) {
                closeUserConnection(localConnection);
                updateRunTime(order, getLogger(), getConnection());
            }
            try {
                getConnection().commit();
            } catch (Exception e) {
            }
        }
    }

    static public void updateRunTime(final Order order, final SOSLogger logger, final SOSConnection conn) {
        try {
            String id = order.id();
            String nextStart = conn.getSingleValue("SELECT \"NEXT_START\" FROM " + JobSchedulerManagedObject.getTableManagedUserJobs() + " WHERE \"ID\"=" + id);
            if (nextStart == null || nextStart.isEmpty()) {
                try {
                    logger.debug3("No next start for order " + id + ". Deleting order.");
                } catch (Exception e) {
                }
                conn.execute("DELETE FROM " + JobSchedulerManagedObject.getTableManagedUserJobs() + " WHERE " + " \"ID\"=" + id);
                conn.commit();
            } else {
                String nextTime = conn.getSingleValue("SELECT " + nextStart);
                logger.debug3("next Start for this order: " + nextTime);
                String jobRunTime = "CONCAT('<run_time let_run = \"yes\"><date date=\"',DATE('" + nextTime + "'),'\"><period single_start=\"', TIME('"
                        + nextTime + "'), '\"/></date></run_time>')";
                conn.execute("UPDATE " + JobSchedulerManagedObject.getTableManagedUserJobs() + " SET \"RUN_TIME\"=" + jobRunTime + ", \"NEXT_TIME\"='"
                        + nextTime + "', UPDATED=1 WHERE " + " \"ID\"=" + id);
            }
        } catch (Exception e) {
            try {
                logger.warn("Error occured setting next runtime: " + e);
                conn.rollback();
            } catch (Exception ex) {
            }
        }
    }

    private void writeError(final Exception e, final Order order) throws Exception {
        try {
            String currentErrorText = e.getMessage();
            Throwable thr = e.getCause();
            int errCode = 0;
            while (thr != null) {
                if (thr instanceof SQLException) {
                    SQLException sqlEx = (SQLException) thr;
                    currentErrorText = sqlEx.getLocalizedMessage();
                    errCode = sqlEx.getErrorCode();
                    break;
                }
                thr = thr.getCause();
            }
            if (currentErrorText != null && currentErrorText.length() > 250) {
                currentErrorText = currentErrorText.substring(currentErrorText.length() - 250);

            }
            getConnection().execute(
                    "UPDATE " + JobSchedulerManagedObject.getTableManagedUserJobs() + " SET \"ERROR\"=1, \"ERROR_TEXT\"='"
                            + currentErrorText.replaceAll("'", "''") + "'," + " \"ERROR_CODE\"='" + errCode + "' WHERE " + " \"ID\"='" + order.id() + "'");
            getConnection().commit();
        } catch (Exception ex) {
            try {
                getLogger().warn("Error occured writing error: " + ex);
            } catch (Exception exe) {
            }
        }
    }

    protected SOSConnection getUserConnection(final String user, final String schema) throws Exception {
        if (ip == null) {
            try {
                ip = getConnection().getSingleValue("SELECT CONVERT(SUBSTRING_INDEX(CURRENT_USER(),_utf8'@',-1) USING latin1)");
            } catch (Exception e) {
                spooler_log.debug1("Could not optain ip Address for this host. Generated" + " database users will be for all hosts.");
                ip = "%";
            }
        }
        String userLeft = user.split("@")[0];
        String userRight = user.split("@")[1];
        String query = "SHOW GRANTS FOR '" + userLeft + "'@'" + userRight + "'";
        ArrayList grants = this.getConnection().getArray(query);
        this.getConnection().commit();
        String newUserName = createRandomString();
        String password = createRandomString();
        revokeUser = "'" + newUserName + "'@'" + ip + "'";
        revokeUserQuoted = "\\'" + newUserName + "\\'@\\'" + ip + "\\'";
        String[] newGrants = new String[grants.size()];
        int grantCounter = 0;
        Iterator it = grants.iterator();
        while (it.hasNext()) {
            HashMap map = (HashMap) it.next();
            String grant = map.values().iterator().next().toString();
            String newGrant = grant.replaceAll("TO '" + userLeft + "'@", "TO '" + newUserName + "'@");
            newGrant = newGrant.replaceAll("@'" + userRight + "'", "@'" + ip + "'");
            newGrant = newGrant.replaceAll("BY PASSWORD '.*'", "BY '" + password + "'");
            try {
                getLogger().debug6("Original GRANT statement: " + grant);
                getLogger().debug6("New GRANT statement: " + newGrant);
            } catch (Exception e) {
            }
            newGrants[grantCounter] = newGrant;
            grantCounter++;
        }
        try {
            getConnection().execute(
                    "INSERT INTO " + JobSchedulerManagedObject.getTableManagedTempUsers() + "(\"NAME\", \"STATUS\", \"MODIFIED\") VALUES (" + "'"
                            + revokeUserQuoted + "', 'BEFORE_CREATION', %now)");
            getConnection().commit();
        } catch (Exception e) {
        }
        try {
            getLogger().debug3("executing new GRANT statements... ");
        } catch (Exception e) {
        }
        for (String newGrant : newGrants) {
            this.getConnection().execute(newGrant);
        }
        try {
            getConnection().execute(
                    "UPDATE " + JobSchedulerManagedObject.getTableManagedTempUsers() + " SET \"STATUS\"='CREATED', \"MODIFIED\"= %now WHERE " + "\"NAME\"='"
                            + revokeUserQuoted + "'");
        } catch (Exception e) {
        }
        getConnection().commit();
        SOSConnection userConnection;
        Properties spoolerProp = this.getJobSettings().getSection("spooler");
        String dbProperty = spoolerProp.getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
        dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
        SOSArguments arguments = new SOSArguments(dbProperty);
        try {
            spooler_log.debug6("..creating user connection object");
            userConnection = SOSConnection.createInstance(spoolerProp.getProperty("db_class"), arguments.as_string("-class=", ""),
                    arguments.as_string("-url=", ""), newUserName, password, getLogger());
        } catch (Exception e) {
            throw new JobSchedulerException("error occurred establishing database connection: " + e.getMessage());
        }
        userConnection.connect();
        if (schema != null && !schema.isEmpty()) {
            userConnection.execute("use " + schema);
        }
        userConnection.commit();
        return userConnection;
    }

    private String createRandomString() {
        String random = Long.toString(Math.abs(rand.nextLong()), 36);
        if (random.length() > 16) {
            return random.substring(0, 16);
        } else if (random.length() < 8) {
            return createRandomString();
        } else {
            return random;
        }
    }

    protected void closeUserConnection(final SOSConnection conn) {
        try {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                getLogger().debug3("executing revoke statements to delete temporary user...");
            } catch (Exception e) {
            }
            try {
                getConnection().execute(
                        "UPDATE " + JobSchedulerManagedObject.getTableManagedTempUsers() + " SET \"STATUS\"='BEFORE_DELETION', \"MODIFIED\"= %now WHERE "
                                + "\"NAME\"='" + revokeUserQuoted + "'");
                getConnection().commit();
            } catch (Exception e) {
            }
            deleteUser(revokeUser);
            getConnection().execute("DELETE FROM " + JobSchedulerManagedObject.getTableManagedTempUsers() + " WHERE \"NAME\"='" + revokeUserQuoted + "'");
        } catch (Exception e) {
            try {
                getLogger().warn("Error occurred removing user: " + e);
            } catch (Exception ex) {
            }
        }
    }

    private void deleteUser(final String userName) throws Exception {
        String query = "SHOW GRANTS FOR " + userName;
        ArrayList grants = getConnection().getArray(query);
        getConnection().commit();
        String revokes[] = new String[grants.size()];
        int counter = grants.size() - 1;
        for (Iterator it = grants.iterator(); it.hasNext();) {
            HashMap map = (HashMap) it.next();
            String grant = map.values().iterator().next().toString();
            String revoke = grant.replaceAll(" WITH GRANT OPTION", " ");
            revoke = revoke.replaceAll("GRANT ", "REVOKE ");
            revoke = revoke.replaceAll(" TO ", " FROM ");
            revokes[counter] = revoke;
            counter--;
        }
        for (String revoke : revokes) {
            if (revoke != null && !revoke.isEmpty()) {
                getConnection().execute(revoke);
            }
        }
        this.getConnection().execute("REVOKE ALL PRIVILEGES ON *.* FROM " + userName);
        this.getConnection().execute("REVOKE GRANT OPTION ON *.* FROM " + userName);
        this.getConnection().execute("DROP USER " + userName);
    }

    private void checkOldTempUsers() {
        try {
            ArrayList users = getConnection().getArray(
                    "SELECT \"NAME\", \"STATUS\" FROM " + JobSchedulerManagedObject.getTableManagedTempUsers() + " WHERE DATEDIFF(%now,\"MODIFIED\")>1");
            getConnection().commit();
            Iterator iter = users.iterator();
            while (iter.hasNext()) {
                HashMap map = (HashMap) iter.next();
                String userName = map.get("name").toString();
                String status = map.get("status").toString();
                try {
                    getLogger().debug3(
                            "User " + userName + " has not been properly deleted and" + " was left with status " + status + ". Trying to delete him now...");
                    deleteUser(userName);
                } catch (Exception e) {
                    try {
                        getLogger().warn("Error occured deleting old temporary user " + userName + " :" + e);
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (Exception e) {
            try {
                getLogger().warn("Error occured deleting old temporary users: " + e);
            } catch (Exception ex) {
            }
        }
    }

    protected void executeStatements(final SOSConnection conn, final String command) throws Exception {
        JobSchedulerException jobSchedulerException = null;
        try {
            conn.setAutoCommit(autoCommit);
            conn.executeStatements(command);
        } catch (Exception e) {
            jobSchedulerException = new JobSchedulerException(e);
        } finally {
            conn.setAutoCommit(false);
        }
        try {
            Vector output = conn.getOutput();
            if (!output.isEmpty()) {
                getLogger().info("Output from Database Server:");
                Iterator it = output.iterator();
                while (it.hasNext()) {
                    String line = (String) it.next();
                    getLogger().info("  " + line);
                }
            } else {
                getLogger().debug9("No Output from Database Server.");
            }
        } catch (Exception e) {
        }
        if (jobSchedulerException != null) {
            throw new JobSchedulerException(jobSchedulerException);
        }
    }

}