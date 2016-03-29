package sos.scheduler.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import sos.connection.SOSConnection;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.spooler.Job_impl;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/** @author mueruevet oeksuez */
public class JobSchedulerSignalJob extends Job_impl {

    protected String application = new String("scheduler");
    protected String sectionname = new String("scheduler");
    private SOSConnection sosConnection = null;
    private String sosSettingsFile = null;
    private Iterator listOfSignalObject = null;
    private int countProcessOk = 0;
    private int countProcessError = 0;
    private long timeInSec = 0;
    private String host = "";
    private int port = 0;
    private int timeout = 0;
    private String protocol = "";
    private String command = "";
    private String jobName = "";
    private String orderId = "";
    private String at = "";
    private String webService = "";
    private String after = "";
    private boolean replace = true;
    private String jobChain = "";
    private String priority = "";
    private String state = "";
    private String title = "";
    private String runTime = "";
    private SOSString sosString = null;
    public final static String TABLE_SCHEDULER_SIGNAL_OBECTS = "SCHEDULER_SIGNAL_OBJECTS";
    public final static String TABLE_SCHEDULER_SIGNAL_PARAMETERS = "SCHEDULER_SIGNAL_PARAMETERS";

    public void initParameters() throws Exception {
        sosString = new SOSString();
        this.setHost("localhost");
        this.setPort(4444);
        this.setProtocol("tcp");
        this.setTimeout(15);
        this.setJobName("");
        this.setOrderId("");
        this.setCommand("");
        this.setAt("");
        this.setWebService("");
        this.setAfter("");
        this.setReplace(true);
        this.setJobChain("");
        this.setPriority("");
        this.setState("");
        this.setTitle("");
        this.setRunTime("");
        countProcessError = 0;
        countProcessOk = 0;
    }

    public void getTaskParameters(boolean logValue) throws Exception {
        try {
            if (spooler_task.params().var("scheduler_remote_host") != null && !spooler_task.params().var("scheduler_remote_host").isEmpty()) {
                this.setHost(spooler_task.params().var("scheduler_remote_host"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_host]: " + this.getHost());
                }
            }
            if (spooler_task.params().var("scheduler_remote_port") != null && !spooler_task.params().var("scheduler_remote_port").isEmpty()) {
                try {
                    this.setPort(Integer.parseInt(spooler_task.params().var("scheduler_remote_port")));
                    if (logValue) {
                        spooler_log.info(".. job parameter [scheduler_remote_port]: " + this.getPort());
                    }
                } catch (Exception e) {
                    throw new Exception("illegal value specified for parameter [scheduler_remote_port], numeric value expected, found: "
                            + spooler_task.params().var("scheduler_remote_port"));
                }
            }
            if (spooler_task.params().var("scheduler_remote_protocol") != null && !spooler_task.params().var("scheduler_remote_protocol").isEmpty()) {
                if (!"tcp".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_protocol"))
                        && !"udp".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_protocol"))) {
                    throw new Exception("illegal value specified for parameter [scheduler_remote_protocol], \"tcp\" or \"udp\" expected, found: "
                            + spooler_task.params().var("scheduler_remote_protocol"));
                }
                this.setProtocol(spooler_task.params().var("scheduler_remote_protocol"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_protocol]: " + this.getProtocol());
                }
            }
            if (spooler_task.params().var("scheduler_remote_timeout") != null && !spooler_task.params().var("scheduler_remote_timeout").isEmpty()) {
                try {
                    this.setTimeout(Integer.parseInt(spooler_task.params().var("scheduler_remote_timeout")));
                    if (logValue) {
                        spooler_log.info(".. job parameter [scheduler_remote_timeout]: " + this.getTimeout());
                    }
                } catch (Exception e) {
                    throw new Exception("illegal value specified for parameter [scheduler_remote_timeout], numeric value expected, found: "
                            + spooler_task.params().var("scheduler_remote_timeout"));
                }
            }
            if (spooler_task.params().var("scheduler_remote_start_at") != null && !spooler_task.params().var("scheduler_remote_start_at").isEmpty()) {
                this.setAt(spooler_task.params().var("scheduler_remote_start_at"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_start_at]: " + this.getAt());
                }
            }
            if (spooler_task.params().var("scheduler_remote_job_start_after") != null
                    && !spooler_task.params().var("scheduler_remote_job_start_after").isEmpty()) {
                this.setAfter(spooler_task.params().var("scheduler_remote_job_start_after"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_job_start_after]: " + this.getAfter());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_replace") != null && !spooler_task.params().var("scheduler_remote_order_replace").isEmpty()) {
                if ("yes".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_order_replace"))
                        || "true".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_order_replace"))
                        || "1".equals(spooler_task.params().var("scheduler_remote_order_replace"))) {
                    this.setReplace(true);
                } else {
                    this.setReplace(false);
                }
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_replace]: " + this.isReplace());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_job_chain") != null
                    && !spooler_task.params().var("scheduler_remote_order_job_chain").isEmpty()) {
                this.setJobChain(spooler_task.params().var("scheduler_remote_order_job_chain"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_job_chain]: " + this.getJobChain());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_priority") != null
                    && !spooler_task.params().var("scheduler_remote_order_priority").isEmpty()) {
                this.setPriority(spooler_task.params().var("scheduler_remote_order_priority"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_priority]: " + this.getPriority());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_state") != null && !spooler_task.params().var("scheduler_remote_order_state").isEmpty()) {
                this.setState(spooler_task.params().var("scheduler_remote_order_state"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_state]: " + this.getState());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_title") != null && !spooler_task.params().var("scheduler_remote_order_title").isEmpty()) {
                this.setTitle(spooler_task.params().var("scheduler_remote_order_title"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_title]: " + this.getTitle());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_run_time") != null
                    && !spooler_task.params().var("scheduler_remote_order_run_time").isEmpty()) {
                this.setRunTime(spooler_task.params().var("scheduler_remote_order_run_time"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_run_time]: " + this.getRunTime());
                }
            }
            if (spooler_task.params().var("sos_settings_file") != null && !spooler_task.params().var("sos_settings_file").isEmpty()) {
                this.setSosSettingsFile(spooler_task.params().var("sos_settings_file"));
                if (logValue) {
                    spooler_log.info(".. job parameter [sos_settings_file]: " + this.getSosSettingsFile());
                }
            }
        } catch (Exception e) {
            throw new Exception("error occurred processing task parameters: " + e.getMessage());
        }
    }

    public boolean spooler_init() {
        try {
            timeInSec = System.currentTimeMillis();
            this.initParameters();
            this.getTaskParameters(true);
            sosConnection = getConnection();
            String selStr = "SELECT \"SIGNAL_ID\"," + "  \"JOB_CHAIN\",  " + "  \"OPERATION\"  " + "  FROM " + TABLE_SCHEDULER_SIGNAL_OBECTS
                    + "  WHERE  \"STATUS\" = 0";
            ArrayList list = sosConnection.getArray(selStr);
            listOfSignalObject = list.iterator();
            if (listOfSignalObject.hasNext()) {
                return true;
            } else {
                spooler_log.debug1("There is no signalling entry in Table " + TABLE_SCHEDULER_SIGNAL_OBECTS);
                return false;
            }
        } catch (Exception e) {
            spooler_log.error("error occurred initializing job: " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_process() {
        String request = "";
        String response = "";
        Variable_set parameters = null;
        SOSSchedulerCommand remoteCommand = null;
        String signalId = null;
        HashMap res = null;
        try {
            if (listOfSignalObject.hasNext()) {
                res = (HashMap) listOfSignalObject.next();
                parameters = spooler_task.params();
                if ((sosString.parseToString(getHost()).isEmpty() || "localhost".equalsIgnoreCase(sosString.parseToString(getHost()))) && getPort() == 4444) {
                    SOSConnectionSettings settings = new SOSConnectionSettings(sosConnection, "SETTINGS", new SOSSchedulerLogger(this.spooler.log()));
                    Properties section = settings.getSection(application, sectionname);
                    if (!sosString.parseToString(section, "scheduler.host").isEmpty()) {
                        setHost(sosString.parseToString(section, "scheduler.host"));
                        spooler_log.info(".. settings [scheduler.host]: " + this.getHost());
                        if (!sosString.parseToString(section, "scheduler.port").isEmpty()) {
                            int sPort = 0;
                            try {
                                sPort = Integer.parseInt(sosString.parseToString(section, "scheduler.port"));
                                setPort(sPort);
                                spooler_log.info(".. settings [scheduler.port]: " + this.getPort());
                            } catch (Exception e) {
                                spooler_log.warn("..error while reading settings[scheduler.port]: " + e.getMessage());
                            }
                        }
                    }
                }
                signalId = sosString.parseToString(res, "signal_id");
                spooler_log.debug("..signal Id: " + signalId);
                request = "<add_order";
                request += " replace=\"" + (this.isReplace() ? "yes" : "no") + "\"";
                if (this.getOrderId() != null && !this.getOrderId().isEmpty()) {
                    request += " id=\"" + this.getOrderId() + "\"";
                }
                if (this.getAt() != null && !this.getAt().isEmpty()) {
                    request += " at=\"" + this.getAt() + "\"";
                }
                if (!sosString.parseToString(res, "job_chain").isEmpty()) {
                    request += " job_chain=\"" + sosString.parseToString(res, "job_chain") + "\"";
                } else if (this.getJobChain() != null && !this.getJobChain().isEmpty()) {
                    request += " job_chain=\"" + this.getJobChain() + "\"";
                }
                if (this.getPriority() != null && !this.getPriority().isEmpty()) {
                    request += " priority=\"" + this.getPriority() + "\"";
                }
                if (this.getState() != null && !this.getState().isEmpty()) {
                    request += " state=\"" + this.getState() + "\"";
                }
                request += " title=\"" + (sosString.parseToString(getTitle()).length() > 0 ? getTitle() + ":" + signalId : signalId) + "\"";
                if (this.getWebService() != null && !this.getWebService().isEmpty()) {
                    request += " web_service=\"" + this.getWebService() + "\"";
                }
                request += ">";
                request += "<params>";
                String[] params = parameters.names().split(";");
                for (int i = 0; i < params.length; i++) {
                    if (!params[i].startsWith("scheduler_remote_") && !"sos_settings_file".equalsIgnoreCase(params[i])) {
                        request += "<param name=\"" + params[i] + "\" value=\"" + parameters.var(params[i]) + "\"/>";
                    }
                }
                String selParameters = "SELECT \"NAME\", \"VALUE\", \"LONG_VALUE\" FROM " + TABLE_SCHEDULER_SIGNAL_PARAMETERS + " WHERE \"SIGNAL_ID\" = "
                        + signalId;
                if (!sosString.parseToString(res.get("operation")).isEmpty()) {
                    String value = sosString.parseToString(res.get("operation")).toLowerCase();
                    request += "<param name=\"operation\" value=\"" + value + "\"/>";
                }
                ArrayList listOfParams = sosConnection.getArray(selParameters);
                for (int i = 0; i < listOfParams.size(); i++) {
                    HashMap h = (HashMap) listOfParams.get(i);
                    String value = !sosString.parseToString(h.get("value")).isEmpty() ? sosString.parseToString(h.get("value"))
                            : sosString.parseToString(h.get("long_value"));
                    request += "<param name=\"" + h.get("name") + "\" value=\"" + value + "\"/>";
                }
                request += "</params>";
                if (this.getRunTime() != null && !this.getRunTime().isEmpty()) {
                    request += this.getRunTime();
                }
                request += "</add_order>";
                remoteCommand = new SOSSchedulerCommand(this.getHost(), this.getPort(), this.getProtocol());
                remoteCommand.connect();
                spooler_log.info("sending request to remote Job Scheduler [" + this.getHost() + ":" + this.getPort() + "]: " + request);
                remoteCommand.sendRequest(request);
                if ("tcp".equalsIgnoreCase(this.getProtocol())) {
                    response = remoteCommand.getResponse();
                    SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
                    String errCode = xpath.selectSingleNodeValue("//ERROR/@code");
                    String errMessage = xpath.selectSingleNodeValue("//ERROR/@text");
                    spooler_log.info("remote job scheduler response: " + response);
                    if ((errCode != null && !errCode.isEmpty()) || (errMessage != null && !errMessage.isEmpty())) {
                        spooler_log.warn("remote Job Scheduler response reports error message: " + errMessage + " [" + errCode + "]");
                    }
                }
                String upStr = "UPDATE " + TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 1 WHERE  \"SIGNAL_ID\" = " + signalId;
                sosConnection.executeUpdate(upStr);
                sosConnection.commit();
            }
            countProcessOk++;
            return listOfSignalObject.hasNext();
        } catch (Exception e) {
            countProcessError++;
            spooler_log.error("error occurred for remote execution: " + e.getMessage());
            if (sosConnection != null) {
                try {
                    sosConnection.rollback();
                    String upStr = "UPDATE " + TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 1001 WHERE  \"SIGNAL_ID\" = " + signalId;
                    sosConnection.executeUpdate(upStr);
                    sosConnection.commit();
                } catch (Exception x) {
                }
            }
            return false;
        } finally {
            if (remoteCommand != null) {
                try {
                    remoteCommand.disconnect();
                } catch (Exception x) {
                    // gracefully ignore this error
                }
            }
        }
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) throws Exception {
        if (protocol == null || protocol.isEmpty()) {
            throw new Exception("no value was given for protocol [tcp, udp]");
        }
        if (!"tcp".equalsIgnoreCase(protocol) && !"udp".equalsIgnoreCase(protocol)) {
            throw new Exception("illegal value specified for protocol [tcp, udp], found: " + protocol);
        }
        this.protocol = protocol.toLowerCase();
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebService() {
        return webService;
    }

    public void setWebService(String webService) {
        this.webService = webService;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public String getSosSettingsFile() {
        return sosSettingsFile;
    }

    public void setSosSettingsFile(String sosSettingsFile) {
        this.sosSettingsFile = sosSettingsFile;
    }

    private SOSConnection getConnection() throws Exception {
        SOSConnection conn = null;
        if (!sosString.parseToString(getSosSettingsFile()).isEmpty()) {
            conn = getConnectionFromINIFile();
        }
        if (conn == null) {
            conn = JobSchedulerJob.getSchedulerConnection(new SOSProfileSettings(spooler.ini_path()), new SOSSchedulerLogger(spooler_log));
        }
        return conn;
    }

    private SOSConnection getConnectionFromINIFile() throws Exception {
        SOSConnection conn = null;
        try {
            spooler_log.debug3("DB Connecting.. .");
            conn = SOSConnection.createInstance(getSosSettingsFile(), new sos.util.SOSSchedulerLogger(spooler_log));
            conn.connect();
            spooler_log.debug3("DB Connected");
        } catch (Exception e) {
            throw (new Exception("error in " + SOSClassUtil.getMethodName() + ": connect to database failed: " + e.toString()));
        }
        return conn;
    }

    public void spooler_exit() throws Exception {
        if (sosConnection != null) {
            try {
                sosConnection.disconnect();
            } catch (Exception x) {
            }
        }
        try {
            showSummary();
        } catch (Exception x) {
        }
    }

    public void showSummary() throws Exception {
        try {
            spooler_log.debug5("..end time in miliseconds: " + System.currentTimeMillis());
            spooler_log.info("---------------------------------------------------------------");
            spooler_log.info("..number of documents                             : " + (countProcessOk + countProcessError));
            spooler_log.info("..number of records processed successfully        : " + countProcessOk);
            spooler_log.info("..number of records processed with errors         : " + countProcessError);
            spooler_log.info("..time elapsed in seconds                         : " + Math.round((System.currentTimeMillis() - timeInSec) / 1000) + "s");
            spooler_log.info("---------------------------------------------------------------");
            String stateText = "..number of create order: " + (countProcessOk + countProcessError) + "(error=" + countProcessError + ";successfully="
                    + countProcessOk + ")";
            spooler_log.info(stateText);
            spooler_job.set_state_text(stateText);
        } catch (Exception e) {
            throw new Exception("\n -> ..error occurred in " + SOSClassUtil.getMethodName() + ": " + e);
        }
    }

}