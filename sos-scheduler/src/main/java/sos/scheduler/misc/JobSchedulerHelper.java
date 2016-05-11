package sos.scheduler.misc;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import sos.connection.SOSConnection;
import sos.connection.SOSMSSQLConnection;
import sos.spooler.Job;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.util.SOSLogger;
import sos.xml.SOSXMLXPath;

/** This class helps to do some tasks in Job Scheduler which are inconvenient
 * when using the Job Scheduler API
 *
 * @author Andreas Liebert */
public class JobSchedulerHelper {

    private Spooler spooler;
    private SOSLogger logger;
    private SimpleDateFormat schedulerDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public JobSchedulerHelper(Spooler spo, SOSLogger log) {
        this.logger = log;
        this.spooler = spo;
    }

    /** Gets a Job Chain object using an absolute or a relative path and the
     * current job
     * 
     * @param jobChainName absolute or relative path of the job chain
     * @param currentJob object representing the current job (to resolve the
     *            relative path)
     * @return Job_chain object
     * @throws Exception if the job chain is not found */
    public Job_chain getJobChain(String jobChainName, Job currentJob) throws Exception {
        Job_chain jobChain = null;
        if (spooler.job_chain_exists(jobChainName)) {
            jobChain = spooler.job_chain(jobChainName);
        } else {
            String jobChainCompletePath = currentJob.folder_path() + "/" + jobChainName;
            if (!spooler.job_chain_exists(jobChainCompletePath)) {
                throw new Exception("Job Chain " + jobChainName + " does not exist");
            }
            jobChain = spooler.job_chain(jobChainCompletePath);
        }
        return jobChain;
    }

    /** Gets a Job Chain path using an absolute or a relative path and the
     * current job
     * 
     * @param jobChainName absolute or relative path of the job chain
     * @param currentJob object representing the current job (to resolve the
     *            relative path)
     * @return Job Chain path
     * @throws Exception if the job chain is not found */
    public String getJobChainPath(String jobChainName, Job currentJob) throws Exception {
        Job_chain jobChain = null;
        if (spooler.job_chain_exists(jobChainName)) {
            return jobChainName;
        } else {
            String jobChainCompletePath = currentJob.folder_path() + "/" + jobChainName;
            if (!spooler.job_chain_exists(jobChainCompletePath)) {
                throw new Exception("Job Chain " + jobChainName + " does not exist");
            }
            return jobChainCompletePath;
        }
    }

    /** Finds the last start time of an order
     * 
     * @param jobChainName Name of the Job Chain
     * @param orderID Id of the order
     * @param connection connected database connectio object to the Job
     *            Scheduler Database
     * @return Calendar object with last start time
     * @throws Exception */
    public Calendar getLastStartOfOrder(String jobChainName, String orderID, SOSConnection connection) throws Exception {
        ResultSet rs = null;
        GregorianCalendar cal;
        try {
            if (jobChainName.startsWith("/")) {
                jobChainName = jobChainName.substring(1);
            }
            String maxQuery = "MAX(s.\"ERROR\")=0";
            if (connection instanceof SOSMSSQLConnection) {
                maxQuery = "MAX(CAST(s.\"ERROR\" AS INT))=0";
            }
            connection.executeStatements("SELECT MAX(h.\"START_TIME\") st FROM SCHEDULER_ORDER_HISTORY h " + "WHERE  h.\"SPOOLER_ID\"='"
                    + spooler.id() + "' AND h.\"JOB_CHAIN\"='" + jobChainName + "' " + "AND h.\"ORDER_ID\"='" + orderID
                    + "' AND h.\"END_TIME\" IS NOT NULL " + "AND h.\"HISTORY_ID\" IN "
                    + "(SELECT s.\"HISTORY_ID\" FROM SCHEDULER_ORDER_STEP_HISTORY s GROUP BY s.\"HISTORY_ID\" HAVING " + maxQuery + ")");

            rs = connection.getResultSet();
            if (rs == null) {
                throw new Exception("Resultset is null");
            }
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                if (ts == null) {
                    return null;
                }
                long milliseconds = ts.getTime() + (ts.getNanos() / 1000000);
                cal = new GregorianCalendar();
                cal.setTimeInMillis(milliseconds);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new Exception("Error retrieving last start of order:" + e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
        }
        return cal;
    }

    /** Return the start time of a currently active order
     * 
     * @param order order object for the start time
     * @return Calendar with the start time of the order
     * @throws Exception */
    public Calendar getStartOfOrder(Order order) throws Exception {
        String xml = order.xml();
        StringBuffer xmlBuf = new StringBuffer(xml);
        SOSXMLXPath xp = new SOSXMLXPath(xmlBuf);
        String startTime = xp.selectSingleNodeValue("/order/@start_time");
        if (startTime == null || startTime.isEmpty()) {
            throw new Exception("No start_time attribute was found for the current order");
        }
        Date dat = schedulerDateFormat.parse(startTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dat);
        return cal;
    }

}