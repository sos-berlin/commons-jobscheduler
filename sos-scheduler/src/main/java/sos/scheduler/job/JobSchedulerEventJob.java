package sos.scheduler.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xalan.xslt.EnvironmentCheck;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sos.connection.SOSConnection;
import sos.connection.SOSPgSQLConnection;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.consoleviews.events.SOSActions;
import sos.scheduler.consoleviews.events.SOSEvaluateEvents;
import sos.scheduler.misc.ParameterSubstitutor;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.util.SOSFile;
import sos.util.SOSLogger;
import sos.xml.SOSXMLTransformer;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author andreas.pueschel@sos-berlin.com
 *
 *         This job is used to process events by a EventService instance */
public class JobSchedulerEventJob extends JobSchedulerJob {

    private static final String NEVER_DATE = "2999-01-01 00:00:00";
    /** event action */
    private String eventAction = "";
    /** event JobScheduler id */
    private String eventSchedulerId = "";
    /** event remote JobScheduler host */
    private String eventRemoteSchedulerHost = "";
    /** event remote JobScheduler port */
    private String eventRemoteSchedulerPort = "";
    /** job chain that caused the event */
    private String eventJobChainName = "";
    /** order identification that caused the event */
    private String eventOrderId = "";
    /** job that caused the event */
    private String eventJobName = "";
    /** event class name */
    private String eventClass = "";
    /** event identification */
    private String eventId = "";
    /** event exit code */
    private String eventExitCode = "";
    /** event expiration date */
    private String eventExpires = "";
    /** event creation date */
    private String eventCreated = "";
    /** event parameters */
    private Element eventParameters = null;
    /** expiration period */
    private String expirationPeriod = "";
    /** expiration cycle */
    private String expirationCycle = "";
    /** calculated expiration date */
    private Calendar expirationDate = null;
    /** event handler file path */
    private String eventHandlerFilepath = "";
    /** event handler file specification */
    private String eventHandlerFilespec = "";
    /** job and order parameters */
    private Variable_set parameters = null;
    /** table name */
    private final String tableEvents = "SCHEDULER_EVENTS";
    /** local DOM objects of events */
    private Document events = null;
    /** local list of event handlers */
    private Collection<File> eventHandlerFileList = new LinkedHashSet<File>();
    /** iterator for local list of event handlers */
    private Iterator<File> eventHandlerFileListIterator = null;
    /** local list of transformed files */
    private Collection<Object> eventHandlerResultFileList = new Vector<Object>();
    /** iterator for local list of transformed files */
    private Iterator<Object> eventHandlerResultFileListIterator = null;
    /** Timeout for Socket communication default=5 **/
    private int socket_timeout = 5;
    private ParameterSubstitutor parameterSubstitutor;

    @Override
    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        EnvironmentCheck ec = new EnvironmentCheck();
        StringWriter sWri = new StringWriter();
        PrintWriter pWri = new PrintWriter(sWri);
        ec.checkEnvironment(pWri);
        pWri.close();
        try {
            getLogger().debug3("Checking Xalan environment...");
            getLogger().debug3(sWri.toString());
        } catch (Exception ex) {
        }
        return rc;
    }

    @Override
    public boolean spooler_process() {
        boolean rc = true;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document eventDocument = docBuilder.newDocument();
            eventDocument.appendChild(eventDocument.createElement("events"));
            this.setEvents(eventDocument);
            this.setParameters(spooler.create_variable_set());
            if (this.getParameters().value("action") != null && "refresh".equalsIgnoreCase(this.getParameters().value("action"))) {
                spooler.variables().set_var(JobSchedulerConstants.eventVariableName, "");
                this.getSchedulerEvents();
                return true;
            }
            // fetch events from global JobScheduler variable
            this.getSchedulerEvents();
            HashSet<String> jobParameterNames = new HashSet<String>();
            try {
                if (spooler_task.params() != null) {
                    this.getParameters().merge(spooler_task.params());
                }
                if (spooler_job.order_queue() != null) {
                    this.getParameters().merge(spooler_task.order().params());
                }
                // event processing parameters
                if (this.getParameters().value("socket_timeout") != null && this.getParameters().value("socket_timeout").length() > 0) {
                    this.setSocket_timeout(this.getParameters().value("socket_timeout"));
                    spooler_log.debug1(".. parameter [socket_timeout]: " + socket_timeout);
                } else {
                    this.setSocket_timeout("5");
                }
                if (this.getParameters().value("action") != null && this.getParameters().value("action").length() > 0) {
                    this.setEventAction(this.getParameters().value("action"));
                    spooler_log.debug1(".. parameter [action]: " + this.getEventAction());
                } else {
                    this.setEventAction("add");
                }
                jobParameterNames.add("action");
                if (this.getParameters().value("scheduler_id") != null && this.getParameters().value("scheduler_id").length() > 0) {
                    this.setEventSchedulerId(this.getParameters().value("scheduler_id"));
                    spooler_log.debug1(".. parameter [scheduler_id]: " + this.getEventSchedulerId());
                } else {
                    this.setEventSchedulerId(spooler.id());
                }
                jobParameterNames.add("scheduler_id");
                if (this.getParameters().value("spooler_id") != null && this.getParameters().value("spooler_id").length() > 0) {
                    this.setEventSchedulerId(this.getParameters().value("spooler_id"));
                    spooler_log.debug1(".. parameter [spooler_id]: " + this.getEventSchedulerId());
                }
                jobParameterNames.add("spooler_id");
                // standard parameters
                if (this.getParameters().value("remote_scheduler_host") != null && this.getParameters().value("remote_scheduler_host").length() > 0) {
                    this.setEventRemoteSchedulerHost(this.getParameters().value("remote_scheduler_host"));
                    spooler_log.debug1(".. parameter [remote_scheduler_host]: " + this.getEventRemoteSchedulerHost());
                } else {
                    this.setEventRemoteSchedulerHost("");
                }
                jobParameterNames.add("remote_scheduler_host");
                if (this.getParameters().value("remote_scheduler_port") != null && this.getParameters().value("remote_scheduler_port").length() > 0) {
                    this.setEventRemoteSchedulerPort(this.getParameters().value("remote_scheduler_port"));
                    spooler_log.debug1(".. parameter [remote_scheduler_port]: " + this.getEventRemoteSchedulerPort());
                } else {
                    this.setEventRemoteSchedulerPort("0");
                }
                jobParameterNames.add("remote_scheduler_port");
                if (this.getParameters().value("job_chain") != null && this.getParameters().value("job_chain").length() > 0) {
                    this.setEventJobChainName(this.getParameters().value("job_chain"));
                    spooler_log.debug1(".. parameter [job_chain]: " + this.getEventJobChainName());
                } else {
                    this.setEventJobChainName("");
                }
                jobParameterNames.add("job_chain");
                if (this.getParameters().value("order_id") != null && this.getParameters().value("order_id").length() > 0) {
                    this.setEventOrderId(this.getParameters().value("order_id"));
                    spooler_log.debug1(".. parameter [order_id]: " + this.getEventOrderId());
                } else {
                    this.setEventOrderId("");
                }
                jobParameterNames.add("order_id");
                if (this.getParameters().value("job_name") != null && this.getParameters().value("job_name").length() > 0) {
                    this.setEventJobName(this.getParameters().value("job_name"));
                    spooler_log.debug1(".. parameter [job_name]: " + this.getEventJobName());
                } else {
                    this.setEventJobName("");
                }
                jobParameterNames.add("job_name");
                if (this.getParameters().value("event_class") != null && this.getParameters().value("event_class").length() > 0) {
                    this.setEventClass(this.getParameters().value("event_class"));
                    spooler_log.debug1(".. parameter [event_class]: " + this.getEventClass());
                } else {
                    this.setEventClass("");
                }
                jobParameterNames.add("event_class");
                if (this.getParameters().value("event_id") != null && this.getParameters().value("event_id").length() > 0) {
                    this.setEventId(this.getParameters().value("event_id"));
                    spooler_log.debug1(".. parameter [event_id]: " + this.getEventId());
                } else {
                    this.setEventId("");
                }
                jobParameterNames.add("event_id");
                if (this.getParameters().value("exit_code") != null && this.getParameters().value("exit_code").length() > 0) {
                    this.setEventExitCode(this.getParameters().value("exit_code"));
                    spooler_log.debug1(".. parameter [exit_code]: " + this.getEventExitCode());
                } else {
                    this.setEventExitCode("");
                }
                jobParameterNames.add("exit_code");
                if (this.getParameters().value("expires") != null && this.getParameters().value("expires").length() > 0) {
                    this.setEventExpires(this.getParameters().value("expires"));
                    spooler_log.debug1(".. parameter [expires]: " + this.getEventExpires());
                } else {
                    this.setEventExpires("");
                }
                jobParameterNames.add("expires");
                if (this.getParameters().value("created") != null && this.getParameters().value("created").length() > 0) {
                    this.setEventCreated(this.getParameters().value("created"));
                    spooler_log.debug1(".. parameter [created]: " + this.getEventCreated());
                } else {
                    this.setEventCreated(SOSDate.getCurrentTimeAsString());
                }
                jobParameterNames.add("created");
                /* recommended job parameters */
                if (this.getParameters().value("expiration_period") != null && this.getParameters().value("expiration_period").length() > 0) {
                    this.setExpirationPeriod(this.getParameters().value("expiration_period"));
                    spooler_log.debug1(".. parameter [expiration_period]: " + this.getExpirationPeriod());
                } else {
                    // by default events are removed after 24 hours
                    this.setExpirationPeriod("24:00:00");
                }
                jobParameterNames.add("expiration_period");
                if (this.getParameters().value("expiration_cycle") != null && this.getParameters().value("expiration_cycle").length() > 0) {
                    this.setExpirationCycle(this.getParameters().value("expiration_cycle"));
                    spooler_log.debug1(".. parameter [expiration_cycle]: " + this.getExpirationCycle());
                } else {
                    this.setExpirationCycle("");
                }
                jobParameterNames.add("expiration_cycle");
                if (this.getParameters().value("event_handler_filepath") != null && this.getParameters().value("event_handler_filepath").length() > 0) {
                    this.setEventHandlerFilepath(this.getParameters().value("event_handler_filepath"));
                    spooler_log.debug1(".. parameter [event_handler_filepath]: " + this.getEventHandlerFilepath());
                } else {
                    this.setEventHandlerFilepath("./config/events");
                }
                jobParameterNames.add("event_handler_filepath");
                if (this.getParameters().value("event_handler_filespec") != null && this.getParameters().value("event_handler_filespec").length() > 0) {
                    this.setEventHandlerFilespec(this.getParameters().value("event_handler_filespec"));
                    spooler_log.debug1(".. parameter [event_handler_filespec]: " + this.getEventHandlerFilespec());
                } else {
                    this.setEventHandlerFilespec("\\.sos.scheduler.xsl$");
                }
                jobParameterNames.add("event_handler_filespec");
                /* any additional parameters */
                this.setEventParameters(getEvents().createElement("params"));
                String[] parameterNames = this.getParameters().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    if (!jobParameterNames.contains(parameterNames[i])) {
                        Element param = getEvents().createElement("param");
                        param.setAttribute("name", parameterNames[i]);
                        param.setAttribute("value", this.getParameters().value(parameterNames[i]));
                        getLogger().debug3("Event parameter [" + parameterNames[i] + "]: " + this.getParameters().value(parameterNames[i]));
                        this.getEventParameters().appendChild(param);
                    }
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing parameters: " + e.getMessage());
            }
            this.setExpirationDate(calculateExpirationDate(expirationCycle, expirationPeriod));
            try {
                if (this.getEventAction().equalsIgnoreCase("add")) {
                    this.getLogger().info("adding event: " + this.getEventClass() + " " + this.getEventId());
                    this.addEvent();
                } else if (this.getEventAction().equalsIgnoreCase("remove")) {
                    this.getLogger().info("removing event: " + this.getEventClass() + " " + this.getEventId());
                    this.removeEvent();
                } else {
                    this.getLogger().info("processing events");
                }
                // process events, launch dependent jobs and orders
                this.processSchedulerEvents();
                // update events in global JobScheduler variable
                this.putSchedulerEvents();
            } catch (Exception e) {
                throw new Exception("error occurred processing event: " + e.getMessage());
            }
            return spooler_job.order_queue() != null ? rc : false;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing event [" + this.getEventClass() + " " + this.getEventId() + "]: " + e.getMessage());
            return false;
        }
    }

    protected static Calendar calculateExpirationDate(final String expirationCycle, final String expirationPeriod) throws Exception {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(SOSDate.getCurrentTime());
            if (expirationCycle.indexOf(":") > -1) {
                String[] timeArray = expirationCycle.split(":");
                int hours = Integer.parseInt(timeArray[0]);
                int minutes = Integer.parseInt(timeArray[1]);
                int seconds = 0;
                if (timeArray.length > 2) {
                    seconds = Integer.parseInt(timeArray[2]);
                }
                cal.set(Calendar.HOUR_OF_DAY, hours);
                cal.set(Calendar.MINUTE, minutes);
                cal.set(Calendar.SECOND, seconds);
                // add one day if the current timestamp is after the calculated
                // cycle
                if (cal.after(SOSDate.getCurrentTime())) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else if (expirationPeriod.indexOf(":") > -1) {
                String[] timeArray = expirationPeriod.split(":");
                int hours = Integer.parseInt(timeArray[0]);
                int minutes = Integer.parseInt(timeArray[1]);
                int seconds = 0;
                if (timeArray.length > 2) {
                    seconds = Integer.parseInt(timeArray[2]);
                }
                // add the interval from the given period to the expiration date
                if (hours > 0) {
                    cal.add(Calendar.HOUR_OF_DAY, hours);
                }
                if (minutes > 0) {
                    cal.add(Calendar.MINUTE, minutes);
                }
                if (seconds > 0) {
                    cal.add(Calendar.SECOND, seconds);
                }
            } else if (expirationPeriod.length() > 0) {
                cal.add(Calendar.SECOND, Integer.parseInt(expirationPeriod));
            }
        } catch (Exception e) {
            throw new Exception("Error calculating expiration date: " + e, e);
        }
        return cal;
    }

    /** get events from JobScheduler global variables */
    private void getSchedulerEvents() throws Exception {
        try {
            String eventSet = spooler.var(JobSchedulerConstants.eventVariableName);
            if (this.getConnection() != null && (eventSet == null || eventSet.length() == 0)) {
                readEventsFromDB(getConnection(), spooler, getEvents(), getLogger());
            } else {
                eventSet = eventSet.replaceAll(String.valueOf((char) 254), "<").replaceAll(String.valueOf((char) 255), ">");
                this.getLogger().debug6("current event set: " + eventSet);
                if (eventSet.length() == 0) {
                    return;
                }
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document eventDocument = docBuilder.parse(new InputSource(new StringReader(eventSet)));
                this.setEvents(eventDocument);
                this.getLogger().debug9("looking for //events/event");
                NodeList nodes = org.apache.xpath.XPathAPI.selectNodeList(this.getEvents(), "//events/event");
                this.getLogger().debug9("nodes.getLength(): " + nodes.getLength());
                int activeNodeCount = 0;
                int expiredNodeCount = 0;
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    Node curEventExpires = node.getAttributes().getNamedItem("expires");
                    if (curEventExpires == null || curEventExpires.getNodeValue() == null || curEventExpires.getNodeValue().length() == 0
                            || "never".equalsIgnoreCase(curEventExpires.getNodeValue())) {
                        activeNodeCount++;
                        continue;
                    }
                    // remove this event should the expiration date be overdue
                    Calendar expiresDate = GregorianCalendar.getInstance();
                    Calendar now = GregorianCalendar.getInstance();
                    expiresDate.setTime(SOSDate.getTime(curEventExpires.getNodeValue()));
                    if (expiresDate.before(now)) {
                        this.getLogger().debug9("Found expired event");
                        this.getEvents().getFirstChild().removeChild(node);
                        this.getLogger().debug9("event removed");
                        expiredNodeCount++;
                    } else {
                        activeNodeCount++;
                    }
                }
                this.getLogger().info(activeNodeCount + " events are active, " + expiredNodeCount + " events have expired");
            }
        } catch (Exception e) {
            if (this.getConnection() != null) {
                try {
                    this.getConnection().rollback();
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
            throw new Exception("events fetched with errors: " + e.getMessage());
        }
    }

    private void readEventsFromDB(final SOSConnection conn, final Spooler spooler, final Document eventsDoc, final SOSLogger log) throws Exception {
        try {
            conn.executeUpdate("DELETE FROM " + tableEvents + " WHERE \"EXPIRES\"<=%now AND (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='"
                    + spooler.id() + "')");
            conn.commit();
            Vector<?> vEvents = conn.getArrayAsVector("SELECT \"SPOOLER_ID\", \"REMOTE_SCHEDULER_HOST\", \"REMOTE_SCHEDULER_PORT\", "
                    + "\"JOB_CHAIN\", \"ORDER_ID\", \"JOB_NAME\", \"EVENT_CLASS\", \"EVENT_ID\", \"EXIT_CODE\", \"CREATED\", \"EXPIRES\", \"PARAMETERS\" FROM "
                    + tableEvents + " WHERE (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='" + spooler.id() + "') ORDER BY \"ID\" ASC");
            Iterator<?> vIterator = vEvents.iterator();
            int vCount = 0;
            while (vIterator.hasNext()) {
                HashMap<?, ?> record = (HashMap<?, ?>) vIterator.next();
                Element event = eventsDoc.createElement("event");
                event.setAttribute("scheduler_id", record.get("spooler_id").toString());
                event.setAttribute("remote_scheduler_host", record.get("remote_scheduler_host").toString());
                event.setAttribute("remote_scheduler_port", record.get("remote_scheduler_port").toString());
                event.setAttribute("job_chain", record.get("job_chain").toString());
                event.setAttribute("order_id", record.get("order_id").toString());
                event.setAttribute("job_name", record.get("job_name").toString());
                event.setAttribute("event_class", record.get("event_class").toString());
                event.setAttribute("event_id", record.get("event_id").toString());
                event.setAttribute("exit_code", record.get("exit_code").toString());
                event.setAttribute("expires", record.get("expires").toString());
                event.setAttribute("created", record.get("created").toString());
                if (record.get("parameters").toString().length() > 0) {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document eventParameters = docBuilder.parse(new InputSource(new StringReader(record.get("parameters").toString())));
                    log.debug9("Importing params node...");
                    Node impParameters = eventsDoc.importNode(eventParameters.getDocumentElement(), true);
                    log.debug9("appending params child...");
                    event.appendChild(impParameters);
                }
                eventsDoc.getLastChild().appendChild(event);
                vCount++;
            }
            log.info(vCount + " events restored from database");
        } catch (Exception e) {
            throw new JobSchedulerException("Failed to read events from database: " + e, e);
        }
    }

    /** update events in JobScheduler global variables */
    private void putSchedulerEvents() throws Exception {
        try {
            String eventsString = this.xmlDocumentToString(this.getEvents());
            this.getLogger().debug9("Updating events: " + eventsString);
            spooler.set_var(JobSchedulerConstants.eventVariableName, eventsString.replaceAll("<", String.valueOf((char) 254)).replaceAll(">", String.valueOf((char) 255)));
        } catch (Exception e) {
            throw new JobSchedulerException("events updated with errors: " + e.getMessage(), e);
        }
    }

    private boolean analyseMonitorEventHandler(final String fileSpec, final String fileSpecLog) throws Exception {
        boolean erg = false;
        this.getLogger().debug6(".. looking for special event handler for: " + fileSpecLog + " " + fileSpec);
        Vector<File> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
        Iterator<File> iter = specialFiles.iterator();
        while (iter.hasNext()) {
            File actionEventHandler = iter.next();
            if (actionEventHandler.exists() && actionEventHandler.canRead()) {
                erg = true;
                this.getLogger().debug1(".. analysing action event handler: " + actionEventHandler.getCanonicalPath());
                SOSEvaluateEvents eval = new SOSEvaluateEvents(spooler.hostname(), spooler.tcp_port());
                try {
                    eval.setActiveEvents(this.getEvents());
                    eval.readConfigurationFile(actionEventHandler);
                } catch (Exception e) {
                    getLogger().error(e.getMessage());
                }
                // Logging
                Iterator<SOSActions> iActions = eval.getListOfActions().iterator();
                while (iActions.hasNext()) {
                    SOSActions a = iActions.next();
                    this.getLogger().debug1(".... checking action " + a.getName());
                    if (a.isActive(eval.getListOfActiveEvents())) {
                        this.getLogger().debug1(".... added action:" + a.getName());
                        this.getEventHandlerResultFileList().add(a.getCommands());
                        NodeList commands = XPathAPI.selectNodeList(a.getCommands(), "command | remove_event | add_event");
                        for (int i = 0; i < commands.getLength(); i++) {
                            Node n = commands.item(i);
                            if ("command".equals(n.getNodeName()) || "remove_event".equals(n.getNodeName()) || "add_event".equals(n.getNodeName())) {
                                this.getLogger().debug1(".. " + n.getNodeName() + " was added");
                                NamedNodeMap attr = n.getAttributes();
                                if (attr != null) {
                                    for (int ii = 0; ii < attr.getLength(); ii++) {
                                        this.getLogger().debug1("...." + attr.item(ii).getNodeName() + "=" + attr.item(ii).getNodeValue());
                                    }
                                }
                            }
                        }
                    }
                }
                // Logging Ende
            }
        }
        return erg;
    }

    private String getText(final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return "";
        }
    }

    private void getParametersFromEvents() throws Exception {
        getLogger().debug5("executing getParametersFromEvents....");
        Document doc = this.getEvents();
        NodeList events = doc.getElementsByTagName("event");
        getLogger().debug5("events length: " + events.getLength());
        for (int i = 0; i < events.getLength(); i++) {
            Node event = events.item(i);
            NamedNodeMap attr = event.getAttributes();
            String event_class = getText(attr.getNamedItem("event_class"));
            String event_id = getText(attr.getNamedItem("event_id"));
            getLogger().debug5("event_class:" + event_class);
            getLogger().debug5("event_id:" + event_id);
            NodeList parameters = XPathAPI.selectNodeList(event, "params/param");
            getLogger().debug5("parameter length: " + parameters.getLength());
            if (parameters != null && parameters.getLength() > 0) {
                for (int ii = 0; ii < parameters.getLength(); ii++) {
                    Node eventParam = parameters.item(ii);
                    NamedNodeMap paramAttr = eventParam.getAttributes();
                    String param_name = getText(paramAttr.getNamedItem("name"));
                    String param_value = getText(paramAttr.getNamedItem("value"));
                    parameterSubstitutor.addKey(event_class + "." + event_id + "." + param_name, param_value);
                    parameterSubstitutor.addKey(event_class + ".*." + param_name, param_value);
                    parameterSubstitutor.addKey(event_id + "." + param_name, param_value);
                    parameterSubstitutor.addKey(param_name, param_value);
                    getLogger().debug5(event_class + "." + event_id + "." + param_name + "=" + param_value);
                }
            }
        }
    }

    private void getMonitorEventHandler() throws IOException, Exception {
        boolean fileFound = false;
        File eventHandlerFile = new File(this.getEventHandlerFilepath());
        if (eventHandlerFile.isDirectory()) {
            if (!eventHandlerFile.canRead()) {
                throw new Exception("event handler directory is not accessible: " + eventHandlerFile.getCanonicalPath());
            }
            this.getLogger().debug6("retrieving event handlers from directory: " + this.getEventHandlerFilepath() + " for file specification: Action");
            String fileSpec = "";
            String fileSpecLog = "";
            if (this.getEventJobChainName().length() > 0) {
                fileSpec = this.getEventJobChainName() + "(\\..*)?\\.job_chain\\.actions.xml$";
                fileSpecLog = "job_chain";
                fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
            }
            if (!fileFound && this.getEventJobName().length() > 0) {
                fileSpec = this.getEventJobName() + "(\\..*)?\\.job\\.actions.xml$";
                fileSpecLog = "job";
                fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
            }
            if (!fileFound && this.getEventClass().length() > 0) {
                fileSpec = this.getEventClass() + "(\\..*)?\\.event_class\\.actions.xml$";
                fileSpecLog = "event_class";
                fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
            }
            if (!fileFound) {
                fileSpec = "(\\..*)?\\.actions\\.xml$";
                fileSpecLog = "";
                fileFound = analyseMonitorEventHandler(fileSpec, fileSpecLog);
            }
        }
    }

    /** process events from individual event handlers */
    private void processSchedulerEvents() throws Exception {
        File eventHandler = null;
        parameterSubstitutor = new ParameterSubstitutor();
        parameterSubstitutor.addKey("current_date", SOSDate.getCurrentTimeAsString());
        getParametersFromEvents();
        try {
            HashMap<File, File> eventHandlerResultReference = new HashMap<File, File>();
            try {
                this.getLogger().debug3(".. current events for event handler processing:");
                this.getLogger().debug3(this.xmlDocumentToString(this.getEvents()));
                this.setEventHandlerResultFileList(new LinkedHashSet<Object>());
                this.setEventHandlerFileList(new LinkedHashSet<File>());
                // add Resultfile for scheduler_action.xml
                this.getMonitorEventHandler();
                File eventHandlerFile = new File(this.getEventHandlerFilepath());
                if (eventHandlerFile.isDirectory()) {
                    if (!eventHandlerFile.canRead()) {
                        throw new Exception("event handler directory is not accessible: " + eventHandlerFile.getCanonicalPath());
                    }
                    this.getLogger().debug6("retrieving event handlers from directory: " + this.getEventHandlerFilepath() + " for file specification: "
                            + this.getEventHandlerFilespec());
                    if (this.getEventJobChainName().length() > 0) {
                        String fileSpec = this.getEventJobChainName() + "(\\..*)?\\.job_chain\\.sos.scheduler.xsl$";
                        this.getLogger().debug6(".. looking for special event handler for job chain: " + fileSpec);
                        Vector<?> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
                        Iterator<?> iter = specialFiles.iterator();
                        while (iter.hasNext()) {
                            File specialEventHandler = (File) iter.next();
                            if (specialEventHandler.exists() && specialEventHandler.canRead()) {
                                this.getEventHandlerFileList().add(specialEventHandler);
                                this.getLogger().debug1(".. using special event handler for job chain: " + specialEventHandler.getCanonicalPath());
                            }
                        }
                    }
                    if (this.getEventJobName().length() > 0) {
                        String fileSpec = this.getEventJobName() + "(\\..*)?\\.job\\.sos.scheduler.xsl$";
                        this.getLogger().debug6(".. looking for special event handler for job: " + fileSpec);
                        Vector<?> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
                        Iterator<?> iter = specialFiles.iterator();
                        while (iter.hasNext()) {
                            File specialEventHandler = (File) iter.next();
                            if (specialEventHandler.exists() && specialEventHandler.canRead()) {
                                this.getEventHandlerFileList().add(specialEventHandler);
                                this.getLogger().debug1(".. using special event handler for job: " + specialEventHandler.getCanonicalPath());
                            }
                        }
                    }
                    if (this.getEventClass().length() > 0) {
                        String fileSpec = this.getEventClass() + "(\\..*)?\\.event_class\\.sos.scheduler.xsl$";
                        this.getLogger().debug6(".. looking for special event handlers for event class: " + fileSpec);
                        Vector<?> specialFiles = SOSFile.getFilelist(this.getEventHandlerFilepath(), fileSpec, 0);
                        Iterator<?> iter = specialFiles.iterator();
                        while (iter.hasNext()) {
                            File specialEventHandler = (File) iter.next();
                            if (specialEventHandler.exists() && specialEventHandler.canRead()) {
                                this.getEventHandlerFileList().add(specialEventHandler);
                                this.getLogger().debug1(".. using special event handler for event class: " + specialEventHandler.getCanonicalPath());
                            }
                        }
                    }
                    // add default event handlers
                    this.getEventHandlerFileList().addAll(SOSFile.getFilelist(this.getEventHandlerFilepath(), this.getEventHandlerFilespec(), 0));
                    this.getLogger().debug1(".. adding list of default event handlers: " + this.getEventHandlerFilepath() + "/"
                            + this.getEventHandlerFilespec());
                } else {
                    if (!eventHandlerFile.canRead()) {
                        throw new Exception("event handler file is not accessible: " + eventHandlerFile.getCanonicalPath());
                    }
                    this.getEventHandlerFileList().add(eventHandlerFile);
                }
                HashMap<String, String> stylesheetParameters = new HashMap<String, String>();
                stylesheetParameters.put("current_date", SOSDate.getCurrentTimeAsString());
                stylesheetParameters.put("expiration_date", SOSDate.getTimeAsString(this.getExpirationDate().getTime()));
                // old Xalan doesn't work with parameters
                getEvents().getDocumentElement().setAttribute("current_date", SOSDate.getCurrentTimeAsString());
                getEvents().getDocumentElement().setAttribute("expiration_date", SOSDate.getTimeAsString(this.getExpirationDate().getTime()));
                this.setEventHandlerFileListIterator(this.getEventHandlerFileList().iterator());
                while (this.getEventHandlerFileListIterator().hasNext()) {
                    eventHandler = this.getEventHandlerFileListIterator().next();
                    if (eventHandler == null) {
                        continue;
                    }
                    File stylesheetResultFile = File.createTempFile("sos", ".xml");
                    stylesheetResultFile.deleteOnExit();
                    this.getEventHandlerResultFileList().add(stylesheetResultFile);
                    eventHandlerResultReference.put(stylesheetResultFile, eventHandler);
                    this.getLogger().debug1(".. processing events with stylesheet: " + eventHandler.getCanonicalPath());
                    SOSXMLTransformer.transform(this.xmlDocumentToString(this.getEvents()), eventHandler, stylesheetResultFile, stylesheetParameters);
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing event handler" + (eventHandler != null ? " [" + eventHandler.getCanonicalPath() + "]" : "")
                        + ": " + e.getMessage());
            }
            try {
                this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
                while (this.getEventHandlerResultFileListIterator().hasNext()) {
                    NodeList commands = null;
                    Object result = this.getEventHandlerResultFileListIterator().next();
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        if (resultFile == null) {
                            continue;
                        }
                        File eventHandlerFile = eventHandlerResultReference.get(resultFile);
                        this.getLogger().debug3(".. content of result file for event handler: " + eventHandlerFile.getCanonicalPath());
                        this.getLogger().debug3(this.getFileContent(resultFile).toString());
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document eventDocument = docBuilder.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
                        Element eCommands = eventDocument.getDocumentElement();
                        if (eCommands != null) {
                            String debug = eCommands.getAttribute("debug");
                            if ("true".equalsIgnoreCase(debug)) {
                                logTransformation(resultFile, eventHandlerFile);
                            }
                        }
                        commands = XPathAPI.selectNodeList(eventDocument, "//command");
                    } else {
                        commands = XPathAPI.selectNodeList((Node) result, "command");
                    }
                    for (int i = 0; i < commands.getLength(); i++) {
                        Node command = commands.item(i);
                        NamedNodeMap commandAttributes = command.getAttributes();
                        String commandHost = spooler.hostname();
                        String commandPort = Integer.toString(spooler.tcp_port());
                        String commandProtocol = "tcp";
                        for (int j = 0; j < commandAttributes.getLength(); j++) {
                            if (commandAttributes.item(j).getNodeName().equals("scheduler_host") && commandAttributes.item(j).getNodeValue().length() > 0) {
                                this.getLogger().debug7("using host from command: " + commandAttributes.item(j).getNodeValue());
                                commandHost = commandAttributes.item(j).getNodeValue();
                            }
                            if (commandAttributes.item(j).getNodeName().equals("scheduler_port") && commandAttributes.item(j).getNodeValue().length() > 0) {
                                commandPort = commandAttributes.item(j).getNodeValue();
                            }
                            if (commandAttributes.item(j).getNodeName().equals("protocol") && commandAttributes.item(j).getNodeValue().length() > 0) {
                                commandProtocol = commandAttributes.item(j).getNodeValue();
                            }
                        }
                        SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
                        schedulerCommand.setTimeout(socket_timeout);
                        if (commandHost.length() > 0) {
                            schedulerCommand.setHost(commandHost);
                            if (commandPort.length() > 0) {
                                schedulerCommand.setPort(Integer.parseInt(commandPort));
                            } else {
                                throw new Exception("empty port has been specified by event handler response for commands");
                            }
                        } else {
                            throw new Exception("empty JobScheduler ID or host and port have been specified by event handler response for commands");
                        }
                        if (commandProtocol.length() > 0) {
                            schedulerCommand.setProtocol(commandProtocol);
                        }
                        try {
                            this.getLogger().debug1(".. connecting to JobScheduler " + schedulerCommand.getHost() + ":" + schedulerCommand.getPort());
                            schedulerCommand.connect();
                            NodeList commandElements = command.getChildNodes();
                            for (int k = 0; k < commandElements.getLength(); k++) {
                                if (commandElements.item(k).getNodeType() != Node.ELEMENT_NODE) {
                                    continue;
                                }
                                String commandRequest = this.xmlNodeToString(commandElements.item(k));
                                commandRequest = parameterSubstitutor.replace(commandRequest);
                                commandRequest = parameterSubstitutor.replaceEnvVars(commandRequest);
                                commandRequest = parameterSubstitutor.replaceSystemProperties(commandRequest);
                                this.getLogger().info(".. sending command to remote JobScheduler [" + commandHost + ":" + commandPort + "]: " + commandRequest);
                                schedulerCommand.sendRequest(commandRequest);
                                SOSXMLXPath answer = new SOSXMLXPath(new StringBuffer(schedulerCommand.getResponse()));
                                String errorText = answer.selectSingleNodeValue("//ERROR/@text");
                                if (errorText != null && errorText.length() > 0) {
                                    throw new Exception("could not send command to remote JobScheduler [" + commandHost + ":" + commandPort + "]: " + errorText);
                                }
                            }
                        } catch (Exception e) {
                            throw new Exception("Error contacting remote JobScheduler: " + e, e);
                        } finally {
                            try {
                                schedulerCommand.disconnect();
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("could not execute command: " + e.getMessage());
            }
            try {
                this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
                while (this.getEventHandlerResultFileListIterator().hasNext()) {
                    Object result = this.getEventHandlerResultFileListIterator().next();
                    NodeList commands = null;
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        if (resultFile == null) {
                            continue;
                        }
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document eventDocument = docBuilder.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
                        commands = XPathAPI.selectNodeList(eventDocument, "//remove_event");
                    } else {
                        commands = XPathAPI.selectNodeList((Node) result, "remove_event");
                    }
                    this.getLogger().debug3("-->" + commands.getLength() + " events should be deleted");
                    for (int i = 0; i < commands.getLength(); i++) {
                        if (commands.item(i) == null) {
                            continue;
                        }
                        if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            this.removeEvents(commands.item(i).getChildNodes());
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("could not remove event caused by event handler: " + e.getMessage());
            }
            try {
                this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
                while (this.getEventHandlerResultFileListIterator().hasNext()) {
                    Object result = this.getEventHandlerResultFileListIterator().next();
                    NodeList commands = null;
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        if (resultFile == null) {
                            continue;
                        }
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document eventDocument = docBuilder.parse(new InputSource(new StringReader(this.getFileContent(resultFile).toString())));
                        commands = XPathAPI.selectNodeList(eventDocument, "//add_event/event");
                    } else {
                        commands = XPathAPI.selectNodeList((Node) result, "add_event/event");
                    }
                    for (int i = 0; i < commands.getLength(); i++) {
                        if (commands.item(i) == null) {
                            continue;
                        }
                        if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            NamedNodeMap attributes = commands.item(i).getAttributes();
                            if (attributes == null) {
                                continue;
                            }
                            Element event = this.getEvents().createElement("event");
                            for (int j = 0; j < attributes.getLength(); j++) {
                                if ("event_name".equalsIgnoreCase(attributes.item(j).getNodeName())) {
                                    continue;
                                }
                                event.setAttribute(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());
                            }
                            this.addEvent(event);
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("could not add event created by event handler: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new Exception("events processed with errors: " + e.getMessage());
        } finally {
            try {
                this.setEventHandlerResultFileListIterator(this.getEventHandlerResultFileList().iterator());
                while (this.getEventHandlerResultFileListIterator().hasNext()) {
                    Object result = this.getEventHandlerResultFileListIterator().next();
                    if (result instanceof File) {
                        File resultFile = (File) result;
                        if (resultFile != null) {
                            if (!resultFile.delete()) {
                                resultFile.deleteOnExit();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                this.getLogger().warn("could not delete temporary file: " + e.getMessage());
            }
        }
    }

    private void logTransformation(final File resultFile, final File stylesheetFile) throws Exception {
        try {
            File logDir = new File(spooler.log_dir());
            File eventLogDir = new File(logDir, "events");
            if (!eventLogDir.exists()) {
                getLogger().info("creating event log dir: " + eventLogDir.getAbsolutePath());
                if (!eventLogDir.mkdir()) {
                    getLogger().warn("directory [" + eventLogDir.getAbsolutePath() + "] could not be created.");
                    return;
                }
            }
            File stylesheetLogDir = new File(eventLogDir, stylesheetFile.getName());
            if (!stylesheetLogDir.exists()) {
                getLogger().info("creating stylesheet log dir: " + stylesheetLogDir.getAbsolutePath());
                if (!stylesheetLogDir.mkdir()) {
                    getLogger().warn("directory [" + stylesheetLogDir.getAbsolutePath() + "] could not be created.");
                    return;
                }
            }
            String timeStamp = SOSDate.getCurrentDateAsString("yyyy-MM-dd_HHmmss_SSS");
            File resultLogFile = new File(stylesheetLogDir, "events_" + timeStamp + "_result.xml");
            SOSFile.copyFile(resultFile, resultLogFile);
            File eventFile = new File(stylesheetLogDir, "events_" + timeStamp + ".xml");
            // write current events to eventFile
            OutputStream fout = new FileOutputStream(eventFile, false);
            OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8");
            OutputFormat format = new OutputFormat(getEvents());
            format.setEncoding("UTF-8");
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(getEvents());
            out.close();
            getLogger().debug("current events logged to: " + eventFile.getAbsolutePath());
            getLogger().debug("transformation result logged to: " + resultLogFile.getAbsolutePath());
        } catch (Exception e) {
            throw new Exception("Error logging Transformation result: " + e, e);
        }
    }

    private void addEvent() throws Exception {
        try {
            this.getLogger().debug9(".. constructing event: schedulerId=" + this.getEventSchedulerId() + ", eventClass=" + this.getEventClass() + ", eventId="
                    + this.getEventId());
            Element event = this.getEvents().createElement("event");
            event.setAttribute("scheduler_id", this.getEventSchedulerId());
            event.setAttribute("remote_scheduler_host", this.getEventRemoteSchedulerHost());
            event.setAttribute("remote_scheduler_port", this.getEventRemoteSchedulerPort());
            event.setAttribute("job_chain", this.getEventJobChainName());
            event.setAttribute("order_id", this.getEventOrderId());
            event.setAttribute("job_name", this.getEventJobName());
            event.setAttribute("event_class", this.getEventClass());
            event.setAttribute("event_id", this.getEventId());
            event.setAttribute("exit_code", this.getEventExitCode());
            event.setAttribute("created", this.getEventCreated());
            if (getEventExpires().length() == 0 || "default".equalsIgnoreCase(getEventExpires())) {
                event.setAttribute("expires", SOSDate.getTimeAsString(this.getExpirationDate().getTime()));
            } else {
                event.setAttribute("expires", this.getEventExpires());
            }
            if (this.getEventParameters() != null && this.getEventParameters().getChildNodes().getLength() > 0) {
                event.appendChild(this.getEventParameters());
            }
            this.addEvent(event);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private void addEvent(final Element event) throws Exception {
        addEvent(event, true);
    }

    private void addEvent(final Element event, final boolean replace) throws Exception {
        if (event.getAttribute("scheduler_id").length() == 0) {
            event.setAttribute("scheduler_id", spooler.id());
        }
        if (replace) {
            Element dummyParent = getEvents().createElement("events");
            Element remEv = (Element) event.cloneNode(true);
            dummyParent.appendChild(remEv);
            getLogger().debug9("remEv: " + remEv);
            getLogger().debug9("remEv.getParentNode(): " + remEv.getParentNode());
            remEv.removeAttribute("created");
            remEv.removeAttribute("expires");
            removeEvents(remEv.getParentNode().getChildNodes());
        }
        try {
            String curEventSchedulerId = this.getAttributeValue(event, "scheduler_id");
            String curEventRemoteSchedulerHost = this.getAttributeValue(event, "remote_scheduler_host");
            String curEventRemoteSchedulerPort = this.getAttributeValue(event, "remote_scheduler_port");
            String curEventClass = this.getAttributeValue(event, "event_class");
            String curEventId = this.getAttributeValue(event, "event_id");
            String curEventExitCode = this.getAttributeValue(event, "exit_code");
            String curEventJobChainName = this.getAttributeValue(event, "job_chain");
            String curEventOrderId = this.getAttributeValue(event, "order_id");
            String curEventJobName = this.getAttributeValue(event, "job_name");
            String curEventCreated = this.getAttributeValue(event, "created");
            String curExpiration_period = this.getAttributeValue(event, "expiration_period");
            String curExpiration_cycle = this.getAttributeValue(event, "expiration_cycle");
            this.getLogger().debug3(".. --> curExpiration_period:" + curExpiration_period);
            this.getLogger().debug3(".. --> curExpiration_cycle:" + curExpiration_cycle);
            if (curExpiration_period == null || curExpiration_period.length() <= 0) {
                curExpiration_period = this.getExpirationPeriod();
            }
            if (curExpiration_cycle == null || curExpiration_cycle.length() <= 0) {
                curExpiration_cycle = this.getExpirationCycle();
            }
            String curEventExpires = this.getAttributeValue(event, "expires");
            this.getLogger().debug3(".. --> curEventExpires:" + curEventExpires);
            if (curEventExpires == null || curEventExpires.length() <= 0) {
                Calendar cal = calculateExpirationDate(curExpiration_cycle, curExpiration_period);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    curEventExpires = format.format(cal.getTime());
                    this.getLogger().debug3(".. --> curEventExpires:" + curEventExpires);
                } catch (Exception pe) {
                    this.getLogger().warn(cal.getTime().toString() + " is not a valid Date. Expires will be set to default");
                }
            }

            if ("never".equalsIgnoreCase(curEventExpires)) {
                curEventExpires = NEVER_DATE;
                this.getLogger().debug3(".. --> curEventExpires:" + curEventExpires);
            }
            this.getLogger().info(".. adding event ...: scheduler id=" + curEventSchedulerId + ", event class=" + curEventClass + ", event id=" + curEventId
                    + ", exit code=" + curEventExitCode + ", job chain=" + curEventJobChainName + ", order id=" + curEventOrderId + ", job=" + curEventJobName);
            if (curEventId.isEmpty()) {
                throw new Exception("Empty event_id is not allowed.");
            }
            this.getEvents().getLastChild().appendChild(event);
            if (this.getConnection() != null) {
                String stmt = "INSERT INTO "
                        + tableEvents
                        + " (\"SPOOLER_ID\", \"REMOTE_SCHEDULER_HOST\", \"REMOTE_SCHEDULER_PORT\", \"JOB_CHAIN\", \"ORDER_ID\", \"JOB_NAME\", \"EVENT_CLASS\", \"EVENT_ID\", \"EXIT_CODE\", \"CREATED\", \"EXPIRES\")"
                        + " VALUES ('" + curEventSchedulerId + "', '" + curEventRemoteSchedulerHost + "', "
                        + (curEventRemoteSchedulerPort.length() == 0 ? "0" : curEventRemoteSchedulerPort) + ", '" + curEventJobChainName + "', '"
                        + curEventOrderId + "', '" + curEventJobName + "', '" + curEventClass + "', '" + curEventId + "', '" + curEventExitCode + "', "
                        + (curEventCreated.length() > 0 ? "%timestamp_iso('" + curEventCreated + "')" : "%now") + ", "
                        + (curEventExpires.length() > 0 ? "%timestamp_iso('" + curEventExpires + "')" : "NULL") + ")";
                this.getConnection().executeUpdate(stmt);
                NodeList nodes = XPathAPI.selectNodeList(event, "params");
                if (nodes != null && nodes.getLength() > 0) {
                    getLogger().debug5("Event has parameters. Storing parameters...");
                    Element params = (Element) nodes.item(0);
                    String paramsString = xmlElementToString(params);
                    getLogger().debug5(paramsString);
                    String sequenceName = "SCHEDULER_EVENTS_ID_SEQ";
                    if (getConnection() instanceof SOSPgSQLConnection)
                        sequenceName = "\"scheduler_events_ID_seq\"";
                    String eventDbID = getConnection().getLastSequenceValue(sequenceName);
                    getConnection().updateClob(tableEvents, "PARAMETERS", paramsString, "\"ID\"=" + eventDbID);
                }
                this.getConnection().commit();
            }
        } catch (Exception e) {
            if (this.getConnection() != null) {
                try {
                    this.getConnection().rollback();
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
            throw new Exception(e);
        }
    }

    private void removeEvents(final NodeList events1) throws Exception {
        try {
            for (int i = 0; i < events1.getLength(); i++) {
                if (events1.item(i) == null || events1.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String xquery = "//event[";
                String and = "";
                Element event = (Element) events1.item(i);
                String curEventClass = event.getAttribute("event_class");
                if (curEventClass != null && curEventClass.equalsIgnoreCase(JobSchedulerConstants.EVENT_CLASS_ALL_EVENTS)) {
                    removeAllEvents();
                    return;
                }
                boolean hasAttributes = false;
                NamedNodeMap eventAttributeList = event.getAttributes();
                for (int j = 0; j < eventAttributeList.getLength(); j++) {
                    Node eventAttribute = eventAttributeList.item(j);
                    if (eventAttribute == null || "event_name".equalsIgnoreCase(eventAttribute.getNodeName())) {
                        continue;
                    }
                    String value = eventAttribute.getNodeValue();
                    if (!eventAttribute.getNodeName().equals("event_title") && value != null && value.length() > 0) {
                        hasAttributes = true;
                        xquery += and + "@" + eventAttribute.getNodeName() + "='" + eventAttribute.getNodeValue() + "'";
                        and = " and ";
                    }
                }
                xquery += "]";
                if (!hasAttributes) {
                    getLogger().warn("current event has no attributes. Removal (of all elements) will not be performed.");
                    continue;
                }
                getLogger().debug7("xquery to remove events: " + xquery);
                NodeList nodes = XPathAPI.selectNodeList(this.getEvents(), xquery);
                for (int j = 0; j < nodes.getLength(); j++) {
                    if (nodes.item(j) == null) {
                        continue;
                    } else if (nodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        this.removeEvent(nodes.item(j));
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private void removeAllEvents() throws Exception {
        try {
            getLogger().info("event class is: " + JobSchedulerConstants.EVENT_CLASS_ALL_EVENTS + ". Removing all events.");
            Document eventDocument = getEvents();
            eventDocument.removeChild(eventDocument.getFirstChild());
            eventDocument.appendChild(eventDocument.createElement("events"));
            if (this.getConnection() != null) {
                String stmt = "DELETE FROM " + tableEvents + " WHERE 1>0";
                this.getConnection().executeUpdate(stmt);
                this.getConnection().commit();
            }
            this.setEventClass("");
        } catch (Exception e) {
            throw new Exception("Error removing all events: " + e, e);
        }
    }

    /** remove an event */
    private void removeEvent() throws Exception {
        try {
            this.getLogger().debug9(".. constructing event: eventClass=" + this.getEventClass() + ", eventId=" + this.getEventId());
            Element event = this.getEvents().createElement("event");
            event.setAttribute("event_class", this.getEventClass());
            event.setAttribute("event_id", this.getEventId());
            event.setAttribute("scheduler_id", this.getEventSchedulerId());
            event.setAttribute("remote_scheduler_host", this.getEventRemoteSchedulerHost());
            if (!"0".equalsIgnoreCase(this.getEventRemoteSchedulerPort())) {
                event.setAttribute("remote_scheduler_port", this.getEventRemoteSchedulerPort());
            }
            event.setAttribute("job_chain", this.getEventJobChainName());
            event.setAttribute("order_id", this.getEventOrderId());
            event.setAttribute("job_name", this.getEventJobName());
            if (this.getEventExitCode().length() > 0) {
                event.setAttribute("exit_code", this.getEventExitCode());
            }
            Element dummyParent = getEvents().createElement("events");
            dummyParent.appendChild(event);
            this.removeEvents(event.getParentNode().getChildNodes());
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /** remove an event */
    private void removeEvent(final Node event) throws Exception {
        try {
            String curEventSchedulerId = this.getAttributeValue(event, "scheduler_id");
            String curEventClass = this.getAttributeValue(event, "event_class");
            String curEventId = this.getAttributeValue(event, "event_id");
            String curEventExitCode = this.getAttributeValue(event, "exit_code");
            String curEventJobChainName = this.getAttributeValue(event, "job_chain");
            String curEventOrderId = this.getAttributeValue(event, "order_id");
            String curEventJobName = this.getAttributeValue(event, "job_name");
            this.getLogger().info(".. removing event ...: scheduler id=" + curEventSchedulerId + ", event class=" + curEventClass + ", event id=" + curEventId
                    + ", exit code=" + curEventExitCode + ", job chain=" + curEventJobChainName + ", order id=" + curEventOrderId + ", job=" + curEventJobName);
            Node nEvents = this.getEvents().getFirstChild();
            this.getLogger().debug7("Events Name: " + nEvents.getLocalName());
            this.getLogger().debug7("Events size: " + nEvents.getChildNodes().getLength());
            this.getEvents().getFirstChild().removeChild(event);
            this.getLogger().debug7("Events size: " + nEvents.getChildNodes().getLength());
            if (this.getConnection() != null) {
                String stmt = "DELETE FROM " + tableEvents + " WHERE ";
                String and = "";
                if (!curEventSchedulerId.isEmpty()) {
                    stmt += and + "\"SPOOLER_ID\"='" + curEventSchedulerId + "'";
                    and = " AND ";
                }
                if (!curEventJobChainName.isEmpty()) {
                    stmt += and + "\"JOB_CHAIN\"='" + curEventJobChainName + "'";
                    and = " AND ";
                }
                if (!curEventOrderId.isEmpty()) {
                    stmt += and + "\"ORDER_ID\"='" + curEventOrderId + "'";
                    and = " AND ";
                }
                if (!curEventJobName.isEmpty()) {
                    stmt += and + "\"JOB_NAME\"='" + curEventJobName + "'";
                    and = " AND ";
                }
                if (!curEventClass.isEmpty()) {
                    stmt += and + "\"EVENT_CLASS\"='" + curEventClass + "'";
                    and = " AND ";
                }
                if (!curEventId.isEmpty()) {
                    stmt += and + "\"EVENT_ID\"='" + curEventId + "'";
                    and = " AND ";
                }
                if (!curEventExitCode.isEmpty()) {
                    stmt += and + "\"EXIT_CODE\"='" + curEventExitCode + "'";
                    and = " AND ";
                }
                this.getConnection().executeUpdate(stmt);
                this.getConnection().commit();
            }
        } catch (Exception e) {
            if (this.getConnection() != null) {
                try {
                    this.getConnection().rollback();
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
            throw new Exception(e);
        }
    }

    private String getAttributeValue(final Node node, final String namedItem) throws Exception {
        try {
            if (node.getAttributes() == null || node.getAttributes().getLength() == 0) {
                return "";
            }
            if (node.getAttributes().getNamedItem(namedItem) == null) {
                return "";
            }
            return node.getAttributes().getNamedItem(namedItem).getNodeValue();
        } catch (Exception e) {
            throw new Exception("error occurred reading attribute value: " + e.getMessage());
        }
    }

    private String xmlNodeToString(final Node node) throws Exception {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();
            document.appendChild(document.importNode(node, true));
            StringWriter out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(document));
            serializer.serialize(document);
            return out.toString();
        } catch (Exception e) {
            throw new Exception("error occurred transforming node: " + e.getMessage());
        }
    }

    private String xmlDocumentToString(final Document document) throws Exception {
        try {
            StringWriter out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(document));
            serializer.serialize(document);
            return out.toString();
        } catch (Exception e) {
            throw new Exception("error occurred transforming document: " + e.getMessage());
        }
    }

    private String xmlElementToString(final Element element) throws Exception {
        try {
            StringWriter out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, new OutputFormat());
            serializer.serialize(element);
            return out.toString();
        } catch (Exception e) {
            throw new Exception("error occurred transforming document: " + e.getMessage());
        }
    }

    private StringBuffer getFileContent(final File file) throws Exception {
        BufferedInputStream in = null;
        StringBuffer content = new StringBuffer();
        if (file == null)
            throw new Exception("no valid file object found");
        if (!file.canRead())
            throw new Exception("file not accessible: " + file.getCanonicalPath());
        try {
            FileInputStream fis = new FileInputStream(file);
            in = new BufferedInputStream(fis);
            byte buffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead));
            }
            fis.close();
            fis = null;
            in.close();
            in = null;
            return content;
        } catch (Exception e) {
            throw new Exception("error occurred reading content of file [" + file.getCanonicalPath() + "]: " + e.getMessage());
        } finally {
            if (in != null) {
                in.close();
                in = null;
            }
        }
    }

    private Variable_set getParameters() {
        return parameters;
    }

    private void setParameters(final Variable_set parameters) {
        this.parameters = parameters;
    }

    private String getEventClass() {
        return eventClass;
    }

    private void setEventClass(final String eventClass) {
        this.eventClass = eventClass;
    }

    private String getEventExpires() {
        return eventExpires;
    }

    private void setEventExpires(final String eventExpires) {
        this.eventExpires = eventExpires;
    }

    private String getEventId() {
        return eventId;
    }

    private void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    private Element getEventParameters() {
        return eventParameters;
    }

    private void setEventParameters(final Element eventParameters) {
        this.eventParameters = eventParameters;
    }

    private String getEventAction() {
        return eventAction;
    }

    private void setEventAction(final String eventAction) throws Exception {
        if (!eventAction.equalsIgnoreCase("add") && !eventAction.equalsIgnoreCase("remove") && !eventAction.equalsIgnoreCase("process")) {
            throw new Exception("invalid action specified [add, remove, process]: " + eventAction);
        }
        this.eventAction = eventAction;
    }

    private Document getEvents() {
        return events;
    }

    private void setEvents(final Document events) {
        this.events = events;
    }

    private String getEventSchedulerId() {
        return eventSchedulerId;
    }

    private void setEventSchedulerId(final String eventSchedulerId) {
        this.eventSchedulerId = eventSchedulerId;
    }

    private String getExpirationPeriod() {
        return expirationPeriod;
    }

    private void setExpirationPeriod(final String expirationPeriod) {
        this.expirationPeriod = expirationPeriod;
    }

    private String getEventJobChainName() {
        return eventJobChainName;
    }

    private void setEventJobChainName(final String eventJobChainName) {
        this.eventJobChainName = eventJobChainName;
    }

    private String getEventJobName() {
        return eventJobName;
    }

    private void setEventJobName(final String eventJobName) {
        this.eventJobName = eventJobName;
    }

    private String getEventHandlerFilepath() {
        return eventHandlerFilepath;
    }

    private void setEventHandlerFilepath(final String eventHandlerFilepath) {
        this.eventHandlerFilepath = eventHandlerFilepath;
    }

    private String getEventHandlerFilespec() {
        return eventHandlerFilespec;
    }

    private void setEventHandlerFilespec(final String eventHandlerFilespec) {
        this.eventHandlerFilespec = eventHandlerFilespec;
    }

    private Collection<File> getEventHandlerFileList() {
        return eventHandlerFileList;
    }

    private void setEventHandlerFileList(final Collection<File> eventHandlerFileList) {
        this.eventHandlerFileList = eventHandlerFileList;
    }

    private Iterator<File> getEventHandlerFileListIterator() {
        return eventHandlerFileListIterator;
    }

    private void setEventHandlerFileListIterator(final Iterator<File> eventHandlerFileListIterator) {
        this.eventHandlerFileListIterator = eventHandlerFileListIterator;
    }

    private Collection<Object> getEventHandlerResultFileList() {
        return eventHandlerResultFileList;
    }

    private void setEventHandlerResultFileList(final Collection<Object> eventHandlerResultFileList) {
        this.eventHandlerResultFileList = eventHandlerResultFileList;
    }

    private Calendar getExpirationDate() {
        return expirationDate;
    }

    private void setExpirationDate(final Calendar expirationDate) {
        this.expirationDate = expirationDate;
    }

    private Iterator<Object> getEventHandlerResultFileListIterator() {
        return eventHandlerResultFileListIterator;
    }

    private void setEventHandlerResultFileListIterator(final Iterator<Object> eventHandlerResultFileListIterator) {
        this.eventHandlerResultFileListIterator = eventHandlerResultFileListIterator;
    }

    private String getEventRemoteSchedulerHost() {
        return eventRemoteSchedulerHost;
    }

    private void setEventRemoteSchedulerHost(final String eventRemoteSchedulerHost) {
        this.eventRemoteSchedulerHost = eventRemoteSchedulerHost;
    }

    private String getEventRemoteSchedulerPort() {
        return eventRemoteSchedulerPort;
    }

    private void setEventRemoteSchedulerPort(final String eventRemoteSchedulerPort) {
        this.eventRemoteSchedulerPort = eventRemoteSchedulerPort;
    }

    private String getEventExitCode() {
        return eventExitCode;
    }

    private void setEventExitCode(final String eventExitCode) {
        this.eventExitCode = eventExitCode;
    }

    private String getEventOrderId() {
        return eventOrderId;
    }

    private void setEventOrderId(final String eventOrderId) {
        this.eventOrderId = eventOrderId;
    }

    private String getEventCreated() {
        return eventCreated;
    }

    private void setEventCreated(final String eventCreated) {
        this.eventCreated = eventCreated;
    }

    private String getExpirationCycle() {
        return expirationCycle;
    }

    private void setExpirationCycle(final String expirationCycle) {
        this.expirationCycle = expirationCycle;
    }

    private void setSocket_timeout(final String s) {
        try {
            socket_timeout = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            spooler_log.warn("Illegal value for parameter socket_timeout:" + s + ". Integer expected. Using default=5");
            socket_timeout = 5;
        }
    }

}
