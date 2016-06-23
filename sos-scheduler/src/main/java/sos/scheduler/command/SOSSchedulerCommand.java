package sos.scheduler.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sos.scheduler.model.ISOSSchedulerSocket;

  
public class SOSSchedulerCommand {

    protected ISOSSchedulerSocket objO = null;
    private static final String SCHEDULER_DEFAULT_CHARSET = "ISO-8859-1";
    private String host = "localhost";
    private int port = 4444;
    private String protocol = "tcp";
    private Socket socket = null;
    private DatagramSocket udpSocket = null;
    private int timeout = 60;
    private BufferedReader in = null;
    private PrintWriter out = null;

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

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void connect(final String host, final int port) throws Exception {
        if (host == null || host.isEmpty()) {
            throw new Exception("hostname missing.");
        }
        if (port == 0) {
            throw new Exception("port missing.");
        }
        if ("udp".equalsIgnoreCase(protocol)) {
            udpSocket = new DatagramSocket();
            udpSocket.connect(InetAddress.getByName(this.host), this.port);
        } else {
            socket = new Socket(host, port);
            if (this.getTimeout() > 0) {
                socket.setSoTimeout(this.getTimeout() * 1000);
            }
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), SCHEDULER_DEFAULT_CHARSET));
            out = new PrintWriter(socket.getOutputStream(), true);
        }
    }

    public void connect() throws Exception {
        this.connect(host, port);
    }

    public void sendRequest(String command) throws Exception {
        if ("udp".equalsIgnoreCase(protocol)) {
            if (command.indexOf("<?xml") == -1) {
                command = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + command + "\r\n";
            }
            byte[] commandBytes = command.getBytes();
            udpSocket.send(new DatagramPacket(commandBytes, commandBytes.length, InetAddress.getByName(host), port));
        } else {
            if (command.indexOf("<?xml") == 0) {
                out.print(command + "\r\n");
            } else {
                out.print("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + command + "\r\n");
            }
            out.flush();
        }
    }

    public String getResponse() throws IOException, RuntimeException {
        int b;
        StringBuilder response = new StringBuilder();
        if (in != null) {
            while ((b = in.read()) != -1) {
                if (b == 0) {
                    break;
                }
                response.append((char) b);
            }
        }
        return response.toString();
    }

    public static void sendCommand(final String host, final int port, final String xmlCommand) throws Exception {
        SOSSchedulerCommand command = null;
        try {
            command = new SOSSchedulerCommand();
            command.setHost(host);
            command.setPort(port);
            command.setProtocol("udp");
            command.connect();
            command.sendRequest(xmlCommand);
        } catch (Exception e) {
            throw new Exception("startJob: could not start job: " + e.getMessage());
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

    public void disconnect() throws Exception {
        if (socket != null) {
            socket.close();
        }
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    public static void main(final String[] args) throws Exception {
        final String USAGE =
                "\nUsage: java -cp com.sos.scheduler-xxx.jar:log4j-xxx.jar sos.scheduler.command.SOSSchedulerCommand"
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
        SOSSchedulerCommand socket = null;
        try {
            socket = new SOSSchedulerCommand();
            socket.setTimeout(timeout);
            socket.connect(host, port);
            socket.sendRequest(command);
            String response = socket.getResponse();
            System.out.println(response);
            try {
                if (command.contains("<modify_spooler") && command.contains("abort_immediately")) {
                    // nothing to do
                } else if (response == null || response.isEmpty()) {
                    errorText = String.format("No response from JobScheduler [%1$s:%2$d]: Please check the security settings", host, port);
                } else {
                    errorText = SOSSchedulerCommand.getResponseErrorText(response);
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
            if (socket != null) {
                socket.disconnect();
            }
        }
        System.exit(rc);
    }

    public int getTimeout() {
        return timeout;
    }

}