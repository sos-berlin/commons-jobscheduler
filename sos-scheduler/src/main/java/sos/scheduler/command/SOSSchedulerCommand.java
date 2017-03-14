package sos.scheduler.command;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sos.scheduler.model.ISOSSchedulerSocket;
import com.sos.xml.SOSXmlCommand;

import sos.spooler.Spooler;

public class SOSSchedulerCommand {

    private static final String XML_COMMAND_API_PATH = "/jobscheduler/master/api/command";
    protected ISOSSchedulerSocket objO = null;
    private String host = "localhost";
    private int port = 40444;
    private String protocol = "http";
    private int timeout = 60;
    private SOSXmlCommand sosXmlCommand;
    private String answer;

    public SOSSchedulerCommand() {
    }

    public SOSSchedulerCommand(final ISOSSchedulerSocket pobjOptions) {
        objO = pobjOptions;
        this.setHost(objO.getServerName());
        this.setPort(objO.getPortNumber());
        this.setProtocol(protocol);
        this.setTimeout(objO.getTCPTimeoutValue());
    }

    public SOSSchedulerCommand(final String host) {
        this.setHost(host);
    }

    public SOSSchedulerCommand(final String host, final int port) {
        this.setHost(host);
        this.setPort(port);
    }

    public SOSSchedulerCommand(final String host, final int port, final String protocol) {
        this.setHost(host);
        this.setPort(port);
        this.setProtocol(protocol);
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        if ("tcp".equalsIgnoreCase(protocol) || "udp".equals(protocol)){
            protocol = "http";
        }
        this.protocol = protocol;
    }

    public void connect(final String host, final int port) throws Exception {
        this.host = host;
        this.port = port;

        if (host == null || host.isEmpty()) {
            throw new Exception("hostname missing.");
        }
        if (port == 0) {
            throw new Exception("port missing.");
        }

        URL url = new URL(protocol, host, port, XML_COMMAND_API_PATH);
        sosXmlCommand = new SOSXmlCommand(url.toExternalForm());
        sosXmlCommand.setConnectTimeout(timeout*1000);
        sosXmlCommand.setReadTimeout(timeout*1000);

    }

    public void connect() throws Exception {
        this.connect(host, port);
    }

    public void sendRequest(String command) throws Exception {

        if (command.indexOf("<?xml") == 0) {
            answer = sosXmlCommand.executeXMLPost(command + "\r\n");
        } else {
            answer = sosXmlCommand.executeXMLPost("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + command + "\r\n");
        }
    }

    public String getResponse(){
        return answer;
    }

    public String getResponseErrorText() throws Exception {
        String errorText = null;
        if (answer != null && !answer.isEmpty()) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = builder.parse(new InputSource(new StringReader(answer)));
            errorText = (String) xPath.evaluate("/spooler/answer/ERROR/@text", doc, XPathConstants.STRING);
        }
        return errorText;
    }

    public static int getTCPPortFromSchedulerXML(final File objSchedulerXml) throws Exception {
        int iPort = 0;
        if (objSchedulerXml.exists()) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = builder.parse(objSchedulerXml);
            String strPort = (String) xPath.evaluate("/spooler/config/@port", doc, XPathConstants.STRING);
            if (strPort == null || strPort.isEmpty()) {
                strPort = (String) xPath.evaluate("/spooler/config/@tcp_port", doc, XPathConstants.STRING);
            }
            if (strPort != null && !strPort.isEmpty()) {
                iPort = Integer.parseInt(strPort);
            }
        }
        return iPort;
    }
    
    
    public static int getHTTPPortFromSchedulerXML(Spooler spooler) {
        int iPort = 0;
        try {
            File schedulerXmlFile = Paths.get(spooler.directory(), "config", "scheduler.xml").toFile();
            if (schedulerXmlFile.exists()) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = builder.parse(schedulerXmlFile);
                String port = (String) xPath.evaluate("/spooler/config/@http_port", doc, XPathConstants.STRING);

                if (port != null && !port.isEmpty()) {
                    iPort = Integer.parseInt(port);
                }
            }
            return iPort;
        } catch (Exception e) {
            return 40444;
        }
    }
    
    public static void sendCommand(final String host, final int port, final String xmlCommand) throws Exception {
        SOSSchedulerCommand command = null;
        try {
            command = new SOSSchedulerCommand(host,port);
            command.connect();
            command.sendRequest(xmlCommand);
        } catch (Exception e) {
            throw new Exception(String.format("sendCommand: could not sendCommand %s to %s:%s --> %s",xmlCommand,host,port + e.getMessage()));
        }
    }

    public static void addOrder(final String host, final int port, final int status, final String jobChain) throws Exception {
        sendCommand(host, port, "<add_order job_chain=\"" + jobChain + "\" state=\"" + status + "\">" + "<params></params></add_order>");
    }

    public static void startJob(final String host, final int port, final String job) throws Exception {
        sendCommand(host, port, "<job job=\"" + job + "\">");
    }

    public static String getResponseErrorText(final String response) throws Exception {
        String errorText = null;
        if (response != null && !response.isEmpty()) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = builder.parse(new InputSource(new StringReader(response)));
            errorText = (String) xPath.evaluate("/spooler/answer/ERROR/@text", doc, XPathConstants.STRING);
        }
        return errorText;
    }
    public void disconnect() throws Exception {}

    public static void main(final String[] args) throws Exception {
        final String USAGE = "\nUsage: java -cp com.sos.scheduler-xxx.jar:log4j-xxx.jar sos.scheduler.command.SOSSchedulerCommand"
                + "  -host <host> -port <port> [-timeout <timeout>]  \"<xml-command>\"";
        String host = "localhost";
        String command = null;
        String schedulerXml = null;
        String errorText = null;
        int port = 0;
        int timeout = 5;
        int rc = 0;
        int argc = args.length;
        int indexEqualSign = -1;
        boolean help = false;
        for (int i = 0; i < argc; i++) {
            if ("-help".equals(args[i]) || "--help".equals(args[i]) || "-h".equals(args[i])) {
                help = true;
                break;
            }
            indexEqualSign = args[i].indexOf('=');
            if ("-host".equals(args[i]) && i + 1 < argc) {
                host = args[i + 1];
            }
            if (args[i].startsWith("-host=")) {
                host = args[i].substring(indexEqualSign);
            }
            if ("-ip-address".equals(args[i]) && i + 1 < argc) {
                host = args[i + 1];
            }
            if (args[i].startsWith("-ip-address=")) {
                host = args[i].substring(indexEqualSign + 1);
            }
            if ("-port".equals(args[i]) && i + 1 < argc) {
                port = Integer.parseInt(args[i + 1]);
            }
            if (args[i].startsWith("-port=")) {
                port = Integer.parseInt(args[i].substring(indexEqualSign + 1));
            }
            if ("-tcp-port".equals(args[i]) && i + 1 < argc) {
                port = Integer.parseInt(args[i + 1]);
            }
            if (args[i].startsWith("-tcp-port=")) {
                port = Integer.parseInt(args[i].substring(indexEqualSign + 1));
            }
            if ("-timeout".equals(args[i]) && i + 1 < argc) {
                timeout = Integer.parseInt(args[i + 1]);
            }
            if (args[i].startsWith("-timeout=")) {
                timeout = Integer.parseInt(args[i].substring(indexEqualSign + 1));
            }
            if ("-config".equals(args[i]) && i + 1 < argc) {
                schedulerXml = args[i + 1];
            }
            if (args[i].startsWith("-config=")) {
                schedulerXml = args[i].substring(indexEqualSign + 1);
            }
            if (args[i].startsWith("<")) {
                command = args[i];
            }
        }
        if (help || argc == 0) {
            System.out.println(USAGE);
            System.exit(0);
        }
        if (port == 0 && schedulerXml != null) {
            port = SOSSchedulerCommand.getTCPPortFromSchedulerXML(new File(schedulerXml));
        }
        if (host == null || port == 0 || command == null) {
            System.err.println("invalid parameter");
            System.err.println(USAGE);
            System.exit(2);
        }
        SOSSchedulerCommand sosSchedulerCommand = null;
        try {
            sosSchedulerCommand = new SOSSchedulerCommand();
            sosSchedulerCommand.setTimeout(timeout);
            sosSchedulerCommand.connect(host, port);
            sosSchedulerCommand.sendRequest(command);
            String response = sosSchedulerCommand.getResponse();
            System.out.println(response);
            try {
                if (command.contains("<modify_spooler") && command.contains("abort_immediately")) {
                    // nothing to do
                } else if (response == null || response.isEmpty()) {
                    errorText = String.format("No response from JobScheduler [%1$s:%2$d]: Please check the security settings", host, port);
                } else {
                    errorText = sosSchedulerCommand.getResponseErrorText();
                }
                if (errorText != null && !errorText.isEmpty()) {
                    System.err.println(errorText);
                    rc = 1;
                }
            } catch (Exception e) {
                //
            }
        } catch (Exception e) {
            System.err.println(String.format("%1$s [%2$s:%3$d]", e.getMessage(), host, port));
            rc = 3;
        } finally {
            if (sosSchedulerCommand != null) {
                sosSchedulerCommand.disconnect();
            }
        }
        System.exit(rc);
    }

    public int getTimeout() {
        return timeout;
    }

}