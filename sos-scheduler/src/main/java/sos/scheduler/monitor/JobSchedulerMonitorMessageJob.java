package sos.scheduler.monitor;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.spooler.Web_service_request;
import sos.spooler.Web_service_response;
import sos.spooler.Xslt_stylesheet;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/** @author andreas pueschel
 * @author mueruevet oeksuez */
public class JobSchedulerMonitorMessageJob extends Job_impl {

    protected int verbosity = 0;
    protected String monitorJob = "JobSchedulerMonitorMessageJob";
    protected String messageType = "status";
    protected boolean isMessagePersistent = true;
    protected Vector messageVariables = new Vector();
    private SOSString sosString = new SOSString();
    private String monitorJobname = "";
    private String monitorJobChainname = "";
    private boolean logMessages = false;
    private boolean NoWebServiceResponse = false;
    private String includeSupervisorMsg = "[WARN](Supervisor;[ERROR](Supervisor";
    private boolean includeSupervisor = false;
    private String excludeMessages = null;
    private String maxLifeTime = null;
    private int maximumReports = -1;
    private String logFileName = "";

    protected String normalizeItem(String item) {
        item = item.replaceAll("&", "&amp;");
        item = item.replaceAll("<", "&lt;");
        item = item.replaceAll(">", "&gt;");
        return item;
    }

    public Vector getLogMessages() {
        Vector messages = new Vector();
        HashMap message = new HashMap();
        this.messageVariables = new Vector();
        HashMap ignoreMSG = new HashMap();
        try {
            long logFilepointer = 0;
            String logFilename = "";
            String logLine = "";
            File logFile = null;
            File prevLogFile = null;
            RandomAccessFile checkFile = null;
            RandomAccessFile checkPrevFile = null;
            ignoreMSG = getIgnoreMessages();
            if ("reset".equalsIgnoreCase(this.getMessageType())) {
                spooler.set_var(getMonitorJob() + ".messages", "");
                spooler_log.info("message stack has been reset");
                message = new HashMap();
                message.put("info", SOSDate.getCurrentTimeAsString() + ": warnings and errors were reset");
                messages.add(message);
                this.setMessageType("status");
            }
            try {
                if (this.isMessagePersistent()) {
                    String messageSet = spooler.var(this.getMonitorJob() + ".messages");
                    if (messageSet != null && !messageSet.isEmpty()) {
                        messageSet = messageSet.replaceAll(String.valueOf((char) 254), "<").replaceAll(String.valueOf((char) 255), ">");
                        SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(messageSet));
                        NodeList nodelist = xpath.selectNodeList("//messages/*");
                        for (int i = 0; i < nodelist.getLength(); i++) {
                            Node node = nodelist.item(i);
                            if (node == null) {
                                continue;
                            }
                            NodeList itemlist = xpath.selectNodeList(node, "*");
                            String curSeverity = "info";
                            String curContent = "";
                            for (int j = 0; j < itemlist.getLength(); j++) {
                                Node item = itemlist.item(j);
                                if (item == null) {
                                    continue;
                                }
                                if ("severity".equals(item.getNodeName()) && item.getFirstChild() != null) {
                                    curSeverity = item.getFirstChild().getNodeValue();
                                } else if ("content".equals(item.getNodeName()) && item.getFirstChild() != null) {
                                    curContent = item.getFirstChild().getNodeValue();
                                }
                            }
                            if (isValidMessages(curContent, ignoreMSG)) {
                                curContent = incrementReportCounter(curContent);
                                message = new HashMap();
                                spooler_log.debug9(".." + curSeverity + " = " + curContent);
                                message.put(curSeverity, curContent);
                                messages.add(message);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                spooler_log.info("persistent messages processed with errors: " + e.getMessage());
            }
            try {
                String logFilepointerVariable = spooler.var(this.getMonitorJob() + ".filepointer");
                String logFilenameVariable = spooler.var(this.getMonitorJob() + ".filename");
                if (logFilepointerVariable != null && !logFilepointerVariable.isEmpty()) {
                    logFilepointer = Long.parseLong(logFilepointerVariable);
                }
                if (logFilenameVariable != null && !logFilenameVariable.isEmpty()) {
                    logFilename = logFilenameVariable;
                }
                if (logFilename != null && !logFilename.isEmpty() && !logFilename.equals(new File(spooler.log().filename()).getCanonicalPath())) {
                    prevLogFile = new File(logFilename);
                    if (!prevLogFile.exists()) {
                        spooler_log.info("log file from previous job execution not found: " + prevLogFile.getCanonicalPath());
                    }
                    checkPrevFile = new RandomAccessFile(prevLogFile, "r");
                    if (logFilepointer > 0) {
                        checkPrevFile.seek(logFilepointer);
                        spooler_log.debug3("starting to check previous log file [" + prevLogFile.getCanonicalPath() + "] from position: " + logFilepointer);
                    }
                    while ((logLine = checkPrevFile.readLine()) != null) {
                        if (logLine.matches("^\\d{4}-\\d{1,2}-\\d{1,2} +\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{1,3})? +\\[WARN\\] +.*")) {
                            message = new HashMap();
                            spooler_log.debug9("..old WARN =" + logLine);
                            message.put("WARN", logLine);
                            messages.add(message);
                        } else if (logLine.matches("^\\d{4}-\\d{1,2}-\\d{1,2} +\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{1,3})? +\\[ERROR\\] +.*")) {
                            message = new HashMap();
                            spooler_log.debug9("..old ERROR =" + logLine);
                            message.put("ERROR", logLine);
                            messages.add(message);
                        }
                    }
                    spooler_log.debug3("previous log file [" + prevLogFile.getCanonicalPath() + "] processed");
                    logFilepointer = 0;
                    logFilename = "";
                }
            } catch (Exception e) {
                spooler_log.info("previous log file [" + ((prevLogFile != null) ? prevLogFile.getCanonicalPath() : "") + "] processed with errors: "
                        + e.getMessage());
                logFilepointer = 0;
                logFilename = "";
            } finally {
                if (checkPrevFile != null) {
                    try {
                        checkPrevFile.close();
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            try {
                if (!sosString.parseToString(getLogFilename()).isEmpty()) {
                    logFile = new File(sosString.parseToString(getLogFilename()));
                } else {
                    logFile = new File(spooler.log().filename());
                }
                if (!logFile.exists()) {
                    throw new Exception("log file not found: " + logFile.getCanonicalPath());
                }
                checkFile = new RandomAccessFile(logFile, "r");
                if (logFilepointer > 0) {
                    checkFile.seek(logFilepointer);
                    spooler_log.debug3("starting to check current log file [" + logFile.getCanonicalPath() + "] from position: " + logFilepointer);
                }
                ArrayList listOfMonitoringJobs = new ArrayList();
                if (!sosString.parseToString(getMonitorJobname()).isEmpty()) {
                    String[] split = getMonitorJobname().split(";");
                    for (int i = 0; i < split.length; i++) {
                        if (!sosString.parseToString(split[i]).isEmpty()) {
                            listOfMonitoringJobs.add(split[i]);
                        }
                    }
                    spooler_log.debug("..monitoring job: " + getMonitorJobname());
                }
                ArrayList listOfMonitoringJobChains = new ArrayList();
                if (!sosString.parseToString(getMonitorJobChainname()).isEmpty()) {
                    String[] split = getMonitorJobChainname().split(";");
                    for (int i = 0; i < split.length; i++) {
                        if (!sosString.parseToString(split[i]).isEmpty()) {
                            listOfMonitoringJobChains.add(split[i]);
                        }
                    }
                    spooler_log.debug("..monitoring jobchain: " + getMonitorJobChainname());
                }
                String currLogJobName = "";
                String currOrderName = "";
                int countOfLines = 0;
                HashMap jobnames = new HashMap();
                HashMap tasksList = new HashMap();
                while ((logLine = checkFile.readLine()) != null) {
                    if (!sosString.parseToString(getMonitorJobname()).isEmpty() && !isMonitorJobname(logLine, listOfMonitoringJobs)) {
                        countOfLines++;
                        continue;
                    }
                    if (!sosString.parseToString(getMonitorJobChainname()).isEmpty() && !isMonitorJobChainname(logLine, listOfMonitoringJobChains)) {
                        countOfLines++;
                        continue;
                    }
                    if (logLine.indexOf("SCHEDULER-930  Task ") > -1) {
                        ArrayList stateList = new ArrayList();
                        stateList.add(logLine);
                        currLogJobName = getCurrentJobname(logLine, false);
                        if (logLine.toLowerCase().indexOf("cause: order") > -1) {
                            String task = getTask(logLine);
                            String order = getOrderNameAndValue(logLine);
                            currLogJobName = currLogJobName + "|" + order;
                            tasksList.put(task, currLogJobName);
                        }
                        messages = removeJobFromMessages(currLogJobName, messages);
                        jobnames.put(currLogJobName, stateList);
                        spooler_log.debug6("..monitoring from [job=" + currLogJobName + "]");
                    }
                    logLine = logLine + "[report_counter=1]";
                    if (logLine.matches("^\\d{4}-\\d{1,2}-\\d{1,2} +\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{1,3})? +\\[WARN\\] +.*")
                            && !isIgnoreMessages(logLine, ignoreMSG)) {
                        String task = getTask(logLine);
                        if (tasksList.get(task) != null) {
                            currLogJobName = sosString.parseToString(tasksList.get(task));
                        } else {
                            currLogJobName = getCurrentJobname(logLine, false);
                        }
                        ArrayList stateList = (ArrayList) jobnames.get(currLogJobName);
                        if (stateList == null) {
                            stateList = new ArrayList();
                            messages = removeJobFromMessages(currLogJobName, messages);
                            jobnames.put(currLogJobName, stateList);
                        }
                        stateList.add(logLine);
                    } else if (logLine.matches("^\\d{4}-\\d{1,2}-\\d{1,2} +\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{1,3})? +\\[ERROR\\] +.*")
                            && !isIgnoreMessages(logLine, ignoreMSG)) {
                        String task = getTask(logLine);
                        if (tasksList.get(task) != null) {
                            currLogJobName = sosString.parseToString(tasksList.get(task));
                        } else {
                            currLogJobName = getCurrentJobname(logLine, false);
                        }
                        ArrayList stateList = (ArrayList) jobnames.get(currLogJobName);
                        if (stateList == null) {
                            stateList = new ArrayList();
                            messages = removeJobFromMessages(currLogJobName, messages);
                            jobnames.put(currLogJobName, stateList);
                        }
                        stateList.add(logLine);
                    }
                }
                if (countOfLines > 0) {
                    spooler_log.debug(".." + countOfLines + " line skip, cause no monitoring job");
                    countOfLines = 0;
                }
                Iterator itKeys = jobnames.keySet().iterator();
                while (itKeys.hasNext()) {
                    currLogJobName = sosString.parseToString(itKeys.next());
                    ArrayList stateList = (ArrayList) jobnames.get(currLogJobName);
                    currOrderName = "";
                    for (int i = 0; i < stateList.size(); i++) {
                        String msgLine = sosString.parseToString(stateList.get(i));
                        if (msgLine.indexOf("SCHEDULER-930  Task ") > -1 && msgLine.toLowerCase().indexOf("cause: order") > -1) {
                            int pos1 = msgLine.toLowerCase().indexOf("cause: order") + "cause: order".length() + 1;
                            if (pos1 > -1) {
                                currOrderName = msgLine.substring(pos1);
                            }
                        }
                        if (msgLine.matches("^\\d{4}-\\d{1,2}-\\d{1,2} +\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{1,3})? +\\[WARN\\] +.*")) {
                            message = new HashMap();
                            if (!sosString.parseToString(currOrderName).isEmpty()) {
                                msgLine = "[order=" + currOrderName + "]" + msgLine;
                            }
                            spooler_log.debug9("..new WARN= " + msgLine);
                            message.put("WARN", msgLine);
                            messages.add(message);
                        } else if (msgLine.matches("^\\d{4}-\\d{1,2}-\\d{1,2} +\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{1,3})? +\\[ERROR\\] +.*")) {
                            message = new HashMap();
                            if (!sosString.parseToString(currOrderName).isEmpty()) {
                                msgLine = "[order=" + currOrderName + "]" + msgLine;
                            }
                            spooler_log.debug9("..new ERROR= " + msgLine);
                            message.put("ERROR", msgLine);
                            messages.add(message);
                        }
                    }
                }
                Vector curMsg = new Vector();
                Iterator logMsg = messages.iterator();
                HashMap h = null;
                while (logMsg.hasNext()) {
                    h = (HashMap) logMsg.next();
                    if (!h.isEmpty()) {
                        curMsg.add(h);
                    }
                }
                messages = curMsg;
                logMsg = messages.iterator();
                if (this.isLogMessages() || spooler_log.level() <= -3) {
                    spooler_log.info("The following warnings and errors are reported:");
                    while (logMsg.hasNext()) {
                        h = (HashMap) logMsg.next();
                        spooler_log.info(transformString(h));
                    }
                }
                spooler.set_var(this.getMonitorJob() + ".filename", logFile.getCanonicalPath());
                spooler.set_var(this.getMonitorJob() + ".filepointer", Long.toString(checkFile.getFilePointer()));
                spooler_log.debug3("current log file [" + logFile.getCanonicalPath() + "] processed to position: " + Long.toString(checkFile.getFilePointer()));
                return messages;
            } catch (Exception e) {
                throw new Exception(e);
            } finally {
                if (checkFile != null) {
                    try {
                        checkFile.close();
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
        } catch (Exception e) {
            spooler_log.info("getLogMessages(): failed to check messages in log file [" + spooler.log().filename() + "]: " + e.getMessage());
            return messages;
        }
    }

    private String getCurrentJobname(String logLine, boolean withTaskId) throws Exception {
        try {
            int sPos = 0;
            if (logLine.indexOf("(Task ") > -1) {
                sPos = logLine.indexOf("(Task ") + 6;
            } else if (logLine.indexOf("(Job ") > -1) {
                sPos = logLine.indexOf("(Job  ") + 6;
            } else {
                sPos = logLine.indexOf("(") + 1;
            }
            int ePos = 0;
            if (withTaskId || logLine.indexOf("(Job  ") > -1) {
                ePos = logLine.substring(sPos).indexOf(")");
            } else if (logLine.indexOf("(Task ") > -1) {
                ePos = logLine.substring(sPos).indexOf(":");
            } else {
                ePos = logLine.substring(sPos).indexOf(")");
            }
            String currLogJobName = "";
            if (sPos == -1 || ePos == -1 || (sPos + ePos) <= -1) {
                currLogJobName = "";
            } else {
                currLogJobName = logLine.substring(sPos, sPos + ePos);
            }
            return currLogJobName;
        } catch (Exception e) {
            spooler_log.info("error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage());
            return "";
        }
    }

    private String getTask(String logLine) {
        String retval = "";
        if (logLine.indexOf("SCHEDULER-930") > -1 && logLine.toLowerCase().indexOf("cause: order") > -1) {
            logLine = logLine.replaceAll(" ", "");
            int pos1 = logLine.indexOf("SCHEDULER-930Task") + "SCHEDULER-930Task".length();
            int pos2 = logLine.indexOf("started");
            if (pos1 > -1 && pos2 > -1) {
                retval = logLine.substring(pos1, pos2);
            }
        } else if (logLine.indexOf("(Task") > -1) {
            int pos1 = logLine.indexOf(":", logLine.indexOf("(Task")) + 1;
            int pos2 = logLine.indexOf(")", logLine.indexOf("(Task"));
            if (pos1 > -1 && pos2 > -1) {
                retval = logLine.substring(pos1, pos2);
            }
        }
        return retval;
    }

    private String getOrderNameAndValue(String logLine) {
        String retval = "";
        if (logLine.startsWith("[order=")) {
            return logLine.substring("[order=".length(), logLine.indexOf("]"));
        }
        if (logLine.toLowerCase().indexOf("cause: order") == -1) {
            return retval;
        }
        logLine = logLine.replaceAll(" ", "");
        int pos1 = logLine.toLowerCase().indexOf("cause:order") + "cause:order".length();
        if (pos1 > -1) {
            retval = logLine.substring(pos1);
        }
        return retval;
    }

    private String getCurrentOrdername(String logLine) throws Exception {
        String currLogOrderName = "";
        try {
            if (logLine.indexOf("SCHEDULER-930  Task ") > -1 && logLine.toLowerCase().indexOf("cause: order") == -1) {
                int sPos = logLine.toLowerCase().indexOf("cause: order") + 12;
                int ePos = logLine.substring(sPos).indexOf(":");
                currLogOrderName = logLine.substring(sPos, sPos + ePos);
            }
            return currLogOrderName;
        } catch (Exception e) {
            spooler_log.info("error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage());
            return currLogOrderName;
        }
    }

    public boolean spooler_process() {
        Order order = null;
        Variable_set orderData = null;
        try {
            try {
                if (spooler_task.params().var("job") != null && !spooler_task.params().var("job").isEmpty()) {
                    this.setMonitorJob(spooler_task.params().var("job"));
                    spooler_log.debug1(".. job parameter [job]: " + this.getMonitorJob());
                } else if (this.getMonitorJob() == null || this.getMonitorJob().isEmpty()) {
                    this.setMonitorJob(spooler_job.name());
                }
                if (spooler_task.params().var("type") != null && !spooler_task.params().var("type").isEmpty()) {
                    this.setMessageType(spooler_task.params().var("type"));
                    spooler_log.debug1(".. job parameter [type]: " + this.getMessageType());
                }
                if (spooler_task.params().var("persistent") != null && !spooler_task.params().var("persistent").isEmpty()) {
                    if ("0".equals(spooler_task.params().var("persistent")) || "false".equalsIgnoreCase(spooler_task.params().var("persistent"))
                            || "no".equalsIgnoreCase(spooler_task.params().var("persistent"))) {
                        this.setMessagePersistent(false);
                        spooler_log.debug1(".. job parameter [persistent]: " + this.isMessagePersistent());
                    } else {
                        this.setMessagePersistent(true);
                    }
                }
                if (spooler_task.params().var("verbose") != null && !spooler_task.params().var("verbose").isEmpty()) {
                    try {
                        this.setVerbosity(Integer.parseInt(spooler_task.params().var("verbose")));
                        spooler_log.debug1(".. job parameter [verbose]: " + this.getVerbosity());
                    } catch (Exception e) {
                        spooler_log.info("unknown verbosity level [0-3] in job parameter [verbose]: " + spooler_task.params().var("verbose"));
                    }
                }
                if (!sosString.parseToString(spooler_task.params().var("monitor_job_name")).isEmpty()) {
                    this.setMonitorJobname(spooler_task.params().var("monitor_job_name"));
                    spooler_log.debug1(".. job parameter [monitor_job_name]: " + this.getMonitorJobname());
                } else {
                    this.setMonitorJobname("");
                }
                if (!sosString.parseToString(spooler_task.params().var("monitor_job_chain_name")).isEmpty()) {
                    this.setMonitorJobChainname(spooler_task.params().var("monitor_job_chain_name"));
                    spooler_log.debug1(".. job parameter [monitor_job_chain_name]: " + this.getMonitorJobChainname());
                } else {
                    this.setMonitorJobChainname("");
                }
                if (!sosString.parseToString(spooler_task.params().var("log_messages")).isEmpty()) {
                    this.setLogMessages(sosString.parseToBoolean(spooler_task.params().var("log_messages")));
                    spooler_log.debug1(".. job parameter [log_messages]: " + this.isLogMessages());
                }

                if (!sosString.parseToString(spooler_task.params().var("no_webservice")).isEmpty()) {
                    this.setNoWebServiceResponse(sosString.parseToBoolean(spooler_task.params().var("no_webservice")));
                    spooler_log.debug1(".. job parameter [no_webservice]: " + this.isNoWebServiceResponse());
                } else {
                    this.setNoWebServiceResponse(false);
                }
                if (!sosString.parseToString(spooler_task.params().var("exclude_messages")).isEmpty()) {
                    this.setExcludeMessages(sosString.parseToString(spooler_task.params().var("exclude_messages")));
                    spooler_log.debug1(".. job parameter [exclude_messages]: " + this.getExcludeMessages());
                } else {
                    setExcludeMessages("");
                }
                if (!sosString.parseToString(spooler_task.params().var("include_supervisor")).isEmpty()) {
                    this.setIncludeSupervisor(sosString.parseToBoolean(spooler_task.params().var("include_supervisor")));
                    spooler_log.debug1(".. job parameter [include_supervisor]: " + this.isIncludeSupervisor());
                } else {
                    this.setIncludeSupervisor(false);
                }
                if (!sosString.parseToString(spooler_task.params().var("maximum_lifetime")).isEmpty()) {
                    this.setMaxLifeTime(sosString.parseToString(spooler_task.params().var("maximum_lifetime")));
                    spooler_log.debug1(".. job parameter [maximum_lifetime]: " + this.getMaxLifeTime());
                } else {
                    this.setMaxLifeTime("");
                }
                if (!sosString.parseToString(spooler_task.params().var("maximum_reports")).isEmpty()) {
                    try {
                        this.setMaximumReports(Integer.parseInt(sosString.parseToString(spooler_task.params().var("maximum_reports"))));
                        spooler_log.debug1(".. job parameter [maximum_reports]: " + this.getMaximumReports());
                    } catch (Exception e) {
                        setMaximumReports(-1);
                        spooler_log.debug1("..job parameter [maximum_reports=" + sosString.parseToString(orderData.var("maximum_reports"))
                                + "] is not valid cause: " + e.getMessage());
                    }
                } else {
                    setMaximumReports(-1);
                }
                if (!sosString.parseToString(spooler_task.params().var("log_file")).isEmpty()) {
                    this.setLogFilename(sosString.parseToString(spooler_task.params().var("log_file")));
                    spooler_log.debug1(".. job parameter [log_file]: " + this.getLogFilename());
                } else {
                    this.setLogFilename("");
                }
                if (spooler_task.job().order_queue() != null) {
                    order = spooler_task.order();
                    if (order.web_service_operation_or_null() != null) {
                        SOSXMLXPath xpath = null;
                        Web_service_request request = order.web_service_operation().request();
                        if (order.web_service().params().var("request_stylesheet") != null && !order.web_service().params().var("request_stylesheet").isEmpty()) {
                            Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                            stylesheet.load_file(order.web_service().params().var("request_stylesheet"));
                            String xml_document = stylesheet.apply_xml(request.string_content());
                            spooler_log.debug1("content of request:\n" + request.string_content());
                            spooler_log.debug1("content of request transformation:\n" + xml_document);
                            xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(xml_document));
                            Variable_set params = spooler.create_variable_set();
                            if (xpath.selectSingleNodeValue("//param[@name[.='verbose']]/@value") != null) {
                                params.set_var("verbose", xpath.selectSingleNodeValue("//param[@name[.='verbose']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='type']]/@value") != null) {
                                params.set_var("type", xpath.selectSingleNodeValue("//param[@name[.='type']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='persistent']]/@value") != null) {
                                params.set_var("persistent", xpath.selectSingleNodeValue("//param[@name[.='persistent']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='monitor_job_name']]/@value") != null) {
                                params.set_var("monitor_job_name", xpath.selectSingleNodeValue("//param[@name[.='monitor_job_name']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='monitor_job_chain_name']]/@value") != null) {
                                params.set_var("monitor_job_chain_name", xpath.selectSingleNodeValue("//param[@name[.='monitor_job_chain_name']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='log_messages']]/@value") != null) {
                                params.set_var("log_messages", xpath.selectSingleNodeValue("//param[@name[.='log_messages']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='no_webservice']]/@value") != null) {
                                params.set_var("no_webservice", xpath.selectSingleNodeValue("//param[@name[.='no_webservice']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='exclude_messages']]/@value") != null) {
                                params.set_var("exclude_messages", xpath.selectSingleNodeValue("//param[@name[.='exclude_messages']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='include_supervisor']]/@value") != null) {
                                params.set_var("include_supervisor", xpath.selectSingleNodeValue("//param[@name[.='include_supervisor']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='maximum_lifetime']]/@value") != null) {
                                params.set_var("maximum_lifetime", xpath.selectSingleNodeValue("//param[@name[.='maximum_lifetime']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='maximum_reports']]/@value") != null) {
                                params.set_var("maximum_reports", xpath.selectSingleNodeValue("//param[@name[.='maximum_reports']]/@value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[@name[.='log_file']]/@value") != null) {
                                params.set_var("log_file", xpath.selectSingleNodeValue("//param[@name[.='log_file']]/@value"));
                            }
                            order.set_payload(params);
                        } else {
                            xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(request.string_content()));
                            Variable_set params = spooler.create_variable_set();
                            if (xpath.selectSingleNodeValue("//param[name[text()='verbose']]/value") != null) {
                                params.set_var("verbose", xpath.selectSingleNodeValue("//param[name[text()='verbose']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='type']]/value") != null) {
                                params.set_var("type", xpath.selectSingleNodeValue("//param[name[text()='type']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='persistent']]/value") != null) {
                                params.set_var("persistent", xpath.selectSingleNodeValue("//param[name[text()='persistent']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='monitor_job_name']]/value") != null) {
                                params.set_var("monitor_job_name", xpath.selectSingleNodeValue("//param[name[text()='monitor_job_name']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='monitor_job_chain_name']]/value") != null) {
                                params.set_var("monitor_job_chain_name", xpath.selectSingleNodeValue("//param[name[text()='monitor_job_chain_name']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='log_messages']]/value") != null) {
                                params.set_var("log_messages", xpath.selectSingleNodeValue("//param[name[text()='log_messages']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='no_webservice']]/value") != null) {
                                params.set_var("no_webservice", xpath.selectSingleNodeValue("//param[name[text()='no_webservice']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='exclude_messages']]/value") != null) {
                                params.set_var("exclude_messages", xpath.selectSingleNodeValue("//param[name[text()='exclude_messages']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='include_supervisor']]/value") != null) {
                                params.set_var("include_supervisor", xpath.selectSingleNodeValue("//param[name[text()='include_supervisor']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='maximum_lifetime']]/value") != null) {
                                params.set_var("maximum_lifetime", xpath.selectSingleNodeValue("//param[name[text()='maximum_lifetime']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='maximum_reports']]/value") != null) {
                                params.set_var("maximum_reports", xpath.selectSingleNodeValue("//param[name[text()='maximum_reports']]/value"));
                            }
                            if (xpath.selectSingleNodeValue("//param[name[text()='log_file']]/value") != null) {
                                params.set_var("log_file", xpath.selectSingleNodeValue("//param[name[text()='log_file']]/value"));
                            }
                            order.set_payload(params);
                        }
                    }
                    if (order != null) {
                        orderData = order.params();
                        if (orderData.var("type") != null && !orderData.var("type").toString().isEmpty()) {
                            this.setMessageType(orderData.var("type").toString());
                            spooler_log.debug1(".. order parameter [type]: " + this.getMessageType());
                        }
                        if (orderData.var("persistent") != null
                                && !orderData.var("persistent").toString().isEmpty()
                                && ("0".equals(orderData.var("persistent")) || "false".equalsIgnoreCase(orderData.var("persistent")) || "no"
                                        .equalsIgnoreCase(orderData.var("persistent")))) {
                            this.setMessagePersistent(false);
                            spooler_log.debug1(".. order parameter [persistent]: " + this.isMessagePersistent());
                        }
                        if (orderData != null && orderData.var("verbose") != null && !orderData.var("verbose").toString().isEmpty()) {
                            try {
                                this.setVerbosity(Integer.parseInt(orderData.var("verbose")));
                                spooler_log.debug1(".. order parameter [verbose]: " + this.getVerbosity());
                            } catch (Exception e) {
                                spooler_log.info("unknown verbosity level [0-3] in order parameter [verbose]: " + orderData.var("verbose"));
                            }
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("monitor_job_name")).isEmpty()) {
                            this.setMonitorJobname(sosString.parseToString(orderData.var("monitor_job_name")));
                            spooler_log.debug1(".. order parameter [monitor_job_name]: " + this.getMonitorJobname());
                        } else {
                            this.setMonitorJobname("");
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("monitor_job_chain_name")).isEmpty()) {
                            this.setMonitorJobChainname(sosString.parseToString(orderData.var("monitor_job_chain_name")));
                            spooler_log.debug1(".. order parameter [monitor_job_chain_name]: " + this.getMonitorJobChainname());
                        } else {
                            this.setMonitorJobChainname("");
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("log_messages")).isEmpty()) {
                            this.setLogMessages(sosString.parseToBoolean(orderData.var("log_messages")));
                            spooler_log.debug1(".. order parameter [log_messages]: " + this.isLogMessages());
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("no_webservice")).isEmpty()) {
                            this.setNoWebServiceResponse(sosString.parseToBoolean(orderData.var("no_webservice")));
                            spooler_log.debug1(".. order parameter [no_webservice]: " + this.isNoWebServiceResponse());
                        } else {
                            this.setNoWebServiceResponse(false);
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("exclude_messages")).isEmpty()) {
                            setExcludeMessages(sosString.parseToString(orderData.var("exclude_messages")));
                            spooler_log.debug1(".. order parameter [exclude_messages]: " + this.getExcludeMessages());
                        } else {
                            setExcludeMessages("");
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("include_supervisor")).isEmpty()) {
                            this.setIncludeSupervisor(sosString.parseToBoolean(orderData.var("include_supervisor")));
                            spooler_log.debug1(".. order parameter [include_supervisor]: " + this.isIncludeSupervisor());
                        } else {
                            this.setIncludeSupervisor(false);
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("maximum_lifetime")).isEmpty()) {
                            setMaxLifeTime(sosString.parseToString(orderData.var("maximum_lifetime")));
                            spooler_log.debug1(".. order parameter [maximum_lifetime]: " + this.getMaxLifeTime());
                        } else {
                            setMaxLifeTime("");
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("maximum_reports")).isEmpty()) {
                            try {
                                setMaximumReports(Integer.parseInt(sosString.parseToString(orderData.var("maximum_reports"))));
                                spooler_log.debug1(".. order parameter [maximum_reports]: " + this.getMaximumReports());
                            } catch (Exception e) {
                                setMaximumReports(-1);
                                spooler_log.debug1("..order parameter [maximum_reports=" + sosString.parseToString(orderData.var("maximum_reports"))
                                        + "] is not valid cause: " + e.getMessage());
                            }
                        } else {
                            setMaximumReports(-1);
                        }
                        if (orderData != null && !sosString.parseToString(orderData.var("log_file")).isEmpty()) {
                            setLogFilename(sosString.parseToString(orderData.var("log_file")));
                            spooler_log.debug1(".. order parameter [log_file]: " + this.getLogFilename());
                        } else {
                            setLogFilename("");
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing parameters: " + e.toString());
            }
            try {
                int countMessages = 0;
                int countInfos = 0;
                int countWarnings = 0;
                int countErrors = 0;
                if (!sosString.parseToString(getMonitorJobname()).isEmpty() && !sosString.parseToString(getMonitorJobChainname()).isEmpty()) {
                    throw new Exception("..parameter [monitor_job_chain_name] and [monitor_job_name] at same time is not allowed");
                }
                Vector messages = getLogMessages();
                if (messages != null && !messages.isEmpty()) {
                    String xml_payload = "";
                    Iterator messageIterator = messages.iterator();
                    while (messageIterator.hasNext()) {
                        HashMap message = (HashMap) messageIterator.next();
                        if (message == null) {
                            continue;
                        }
                        countMessages++;
                        if (spooler_task.job().order_queue() != null) {
                            if (message.get("WARN") != null) {
                                countWarnings++;
                                xml_payload += "<message id='"
                                        + countMessages
                                        + "' severity='WARN'><![CDATA["
                                        + this.normalizeItem(message.get("WARN").toString()).substring(0,
                                                message.get("WARN").toString().indexOf("[report_counter=")) + "]]></message>";
                            } else if (message.get("ERROR") != null) {
                                countErrors++;
                                xml_payload += "<message id='"
                                        + countMessages
                                        + "' severity='ERROR'><![CDATA["
                                        + this.normalizeItem(message.get("ERROR").toString()).substring(0,
                                                message.get("ERROR").toString().indexOf("[report_counter=")) + "]]></message>";
                            } else if (message.get("info") != null) {
                                countInfos++;
                                xml_payload += "<message id='" + countMessages + "' severity='info'><![CDATA["
                                        + this.normalizeItem(message.get("info").toString()) + "]]></message>";
                            }
                        } else {
                            if (message.get("WARN") != null) {
                                countWarnings++;
                                spooler_task.params().set_var("message_" + countMessages, this.normalizeItem(message.get("WARN").toString()));
                            } else if (message.get("ERROR") != null) {
                                countErrors++;
                                spooler_task.params().set_var("message_" + countMessages, this.normalizeItem(message.get("ERROR").toString()));
                            } else if (message.get("info") != null) {
                                countInfos++;
                                spooler_task.params().set_var("message_" + countMessages, this.normalizeItem(message.get("info").toString()));
                            }
                        }
                        if (isMessagePersistent()) {
                            if (message.get("WARN") != null && message.get("WARN").toString() != null) {
                                this.messageVariables.add("<message id='" + countMessages + "'><severity>WARN</severity><content>"
                                        + this.normalizeItem(message.get("WARN").toString()) + "</content></message>");
                            } else if (message.get("ERROR") != null && message.get("ERROR").toString() != null) {
                                this.messageVariables.add("<message id='" + countMessages + "'><severity>ERROR</severity><content>"
                                        + this.normalizeItem(message.get("ERROR").toString()) + "</content></message>");
                            }
                        }
                    }
                    if (spooler_task.job().order_queue() != null) {
                        order.set_xml_payload("<?xml version='1.0' encoding='iso-8859-1'?><xml_payload><messages errors='" + countErrors + "' warnings='"
                                + countWarnings + "' infos='" + countInfos + "'>" + xml_payload + "</messages></xml_payload>");
                    } else {
                        spooler_task.params().set_var("errors", Integer.toString(countErrors));
                        spooler_task.params().set_var("warnings", Integer.toString(countWarnings));
                        spooler_task.params().set_var("infos", Integer.toString(countInfos));
                    }
                }
                spooler_log.info(countErrors + " errors, " + countWarnings + " warnings found");
            } catch (Exception e) {
                throw new Exception("error occurred processing messages: " + e.getMessage());
            }
            try {
                if (spooler_task.job().order_queue() != null) {
                    order = spooler_task.order();
                    if (order.web_service_operation_or_null() != null) {
                        if (!isNoWebServiceResponse()) {
                            spooler_log.debug9("... Creating Web_service_response ");
                            Web_service_response response = order.web_service_operation().response();
                            if (order.web_service().params().var("response_stylesheet") != null
                                    && !order.web_service().params().var("response_stylesheet").isEmpty()) {
                                spooler_log.debug9("... Creating Xslt_stylesheet ");
                                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                                spooler_log.debug9("... load_file " + order.web_service().params().var("response_stylesheet"));
                                stylesheet.load_file(order.web_service().params().var("response_stylesheet"));
                                spooler_log.debug9("... apply_xml ");
                                String xml_document = stylesheet.apply_xml(order.xml());
                                spooler_log.debug1("content of response transformation:\n" + xml_document);
                                spooler_log.debug9("... set_string_content ");
                                response.set_string_content(xml_document);
                            } else {
                                spooler_log.debug9("... set_string_content ");
                                response.set_string_content(order.xml());
                            }
                            spooler_log.debug9("... sending response");
                            response.send();
                            spooler_log.debug1("web service response successfully processed for order \"" + order.id() + "\"");
                        } else {
                            spooler_log.debug9("... web service not response cause [no_webservice=" + isNoWebServiceResponse() + "]");
                        }
                    } else {
                        spooler_log.debug9("order.web_service_operation_or_null() is NULL");
                    }
                } else {
                    spooler_log.debug9("spooler_task.job().order_queue() is NULL");
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing web service response: " + e.getMessage());
            }
            try {
                spooler_log.debug9("	try to update scheduler variables for persistent warnings and errors");
                String messageSet = "";
                if (this.isMessagePersistent()) {
                    spooler.set_var(this.getMonitorJob() + ".messages", "");
                    if (this.messageVariables != null && !this.messageVariables.isEmpty()) {
                        Iterator variableIterator = this.messageVariables.iterator();
                        while (variableIterator.hasNext()) {
                            String messageVariable = (String) variableIterator.next();
                            if (messageVariable != null && !messageVariable.isEmpty()) {
                                messageSet += messageVariable;
                            }
                        }
                        if (!messageSet.isEmpty()) {
                            messageSet = "<messages>" + messageSet + "</messages>";
                            spooler.set_var(this.getMonitorJob() + ".messages",
                                    messageSet.replaceAll("<", String.valueOf((char) 254)).replaceAll(">", String.valueOf((char) 255)));
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("error occurred updating scheduler variables: " + e.getMessage());
            }
            return spooler_task.job().order_queue() != null;
        } catch (Exception e) {
            spooler_log.info("spooler_process(): " + e.getMessage());
            return false;
        }
    }

    public int getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    public String getMonitorJob() {
        return monitorJob;
    }

    public void setMonitorJob(String monitorJob) {
        this.monitorJob = monitorJob;
    }

    public boolean isMessagePersistent() {
        return isMessagePersistent;
    }

    public void setMessagePersistent(boolean isMessagePersistent) {
        this.isMessagePersistent = isMessagePersistent;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMonitorJobChainname() {
        return monitorJobChainname;
    }

    public void setMonitorJobChainname(String monitorJobChainname) {
        this.monitorJobChainname = monitorJobChainname;
    }

    public String getMonitorJobname() {
        return monitorJobname;
    }

    public void setMonitorJobname(String monitorJobname) {
        this.monitorJobname = monitorJobname;
    }

    public void setLogMessages(boolean logMessages) {
        this.logMessages = logMessages;
    }

    public boolean isLogMessages() {
        return logMessages;
    }

    private String transformString(HashMap h) throws Exception {
        String retVal = "";
        String currStr = "";
        int startOrdername = -1;
        int endOrdername = -1;
        int startTS = -1;
        int endTS = -1;
        String sTimestamp = "";
        String msg = "";
        String logtype = "WARN";
        try {
            if (h.containsKey("WARN")) {
                logtype = "WARN";
            } else if (h.containsKey("ERROR")) {
                logtype = "ERROR";
            }
            currStr = sosString.parseToString(h.get(logtype));
            if (currStr.startsWith("[order=")) {
                startOrdername = currStr.indexOf("[order=") > -1 ? currStr.indexOf("[order=") + 7 : -1;
                endOrdername = currStr.indexOf("]") > -1 ? currStr.indexOf("]") : -1;
                startTS = currStr.indexOf("]") > -1 ? currStr.indexOf("]") + 1 : -1;
                endTS = startTS > -1 ? startTS + 23 : -1;
                if (startOrdername == -1 || endOrdername == -1 || startTS == -1 || endTS == -1) {
                    sTimestamp = (sosString.parseToString(currStr).length() > 23 ? currStr.substring(0, 23) : "");
                    return "[Timestamp: " + sTimestamp + "]" + sosString.parseToString(h.get(logtype));
                }
                retVal = "[Timestamp: " + currStr.substring(startTS, endTS) + "] " + "[" + logtype + "] " + "[Job Chain:"
                        + currStr.substring(startOrdername, endOrdername) + ", " + "Job: " + this.getCurrentJobname(currStr, true) + "] "
                        + currStr.substring(currStr.indexOf(")") + 1, currStr.length());
            } else {
                sTimestamp = (sosString.parseToString(currStr).length() > 23 ? currStr.substring(0, 23) : "");
                msg = (sosString.parseToString(currStr).indexOf("]") > -1 ? currStr.substring(currStr.indexOf("]") + 1) : sosString.parseToString(currStr));
                retVal = "[Timestamp: " + sTimestamp + "] [" + logtype + "] [Job:" + this.getCurrentJobname(currStr, false) + "] " + msg;
            }
            return retVal;
        } catch (Exception e) {
            sTimestamp = (sosString.parseToString(currStr).length() > 23 ? currStr.substring(0, 23) : "");
            if (h.containsKey("WARN")) {
                return "[Timestamp: " + sTimestamp + "]" + sosString.parseToString(h.get("WARN"));
            } else {
                return "[Timestamp: " + sTimestamp + "]" + sosString.parseToString(h.get("ERROR"));
            }
        }
    }

    public boolean isNoWebServiceResponse() {
        return NoWebServiceResponse;
    }

    public void setNoWebServiceResponse(boolean noWebServiceResponse) {
        NoWebServiceResponse = noWebServiceResponse;
    }

    public String getExcludeMessages() {
        return excludeMessages;
    }

    public void setExcludeMessages(String excludeMessages) {
        this.excludeMessages = excludeMessages;
    }

    public boolean isIncludeSupervisor() {
        return includeSupervisor;
    }

    public void setIncludeSupervisor(boolean includeSupervisor) {
        this.includeSupervisor = includeSupervisor;
    }

    public String getMaxLifeTime() {
        return maxLifeTime;
    }

    public void setMaxLifeTime(String maxLifeTime) {
        this.maxLifeTime = maxLifeTime;
    }

    public int getMaximumReports() {
        return maximumReports;
    }

    public void setMaximumReports(int maximumReports) {
        this.maximumReports = maximumReports;
    }

    private boolean isValidMessages(String curContent, HashMap ignoreMSG) throws Exception {
        try {
            if (isOutdatedLifeTime(curContent)) {
                return false;
            }
            if (isMaximunReports(curContent)) {
                return false;
            }
            if (isIgnoreMessages(curContent, ignoreMSG)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            spooler_log.info("..error in " + SOSClassUtil.getMethodName() + " cause: " + e.getMessage());
            return true;
        }
    }

    private HashMap getIgnoreMessages() throws Exception {
        HashMap ignoreMSG = new HashMap();
        String iMessages = null;
        try {
            iMessages = (!includeSupervisor ? sosString.parseToString(includeSupervisorMsg) + ";" : "") + sosString.parseToString(excludeMessages);
            if (!sosString.parseToString(iMessages).isEmpty()) {
                String[] splitIM = iMessages.split(";");
                for (int i = 0; i < splitIM.length; i++) {
                    if (!sosString.parseToString(splitIM[i]).isEmpty()) {
                        ignoreMSG.put(splitIM[i], null);
                    }
                }
            }
            return ignoreMSG;
        } catch (Exception e) {
            spooler_log.info("error in getIgnoreMessages(): could not get exclude messages: " + e.getMessage());
            return ignoreMSG;
        }
    }

    private boolean isIgnoreMessages(String logLine, HashMap ignoreMSG) {
        if (ignoreMSG.isEmpty()) {
            return false;
        }
        Iterator it = ignoreMSG.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString().replaceAll(" ", "");
            String trim = logLine.replaceAll(" ", "");
            if (trim.indexOf(key) > -1) {
                spooler_log.info("Message ignored cause Parameter exclude messages found for [logline=" + logLine + "]");
                return true;
            }
        }
        return false;
    }

    private boolean isMaximunReports(String curContent) throws Exception {
        try {
            if (sosString.parseToString(curContent).isEmpty()) {
                return false;
            }
            if (getMaximumReports() > -1) {
                int counter = -1;
                if (curContent.indexOf("[report_counter=") > -1) {
                    int pos1 = curContent.indexOf("[report_counter=") + ("[report_counter=".length());
                    int pos2 = curContent.lastIndexOf("]");
                    if (pos1 == -1 || pos2 == -1) {
                        return false;
                    }
                    counter = Integer.parseInt(curContent.substring(pos1, pos2));
                    counter++;
                    if (counter > getMaximumReports()) {
                        spooler_log.info("..the following warnings and errors were dropped due to maximum_reports constrains (" + getMaximumReports()
                                + ") for [logline=" + curContent + "]");
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            spooler_log.info("error in isMaximunReports: " + e.getMessage());
            return false;
        }
    }

    private String incrementReportCounter(String curContent) throws Exception {
        try {
            int counter = -1;
            if (!sosString.parseToString(curContent).isEmpty() && curContent.indexOf("[report_counter=") > -1) {
                int pos1 = curContent.indexOf("[report_counter=");
                int pos2 = curContent.lastIndexOf("]");
                counter = Integer.parseInt(curContent.substring(pos1 + ("[report_counter=".length()), pos2));
                counter++;
                curContent = curContent.substring(0, pos1) + "[report_counter=" + counter + "]";
            }
            return curContent;
        } catch (Exception e) {
            spooler_log.info("error in incrementReportCounter. Could not increment report counter " + e.getMessage());
            return curContent;
        }
    }

    private boolean isOutdatedLifeTime(String curContent) throws Exception {
        String format = "yyyy-MM-dd HH:mm:ss";
        java.util.Date msgTime = null;
        String msgsTime = "";
        String[] splitTime = null;
        Calendar cal = null;
        try {
            if (sosString.parseToString(curContent).isEmpty()) {
                return false;
            }
            if (!sosString.parseToString(getMaxLifeTime()).isEmpty()) {
                if (curContent.toUpperCase().indexOf(".") > -1) {
                    msgsTime = curContent.substring(0, curContent.toUpperCase().indexOf("."));
                }
                if (!sosString.parseToString(msgsTime).isEmpty()) {
                    SOSDate.setDateTimeFormat(format);
                    msgTime = SOSDate.getDate(msgsTime, format);
                    cal = Calendar.getInstance();
                    cal.setTime(msgTime);
                    splitTime = getMaxLifeTime().split(":");
                    if (splitTime.length == 3) {
                        cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTime[0]));
                        cal.add(Calendar.MINUTE, Integer.parseInt(splitTime[1]));
                        cal.add(Calendar.SECOND, Integer.parseInt(splitTime[2]));
                    } else if (splitTime.length == 2) {
                        cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTime[0]));
                        cal.add(Calendar.MINUTE, Integer.parseInt(splitTime[1]));
                    } else if (splitTime.length == 1) {
                        cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTime[0]));
                    }
                    spooler_log.debug9("message time with add maximum_lifetime " + cal.getTime() + "current Time: " + SOSDate.getCurrentTime());
                    if (cal.getTime().before(SOSDate.getCurrentTime())) {
                        spooler_log.info("..the following warnings and errors were dropped due to lifetime constrains (" + getMaxLifeTime()
                                + ") for [log_line=" + curContent + "]");
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            spooler_log.info("error in isOutdatedLifeTime: " + e.getMessage() + " [log line=" + curContent + "], [message_time=" + msgsTime + "]");
            return false;
        }
    }

    private boolean isMonitorJobname(String logLine, ArrayList listOfMonitoringJobs) {
        try {
            if (listOfMonitoringJobs.isEmpty()) {
                return true;
            } else {
                String jobname = getCurrentJobname(logLine, false);
                return listOfMonitoringJobs.contains(jobname);
            }
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isMonitorJobChainname(String logLine, ArrayList listOfMonitoringJobChains) {
        try {
            if (listOfMonitoringJobChains.isEmpty()) {
                return true;
            } else {
                String ordername = getCurrentOrdername(logLine);
                return listOfMonitoringJobChains.contains(ordername);
            }
        } catch (Exception e) {
            return true;
        }
    }

    public String getLogFilename() {
        return logFileName;
    }

    public void setLogFilename(String logFilename) {
        this.logFileName = logFilename;
    }

    private Vector removeJobFromMessages(String jobname, Vector messages) {
        try {
            Iterator msg = messages.iterator();
            while (msg.hasNext()) {
                HashMap h = (HashMap) msg.next();
                String msgJobname = "";
                String ordername = getOrderNameAndValue(sosString.parseToString(h.get("WARN")));
                if (h.containsKey("WARN")) {
                    msgJobname = getCurrentJobname(sosString.parseToString(h.get("WARN")), false) + (!ordername.isEmpty() ? "|" + ordername : "");
                } else if (h.containsKey("ERROR")) {
                    msgJobname = getCurrentJobname(sosString.parseToString(h.get("ERROR")), false) + (!ordername.isEmpty() ? "|" + ordername : "");
                }
                if (jobname.equalsIgnoreCase(msgJobname)) {
                    if (h.containsKey("WARN")) {
                        spooler_log.debug9("..clear messages " + sosString.parseToString(h.get("WARN")));
                    } else if (h.containsKey("ERROR")) {
                        spooler_log.debug9("..clear messages " + sosString.parseToString(h.get("ERROR")));
                    }
                    h.clear();
                }
            }
            return messages;
        } catch (Exception e) {
            return messages;
        }
    }

}