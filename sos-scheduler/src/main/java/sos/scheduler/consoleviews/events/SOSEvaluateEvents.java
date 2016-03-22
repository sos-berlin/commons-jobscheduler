package sos.scheduler.consoleviews.events;

import java.io.File;
import java.io.StringReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerConstants;
import sos.util.SOSDate;

public class SOSEvaluateEvents {

    private LinkedHashSet listOfActions;
    private LinkedHashSet listOfActiveEvents;
    private String errmsg;
    private String host;
    private int port;
    private Document activeEvents = null;
    private final static String conSVNVersion = "$Id$";
    private static final Logger LOGGER = Logger.getLogger(SOSEvaluateEvents.class);

    public SOSEvaluateEvents(final String host_, final int port_) {
        super();
        host = host_;
        port = port_;
        listOfActiveEvents = new LinkedHashSet();
    }

    public void reconnect(final String host_, final int port_) {
        host = host_;
        port = port_;
        listOfActiveEvents = new LinkedHashSet();
    }

    public String getEventStatus(final SchedulerEvent event) {
        String erg = "missing";
        Iterator i = listOfActiveEvents.iterator();
        while (i.hasNext()) {
            SchedulerEvent e = (SchedulerEvent) i.next();
            if (event.isEqual(e)) {
                event.created = e.created;
                event.expires = e.expires;
                erg = "active";
            }
        }
        return erg;
    }

    private String sendCommand(final String command) {
        String s = "";
        SOSSchedulerCommand socket = null;
        try {
            socket = new SOSSchedulerCommand();
            socket.connect(host, port);
            LOGGER.debug(String.format("Sending command '%3$s' to JobScheduler %1$s:%2$d ", host, port, command));
            socket.sendRequest(command);
            s = socket.getResponse();
            LOGGER.debug(String.format("Answer is '%1$s' ", s));
        } catch (Exception ee) {
            LOGGER.error("Error sending Command: " + ee.getMessage(), ee);
        } finally {
            if (socket != null) {
                try {
                    socket.disconnect();
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
        }
        return s;
    }

    public void buildEventsFromXMl() throws DOMException, Exception {
        String response = "";
        Document doc = null;
        if (activeEvents == null) {
            response = sendCommand("<param.get name=\"" + JobSchedulerConstants.eventVariableName + "\"/>");
        }
        if (!"".equals(response) || activeEvents != null) {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            try {
                listOfActiveEvents.clear();
                if (activeEvents == null) {
                    docBuilder = docFactory.newDocumentBuilder();
                    doc = docBuilder.parse(new InputSource(new StringReader(response)));
                    NodeList params = doc.getElementsByTagName("param");
                    if (params.item(0) == null) {
                        errmsg = "No events param found in Job Scheduler answer for " + JobSchedulerConstants.eventVariableName;
                    } else {
                        NamedNodeMap attrParam = params.item(0).getAttributes();
                        String eventString = getText(attrParam.getNamedItem("value"));
                        eventString = eventString.replaceAll(String.valueOf((char) 254), "<").replaceAll(String.valueOf((char) 255), ">");
                        docFactory = DocumentBuilderFactory.newInstance();
                        docBuilder = docFactory.newDocumentBuilder();
                        doc = docBuilder.parse(new InputSource(new StringReader(eventString)));
                    }
                } else {
                    doc = activeEvents;
                }
                if (doc != null) {
                    NodeList nodes = org.apache.xpath.XPathAPI.selectNodeList(doc, "//events/event");
                    int activeNodeCount = 0;
                    int expiredNodeCount = 0;
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Node curEventExpires = node.getAttributes().getNamedItem("expires");
                        if (curEventExpires == null || curEventExpires.getNodeValue() == null || curEventExpires.getNodeValue().isEmpty()) {
                            activeNodeCount++;
                            continue;
                        }
                        boolean isNever = curEventExpires.getNodeValue().equalsIgnoreCase("never");
                        Calendar expiresDate = GregorianCalendar.getInstance();
                        Calendar now = GregorianCalendar.getInstance();
                        if (!isNever) {
                            expiresDate.setTime(SOSDate.getTime(curEventExpires.getNodeValue()));
                        }
                        if (!isNever && expiresDate.before(now)) {
                            doc.getFirstChild().removeChild(node);
                            expiredNodeCount++;
                        } else {
                            activeNodeCount++;
                        }
                    }
                    NodeList events = doc.getElementsByTagName("event");
                    for (int ii = 0; ii < events.getLength(); ii++) {
                        Node n = events.item(ii);
                        NamedNodeMap attr = n.getAttributes();
                        SchedulerEvent e = new SchedulerEvent();
                        e.event_class = getText(attr.getNamedItem("event_class"));
                        e.event_id = getText(attr.getNamedItem("event_id"));
                        e.job_name = getText(attr.getNamedItem("job_name"));
                        e.job_chain = getText(attr.getNamedItem("job_chain"));
                        e.order_id = getText(attr.getNamedItem("order_id"));
                        e.exit_code = getText(attr.getNamedItem("exit_code"));
                        e.created = getText(attr.getNamedItem("created"));
                        e.expires = getText(attr.getNamedItem("expires"));
                        e.comment = getText(attr.getNamedItem("comment"));
                        e.scheduler_id = getText(attr.getNamedItem("scheduler_id"));
                        e.remote_scheduler_host = getText(attr.getNamedItem("remote_scheduler_host"));
                        e.remote_scheduler_port = getText(attr.getNamedItem("remote_scheduler_port"));
                        listOfActiveEvents.add(e);
                    }
                }
            } catch (ParserConfigurationException e) {
                errmsg = "XML-Answer from Scheduler was invalid";
            }
        }
    }

    private void fillTreeItem(final SOSActions a, final Node n) {
        a.commandNodes = n.getChildNodes();
        for (int i = 0; i < a.commandNodes.getLength(); i++) {
            Node command = a.commandNodes.item(i);
            if ("command".equals(command.getNodeName()) || "remove_event".equals(command.getNodeName()) || "add_event".equals(command.getNodeName())) {
                SOSEventCommand ec = new SOSEventCommand();
                ec.setCommand(command);
                a.listOfCommands.add(ec);
            }
        }
    }

    private void fillEvents(final Node eventGroup, final SOSEventGroups evg) {
        NodeList events = eventGroup.getChildNodes();
        for (int i = 0; i < events.getLength(); i++) {
            Node event = events.item(i);
            if (event.getNodeName().equals("event")) {
                NamedNodeMap attr = event.getAttributes();
                SchedulerEvent e = new SchedulerEvent();
                e.event_class = getTextDefault("", attr.getNamedItem("event_class"));
                if ("".equals(e.event_class)) {
                    e.event_class = evg.event_class;
                }
                e.setEvent_name(getText(attr.getNamedItem("event_name")));
                e.event_title = getText(attr.getNamedItem("event_title"));
                e.event_id = getText(attr.getNamedItem("event_id"));
                e.job_name = getText(attr.getNamedItem("job_name"));
                e.job_chain = getText(attr.getNamedItem("job_chain"));
                e.order_id = getText(attr.getNamedItem("order_id"));
                e.exit_code = getText(attr.getNamedItem("exit_code"));
                e.created = getText(attr.getNamedItem("created"));
                e.expires = getText(attr.getNamedItem("expires"));
                e.comment = getText(attr.getNamedItem("comment"));
                e.remote_scheduler_host = getText(attr.getNamedItem("remote_scheduler_host"));
                e.remote_scheduler_port = getText(attr.getNamedItem("remote_scheduler_port"));
                e.scheduler_id = getText(attr.getNamedItem("scheduler_id"));
                evg.listOfEvents.add(e);
            }
        }
    }

    private void fillEventGroups(final SOSActions a, final Node n) {
        NamedNodeMap attrEvents = n.getAttributes();
        a.logic = getText(attrEvents.getNamedItem("logic"));
        NodeList eventGroups = n.getChildNodes();
        for (int i = 0; i < eventGroups.getLength(); i++) {
            Node eventGroup = eventGroups.item(i);
            if ("event_group".equals(eventGroup.getNodeName())) {
                NamedNodeMap attr = eventGroup.getAttributes();
                SOSEventGroups evg = new SOSEventGroups(getText(attr.getNamedItem("group")));
                evg.logic = getText(attr.getNamedItem("logic"));
                evg.group = getText(attr.getNamedItem("group"));
                evg.event_class = getText(attr.getNamedItem("event_class"));
                fillEvents(eventGroup, evg);
                a.listOfEventGroups.add(evg);
            }
        }
    }

    private void fillAction(final SOSActions a, final NodeList actionChilds) {
        for (int i = 0; i < actionChilds.getLength(); i++) {
            Node n = actionChilds.item(i);
            if ("commands".equals(n.getNodeName())) {
                a.commands = n;
                fillTreeItem(a, n);
            } else if ("events".equals(n.getNodeName())) {
                fillEventGroups(a, n);
            }
        }
    }

    public void readConfigurationFile(final File f) throws DOMException, Exception {
        listOfActions = new LinkedHashSet();
        if (f.exists()) {
            buildEventsFromXMl();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            try {
                listOfActions.clear();
                docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(f);
                NodeList actions = doc.getElementsByTagName("action");
                if (actions.item(0) == null) {
                    errmsg = "No actions defined in " + f.getCanonicalPath();
                    LOGGER.debug(errmsg);
                } else {
                    for (int i = 0; i < actions.getLength(); i++) {
                        NamedNodeMap attr = actions.item(i).getAttributes();
                        String action_name = getText(attr.getNamedItem("name"));
                        SOSActions a = new SOSActions(action_name);
                        listOfActions.add(a);
                        fillAction(a, actions.item(i).getChildNodes());
                    }
                }
            } catch (ParserConfigurationException e) {
                errmsg = "Error reading actions from " + f.getAbsolutePath();
                LOGGER.debug(errmsg);
            }
        }
    }

    private String getText(final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return "";
        }
    }

    private String getTextDefault(final String d, final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return d;
        }
    }

    public LinkedHashSet getListOfActions() {
        return listOfActions;
    }

    public LinkedHashSet getListOfActiveEvents() {
        return listOfActiveEvents;
    }

    public static void main(final String[] args) {
        SOSEvaluateEvents eval = new SOSEvaluateEvents("localhost", 4454);
        String configuration_filename = "c:/roche/scheduler/config/events/splitt_gsg.actions.xml";
        File f = new File(configuration_filename);
        try {
            eval.readConfigurationFile(f);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Iterator iActions = eval.getListOfActions().iterator();
        while (iActions.hasNext()) {
            SOSActions a = (SOSActions) iActions.next();
            // Die Nodelist verwenden
            for (int i = 0; i < a.getCommandNodes().getLength(); i++) {
                Node n = a.getCommandNodes().item(i);
                if ("command".equals(n.getNodeName())) {
                    LOGGER.debug(n.getNodeName());
                    NamedNodeMap attr = n.getAttributes();
                    if (attr != null) {
                        for (int ii = 0; ii < attr.getLength(); ii++) {
                            LOGGER.debug(attr.item(ii).getNodeName() + "=" + attr.item(ii).getNodeValue());
                        }
                    }
                }
            }
            for (int i = 0; i < a.getCommands().getChildNodes().getLength(); i++) {
                Node n = a.getCommands().getChildNodes().item(i);
                if ("command".equals(n.getNodeName())) {
                    LOGGER.debug(n.getNodeName());
                    NamedNodeMap attr = n.getAttributes();
                    if (attr != null) {
                        for (int ii = 0; ii < attr.getLength(); ii++) {
                            LOGGER.debug(attr.item(ii).getNodeName() + "=" + attr.item(ii).getNodeValue());
                        }
                    }
                }
            }
            if (a != null) {
                Iterator i = a.getListOfEventGroups().iterator();
                while (i.hasNext()) {
                    SOSEventGroups evg = (SOSEventGroups) i.next();
                    Iterator iEvents = evg.getListOfEvents().iterator();
                    while (iEvents.hasNext()) {
                        SchedulerEvent event = (SchedulerEvent) iEvents.next();
                        LOGGER.debug(event.getJob_name() + " " + eval.getEventStatus(event));
                    }
                }
            }
            if (a.isActive(eval.getListOfActiveEvents())) {
                LOGGER.debug(a.name + " is active");
            } else {
                LOGGER.debug(a.name + " is NOT active");
            }
        }
    }

    public void setActiveEvents(final Document activeEvents) {
        this.activeEvents = activeEvents;
    }

}
