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

/**
 * static helper class for Managed Jobs
 * @author Andreas Püschel <andreas.pueschel@sos-berlin.com>
 * @since 2005-03-05
 * @version 1.0
 */
public class JobSchedulerManagedObject {

	private static final String	conParameterDATABASE_CONNECTION	= "database_connection";

	private static final String	conParameterSCHEDULER_MANAGED_JOBS_VERSION	= "scheduler_managed_jobs_version";

	private static final String	conParameterCOMMAND					= "command";

	private static final String	conParameterSCHEDULER_ORDER_COMMAND	= "scheduler_order_command";

	private static String		tableLiveObjects					= "LIVE_OBJECTS";

	private static String		tableLiveObjectHistory				= "LIVE_OBJECT_HISTORY";

	private static String		tableLiveObjectMetadata				= "LIVE_OBJECT_METADATA";

	private static String		tableLiveObjectReferences			= "LIVE_OBJECT_REFERENCES";

	private static String		tableLiveJobs						= "LIVE_JOBS";

	private static String		tableLiveJobChains					= "LIVE_JOB_CHAINS";

	private static String		tableLiveLocks						= "LIVE_LOCKS";

	private static String		tableLiveOrders						= "LIVE_ORDERS";

	private static String		tableLiveProcessClasses				= "LIVE_PROCESS_CLASSES";

	private static String		tableLiveSchedules					= "LIVE_SCHEDULES";

	/** Tabelle der persistenten Aufträge */
	private static String		tableManagedOrders					= "SCHEDULER_MANAGED_ORDERS";

	/** Tabelle der persistenten Auftragsparameter */
	private static String		tableManagedOrderParameters			= "SCHEDULER_MANAGED_ORDER_PARAMETERS";

	/** Tabelle der Datenbankverbindungen */
	private static String		tableManagedConnections				= "SCHEDULER_MANAGED_CONNECTIONS";

	/** Tabelle der Job-Typen */
	private static String		tableManagedJobTypes				= "SCHEDULER_MANAGED_JOB_TYPES";

	/** Tabelle der Jobs */
	private static String		tableManagedJobs					= "SCHEDULER_MANAGED_JOBS";

	/** Tabelle der Benutzer-Aufträge */
	private static String		tableManagedUserJobs				= "SCHEDULER_MANAGED_USER_JOBS";

	/** Tabelle der Temporären Benutzer für die User Jobs */
	private static String		tableManagedTempUsers				= "SCHEDULER_MANAGED_TEMP_USERS";

	/** Tabelle der Workflow-Modelle */
	private static String		tableManagedModels					= "SCHEDULER_MANAGED_MODELS";

	/** Tabelle der Temporären Benutzer für die User Jobs */
	private static String		tableManagedUserVariables			= "SCHEDULER_MANAGED_USER_VARIABLES";

	/** Tabelle der Workflow-Pakete */
	private static String		tableWorkflowPackages				= "WORKFLOW_PACKAGES";

	/** Tabelle der Workflow-Historie */
	private static String		tableWorkflowHistory				= "WORKFLOW_HISTORYS";

	/** Tabelle der Settings */
	private static String		tableSettings						= "SETTINGS";

	/**
	 * Table for persistent objects
	 */
	public static String		tableManagedObjects					= "SCHEDULER_MANAGED_OBJECTS";

	/**
	 * Table for the tree
	 */
	public static String		tableManagedTree					= "SCHEDULER_MANAGED_TREE";

	/**
	 * Table for submitted commands
	 */
	public static String		tableManagedSubmits					= "SCHEDULER_MANAGED_SUBMISSIONS";

	/**
	 * Table for object refernces
	 */
	public static String		tableManagedReferences				= "SCHEDULER_MANAGED_REFERNCES";

	/**
	 * @return Returns the tableManagedOrderParameters.
	 */
	public static String getTableManagedOrderParameters() {
		return tableManagedOrderParameters;
	}

	/**
	 * @param tableManagedOrderParameters The tableManagedOrderParameters to set.
	 */
	public static void setTableManagedOrderParameters(final String tableManagedOrderParameters) {
		JobSchedulerManagedObject.tableManagedOrderParameters = tableManagedOrderParameters;
	}

	/**
	 * @return Returns the tableManagedOrders.
	 */
	public static String getTableManagedOrders() {
		return tableManagedOrders;
	}

	/**
	 * @param tableManagedOrders The tableManagedOrders to set.
	 */
	public static void setTableManagedOrders(final String tableManagedOrders) {
		JobSchedulerManagedObject.tableManagedOrders = tableManagedOrders;
	}

	/**
	 * @return Returns the tableWorkflowHistory.
	 */
	public static String getTableWorkflowHistory() {
		return tableWorkflowHistory;
	}

	/**
	 * @param tableWorkflowHistory The tableWorkflowHistory to set.
	 */
	public static void setTableWorkflowHistory(final String tableWorkflowHistory) {
		JobSchedulerManagedObject.tableWorkflowHistory = tableWorkflowHistory;
	}

	/**
	 * @return Returns the tableManagedModels.
	 */
	public static String getTableManagedModels() {
		return tableManagedModels;
	}

	/**
	 * @param tableWorkflowModels The tableManagedModels to set.
	 */
	public static void setTableManagedModels(final String tableManagedModels) {
		JobSchedulerManagedObject.tableManagedModels = tableManagedModels;
	}

	/**
	 * @return Returns the tableWorkflowPackages.
	 */
	public static String getTableWorkflowPackages() {
		return tableWorkflowPackages;
	}

	/**
	 * @param tableWorkflowPackages The tableWorkflowPackages to set.
	 */
	public static void setTableWorkflowPackages(final String tableWorkflowPackages) {
		JobSchedulerManagedObject.tableWorkflowPackages = tableWorkflowPackages;
	}

	/**
	 * @return Returns the tableManagedConnections.
	 */
	public static String getTableManagedConnections() {
		return tableManagedConnections;
	}

	/**
	 * @param tableManagedConnections The tableManagedConnections to set.
	 */
	public static void setTableManagedConnections(final String tableManagedConnections) {
		JobSchedulerManagedObject.tableManagedConnections = tableManagedConnections;
	}

	/**
	 * @return Returns the tableManagedJobs.
	 */
	public static String getTableManagedJobs() {
		return tableManagedJobs;
	}

	/**
	 * @param tableManagedJobs The tableManagedJobs to set.
	 */
	public static void setTableManagedJobs(final String tableManagedJobs) {
		JobSchedulerManagedObject.tableManagedJobs = tableManagedJobs;
	}

	/**
	 * @return Returns the tableManagedJobTypes.
	 */
	public static String getTableManagedJobTypes() {
		return tableManagedJobTypes;
	}

	/**
	 * @param tableManagedJobTypes The tableManagedJobTypes to set.
	 */
	public static void setTableManagedJobTypes(final String tableManagedJobTypes) {
		JobSchedulerManagedObject.tableManagedJobTypes = tableManagedJobTypes;
	}

	/**
	 * @return Returns the tableManagedUserJobs.
	 */
	public static String getTableManagedUserJobs() {
		return tableManagedUserJobs;
	}

	/**
	 * @param tableManagedUserJobs The tableManagedUserJobs to set.
	 */
	public static void setTableManagedUserJobs(final String tableManagedUserJobs) {
		JobSchedulerManagedObject.tableManagedUserJobs = tableManagedUserJobs;
	}

	/**
	 * Gibt ein Connection Object zurück, welches die Order-spezifische Verbindung darstellt.
	 * Funktioniert mit Managed Job 2
	 * @param conn Connection, die benutzt wird, um die Daten für die neue Connection zu finden
	 * @param job job Object, von dessen Order die Connection besorgt werden soll (meistens "this")
	 * @return Connection, die zum Order gehört. Diese ist noch nicht initialisiert, es müssen also
	 * noch connect() und später disconnect() aufgerufen werden.
	 * @throws Exception
	 */
	public static SOSConnection getOrderConnection(final SOSConnection connection, final Job_impl job) throws Exception {

		SOSConnection localConnection = null;
		Order order = null;

		boolean useManagedConnection = true;
		// boolean flgOperationWasSuccessful = false;
		HashMap result = null;
		String spoolerId = job.spooler.id().toLowerCase();
		Variable_set taskParams = job.spooler_task.params();
		Variable_set orderPayload = null;

		String managedVersion = job.spooler.var(conParameterSCHEDULER_MANAGED_JOBS_VERSION);
		if (managedVersion == null || managedVersion.length() == 0)
			managedVersion = "1";
		job.spooler_log.debug6("scheduler_managed_jobs_version: " + managedVersion);
		Variable_set mergedParams = job.spooler.create_variable_set();
		mergedParams.merge(taskParams);
		if (job.spooler_task.job().order_queue() != null) {
			order = job.spooler_task.order();
			orderPayload = order.params();
			mergedParams.merge(orderPayload);
		}
		String connectionName = mergedParams.var(conParameterDATABASE_CONNECTION);

		// check if job uses ProcessDatabaseJob Parameters:
		if (mergedParams.var("db_class").length() > 0 && mergedParams.var("db_driver").length() > 0 && mergedParams.var("db_url").length() > 0) {
			useManagedConnection = false;
		}
		if (job.spooler_task.job().order_queue() != null && useManagedConnection) {
			String jobChain = order.job_chain().name();
			// String jobChainModel = "";
			String orderId = getOrderIdInTable(spoolerId, jobChain, order);

			job.spooler_log.debug3("Connection from payload:" + connectionName);

			if (managedVersion.equalsIgnoreCase("1")) {
				if (connectionName == null || connectionName.length() == 0) {
					connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\"='" + spoolerId
							+ "' AND \"JOB_CHAIN\"='" + jobChain + "'" + " AND \"ORDER_ID\"='" + orderId + "'");
				}

				if (connectionName == null || connectionName.length() == 0) {
					connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders()
							+ " WHERE \"SPOOLER_ID\" IS NULL AND \"JOB_CHAIN\"='" + jobChain + "' AND " + "\"ORDER_ID\"='" + orderId + "'");
				}

				/*
				 if (connectionName == null || connectionName.length() == 0) {
				 jobChainModel = connection.getSingleValue("SELECT \"ID\" FROM " + getTableManagedModels() +
				 " WHERE \"NAME\"='" + jobChain + "'");
				 
				 if (jobChainModel == null || jobChainModel.length() == 0)
				 throw (new Exception("no model found for job chain [" + jobChain + "]of managed order: " + orderId));
				 }
				 */
			}

		}

		if (managedVersion.equalsIgnoreCase("1") && useManagedConnection) {
			if (connectionName == null || connectionName.length() == 0) {
				connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\"='" + spoolerId
						+ "' AND \"JOB_NAME\"='" + job.spooler_job.name() + "'");
			}

			if (connectionName == null || connectionName.length() == 0) {
				connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\" IS NULL AND "
						+ "\"JOB_NAME\"='" + job.spooler_job.name() + "'");
			}
		}

		if (useManagedConnection) {
			if (connectionName == null || connectionName.length() == 0)
				throw new Exception("no database connection identifier found for managed order");

			result = connection.getSingle("SELECT \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CONNECTION\" FROM "
					+ JobSchedulerManagedObject.getTableManagedConnections() + " WHERE \"CONNECTION\"='" + connectionName + "'");
		}
		else {
			result = new HashMap();
			result.put("class", mergedParams.var("db_class"));
			result.put("driver", mergedParams.var("db_driver"));
			result.put("url", mergedParams.var("db_url"));
			result.put("username", mergedParams.var("db_user"));
			result.put("password", mergedParams.var("db_password"));
		}

		if (result.isEmpty())
			throw new Exception("no connection settings found for managed connection: " + connectionName);

		try {
			job.spooler_log.debug6("..creating local connection object");

			localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
					result.get("username").toString(), result.get("password").toString(), new SOSSchedulerLogger(job.spooler_log));

		}
		catch (Exception e) {
			throw new Exception("error occurred establishing database connection: " + e.getMessage());
		}

		return localConnection;
	}

	/**
	 * Gibt ein Connection Object zurück, welches die Job-spezifische Verbindung darstellt
	 * @param connection Connection, die benutzt wird, um die Daten für die neue Connection zu finden
	 * @param spoolerId ID des Schedulers
	 * @param jobName Name des Jobs
	 * @param log SOSLogger für Logging der Datenbankverbindung
	 * @return Connection, die zum Job gehört. Diese ist noch nicht initialisiert, es müssen also
	 * noch connect() und später disconnect() aufgerufen werden.
	 * @throws Exception
	 */
	public static SOSConnection getJobConnection(final SOSConnection connection, final String spoolerId, final String jobName, SOSLogger log) throws Exception {
		SOSConnection localConnection;
		String connectionName = "";
		HashMap result = null;
		if (log == null)
			log = new SOSStandardLogger(SOSLogger.INFO);

		if (connectionName == null || connectionName.length() == 0) {
			connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\"='" + spoolerId
					+ "' AND \"JOB_NAME\"='" + jobName + "'");
		}

		if (connectionName == null || connectionName.length() == 0) {
			connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\" IS NULL AND "
					+ "\"JOB_NAME\"='" + jobName + "'");
		}

		if (connectionName == null || connectionName.length() == 0)
			throw new Exception("no database connection identifier found for managed job");

		result = connection.getSingle("SELECT \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CONNECTION\" FROM "
				+ JobSchedulerManagedObject.getTableManagedConnections() + " WHERE \"CONNECTION\"='" + connectionName + "'");
		if (result.isEmpty())
			throw new Exception("no connection settings found for managed connection: " + connectionName);

		try {
			if (log != null) {
				log.debug6("..creating local connection object");

				localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
						result.get("username").toString(), result.get("password").toString(), log);
			}
			else {
				localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
						result.get("username").toString(), result.get("password").toString());
			}

		}
		catch (Exception e) {
			throw new Exception("error occurred establishing database connection: " + e.getMessage());
		}

		return localConnection;

	}

	/**
	 * Gibt ein Connection Object zurück, welches die Job-spezifische Verbindung darstellt
	 * @param connection Connection, die benutzt wird, um die Daten für die neue Connection zu finden
	 * @param spoolerId ID des Schedulers
	 * @param jobChain Name der Jobchain
	 * @param order Id des Auftrags (order.id())
	 * @param databaseConnection Identifizierer der Datenbankverbindung aus
	 * @return Connection, die zum Job gehört. Diese ist noch nicht initialisiert, es müssen also
	 * noch connect() und später disconnect() aufgerufen werden.
	 * @throws Exception
	 */
	public static SOSConnection getOrderConnection(final SOSConnection connection, final String spoolerId, final String jobChain, final String order, final String databaseConnection)
			throws Exception {
		return getOrderConnection(connection, spoolerId, jobChain, order, databaseConnection, null);
	}

	/**
	 * Gibt ein Connection Object zurück, welches die Job-spezifische Verbindung darstellt
	 * @param connection Connection, die benutzt wird, um die Daten für die neue Connection zu finden
	 * @param spoolerId ID des Schedulers
	 * @param jobChain Name der Jobchain
	 * @param order Id des Auftrags (order.id())
	 * @param databaseConnection Identifizierer der Datenbankverbindung aus 
	 * @param log SOSLogger für Logging der Datenbankverbindung (aus order.payload.value("database_connection") in javascript)
	 * @return Connection, die zum Job gehört. Diese ist noch nicht initialisiert, es müssen also
	 * noch connect() und später disconnect() aufgerufen werden.
	 * @throws Exception
	 */
	public static SOSConnection getOrderConnection(final SOSConnection connection, final String spoolerId, final String jobChain, final String order, final String databaseConnection,
			SOSLogger log) throws Exception {

		SOSConnection localConnection;
		String connectionName = "";
		String jobChainModel = "";
		HashMap result = null;

		if (log == null)
			log = new SOSStandardLogger(SOSLogger.INFO);
		String orderId = getOrderIdInTable(spoolerId, jobChain, order);

		connectionName = databaseConnection;

		if (log != null)
			log.debug3("Connection from payload:" + connectionName);

		if (connectionName == null || connectionName.length() == 0) {
			connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\"='" + spoolerId
					+ "' AND \"JOB_CHAIN\"='" + jobChain + "'" + " AND \"ORDER_ID\"='" + orderId + "'");
		}

		if (connectionName == null || connectionName.length() == 0) {
			connectionName = connection.getSingleValue("SELECT \"CONNECTION\" FROM " + getTableManagedOrders()
					+ " WHERE \"SPOOLER_ID\" IS NULL AND \"JOB_CHAIN\"='" + jobChain + "' AND " + "\"ORDER_ID\"='" + orderId + "'");
		}

		if (connectionName == null || connectionName.length() == 0) {
			jobChainModel = connection.getSingleValue("SELECT \"ID\" FROM " + getTableManagedModels() + " WHERE \"NAME\"='" + jobChain + "'");

			if (jobChainModel == null || jobChainModel.length() == 0)
				throw new Exception("no model found for job chain [" + jobChain + "]of managed order: " + orderId);
		}

		result = connection.getSingle("SELECT \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CONNECTION\" FROM "
				+ JobSchedulerManagedObject.getTableManagedConnections() + " WHERE \"CONNECTION\"='" + connectionName + "'");
		if (result.isEmpty())
			throw new Exception("no connection settings found for managed connection: " + connectionName);

		try {
			if (log != null) {
				log.debug6("..creating local connection object");

				localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
						result.get("username").toString(), result.get("password").toString(), log);
			}
			else {
				localConnection = SOSConnection.createInstance(result.get("class").toString(), result.get("driver").toString(), result.get("url").toString(),
						result.get("username").toString(), result.get("password").toString());
			}
		}
		catch (Exception e) {
			throw new Exception("error occurred establishing database connection: " + e);
		}

		return localConnection;
	}

	/**
	 * returns the job-specific command
	 * @param connection Connection, which will be used to find the data for the command
	 * @param job job object whose command shall be returned (e.g. "this") 
	 * 
	 * @throws Exception
	 */
	public static String getJobCommand(final SOSConnection connection, final Job_impl job) throws Exception {

		String command = "";
		try {
			sos.spooler.Variable_set params = job.spooler_task.params();
			command = params.var(conParameterCOMMAND);
			if (command == null || command.length() == 0)
				command = params.var(conParameterSCHEDULER_ORDER_COMMAND);
		}
		catch (Exception e) {
		}
		job.spooler_log.debug3("job command: " + command);

		if (command != null && command.length() > 0) {
			if (isHex(command))
				command = new String(fromHexString(command), "US-ASCII");
			return command;
		}
		try {
			String managedVersion = job.spooler.var(conParameterSCHEDULER_MANAGED_JOBS_VERSION);
			if (managedVersion == null || managedVersion.length() == 0)
				managedVersion = "1";
			job.spooler_log.debug6("scheduler_managed_jobs_version: " + managedVersion);

			if (managedVersion.equalsIgnoreCase("1")) {
				if (job.spooler.id() != null && job.spooler.id().length() > 0) {
					command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\"='"
							+ job.spooler.id().toLowerCase() + "'" + " AND \"JOB_NAME\"='" + job.spooler_job.name() + "'");
				}

				if (command == null || command.length() == 0) {
					command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedJobs() + " WHERE \"SPOOLER_ID\" IS NULL" + " AND \"JOB_NAME\"='"
							+ job.spooler_job.name() + "'");
				}
			}
		}
		catch (Exception e) {
		}

		job.spooler_log.debug3("job command: " + command);

		return command;
	}

	/**
	 * returns the order-specific command
	 * @param connection Connection, which will be used to find the data for the command
	 * @param job job object whose order's command shall be returned (e.g. "this")     * 
	 * 
	 * @throws Exception
	 */
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
			
			if (commandScript != null && commandScript.length() > 0){
				job.spooler_log.debug9("command in script tag found...");
				job.spooler_task.params().set_var(conParameterCOMMAND,commandScript);
			}
			
			params.merge(job.spooler_task.params());
			params.merge(order.params());
			
			job.spooler_log.debug9("trying to get Command from parameters...");
 			
			command = params.var(conParameterCOMMAND);
			if (command == null || command.length() == 0){
				command = params.var(conParameterSCHEDULER_ORDER_COMMAND);
			}
		}
		catch (Exception e) {
		}

		if (command != null && command.length() > 0) {
			if (isHex(command))
				command = new String(fromHexString(command), "US-ASCII");
		}

		try {
			String managedVersion = job.spooler.var(conParameterSCHEDULER_MANAGED_JOBS_VERSION);
			if (managedVersion == null || managedVersion.length() == 0)
				managedVersion = "1";
			job.spooler_log.debug6("scheduler_managed_jobs_version: " + managedVersion);
			if ((command == null || command.length() == 0) && managedVersion.equalsIgnoreCase("1")) {
				job.spooler_log.debug9("trying to get Command from table " + getTableManagedOrders() + " ...");
				if (!order.id().startsWith("-")) {
					command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\"='"
							+ job.spooler.id().toLowerCase() + "' AND \"JOB_CHAIN\"='" + order.job_chain().name() + "'" + " AND \"ORDER_ID\"='" + orderID + "'");
				}
				else {
					command = connection.getClob("SELECT \"COMMAND\" FROM " + getTableManagedOrders() + " WHERE \"SPOOLER_ID\" IS NULL AND \"JOB_CHAIN\"='"
							+ order.job_chain().name() + "'" + " AND \"ORDER_ID\"='" + orderID + "'");
				}
			}
		}
		catch (Exception e) {
		}

		job.spooler_log.debug3("order command: " + command);
		//job.spooler_log.debug3("command.lentgh: "+ command.length());

		return command;
	}

	private static String substringAfter(final String str, final String separator) {

		if (str.length() == 0) {
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

	/**
	 * If a managed order is created inside the Job Scheduler it is assigned a new ID.
	 * This method returns the original ID from the SCHEDULER_MANAGED_ORDERS table.
	 * @param spoolerID Scheduler ID
	 * @param jobChain name of the Job Chain     * 
	 */
	public static String getOrderIdInTable(final String spoolerID, final String jobChain, final Order order) {

		return getOrderIdInTable(spoolerID, jobChain, order.id());
	}

	/**
	 * If a managed order is created inside the Job Scheduler it is assigned a new ID.
	 * This method returns the original ID from the SCHEDULER_MANAGED_ORDERS table.
	 * @param orderId Id of the order (order.id())
	 */
	public static String getOrderIdInTable(String spoolerID, final String jobChain, final String orderId) {

		if (orderId.startsWith("-"))
			spoolerID = "";
		String prefix = spoolerID + "-" + jobChain + "-";
		return substringAfter(orderId, prefix);
	}

	public static String replaceVariablesInCommand(final String command, final Variable_set vars) throws Exception {

		return replaceVariablesInCommand(command, vars, null);
	}

	/**
	 * replaces variables of the ${name} and §{name} schemes in a command with values from
	 * Variable_set
	 * @param command original command string
	 * @param vars job parameters
	 * @return new command with replaced variables
	 */
	public static String replaceVariablesInCommand(String command, final Variable_set vars, final SOSLogger log) throws Exception {

		String[] keys = vars.names().split(";");
		if (log != null)
			log.debug3("doing replacements for " + keys.length + " parameters.");
		for (String parameterName : keys) {
			String parameterValue = vars.var(parameterName).replaceAll("\\\\", "\\\\\\\\");
			command = command.replaceAll("(?i)(\\$|§)\\{" + parameterName + "\\}", parameterValue.replaceAll("\\[quot\\]", "'"));
		}

		return command;
	}

	/**
	* Convert a hex string to a byte array.
	* Permits upper or lower case hex.
	*
	* @param s String must have even number of characters.
	* and be formed only of digits 0-9 A-F or
	* a-f. No spaces, minus or plus signs.
	* @return corresponding byte array.
	*/
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

	/**
	* convert a single char to corresponding nibble.
	*
	* @param c char to convert. must be 0-9 a-f A-F, no
	* spaces, plus or minus signs.
	*
	* @return corresponding integer
	*/
	private static int charToNibble(final char c) {

		if ('0' <= c && c <= '9') {
			return c - '0';
		}
		else
			if ('a' <= c && c <= 'f') {
				return c - 'a' + 0xa;
			}
			else
				if ('A' <= c && c <= 'F') {
					return c - 'A' + 0xa;
				}
				else {
					throw new IllegalArgumentException("Invalid hex character: " + c);
				}
	}

	public static String toHexString(final byte[] b) {

		StringBuffer buf = new StringBuffer();
		for (byte element : b) {
			int bi = 0xff & element;
			int c = '0' + bi / 16 % 16;
			if (c > '9')
				c = 'A' + (c - '0' - 10);
			buf.append((char) c);
			c = '0' + bi % 16;
			if (c > '9')
				c = 'a' + (c - '0' - 10);
			buf.append((char) c);
		}
		return buf.toString();
	}

	/**
	 * @return Returns the tableManagedTempUsers.
	 */
	public static String getTableManagedTempUsers() {
		return tableManagedTempUsers;
	}

	/**
	 * @param tableManagedTempUsers The tableManagedTempUsers to set.
	 */
	public static void setTableManagedTempUsers(final String tableManagedTempUsers) {
		JobSchedulerManagedObject.tableManagedTempUsers = tableManagedTempUsers;
	}

	/**
	 * @return Returns the tableSettings.
	 */
	public static String getTableSettings() {
		return tableSettings;
	}

	/**
	 * @param tableSettings The tableSettings to set.
	 */
	public static void setTableSettings(final String tableSettings) {
		JobSchedulerManagedObject.tableSettings = tableSettings;
	}

	/**
	 * @return Returns the tableManagedUserVariables.
	 */
	public static String getTableManagedUserVariables() {
		return tableManagedUserVariables;
	}

	/**
	 * @param tableManagedUserVariables The tableManagedUserVariables to set.
	 */
	public static void setTableManagedUserVariables(final String tableManagedUserVariables) {
		JobSchedulerManagedObject.tableManagedUserVariables = tableManagedUserVariables;
	}

	/**
	 * Return true if the input argument character is
	 * a digit, a space, or A-F.
	 */
	public static final boolean isHexStringChar(final char c) {
		return Character.isDigit(c) || Character.isWhitespace(c) || "0123456789abcdefABCDEF".indexOf(c) >= 0;
	}

	/**
	 * Return true if the argument string seems to be a
	 * Hex data string, like "a0 13 2f ".  Whitespace is
	 * ignored.
	 */
	public static final boolean isHex(final String sampleData) {

		for (int i = 0; i < sampleData.length(); i++) {
			if (!isHexStringChar(sampleData.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * @return the tableLiveJobChains
	 */
	public static String getTableLiveJobChains() {
		return tableLiveJobChains;
	}

	/**
	 * @param tableLiveJobChains the tableLiveJobChains to set
	 */
	public static void setTableLiveJobChains(final String tableLiveJobChains) {
		JobSchedulerManagedObject.tableLiveJobChains = tableLiveJobChains;
	}

	/**
	 * @return the tableLiveJobs
	 */
	public static String getTableLiveJobs() {
		return tableLiveJobs;
	}

	/**
	 * @param tableLiveJobs the tableLiveJobs to set
	 */
	public static void setTableLiveJobs(final String tableLiveJobs) {
		JobSchedulerManagedObject.tableLiveJobs = tableLiveJobs;
	}

	/**
	 * @return the tableLiveLocks
	 */
	public static String getTableLiveLocks() {
		return tableLiveLocks;
	}

	/**
	 * @param tableLiveLocks the tableLiveLocks to set
	 */
	public static void setTableLiveLocks(final String tableLiveLocks) {
		JobSchedulerManagedObject.tableLiveLocks = tableLiveLocks;
	}

	/**
	 * @return the tableLiveObjectHistory
	 */
	public static String getTableLiveObjectHistory() {
		return tableLiveObjectHistory;
	}

	/**
	 * @param tableLiveObjectHistory the tableLiveObjectHistory to set
	 */
	public static void setTableLiveObjectHistory(final String tableLiveObjectHistory) {
		JobSchedulerManagedObject.tableLiveObjectHistory = tableLiveObjectHistory;
	}

	/**
	 * @return the tableLiveObjectMetadata
	 */
	public static String getTableLiveObjectMetadata() {
		return tableLiveObjectMetadata;
	}

	/**
	 * @param tableLiveObjectMetadata the tableLiveObjectMetadata to set
	 */
	public static void setTableLiveObjectMetadata(final String tableLiveObjectMetadata) {
		JobSchedulerManagedObject.tableLiveObjectMetadata = tableLiveObjectMetadata;
	}

	/**
	 * @return the tableLiveObjectReferences
	 */
	public static String getTableLiveObjectReferences() {
		return tableLiveObjectReferences;
	}

	/**
	 * @param tableLiveObjectReferences the tableLiveObjectReferences to set
	 */
	public static void setTableLiveObjectReferences(final String tableLiveObjectReferences) {
		JobSchedulerManagedObject.tableLiveObjectReferences = tableLiveObjectReferences;
	}

	/**
	 * @return the tableLiveObjects
	 */
	public static String getTableLiveObjects() {
		return tableLiveObjects;
	}

	/**
	 * @param tableLiveObjects the tableLiveObjects to set
	 */
	public static void setTableLiveObjects(final String tableLiveObjects) {
		JobSchedulerManagedObject.tableLiveObjects = tableLiveObjects;
	}

	/**
	 * @return the tableLiveOrders
	 */
	public static String getTableLiveOrders() {
		return tableLiveOrders;
	}

	/**
	 * @param tableLiveOrders the tableLiveOrders to set
	 */
	public static void setTableLiveOrders(final String tableLiveOrders) {
		JobSchedulerManagedObject.tableLiveOrders = tableLiveOrders;
	}

	/**
	 * @return the tableLiveProcessClasses
	 */
	public static String getTableLiveProcessClasses() {
		return tableLiveProcessClasses;
	}

	/**
	 * @param tableLiveProcessClasses the tableLiveProcessClasses to set
	 */
	public static void setTableLiveProcessClasses(final String tableLiveProcessClasses) {
		JobSchedulerManagedObject.tableLiveProcessClasses = tableLiveProcessClasses;
	}

	/**
	 * @return the tableLiveSchedules
	 */
	public static String getTableLiveSchedules() {
		return tableLiveSchedules;
	}

	/**
	 * @param tableLiveSchedules the tableLiveSchedules to set
	 */
	public static void setTableLiveSchedules(final String tableLiveSchedules) {
		JobSchedulerManagedObject.tableLiveSchedules = tableLiveSchedules;
	}
}
