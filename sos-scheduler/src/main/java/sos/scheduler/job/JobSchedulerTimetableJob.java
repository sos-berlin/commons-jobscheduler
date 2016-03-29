package sos.scheduler.job;

import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.xml.SOSXMLXPath;

/** @author andreas pueschel
 *
 *         This job is used to - create schedules in a database, - to update
 *         these schedule from the job and order history - to check if jobs and
 *         orders were started and to escalate in case of delays - to create
 *         reports from schedules */
public class JobSchedulerTimetableJob extends JobSchedulerJob {

    private String operation = "";
    private String operationFrom = "";
    private String operationTo = "";
    private Date operationFromDate = null;
    private Date operationToDate = null;
    private long operationDefaultLimit = 0;
    private long operationLimit = 0;
    private Variable_set parameters = null;
    private String tableTimetable = "SCHEDULER_TIMETABLE";
    private String tableTimetableHistory = "SCHEDULER_TIMETABLE_HISTORY";
    private String tableTimetableTriggers = "SCHEDULER_TIMETABLE_TRIGGERS";
    private String tableTimetableAlerts = "SCHEDULER_TIMETABLE_ALERTS";
    private String tableJobHistory = "SCHEDULER_HISTORY";
    private String tableOrderHistory = "SCHEDULER_ORDER_HISTORY";
    private String tableVariables = "SCHEDULER_VARIABLES";

    public boolean spooler_process() {
        boolean rc = true;
        try {
            this.setParameters(spooler.create_variable_set());
            try {
                if (spooler_task.params() != null) {
                    this.getParameters().merge(spooler_task.params());
                }
                if (spooler_job.order_queue() != null) {
                    this.getParameters().merge(spooler_task.order().params());
                }
                if (this.getParameters().value("operation") != null && !this.getParameters().value("operation").isEmpty()) {
                    this.setOperation(this.getParameters().value("operation"));
                    spooler_log.debug1(".. parameter [operation]: " + this.getOperation());
                } else {
                    this.setOperation("timetable");
                }
                if (this.getParameters().value("operation_from") != null && !this.getParameters().value("operation_from").isEmpty()) {
                    this.setOperationFrom(this.getParameters().value("operation_from"));
                    spooler_log.debug1(".. parameter [operation_from]: " + this.getOperationFrom());
                } else {
                    this.setOperationFrom(SOSDate.getCurrentTimeAsString());
                }
                if (this.getParameters().value("operation_to") != null && !this.getParameters().value("operation_to").isEmpty()) {
                    if (this.getParameters().value("operation_to").indexOf(' ') == -1) {
                        this.getParameters().set_var("operation_to", this.getParameters().value("operation_to") + " 23:59:59");
                    }
                    this.setOperationTo(this.getParameters().value("operation_to"));
                    spooler_log.debug1(".. parameter [operation_to]: " + this.getOperationTo());
                } else {
                    try {
                        this.setOperationTo(SOSDate.getCurrentTimeAsString());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(this.getOperationToDate());
                        calendar.add(Calendar.MONTH, 3);
                        this.setOperationTo(SOSDate.getTimeAsString(calendar.getTime()));
                    } catch (Exception e) {
                        throw new Exception("error occurred setting operation target date: " + e.getMessage());
                    }
                }
                if (this.getParameters().value("operation_limit") != null && !this.getParameters().value("operation_limit").isEmpty()) {
                    try {
                        this.setOperationDefaultLimit(Long.parseLong(this.getParameters().value("operation_limit")));
                        spooler_log.debug1(".. parameter [operation_limit]: " + this.getOperationDefaultLimit());
                    } catch (Exception e) {
                        throw new Exception("invalid non-numeric parameter specified [operation_limit]: " + this.getParameters().value("operation_limit"));
                    }
                } else {
                    this.setOperationDefaultLimit(10000);
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing parameters: " + e.getMessage(), e);
            }
            try {
                if (this.getConnection() == null) {
                    throw new Exception("Job Scheduler is running without database: this job requires a datbase connection");
                }
            } catch (Exception e) {
                throw new Exception("error occurred connecting to database: " + e.getMessage());
            }
            try {
                long itemsCreated = 0;
                if ("timetable".equals(this.getOperation())) {
                    itemsCreated = this.createTimetable(this.getOperationFromDate(), this.getOperationToDate());
                    this.getLogger().info(itemsCreated + " job and order timetable entries created");
                } else if ("history".equals(this.getOperation())) {
                    itemsCreated = this.createTimetableHistory();
                    this.getLogger().info("timetable history is up to date");
                } else if ("check".equals(this.getOperation())) {
                    itemsCreated = this.checkTimetableHistory();
                } else if ("report".equals(this.getOperation())) {
                    itemsCreated = this.createTimetableReport();
                } else {
                    throw new Exception("unsupported operation [timetable, history, check, report]: " + this.getOperation());
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage(), e);
            }
            return spooler_job.order_queue() != null ? rc : false;
        } catch (Exception e) {
            spooler_log.error("error occurred for operation [" + this.getOperation() + "]: " + e.getMessage());
            return false;
        }
    }

    public long createTimetable(Date scheduleFrom, Date scheduleTo) throws Exception {
        long itemsCreated = 0;
        long itemCount = 0;
        long timetableID = 0;
        String itemID = "";
        try {
            String calendarRequest = "";
            String calendarResponse = "";
            calendarRequest = "<?xml version='1.0' encoding='iso-8859-1'?><show_calendar " + "from='" + SOSDate.getTimeAsString(scheduleFrom).replace(' ', 'T')
                    + "' " + "before='" + SOSDate.getTimeAsString(scheduleTo).replace(' ', 'T') + "' " + "limit='" + this.getOperationDefaultLimit()
                    + "' what='orders'/>";
            this.getLogger().debug6("..requesting calendar: " + calendarRequest);
            calendarResponse = spooler.execute_xml(calendarRequest);
            this.getLogger().debug6("..response received: " + calendarResponse);
            SOSXMLXPath response = new SOSXMLXPath(new StringBuffer(calendarResponse));
            String errorCode = response.selectSingleNodeValue("//ERROR/@code");
            String errorText = response.selectSingleNodeValue("//ERROR/@text");
            if ((errorCode != null && !errorCode.isEmpty()) || (errorText != null && !errorText.isEmpty())) {
                throw new Exception("error occurred requesting calendar [" + calendarRequest + "]: "
                        + (errorCode != null && !errorCode.isEmpty() ? " error code: " + errorCode : "")
                        + (errorText != null && !errorText.isEmpty() ? " error text: " + errorText : ""));
            }
            this.getConnection().executeUpdate("DELETE FROM " + this.getTableTimetable() + " WHERE \"ID\" NOT IN (SELECT \"ID\" FROM "
                    + this.getTableTimetableHistory() + ")" + " AND \"START_TIME\" BETWEEN %timestamp_iso('" + SOSDate.getTimeAsString(scheduleFrom)
                    + "') AND %timestamp_iso('" + SOSDate.getTimeAsString(scheduleTo) + "')");
            itemID = this.getConnection().getSingleValue("SELECT \"WERT\" FROM " + this.getTableVariables() + " WHERE \"NAME\"='scheduler_timetable_id'");
            if (itemID == null || itemID.isEmpty()) {
                itemID = "0";
                this.getConnection().executeUpdate("INSERT INTO " + this.getTableVariables() + " (\"NAME\", \"WERT\") VALUES ('scheduler_timetable_id', "
                        + itemID + ")");
            }
            try {
                timetableID = Long.parseLong(itemID);
            } catch (Exception e) {
                throw new Exception("illegal non-numeric value found for setting [scheduler_timetable_id] in table " + this.getTableVariables() + ":" + itemID);
            }
            NamedNodeMap calendarNodeAttributes = null;
            String at = "";
            String job = "";
            String jobChain = "";
            String orderID = "";
            SOSXMLXPath calendarDom = new SOSXMLXPath(new StringBuffer(calendarResponse));
            NodeList calendarNodes = calendarDom.selectNodeList("//spooler//answer//calendar//at");
            for (int i = 0; i < calendarNodes.getLength(); i++) {
                at = "";
                job = "";
                jobChain = "";
                orderID = "";
                Node calendarNode = calendarNodes.item(i);
                if ("at".equalsIgnoreCase(calendarNode.getNodeName())) {
                    itemCount++;
                    calendarNodeAttributes = calendarNode.getAttributes();
                    if (calendarNodeAttributes != null) {
                        if (calendarNodeAttributes.getNamedItem("at") != null) {
                            at = calendarNodeAttributes.getNamedItem("at").getNodeValue();
                        }
                        at = at.replace('T', ' ');
                        if (calendarNodeAttributes.getNamedItem("job") != null) {
                            job = calendarNodeAttributes.getNamedItem("job").getNodeValue();
                        }
                        if (calendarNodeAttributes.getNamedItem("job_chain") != null) {
                            jobChain = calendarNodeAttributes.getNamedItem("job_chain").getNodeValue();
                        }
                        if (calendarNodeAttributes.getNamedItem("order") != null) {
                            orderID = calendarNodeAttributes.getNamedItem("order").getNodeValue();
                        }
                    }
                    this.getLogger().debug6("calendar item " + itemCount + ": at=" + at + ", job=" + job + ", job_chain=" + jobChain + ", order=" + orderID);
                    if (!at.isEmpty()) {
                        this.getConnection().executeUpdate("INSERT INTO " + this.getTableTimetable()
                                + " (\"ID\", \"SPOOLER_ID\", \"JOB_CHAIN\", \"ORDER_ID\", \"JOB_NAME\", \"START_TIME\") " + "VALUES (" + ++timetableID + ","
                                + "'" + spooler.id() + "'," + "'" + (!jobChain.isEmpty() ? jobChain : "null") + "'," + "'"
                                + (!orderID.isEmpty() ? orderID : "null") + "', " + "'" + (!job.isEmpty() ? job : "null") + "', "
                                + ("now".equalsIgnoreCase(at) ? "%now" : "%timestamp_iso('" + at + "')") + ")");
                    }
                }
            }
            this.getConnection().executeUpdate("UPDATE " + this.getTableVariables() + " SET \"WERT\"=" + timetableID
                    + " WHERE \"NAME\"='scheduler_timetable_id'");
            this.getConnection().commit();
            itemsCreated += itemCount;
            if (itemCount >= this.getOperationDefaultLimit() && at != null && !at.isEmpty() && SOSDate.getTime(at).before(scheduleTo)) {
                itemsCreated += this.createTimetable(SOSDate.getTime(at), scheduleTo);
            }
            return itemsCreated;
        } catch (Exception e) {
            try {
                this.getConnection().rollback();
            } catch (Exception ex) {
                // gracefully ignore this error
            }
            throw new Exception("error occurred creating timetable: " + e.getMessage(), e);
        }
    }

    public long createTimetableHistory() throws Exception {
        long itemsCreated = 0;
        try {
            this.getConnection().executeUpdate("INSERT INTO " + this.getTableTimetableHistory() + " (\"ID\", \"HISTORY_ID\") "
                    + "SELECT t.\"ID\", h.\"ID\" as \"HISTORY_ID\" " + "FROM " + this.getTableTimetable() + " t, " + this.getTableJobHistory() + " h "
                    + "WHERE (t.\"ID\") NOT IN (SELECT th.\"ID\" FROM " + this.getTableTimetableHistory()
                    + " th WHERE th.\"ID\"=t.\"ID\" AND th.\"HISTORY_ID\"=h.\"ID\") " + "AND t.\"SPOOLER_ID\"=h.\"SPOOLER_ID\" "
                    + "AND t.\"JOB_NAME\"=h.\"JOB_NAME\" " + "AND t.\"START_TIME\"<=h.\"START_TIME\" " + "AND t.\"START_TIME\" <= "
                    + "(SELECT MIN(\"START_TIME\") FROM " + this.getTableTimetable() + " t2 " + "WHERE t.\"SPOOLER_ID\"=t2.\"SPOOLER_ID\" "
                    + "AND t.\"JOB_NAME\"=t2.\"JOB_NAME\" " + "AND t.\"START_TIME\"<=t2.\"START_TIME\")");
            this.getConnection().executeUpdate("INSERT INTO " + this.getTableTimetableHistory() + " (\"ID\", \"HISTORY_ID\") "
                    + "SELECT t.\"ID\", h.\"HISTORY_ID\" " + "FROM " + this.getTableTimetable() + " t, " + this.getTableOrderHistory() + " h "
                    + "WHERE (t.\"ID\") NOT IN (SELECT th.\"ID\" FROM " + this.getTableTimetableHistory()
                    + " th WHERE th.\"ID\"=t.\"ID\" AND th.\"HISTORY_ID\"=h.\"HISTORY_ID\") " + "AND t.\"SPOOLER_ID\"=h.\"SPOOLER_ID\" "
                    + "AND t.\"JOB_CHAIN\"=h.\"JOB_CHAIN\" " + "AND t.\"START_TIME\"<=h.\"START_TIME\" " + "AND t.\"START_TIME\" <= "
                    + "(SELECT MIN(\"START_TIME\") FROM " + this.getTableTimetable() + " t2 " + "WHERE t.\"SPOOLER_ID\"=t2.\"SPOOLER_ID\" "
                    + "AND t.\"JOB_NAME\"=t2.\"JOB_NAME\" " + "AND t.\"START_TIME\"<=t2.\"START_TIME\")");
            this.getConnection().commit();
            return itemsCreated;
        } catch (Exception e) {
            try {
                this.getConnection().rollback();
            } catch (Exception ex) {
                // gracefully ignore this error
            }
            throw new Exception("error occurred creating history for timetable: " + e.getMessage());
        }
    }

    public long checkTimetableHistory() throws Exception {
        long itemsCreated = 0;
        try {
            return itemsCreated;
        } catch (Exception e) {
            throw new Exception("error occurred checking timetable history: " + e.getMessage());
        }
    }

    public long createTimetableReport() throws Exception {
        long itemsCreated = 0;
        try {
            return itemsCreated;
        } catch (Exception e) {
            throw new Exception("error occurred creating timetable report: " + e.getMessage());
        }
    }

    public Variable_set getParameters() {
        return parameters;
    }

    public void setParameters(Variable_set parameters) {
        this.parameters = parameters;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) throws Exception {
        if ("timetable".equalsIgnoreCase(operation)) {
            this.operation = "timetable";
        } else if ("history".equalsIgnoreCase(operation)) {
            this.operation = "history";
        } else if ("check".equalsIgnoreCase(operation)) {
            this.operation = "check";
        } else if ("report".equalsIgnoreCase(operation)) {
            this.operation = "report";
        } else {
            throw new Exception("illegal operation [timetable, history, check, report] specified: " + operation);
        }
    }

    public String getOperationFrom() {
        return operationFrom;
    }

    public void setOperationFrom(String operationFrom) throws Exception {
        Date fromDate = null;
        try {
            operationFrom = operationFrom.trim();
            if (operationFrom.indexOf(' ') == -1) {
                operationFrom += " 00:00:00";
            } else {
                if (operationFrom.indexOf(':') == -1) {
                    operationFrom += ":00:00";
                } else if (operationFrom.indexOf(':') == operationFrom.lastIndexOf(':')) {
                    operationFrom += ":00";
                }
            }
            fromDate = SOSDate.getTime(operationFrom);
        } catch (Exception e) {
            throw new Exception("illegal date format specified for parameter [operation_from]: " + operationFrom);
        }
        this.setOperationFromDate(fromDate);
        this.operationFrom = SOSDate.getTimeAsString(fromDate);
    }

    public Date getOperationFromDate() {
        return operationFromDate;
    }

    public void setOperationFromDate(Date operationFromDate) {
        this.operationFromDate = operationFromDate;
    }

    public String getOperationTo() {
        return operationTo;
    }

    public void setOperationTo(String operationTo) throws Exception {
        Date toDate = null;
        try {
            operationTo = operationTo.trim();
            if (operationTo.indexOf(' ') == -1) {
                operationTo += " 00:00:00";
            } else {
                if (operationTo.indexOf(':') == -1) {
                    operationTo += ":00:00";
                } else if (operationTo.indexOf(':') == operationTo.lastIndexOf(':')) {
                    operationTo += ":00";
                }
            }
            toDate = SOSDate.getTime(operationTo);
        } catch (Exception e) {
            throw new Exception("illegal date format specified for parameter [operation_to]: " + operationTo);
        }
        this.setOperationToDate(toDate);
        this.operationTo = SOSDate.getTimeAsString(toDate);
    }

    public Date getOperationToDate() {
        return operationToDate;
    }

    public void setOperationToDate(Date operationToDate) {
        this.operationToDate = operationToDate;
    }

    public long getOperationDefaultLimit() {
        return operationDefaultLimit;
    }

    public void setOperationDefaultLimit(long operationDefaultLimit) {
        this.operationDefaultLimit = operationDefaultLimit;
    }

    public long getOperationLimit() {
        return operationLimit;
    }

    public void setOperationLimit(long operationLimit) {
        this.operationLimit = operationLimit;
    }

    public String getTableTimetable() {
        return tableTimetable;
    }

    public void setTableTimetable(String tableTimetable) {
        this.tableTimetable = tableTimetable;
    }

    public String getTableTimetableAlerts() {
        return tableTimetableAlerts;
    }

    public void setTableTimetableAlerts(String tableTimetableAlerts) {
        this.tableTimetableAlerts = tableTimetableAlerts;
    }

    public String getTableTimetableHistory() {
        return tableTimetableHistory;
    }

    public void setTableTimetableHistory(String tableTimetableHistory) {
        this.tableTimetableHistory = tableTimetableHistory;
    }

    public String getTableTimetableTriggers() {
        return tableTimetableTriggers;
    }

    public void setTableTimetableTriggers(String tableTimetableTriggers) {
        this.tableTimetableTriggers = tableTimetableTriggers;
    }

    public String getTableJobHistory() {
        return tableJobHistory;
    }

    public void setTableJobHistory(String tableJobHistory) {
        this.tableJobHistory = tableJobHistory;
    }

    public String getTableOrderHistory() {
        return tableOrderHistory;
    }

    public void setTableOrderHistory(String tableOrderHistory) {
        this.tableOrderHistory = tableOrderHistory;
    }

    public String getTableVariables() {
        return tableVariables;
    }

    public void setTableVariables(String tableVariables) {
        this.tableVariables = tableVariables;
    }

}