package sos.scheduler.job;

import java.io.File;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.spooler.Supervisor_client;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSCsvFile;

/** Forward events to the Supervisor Job Scheduler instance should events not
 * have previously been sent but stored in a temporary file
 * (./logs/scheduler.events)
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2008-05-07 */
public class JobSchedulerDequeueEventsJob extends JobSchedulerJobAdapter {

    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerDequeueEventsJob";
    public final String conSVNVersion = "$Id$";

    /** path and name of the event file */
    private String eventFilename = "";

    /** file type prefix for event file */
    private String eventFilenamePrefix = "";

    /** event action */
    private String eventAction = "";

    /** Supervisor Job Scheduler host */
    private String eventSupervisorSchedulerHost = "";

    /** Supervisor Job Scheduler port */
    private int eventSupervisorSchedulerPort = 0;

    /** Supervisor Job Scheduler connection timeout */
    private int eventSupervisorSchedulerTimeout = 0;

    /** name of event processing job chain in Supervisor Job Scheduler */
    private String eventSupervisorSchedulerJobChainName = "";

    /** Job and Order parameters */
    private Variable_set parameters = null;

    /** process single mail order */
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
                if (spooler_task.params() != null)
                    this.getParameters().merge(spooler_task.params());
                if (spooler_job.order_queue() != null)
                    this.getParameters().merge(spooler_task.order().params());

                if (isNotEmpty(this.getParameters().value("event_file"))) {
                    this.setEventFilename(this.getParameters().value("event_file"));
                    this.getLogger().debug1(".. parameter [event_file]: " + this.getEventFilename());
                } else {
                    this.setEventFilename(spooler.log_dir() + "/scheduler.events");
                }

                // if (this.getParameters().value("event_file_prefix") != null
                // && this.getParameters().value("event_file_prefix").length() >
                // 0) {
                // this.setEventFilenamePrefix(this.getParameters().value("input_file_prefix"));
                // spooler_log.debug1(".. parameter [event_file_prefix]: " +
                // this.getEventFilenamePrefix());
                // } else {
                // this.setEventFilenamePrefix("-in tab -csv | ");
                // }

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

                // File eventFileCopy = new File(this.getEventFilename() + "~");
                // if (!eventFileCopy.exists()) {
                // if (!eventFile.renameTo(eventFileCopy)) throw new
                // Exception("could not create working copy of event file: renaming "
                // + eventFile.getCanonicalPath() + " to " +
                // eventFileCopy.getCanonicalPath());
                // } else {
                // spooler_log.info("working copy of event file found - starting to process this file: "
                // + eventFileCopy.getCanonicalPath());
                // if (!eventFileCopy.canWrite()) throw new
                // Exception("required write permission for event file working copy is missing: "
                // + eventFileCopy.getCanonicalPath());
                // }

                // sos.hostware.File hwFile = new sos.hostware.File();

                // if (eventFileCopy.getName().startsWith("-")) {
                // hwFile.open( eventFileCopy.getCanonicalPath());
                // } else {
                // hwFile.open( this.getEventFilenamePrefix() + " " +
                // eventFileCopy.getAbsolutePath());
                // }
                JSCsvFile hwFile = new JSCsvFile(this.getEventFilename() + "~");

                if (!hwFile.exists()) {
                    if (!eventFile.renameTo(hwFile)) {
                        throw new JobSchedulerException(String.format("could not create working copy of event file: renaming %1$s to %2$s", eventFile.getCanonicalPath(), hwFile.getCanonicalPath()));
                    }
                } else {
                    this.getLogger().info("working copy of event file found - starting to process this file: " + hwFile.getCanonicalPath());
                    if (!hwFile.canWrite()) {
                        throw new JobSchedulerException(String.format("required write permission for event file working copy is missing: %1$s", hwFile.getCanonicalPath()));
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
                        throw new JobSchedulerException(String.format("number of fields in event file [%1$s] is too small: %2$s", eventFile.getCanonicalPath(), strValues.length));
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
                    if (strValues.length > eventMinFieldCount) {
                        // this.getLogger().info("--->" + strValues[10] + " " +
                        // strValues[10].indexOf(""));
                        if (strValues[10].indexOf("=") == -1) {// Kompatibilitätsabfrage
                            if (isNotEmpty(strValues[10])) {
                                command += "<param name=\"expires\"        value=\"" + strValues[10] + "\"/>";
                            }
                            expiration_column = 0;
                        }
                    }

                    for (int i = eventMaxFieldCount - expiration_column; i < strValues.length; i++) {
                        int posFound = strValues[i].indexOf("=");
                        if (posFound != -1) {
                            command += "<param name=\"" + strValues[i].substring(0, posFound) + "\"        value=\"" + strValues[i].substring(posFound + 1)
                                    + "\"/>";
                        }
                    }

                    command += "</params></add_order>";
                    this.getLogger().info(String.format(".. sending command to remote Job Scheduler [%1$s:%2$s]: %3$s", this.getEventSupervisorSchedulerHost(), this.getEventSupervisorSchedulerPort(), command));

                    schedulerCommand.sendRequest(command);
                    SOSXMLXPath answer = new SOSXMLXPath(new StringBuffer(schedulerCommand.getResponse()));
                    String errorText = answer.selectSingleNodeValue("//ERROR/@text");
                    if (isNotEmpty(errorText)) {
                        throw new JobSchedulerException(String.format("could not send command to Supervisor Job Scheduler [%1$s:%2$s]: %3$s", this.getEventSupervisorSchedulerHost(), this.getEventSupervisorSchedulerPort(), errorText));
                    }
                }

                // while (!hwFile.eof())
                // {
                // Record record = hwFile.get();
                // spooler_log.info("--->" + record);
                // if (record.field_count() < 10) throw new
                // Exception("number of fields in event file [" +
                // eventFile.getCanonicalPath() + "] is too small: " +
                // record.field_count());
                // eventCount++;
                // spooler_log.info("... will be processed");
                // spooler_log.info(record.string(0) + " event");
                //
                // String command =
                // "<add_order title=\"dequeued event\" job_chain=\"" +
                // this.getEventSupervisorSchedulerJobChainName() + "\">";
                // command += "<params>";
                //
                // command += "<param name=\"action\"          value=\"" +
                // getValue(record.string(0)) + "\"/>";
                // command += "<param name=\"scheduler_host\"  value=\"" +
                // getValue(record.string(1)) + "\"/>";
                // command += "<param name=\"scheduler_port\"  value=\"" +
                // getValue(record.string(2)) + "\"/>";
                // command += "<param name=\"job_chain\"       value=\"" +
                // getValue(record.string(3)) + "\"/>";
                // command += "<param name=\"order_id\"        value=\"" +
                // getValue(record.string(4)) + "\"/>";
                // command += "<param name=\"job_name\"        value=\"" +
                // getValue(record.string(5)) + "\"/>";
                // command += "<param name=\"event_class\"     value=\"" +
                // getValue(record.string(6)) + "\"/>";
                // command += "<param name=\"event_id\"        value=\"" +
                // getValue(record.string(7)) + "\"/>";
                // command += "<param name=\"exit_code\"       value=\"" +
                // getValue(record.string(8)) + "\"/>";
                // command += "<param name=\"created\"         value=\"" +
                // getValue(record.string(9)) + "\"/>";
                //
                // int expiration_column = 1;
                // if (record.field_count() > eventMinFieldCount) {
                // spooler_log.info("--->" + record.string(10) + " " +
                // record.string(10).indexOf(""));
                // if (record.string(10).indexOf("=") == -1) {//
                // Kompatibilitätsabfrage
                // if (record.string(10) != null && record.string(10).length() >
                // 0)
                // command += "<param name=\"expires\"        value=\"" +
                // record.string(10) + "\"/>";
                // expiration_column = 0;
                // }
                // }
                //
                // for(int i=eventMaxFieldCount-expiration_column;
                // i<record.field_count(); i++) {
                // int posFound = record.string(i).indexOf("=");
                // if (posFound != -1) {
                // command += "<param name=\"" + record.string(i).substring(0,
                // posFound) + "\"        value=\"" +
                // record.string(i).substring(posFound+1) + "\"/>";
                // }
                // }
                //
                // command += "</params></add_order>";
                // this.getLogger().info(".. sending command to remote Job Scheduler ["
                // + this.getEventSupervisorSchedulerHost() + ":" +
                // this.getEventSupervisorSchedulerPort() + "]: " + command );
                //
                // schedulerCommand.sendRequest(command);
                // SOSXMLXPath answer = new SOSXMLXPath(new
                // StringBuffer(schedulerCommand.getResponse()));
                // String errorText =
                // answer.selectSingleNodeValue("//ERROR/@text");
                // if (errorText != null && errorText.length() > 0) {
                // throw new
                // Exception("could not send command to Supervisor Job Scheduler ["
                // + this.getEventSupervisorSchedulerHost() + ":" +
                // this.getEventSupervisorSchedulerPort() + "]: " + errorText );
                // }
                // }

                schedulerCommand.disconnect();
                hwFile.close();

                // if (!eventFileCopy.delete()) {
                // this.getLogger().info("could not delete temporary working copy of event file, re-trying later");
                // eventFileCopy.deleteOnExit();
                // }

                if (!hwFile.delete()) {
                    this.getLogger().info("could not delete temporary working copy of event file, re-trying later");
                    hwFile.deleteOnExit();
                }

            } catch (Exception e) {
                throw new JobSchedulerException("error occurred forwarding events to Supervisor Job Scheduler [" + this.getEventSupervisorSchedulerHost() + ":"
                        + this.getEventSupervisorSchedulerPort() + "]: " + e.getMessage(), e);
            }

            if (eventCount > 0) {
                this.getLogger().info(eventCount + " events dequeued to Supervisor Job Scheduler [" + this.getEventSupervisorSchedulerHost() + ":"
                        + this.getEventSupervisorSchedulerPort() + "] from event file: " + eventFile.getCanonicalPath());
            }

            return spooler_job.order_queue() != null ? rc : false;

        } catch (Exception e) {
            spooler_log.warn("error occurred dequeueing events: " + e.getMessage());
            return false;
        }
    }

    private String csvLineToSting(final String[] strValues) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::csvLineToSting";

        String str = "";
        for (String strValue : strValues) {
            str += ";" + strValue;
        }
        if (str.length() > 0) {
            str = str.substring(1);
        }

        return str;
    } // private String csvLineToSting

    private String getValue(final String s) {
        if (s == null || s.equals("null")) {
            return "";
        } else {
            return s;
        }

    }

    /** @return the parameters */
    @Override
    public Variable_set getParameters() {
        return parameters;
    }

    /** @param parameters the parameters to set */
    @Override
    public void setParameters(final Variable_set parameters) {
        this.parameters = parameters;
    }

    /** @return the eventFilename */
    public String getEventFilename() {
        return eventFilename;
    }

    /** @param eventFilename the eventFilename to set */
    public void setEventFilename(final String eventFilename) {
        this.eventFilename = eventFilename;
    }

    /** @return the eventSupervisorSchedulerHost */
    public String getEventSupervisorSchedulerHost() {
        return eventSupervisorSchedulerHost;
    }

    /** @param eventSupervisorSchedulerHost the eventSupervisorSchedulerHost to
     *            set */
    public void setEventSupervisorSchedulerHost(final String eventSupervisorSchedulerHost) {
        this.eventSupervisorSchedulerHost = eventSupervisorSchedulerHost;
    }

    /** @return the eventSupervisorSchedulerPort */
    public int getEventSupervisorSchedulerPort() {
        return eventSupervisorSchedulerPort;
    }

    /** @param eventSupervisorSchedulerPort the eventSupervisorSchedulerPort to
     *            set */
    public void setEventSupervisorSchedulerPort(final int eventSupervisorSchedulerPort) {
        this.eventSupervisorSchedulerPort = eventSupervisorSchedulerPort;
    }

    /** @return the eventFilenamePrefix */
    public String getEventFilenamePrefix() {
        return eventFilenamePrefix;
    }

    /** @param eventFilenamePrefix the eventFilenamePrefix to set */
    public void setEventFilenamePrefix(final String eventFilenamePrefix) {
        this.eventFilenamePrefix = eventFilenamePrefix;
    }

    /** @return the eventSupervisorSchedulerJobChainName */
    public String getEventSupervisorSchedulerJobChainName() {
        return eventSupervisorSchedulerJobChainName;
    }

    /** @param eventSupervisorSchedulerJobChainName the
     *            eventSupervisorSchedulerJobChainName to set */
    public void setEventSupervisorSchedulerJobChainName(final String eventSupervisorSchedulerJobChainName) {
        this.eventSupervisorSchedulerJobChainName = eventSupervisorSchedulerJobChainName;
    }

    /** @return the eventAction */
    public String getEventAction() {
        return eventAction;
    }

    /** @param eventAction the eventAction to set */
    public void setEventAction(final String eventAction) {
        this.eventAction = eventAction;
    }

    /** @return the eventSupervisorSchedulerTimeout */
    public int getEventSupervisorSchedulerTimeout() {
        return eventSupervisorSchedulerTimeout;
    }

    /** @param eventSupervisorSchedulerTimeout the
     *            eventSupervisorSchedulerTimeout to set */
    public void setEventSupervisorSchedulerTimeout(final int eventSupervisorSchedulerTimeout) {
        this.eventSupervisorSchedulerTimeout = eventSupervisorSchedulerTimeout;
    }

}