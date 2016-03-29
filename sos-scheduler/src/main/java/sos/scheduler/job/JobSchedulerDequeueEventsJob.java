package sos.scheduler.job;

import java.io.File;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.spooler.Supervisor_client;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSCsvFile;

/** @author andreas pueschel */
public class JobSchedulerDequeueEventsJob extends JobSchedulerJobAdapter {

    private String eventFilename = "";
    private String eventFilenamePrefix = "";
    private String eventAction = "";
    private String eventSupervisorSchedulerHost = "";
    private int eventSupervisorSchedulerPort = 0;
    private int eventSupervisorSchedulerTimeout = 0;
    private String eventSupervisorSchedulerJobChainName = "";
    private Variable_set parameters = null;

    @Override
    public boolean spooler_process() {
        boolean rc = true;
        int eventCount = 0;
        int eventMinFieldCount = 10;
        int eventMaxFieldCount = 11;
        File eventFile = null;
        try {
            this.setParameters(spooler.create_variable_set());
            Supervisor_client supervisor = null;
            try {
                supervisor = spooler.supervisor_client();
            } catch (Exception e1) {
            }
            try {
                if (spooler_task.params() != null) {
                    this.getParameters().merge(spooler_task.params());
                }
                if (spooler_job.order_queue() != null) {
                    this.getParameters().merge(spooler_task.order().params());
                }
                if (isNotEmpty(this.getParameters().value("event_file"))) {
                    this.setEventFilename(this.getParameters().value("event_file"));
                    this.getLogger().debug1(".. parameter [event_file]: " + this.getEventFilename());
                } else {
                    this.setEventFilename(spooler.log_dir() + "/scheduler.events");
                }
                if (isNotEmpty(this.getParameters().value("supervisor_host"))) {
                    this.setEventSupervisorSchedulerHost(this.getParameters().value("supervisor_host"));
                    this.getLogger().debug1(".. parameter [supervisor_host]: " + this.getEventSupervisorSchedulerHost());
                } else {
                    if (supervisor != null && isNotEmpty(supervisor.hostname())) {
                        this.setEventSupervisorSchedulerHost(supervisor.hostname());
                    } else {
                        this.setEventSupervisorSchedulerHost(spooler.hostname());
                    }
                }
                if (isNotEmpty(this.getParameters().value("supervisor_port"))) {
                    try {
                        this.setEventSupervisorSchedulerPort(Integer.parseInt(this.getParameters().value("supervisor_port")));
                        this.getLogger().debug1(".. parameter [supervisor_port]: " + this.getEventSupervisorSchedulerPort());
                    } catch (Exception ex) {
                        throw new JobSchedulerException("illegal non-numeric value for Supervisor Job Scheduler port specified: "
                                + this.getParameters().value("supervisor_port"), ex);
                    }
                } else {
                    if (supervisor != null && isNotEmpty(supervisor.hostname())) {
                        this.setEventSupervisorSchedulerPort(supervisor.tcp_port());
                    } else {
                        this.setEventSupervisorSchedulerPort(spooler.tcp_port());
                    }
                }
                if (isNotEmpty(this.getParameters().value("supervisor_timeout"))) {
                    try {
                        this.setEventSupervisorSchedulerTimeout(Integer.parseInt(this.getParameters().value("supervisor_timeout")));
                        this.getLogger().debug1(".. parameter [supervisor_timeout]: " + this.getEventSupervisorSchedulerTimeout());
                    } catch (Exception ex) {
                        throw new JobSchedulerException("illegal non-numeric value for Supervisor Job Scheduler timeout specified: "
                                + this.getParameters().value("supervisor_timeout"), ex);
                    }
                } else {
                    this.setEventSupervisorSchedulerTimeout(15);
                }
                if (isNotEmpty(this.getParameters().value("supervisor_job_chain"))) {
                    this.setEventSupervisorSchedulerJobChainName(this.getParameters().value("supervisor_job_chain"));
                    this.getLogger().debug1(".. parameter [supervisor_job_chain]: " + this.getEventSupervisorSchedulerJobChainName());
                } else {
                    this.setEventSupervisorSchedulerJobChainName("/sos/events/scheduler_event_service");
                }
            } catch (Exception e) {
                throw new JobSchedulerException("error occurred processing parameters: " + e.getMessage(), e);
            }
            try {
                eventFile = new File(this.getEventFilename());
                if (!eventFile.exists()) {
                    this.getLogger().info("event file does not exist: " + eventFile.getCanonicalPath());
                } else if (!eventFile.canWrite()) {
                    throw new JobSchedulerException("required write permission for event file is missing: " + eventFile.getCanonicalPath());
                }
                JSCsvFile hwFile = new JSCsvFile(this.getEventFilename() + "~");
                if (!hwFile.exists()) {
                    if (!eventFile.renameTo(hwFile)) {
                        throw new JobSchedulerException(String.format("could not create working copy of event file: renaming %1$s to %2$s",
                                eventFile.getCanonicalPath(), hwFile.getCanonicalPath()));
                    }
                } else {
                    this.getLogger().info("working copy of event file found - starting to process this file: " + hwFile.getCanonicalPath());
                    if (!hwFile.canWrite()) {
                        throw new JobSchedulerException(String.format("required write permission for event file working copy is missing: %1$s",
                                hwFile.getCanonicalPath()));
                    }
                }
                SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
                schedulerCommand.setHost(this.getEventSupervisorSchedulerHost());
                schedulerCommand.setPort(this.getEventSupervisorSchedulerPort());
                schedulerCommand.setTimeout(this.getEventSupervisorSchedulerTimeout());
                schedulerCommand.connect();
                String[] strValues = null;
                hwFile.ColumnDelimiter("\t");
                while ((strValues = hwFile.readCSVLine()) != null) {
                    this.getLogger().info("--->" + csvLineToSting(strValues));
                    if (strValues.length < eventMinFieldCount) {
                        throw new JobSchedulerException(String.format("number of fields in event file [%1$s] is too small: %2$s", eventFile.getCanonicalPath(),
                                strValues.length));
                    }
                    eventCount++;
                    this.getLogger().info("... will be processed");
                    this.getLogger().info(strValues[0] + " event");
                    String command = "<add_order title=\"dequeued event\" job_chain=\"" + this.getEventSupervisorSchedulerJobChainName() + "\">";
                    command += "<params>";
                    command += "<param name=\"action\"          value=\"" + getValue(strValues[0]) + "\"/>";
                    command += "<param name=\"scheduler_host\"  value=\"" + getValue(strValues[1]) + "\"/>";
                    command += "<param name=\"scheduler_port\"  value=\"" + getValue(strValues[2]) + "\"/>";
                    command += "<param name=\"job_chain\"       value=\"" + getValue(strValues[3]) + "\"/>";
                    command += "<param name=\"order_id\"        value=\"" + getValue(strValues[4]) + "\"/>";
                    command += "<param name=\"job_name\"        value=\"" + getValue(strValues[5]) + "\"/>";
                    command += "<param name=\"event_class\"     value=\"" + getValue(strValues[6]) + "\"/>";
                    command += "<param name=\"event_id\"        value=\"" + getValue(strValues[7]) + "\"/>";
                    command += "<param name=\"exit_code\"       value=\"" + getValue(strValues[8]) + "\"/>";
                    command += "<param name=\"created\"         value=\"" + getValue(strValues[9]) + "\"/>";
                    int expiration_column = 1;
                    if (strValues.length > eventMinFieldCount && strValues[10].indexOf("=") == -1) {
                        if (isNotEmpty(strValues[10])) {
                            command += "<param name=\"expires\"        value=\"" + strValues[10] + "\"/>";
                        }
                        expiration_column = 0;
                    }
                    for (int i = eventMaxFieldCount - expiration_column; i < strValues.length; i++) {
                        int posFound = strValues[i].indexOf("=");
                        if (posFound != -1) {
                            command += "<param name=\"" + strValues[i].substring(0, posFound) + "\"        value=\"" + strValues[i].substring(posFound + 1)
                                    + "\"/>";
                        }
                    }
                    command += "</params></add_order>";
                    this.getLogger().info(
                            String.format(".. sending command to remote Job Scheduler [%1$s:%2$s]: %3$s", this.getEventSupervisorSchedulerHost(),
                                    this.getEventSupervisorSchedulerPort(), command));
                    schedulerCommand.sendRequest(command);
                    SOSXMLXPath answer = new SOSXMLXPath(new StringBuffer(schedulerCommand.getResponse()));
                    String errorText = answer.selectSingleNodeValue("//ERROR/@text");
                    if (isNotEmpty(errorText)) {
                        throw new JobSchedulerException(String.format("could not send command to Supervisor Job Scheduler [%1$s:%2$s]: %3$s",
                                this.getEventSupervisorSchedulerHost(), this.getEventSupervisorSchedulerPort(), errorText));
                    }
                }
                schedulerCommand.disconnect();
                hwFile.close();
                if (!hwFile.delete()) {
                    this.getLogger().info("could not delete temporary working copy of event file, re-trying later");
                    hwFile.deleteOnExit();
                }
            } catch (Exception e) {
                throw new JobSchedulerException("error occurred forwarding events to Supervisor Job Scheduler [" + this.getEventSupervisorSchedulerHost() + ":"
                        + this.getEventSupervisorSchedulerPort() + "]: " + e.getMessage(), e);
            }
            if (eventCount > 0) {
                this.getLogger().info(
                        eventCount + " events dequeued to Supervisor Job Scheduler [" + this.getEventSupervisorSchedulerHost() + ":"
                                + this.getEventSupervisorSchedulerPort() + "] from event file: " + eventFile.getCanonicalPath());
            }
            return spooler_job.order_queue() != null ? rc : false;
        } catch (Exception e) {
            spooler_log.warn("error occurred dequeueing events: " + e.getMessage());
            return false;
        }
    }

    private String csvLineToSting(final String[] strValues) {
        String str = "";
        for (String strValue : strValues) {
            str += ";" + strValue;
        }
        if (str.length() > 0) {
            str = str.substring(1);
        }
        return str;
    }

    private String getValue(final String s) {
        if (s == null || "null".equals(s)) {
            return "";
        } else {
            return s;
        }
    }

    @Override
    public Variable_set getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(final Variable_set parameters) {
        this.parameters = parameters;
    }

    public String getEventFilename() {
        return eventFilename;
    }

    public void setEventFilename(final String eventFilename) {
        this.eventFilename = eventFilename;
    }

    public String getEventSupervisorSchedulerHost() {
        return eventSupervisorSchedulerHost;
    }

    public void setEventSupervisorSchedulerHost(final String eventSupervisorSchedulerHost) {
        this.eventSupervisorSchedulerHost = eventSupervisorSchedulerHost;
    }

    public int getEventSupervisorSchedulerPort() {
        return eventSupervisorSchedulerPort;
    }

    public void setEventSupervisorSchedulerPort(final int eventSupervisorSchedulerPort) {
        this.eventSupervisorSchedulerPort = eventSupervisorSchedulerPort;
    }

    public String getEventFilenamePrefix() {
        return eventFilenamePrefix;
    }

    public void setEventFilenamePrefix(final String eventFilenamePrefix) {
        this.eventFilenamePrefix = eventFilenamePrefix;
    }

    public String getEventSupervisorSchedulerJobChainName() {
        return eventSupervisorSchedulerJobChainName;
    }

    public void setEventSupervisorSchedulerJobChainName(final String eventSupervisorSchedulerJobChainName) {
        this.eventSupervisorSchedulerJobChainName = eventSupervisorSchedulerJobChainName;
    }

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(final String eventAction) {
        this.eventAction = eventAction;
    }

    public int getEventSupervisorSchedulerTimeout() {
        return eventSupervisorSchedulerTimeout;
    }

    public void setEventSupervisorSchedulerTimeout(final int eventSupervisorSchedulerTimeout) {
        this.eventSupervisorSchedulerTimeout = eventSupervisorSchedulerTimeout;
    }

}