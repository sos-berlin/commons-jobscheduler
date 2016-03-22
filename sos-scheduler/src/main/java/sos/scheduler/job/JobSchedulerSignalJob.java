package sos.scheduler.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import sos.connection.SOSConnection;
import sos.spooler.Job_impl;
import sos.spooler.Variable_set;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.util.SOSClassUtil;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/** sos.scheduler.job.JobSchedulerSignalJob.java
 * 
 * - der Job liest aus den Tabellen scheduler_signal_objects,
 * scheduler_signal_parameters und erzeugt soviele Aufträge wie Datensätze
 * vorhanden sind
 * 
 * - der Job erzeugt Aufträge immer via XML, egal, ob der Auftrag an den
 * Scheduler gerichtet ist, der JobSchedulerSignalJob ausführt oder ein anderer
 * 
 * - Host und Port des Schedulers, der den Auftrag erhält werden aus SETTINGS
 * gelesen (default) bzw. kommen als Job-Parameter an (prioritär)
 * 
 * 
 * @author mueruevet.oeksuez@sos-berlin.com */
public class JobSchedulerSignalJob extends Job_impl {

    /** databse connection and settings */
    private SOSConnection sosConnection = null;

    /** host und port werden entweder als Jobparameter oder über die Settings
     * Tabelle gelesen. */
    private String sosSettingsFile = null;

    /** Job Scheduler signalling interface tables */
    public final static String TABLE_SCHEDULER_SIGNAL_OBECTS = "SCHEDULER_SIGNAL_OBJECTS";

    /** Job Scheduler signalling interface tables */
    public final static String TABLE_SCHEDULER_SIGNAL_PARAMETERS = "SCHEDULER_SIGNAL_PARAMETERS";

    /** Liste aller Ergenisse der Tabelle SCHEDULER_SIGNAL_OBECTS mit der Status
     * = 0. */
    private Iterator listOfSignalObject = null;

    /** Zähler aller erfolgreich abgeschlossene process Schritten */
    private int countProcessOk = 0;

    /** Zähler aller fehlerhaften process Schritten */
    private int countProcessError = 0;

    /** Attribut: verbrauchte Zeit in Sekunden */
    private long timeInSec = 0;

    /* remote execution parameters */
    private String host = "";
    private int port = 0;
    private int timeout = 0;
    private String protocol = "";

    /* routing parameters */
    private String command = "";
    private String jobName = "";
    private String orderId = "";

    /* parameters for jobs and orders */
    private String at = "";
    private String webService = "";

    /* parameters for jobs only */
    private String after = "";

    /* parameters for orders only */
    private boolean replace = true;
    private String jobChain = "";
    private String priority = "";
    private String state = "";
    private String title = "";
    private String runTime = "";

    private SOSString sosString = null;
    /** Attribut: Anwendungsname */
    protected String application = new String("scheduler");
    protected String sectionname = new String("scheduler");

    public void initParameters() throws Exception {

        sosString = new SOSString();
        /* remote execution parameters */
        this.setHost("localhost");
        this.setPort(4444);
        this.setProtocol("tcp");
        this.setTimeout(15);

        /* routing parameters */
        this.setJobName("");
        this.setOrderId("");
        this.setCommand("");

        /* parameters for jobs and orders */
        this.setAt("");
        this.setWebService("");

        /* parameters for jobs only */
        this.setAfter("");

        /* parameters for orders only */
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
            if (spooler_task.params().var("scheduler_remote_host") != null && spooler_task.params().var("scheduler_remote_host").length() > 0) {
                this.setHost(spooler_task.params().var("scheduler_remote_host"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_host]: " + this.getHost());
            }

            if (spooler_task.params().var("scheduler_remote_port") != null && spooler_task.params().var("scheduler_remote_port").length() > 0) {
                try {
                    this.setPort(Integer.parseInt(spooler_task.params().var("scheduler_remote_port")));
                    if (logValue)
                        spooler_log.info(".. job parameter [scheduler_remote_port]: " + this.getPort());
                } catch (Exception e) {
                    throw new Exception("illegal value specified for parameter [scheduler_remote_port], numeric value expected, found: "
                            + spooler_task.params().var("scheduler_remote_port"));
                }
            }

            if (spooler_task.params().var("scheduler_remote_protocol") != null && spooler_task.params().var("scheduler_remote_protocol").length() > 0) {
                if (!spooler_task.params().var("scheduler_remote_protocol").equalsIgnoreCase("tcp")
                        && !spooler_task.params().var("scheduler_remote_protocol").equalsIgnoreCase("udp")) {
                    throw new Exception("illegal value specified for parameter [scheduler_remote_protocol], \"tcp\" or \"udp\" expected, found: "
                            + spooler_task.params().var("scheduler_remote_protocol"));
                }
                this.setProtocol(spooler_task.params().var("scheduler_remote_protocol"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_protocol]: " + this.getProtocol());
            }

            if (spooler_task.params().var("scheduler_remote_timeout") != null && spooler_task.params().var("scheduler_remote_timeout").length() > 0) {
                try {
                    this.setTimeout(Integer.parseInt(spooler_task.params().var("scheduler_remote_timeout")));
                    if (logValue)
                        spooler_log.info(".. job parameter [scheduler_remote_timeout]: " + this.getTimeout());
                } catch (Exception e) {
                    throw new Exception("illegal value specified for parameter [scheduler_remote_timeout], numeric value expected, found: "
                            + spooler_task.params().var("scheduler_remote_timeout"));
                }
            }

            /* parameters for jobs and orders */

            if (spooler_task.params().var("scheduler_remote_start_at") != null && spooler_task.params().var("scheduler_remote_start_at").length() > 0) {
                this.setAt(spooler_task.params().var("scheduler_remote_start_at"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_start_at]: " + this.getAt());
            }

            /** parameters for jobs */
            if (spooler_task.params().var("scheduler_remote_job_start_after") != null
                    && spooler_task.params().var("scheduler_remote_job_start_after").length() > 0) {
                this.setAfter(spooler_task.params().var("scheduler_remote_job_start_after"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_job_start_after]: " + this.getAfter());
            }

            /* parameters for orders */

            if (spooler_task.params().var("scheduler_remote_order_replace") != null && spooler_task.params().var("scheduler_remote_order_replace").length() > 0) {
                if (spooler_task.params().var("scheduler_remote_order_replace").equalsIgnoreCase("yes")
                        || spooler_task.params().var("scheduler_remote_order_replace").equalsIgnoreCase("true")
                        || spooler_task.params().var("scheduler_remote_order_replace").equals("1")) {
                    this.setReplace(true);
                } else {
                    this.setReplace(false);
                }
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_order_replace]: " + this.isReplace());
            }

            if (spooler_task.params().var("scheduler_remote_order_job_chain") != null
                    && spooler_task.params().var("scheduler_remote_order_job_chain").length() > 0) {
                this.setJobChain(spooler_task.params().var("scheduler_remote_order_job_chain"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_order_job_chain]: " + this.getJobChain());
            }

            if (spooler_task.params().var("scheduler_remote_order_priority") != null
                    && spooler_task.params().var("scheduler_remote_order_priority").length() > 0) {
                this.setPriority(spooler_task.params().var("scheduler_remote_order_priority"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_order_priority]: " + this.getPriority());
            }

            if (spooler_task.params().var("scheduler_remote_order_state") != null && spooler_task.params().var("scheduler_remote_order_state").length() > 0) {
                this.setState(spooler_task.params().var("scheduler_remote_order_state"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_order_state]: " + this.getState());
            }

            if (spooler_task.params().var("scheduler_remote_order_title") != null && spooler_task.params().var("scheduler_remote_order_title").length() > 0) {
                this.setTitle(spooler_task.params().var("scheduler_remote_order_title"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_order_title]: " + this.getTitle());
            }

            if (spooler_task.params().var("scheduler_remote_order_run_time") != null
                    && spooler_task.params().var("scheduler_remote_order_run_time").length() > 0) {
                this.setRunTime(spooler_task.params().var("scheduler_remote_order_run_time"));
                if (logValue)
                    spooler_log.info(".. job parameter [scheduler_remote_order_run_time]: " + this.getRunTime());
            }

            if (spooler_task.params().var("sos_settings_file") != null && spooler_task.params().var("sos_settings_file").length() > 0) {
                this.setSosSettingsFile(spooler_task.params().var("sos_settings_file"));
                if (logValue)
                    spooler_log.info(".. job parameter [sos_settings_file]: " + this.getSosSettingsFile());
            }

        } catch (Exception e) {
            throw new Exception("error occurred processing task parameters: " + e.getMessage());
        }
    }

    /** Job Scheduler API implementation.
     * 
     * Initializiation.
     * 
     * @return boolean */
    public boolean spooler_init() {

        try {
            timeInSec = System.currentTimeMillis();

            this.initParameters();
            this.getTaskParameters(true);

            // DB Einstellungen aus der sos_settings.file (=jobparameter) oder
            // aus der factory.ini
            sosConnection = getConnection();

            // Status: 0=Auftrag ist offen
            String selStr = "SELECT \"SIGNAL_ID\"," + "  \"JOB_CHAIN\",  " + "  \"OPERATION\"  " + "  FROM " + TABLE_SCHEDULER_SIGNAL_OBECTS
                    + "  WHERE  \"STATUS\" = 0";
            ArrayList list = sosConnection.getArray(selStr);
            listOfSignalObject = list.iterator();

            if (listOfSignalObject.hasNext())
                return true;
            else {
                spooler_log.debug1("There is no signalling entry in Table " + TABLE_SCHEDULER_SIGNAL_OBECTS);
                return false;
            }

        } catch (Exception e) {
            spooler_log.error("error occurred initializing job: " + e.getMessage());
            return false;
        }
    }

    /** Job Scheduler API implementation.
     * 
     * Method is executed once per job or repeatedly per order.
     * 
     * @return boolean */
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

                // wenn scheduler_remote_host und scheduler_remote_port nicht
                // angegeben sind, dann
                // die Einstellungen aus der Settings lesen
                if ((sosString.parseToString(getHost()).length() == 0 || sosString.parseToString(getHost()).equalsIgnoreCase("localhost")) && getPort() == 4444) {
                    SOSConnectionSettings settings = new SOSConnectionSettings(sosConnection, "SETTINGS", new SOSSchedulerLogger(this.spooler.log()));

                    Properties section = settings.getSection(application, sectionname);
                    if (sosString.parseToString(section, "scheduler.host").length() > 0) {
                        setHost(sosString.parseToString(section, "scheduler.host"));
                        spooler_log.info(".. settings [scheduler.host]: " + this.getHost());
                        // Port auslesen nur wenn host auch aus der
                        // Settingstabelle ausgelesen wurde.
                        if (sosString.parseToString(section, "scheduler.port").length() > 0) {
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
                if (this.getOrderId() != null && this.getOrderId().length() > 0)
                    request += " id=\"" + this.getOrderId() + "\"";
                if (this.getAt() != null && this.getAt().length() > 0)
                    request += " at=\"" + this.getAt() + "\"";
                if (sosString.parseToString(res, "job_chain").length() > 0)
                    request += " job_chain=\"" + sosString.parseToString(res, "job_chain") + "\"";
                else if (this.getJobChain() != null && this.getJobChain().length() > 0)
                    request += " job_chain=\"" + this.getJobChain() + "\"";
                if (this.getPriority() != null && this.getPriority().length() > 0)
                    request += " priority=\"" + this.getPriority() + "\"";
                if (this.getState() != null && this.getState().length() > 0)
                    request += " state=\"" + this.getState() + "\"";
                request += " title=\"" + (sosString.parseToString(getTitle()).length() > 0 ? getTitle() + ":" + signalId : signalId) + "\"";
                if (this.getWebService() != null && this.getWebService().length() > 0)
                    request += " web_service=\"" + this.getWebService() + "\"";
                request += ">";
                request += "<params>";
                String[] params = parameters.names().split(";");
                for (int i = 0; i < params.length; i++) {
                    if (!params[i].startsWith("scheduler_remote_") && !params[i].equalsIgnoreCase("sos_settings_file")) {
                        request += "<param name=\"" + params[i] + "\" value=\"" + parameters.var(params[i]) + "\"/>";
                    }
                }

                // Parameter aus der Tabelle SCHEDULER_SIGNAL_PARAMETERS
                String selParameters = "SELECT \"NAME\", \"VALUE\", \"LONG_VALUE\" FROM " + TABLE_SCHEDULER_SIGNAL_PARAMETERS + " WHERE \"SIGNAL_ID\" = "
                        + signalId;

                if (sosString.parseToString(res.get("operation")).length() > 0) {
                    String value = sosString.parseToString(res.get("operation")).toLowerCase();
                    request += "<param name=\"operation\" value=\"" + value + "\"/>";
                }

                ArrayList listOfParams = sosConnection.getArray(selParameters);
                for (int i = 0; i < listOfParams.size(); i++) {
                    HashMap h = (HashMap) listOfParams.get(i);
                    String value = (sosString.parseToString(h.get("value")).length() > 0) ? sosString.parseToString(h.get("value"))
                            : sosString.parseToString(h.get("long_value"));
                    request += "<param name=\"" + h.get("name") + "\" value=\"" + value + "\"/>";
                }

                request += "</params>";
                if (this.getRunTime() != null && this.getRunTime().length() > 0)
                    request += this.getRunTime();
                request += "</add_order>";

                remoteCommand = new SOSSchedulerCommand(this.getHost(), this.getPort(), this.getProtocol());
                remoteCommand.connect();

                spooler_log.info("sending request to remote Job Scheduler [" + this.getHost() + ":" + this.getPort() + "]: " + request);
                remoteCommand.sendRequest(request);

                if (this.getProtocol().equalsIgnoreCase("tcp")) { // no response
                                                                  // is returned
                                                                  // for UDP
                                                                  // messages
                    response = remoteCommand.getResponse();
                    SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
                    String errCode = xpath.selectSingleNodeValue("//ERROR/@code");
                    String errMessage = xpath.selectSingleNodeValue("//ERROR/@text");
                    spooler_log.info("remote job scheduler response: " + response);

                    if ((errCode != null && errCode.length() > 0) || (errMessage != null && errMessage.length() > 0)) {
                        spooler_log.warn("remote Job Scheduler response reports error message: " + errMessage + " [" + errCode + "]");
                    }
                }
                // status = 1 entspricht Export-Auftrag erzeugt
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
                    String upStr = "UPDATE " +

                    TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 1001 WHERE  \"SIGNAL_ID\" = " + signalId;
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
                }  // gracefully ignore this error
            }
        }

    }

    /** @return Returns the after. */
    public String getAfter() {
        return after;
    }

    /** @param after The after to set. */
    public void setAfter(String after) {
        this.after = after;
    }

    /** @return Returns the at. */
    public String getAt() {
        return at;
    }

    /** @param at The at to set. */
    public void setAt(String at) {
        this.at = at;
    }

    /** @return Returns the command. */
    public String getCommand() {
        return command;
    }

    /** @param command The command to set. */
    public void setCommand(String command) {
        this.command = command;
    }

    /** @return Returns the host. */
    public String getHost() {
        return host;
    }

    /** @param host The host to set. */
    public void setHost(String host) {
        this.host = host;
    }

    /** @return Returns the jobChain. */
    public String getJobChain() {
        return jobChain;
    }

    /** @param jobChain The jobChain to set. */
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    /** @return Returns the jobName. */
    public String getJobName() {
        return jobName;
    }

    /** @param jobName The jobName to set. */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /** @return Returns the orderId. */
    public String getOrderId() {
        return orderId;
    }

    /** @param orderId The orderId to set. */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /** @return Returns the port. */
    public int getPort() {
        return port;
    }

    /** @param port The port to set. */
    public void setPort(int port) {
        this.port = port;
    }

    /** @return Returns the priority. */
    public String getPriority() {
        return priority;
    }

    /** @param priority The priority to set. */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /** @return Returns the protocol. */
    public String getProtocol() {
        return protocol;
    }

    /** @param protocol The protocol to set. */
    public void setProtocol(String protocol) throws Exception {

        if (protocol == null || protocol.length() == 0)
            throw new Exception("no value was given for protocol [tcp, udp]");

        if (!protocol.equalsIgnoreCase("tcp") && !protocol.equalsIgnoreCase("udp"))
            throw new Exception("illegal value specified for protocol [tcp, udp], found: " + protocol);

        this.protocol = protocol.toLowerCase();
    }

    /** @return Returns the runTime. */
    public String getRunTime() {
        return runTime;
    }

    /** @param runTime The runTime to set. */
    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    /** @return Returns the state. */
    public String getState() {
        return state;
    }

    /** @param state The state to set. */
    public void setState(String state) {
        this.state = state;
    }

    /** @return Returns the timeout. */
    public int getTimeout() {
        return timeout;
    }

    /** @param timeout The timeout to set. */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /** @return Returns the title. */
    public String getTitle() {
        return title;
    }

    /** @param title The title to set. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return Returns the webService. */
    public String getWebService() {
        return webService;
    }

    /** @param webService The webService to set. */
    public void setWebService(String webService) {
        this.webService = webService;
    }

    /** @return Returns the replace. */
    public boolean isReplace() {
        return replace;
    }

    /** @param replace The replace to set. */
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
        if (sosString.parseToString(getSosSettingsFile()).length() > 0) {
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

    /**
	 * 
	 */
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
