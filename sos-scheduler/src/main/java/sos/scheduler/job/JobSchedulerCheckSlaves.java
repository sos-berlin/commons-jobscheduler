package sos.scheduler.job;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.util.SOSDate;
import sos.util.SOSString;

/** @author Andreas Liebert */
public class JobSchedulerCheckSlaves extends JobSchedulerJob {

    private Vector requestedSlaves;
    private Vector registeredSlaves;
    private Vector connectedSlaves;
    private DocumentBuilder docBuilder;
    private boolean warnIfNotConnected = true;
    private boolean warnIfNotRegistered = true;
    private boolean checkJobs = true;
    private int maxRetry = 50;
    private int maxRetryInterval = 14400;
    private int minWarningAge = 120;
    private String schedulerDateTime = "";
    private boolean hasRequests;

    private class SlaveScheduler {

        private String host = "";
        private String ip = "";
        private int port;
        private boolean connected;
        private String disconnectedAt = "";

        public SlaveScheduler(String host, int port) {
            super();
            if (isIP(host)) {
                this.ip = host;
            } else {
                this.host = host;
            }
            this.port = port;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public SlaveScheduler(String host, String ip, int port) {
            super();
            this.host = host;
            this.ip = ip;
            this.port = port;
            this.connected = true;
        }

        public SlaveScheduler(String host, String ip, int port, String disconnectedAt) {
            this(host, ip, port);
            this.connected = false;
            this.disconnectedAt = disconnectedAt;
        }

        private boolean isIP(String strServer) {
            char ch;
            int i;
            boolean bIsAnIP;
            bIsAnIP = true;
            for (i = 0; i < strServer.length(); i++) {
                ch = strServer.charAt(i);
                if (!(((ch >= '0') && (ch <= '9')) || (ch == '.'))) {
                    bIsAnIP = false;
                    break;
                }
            }
            return bIsAnIP;
        }

        public boolean equals(Object other) {
            if (other instanceof SlaveScheduler) {
                SlaveScheduler otherSlave = (SlaveScheduler) other;
                if (otherSlave.port != this.port) {
                    return false;
                }
                if (otherSlave.ip.equals(this.ip)) {
                    return true;
                }
                if (otherSlave.host.equals(this.host)) {
                    return true;
                }
                return false;
            }
            return super.equals(other);
        }
        
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            String displayHost = this.host;
            if (displayHost == null || displayHost.length() == 0) {
                displayHost = this.ip;
            }
            return displayHost + ":" + port;
        }

        public String getDisconnectedAt() {
            return disconnectedAt;
        }

        public void setDisconnectedAt(String disconnectedAt) {
            this.disconnectedAt = disconnectedAt;
        }
    }

    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        if (!rc) {
            return false;
        }
        try {
            requestedSlaves = getSlaveList();
            registeredSlaves = new Vector();
            connectedSlaves = new Vector();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            if (getJobProperties().get("warn_if_not_connected") != null) {
                String winc = getJobProperties().get("warn_if_not_connected").toString();
                if ("false".equalsIgnoreCase(winc) || "0".equals(winc)) {
                    warnIfNotConnected = false;
                }
                spooler_log.info(".. job setting [warn_if_not_connected]: " + winc);
            }
            if (getJobProperties().get("warn_if_not_registered") != null) {
                String winr = getJobProperties().get("warn_if_not_registered").toString();
                if ("false".equalsIgnoreCase(winr) || "0".equals(winr)) {
                    warnIfNotRegistered = false;
                }
                spooler_log.info(".. job setting [warn_if_not_registered]: " + winr);
            }
            if (getJobProperties().get("check_jobs") != null) {
                String check = getJobProperties().get("check_jobs").toString();
                if ("false".equalsIgnoreCase(check) || "0".equals(check)) {
                    checkJobs = false;
                }
                spooler_log.info(".. job setting [checkJobs]: " + check);
            }
            if (getJobProperties().get("delay_after_error") != null) {
                String[] delays = getJobProperties().get("delay_after_error").toString().split(";");
                if (delays.length > 0) {
                    spooler_job.clear_delay_after_error();
                }
                for (int i = 0; i < delays.length; i++) {
                    String[] delay = delays[i].split(":");
                    spooler_job.set_delay_after_error(Integer.parseInt(delay[0]), delay[1]);
                }
            } else {
                spooler_job.set_delay_after_error(1, maxRetryInterval);
                spooler_job.set_delay_after_error(maxRetry, "STOP");
            }
            if (getJobProperties().get("min_warning_age") != null) {
                String min_warning_age = getJobProperties().get("min_warning_age").toString();
                if (min_warning_age.indexOf(":") > -1) {
                    minWarningAge = calculateSeconds(min_warning_age);
                } else {
                    minWarningAge = Integer.parseInt(min_warning_age);
                }
            }
            return true;
        } catch (Exception e) {
            try {
                getLogger().error("Error occured during initialisation: " + e);
            } catch (Exception ex) {
            }
            return false;
        }
    }

    private Vector getSlaveList() throws Exception {
        try {
            Vector slaveList = new Vector();
            int slaveCounter = 0;
            SOSString sosString = new SOSString();
            Properties settings = getJobProperties();
            Enumeration keys = settings.keys();
            while (keys.hasMoreElements()) {
                String key = sosString.parseToString(keys.nextElement());
                if (key.startsWith("slave_")) {
                    String number = key.substring(6);
                    String sSlave = sosString.parseToString(settings, key);
                    String[] slaveArray = sSlave.split(":");
                    int port;
                    try {
                        if (slaveArray.length != 2) {
                            throw new Exception();
                        }
                        port = Integer.parseInt(slaveArray[1]);
                    } catch (Exception e) {
                        throw new Exception("\"" + sSlave + "\" is not a valid value for slave. Values must have" + " the form host:port or ip:port");
                    }
                    slaveList.add(new SlaveScheduler(slaveArray[0], port));
                    slaveCounter++;
                }
            }
            hasRequests = !slaveList.isEmpty();
            return slaveList;
        } catch (Exception e) {
            throw new Exception("Error occured retrieving settings for slaves: " + e, e);
        }
    }

    public boolean spooler_process() throws Exception {
        String hasRun = spooler.var("CheckSlavesHasRun");
        if (hasRun.length() == 0) {
            getLogger().info("This is the first run of JobSchedulerCheckSlaves. Slave Schedulers may "
                    + "not have registered yet. Delaying spooler_process() for 120s");
            getLogger().debug6("CheckSlavesHasRun: " + hasRun);
            spooler.set_var("CheckSlavesHasRun", "true");
            spooler_task.set_delay_spooler_process(120);
            return true;
        }
        checkRegistered();
        reportSlaves();
        if (checkJobs) {
            checkSchedulerJobs();
        }
        return false;
    }

    private void checkRegistered() throws Exception {
        try {
            getLogger().info("Sending show_state command...");
            String answer = spooler.execute_xml("<show_state what=\"remote_schedulers\"/>");
            getLogger().debug9("answer from Scheduler: " + answer);
            Document spoolerDocument = docBuilder.parse(new ByteArrayInputStream(answer.getBytes()));
            Element spoolerElement = spoolerDocument.getDocumentElement();
            Node answerNode = spoolerElement.getFirstChild();
            while (answerNode != null && answerNode.getNodeType() != Node.ELEMENT_NODE) {
                answerNode = answerNode.getNextSibling();
            }
            if (answerNode == null) {
                throw new Exception("answer contains no xml elements");
            }
            Element answerElement = (Element) answerNode;
            if (!"answer".equals(answerElement.getNodeName())) {
                throw new Exception("element <answer> is missing");
            }
            schedulerDateTime = answerElement.getAttribute("time");
            NodeList schedulerNodes = answerElement.getElementsByTagName("remote_scheduler");
            getLogger().debug3(schedulerNodes.getLength() + " remote_scheduler elements found.");
            for (int i = 0; i < schedulerNodes.getLength(); i++) {
                Node remoteSchedulerNode = schedulerNodes.item(i);
                if (remoteSchedulerNode != null && remoteSchedulerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element remoteScheduler = (Element) remoteSchedulerNode;
                    SlaveScheduler slave;
                    String host = remoteScheduler.getAttribute("hostname");
                    String ip = remoteScheduler.getAttribute("ip");
                    String tcp_port = remoteScheduler.getAttribute("tcp_port");
                    String connected = remoteScheduler.getAttribute("connected");
                    if ("no".equalsIgnoreCase(connected)) {
                        String disconnectedAt = remoteScheduler.getAttribute("disconnected_at");
                        slave = new SlaveScheduler(host, ip, Integer.parseInt(tcp_port), disconnectedAt);
                    } else {
                        slave = new SlaveScheduler(host, ip, Integer.parseInt(tcp_port));
                    }
                    if (requestedSlaves.contains(slave)) {
                        requestedSlaves.remove(slave);
                        if (slave.isConnected()) {
                            connectedSlaves.add(slave);
                        } else {
                            registeredSlaves.add(slave);
                        }
                    }
                    // Wenn keine Slaves explizit angegeben sind, dann wenigstens gucken, ob alle registrierten auch connected sind.
                    if (!hasRequests) {
                        if (slave.isConnected()) {
                            connectedSlaves.add(slave);
                        } else {
                            registeredSlaves.add(slave);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error occured checking remote schedulers: " + e, e);
        }
    }

    private void reportSlaves() throws Exception {
        if (!connectedSlaves.isEmpty()) {
            getLogger().info("The following slave Job Schedulers are registered and connected:");
            logSlaves(connectedSlaves);
        }
        if (!registeredSlaves.isEmpty()) {
            boolean minAgeReached = needsWarning(registeredSlaves);
            String warning = "The following slave Job Schedulers are registered but not connected:";
            if (warnIfNotConnected && minAgeReached) {
                getLogger().warn("Some slave Job Schedulers are registered but not connected, see log for details.");
            }
            getLogger().info(warning);
            logSlaves(registeredSlaves);
        }
        if (!requestedSlaves.isEmpty()) {
            String warning = "The following slave Job Schedulers are not registered and not connected:";
            if (warnIfNotRegistered) {
                getLogger().warn("Some slave Job Schedulers are not registered and not connected, see log for details.");
            }
            getLogger().info(warning);
            logSlaves(requestedSlaves);
        }
    }

    private void logSlaves(Collection slaves) throws Exception {
        Iterator iter = slaves.iterator();
        while (iter.hasNext()) {
            getLogger().info("  " + iter.next().toString());
        }
    }

    private void checkSchedulerJobs() throws Exception {
        Iterator iter = connectedSlaves.iterator();
        while (iter.hasNext()) {
            String hostPort = iter.next().toString();
            getLogger().info("Checking jobs for Scheduler " + hostPort + " ...");
            if (hostPort != null) {
                String[] aHostPort = hostPort.split(":");
                String host = aHostPort[0];
                String port = "";
                int iPort = 0;
                SOSSchedulerCommand command;
                if (aHostPort.length > 1) {
                    port = aHostPort[1];
                    try {
                        iPort = Integer.parseInt(port);
                    } catch (Exception e) {

                    }
                }
                if (iPort > 0) {
                    command = new SOSSchedulerCommand(host, iPort);
                } else {
                    command = new SOSSchedulerCommand(host);
                }
                try {
                    command.connect();
                    command.sendRequest("<show_state/>");
                    String response = command.getResponse().trim();
                    getLogger().debug6("Response from Job Scheduler: " + response);
                    Document spoolerDocument = docBuilder.parse(new ByteArrayInputStream(response.getBytes()));
                    Element spoolerElement = spoolerDocument.getDocumentElement();
                    Node answerNode = spoolerElement.getFirstChild();
                    while (answerNode != null && answerNode.getNodeType() != Node.ELEMENT_NODE) {
                        answerNode = answerNode.getNextSibling();
                    }
                    if (answerNode == null) {
                        throw new Exception("answer contains no xml elements");
                    }
                    Element answerElement = (Element) answerNode;
                    NodeList jobs = answerElement.getElementsByTagName("job");
                    if (jobs != null && jobs.getLength() > 0) {
                        for (int i = 0; i < jobs.getLength(); i++) {
                            Node jobNode = jobs.item(i);
                            if (jobNode != null && jobNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element jobElement = (Element) jobNode;
                                String state = jobElement.getAttribute("state");
                                String jobName = jobElement.getAttribute("job");
                                if ("stopped".equalsIgnoreCase(state)) {
                                    getLogger().warn("Job " + jobName + " on Job Scheduler \"" + hostPort + "\" is stopped.");
                                } else if ("enqueued".equalsIgnoreCase(state)) {
                                    getLogger().info("Job " + jobName + " on Job Scheduler \"" + hostPort + "\" is enqueued.");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    getLogger().warn("Error occured querying Job Scheduler at\"" + hostPort + "\": " + e);
                } finally {
                    try {
                        command.disconnect();
                    } catch (Exception ex) {
                    }
                }

            }
        }
    }

    private int calculateSeconds(String hoursMinSec) {
        int age = 0;
        String[] timeArray = hoursMinSec.split(":");
        int hours = Integer.parseInt(timeArray[0]);
        int minutes = Integer.parseInt(timeArray[1]);
        int seconds = 0;
        if (timeArray.length > 2) {
            seconds = Integer.parseInt(timeArray[2]);
        }
        age = hours * 3600 + minutes * 60 + seconds;
        return age;
    }

    private boolean needsWarning(Collection slaves) {
        Iterator iter = slaves.iterator();
        while (iter.hasNext()) {
            SlaveScheduler slave = (SlaveScheduler) iter.next();
            String eventTime = slave.getDisconnectedAt();
            if (needsWarning(eventTime, schedulerDateTime))
                return true;
        }
        return false;
    }

    private boolean needsWarning(String eventTime, String schedulerTime) {
        try {
            Date eventDate = SOSDate.getTime(eventTime.substring(0, 19));
            Date schedulerDate = SOSDate.getTime(schedulerTime.substring(0, 19));
            GregorianCalendar eventCal = new GregorianCalendar();
            GregorianCalendar schedulerCal = new GregorianCalendar();
            eventCal.setTime(eventDate);
            schedulerCal.setTime(schedulerDate);
            eventCal.add(java.util.Calendar.SECOND, minWarningAge);
            if (eventCal.before(schedulerCal)) {
                return true;
            }
        } catch (Exception e) {
            try {
                getLogger().warn("Failed to convert String to Date: " + e);
            } catch (Exception ex) {
            }
        }
        return false;
    }
}
