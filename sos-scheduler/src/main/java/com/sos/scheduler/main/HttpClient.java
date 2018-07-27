package com.sos.scheduler.main;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.sos.exception.SOSNoResponseException;
import com.sos.exception.SOSException;
import com.sos.xml.SOSXmlCommand;

import sos.xml.SOSXMLXPath;

public class HttpClient {
    
    private static final String SCHEDULER_XML_OPTION = "-config=";
    private static final String HTTP_PORT_OPTION = "-http-port=";
    private static final String WITH_INDENT_OPTION = "-with-indent";
    private static final String XML_COMMAND_OPTION = "-xml-command=";
    private static final String XML_COMMAND_API_PATH = "http://%1$s/jobscheduler/master/api/command";

    public static void main(String[] args) {
        String port = null;
        String hostPort = null;
        String xmlCommand = null;
        String schedulerXml = null;
        String portFromCommandLine = null;
        boolean answerWithIndent = false;
        for (int i=0; i < args.length; i++) {
            String arg = args[i].replaceFirst("^\"", "").replaceFirst("\"$", "");
            if (arg.startsWith(SCHEDULER_XML_OPTION)) {
                schedulerXml = getValueOfCliOption(arg, SCHEDULER_XML_OPTION);
            } else if (arg.startsWith(HTTP_PORT_OPTION)) {
                portFromCommandLine = getValueOfCliOption(arg, HTTP_PORT_OPTION);
            } else if (arg.equals(WITH_INDENT_OPTION)) {
                answerWithIndent = true;
            } else if (arg.startsWith(XML_COMMAND_OPTION)) {
                xmlCommand = getValueOfCliOption(arg, XML_COMMAND_OPTION);
            }
        }
        try {
            if (xmlCommand == null) {
                throw new IllegalArgumentException("xml command is required"); 
            }
            if (portFromCommandLine != null && !portFromCommandLine.isEmpty()) {
                port = portFromCommandLine; 
            } else if (schedulerXml != null && !schedulerXml.isEmpty()) {
                port = getPortFromSchedulerXml(schedulerXml);
            }
            if (port == null) {
                throw new IllegalArgumentException("http port is required"); 
            }
            hostPort = "127.0.0.1:" + port;
            if (port.indexOf(":") > -1) {
                hostPort = port.replaceFirst("^0\\.0\\.0\\.0", "127.0.0.1");
            }
            SOSXmlCommand sosXmlCommand = new SOSXmlCommand(String.format(XML_COMMAND_API_PATH, hostPort));
            sosXmlCommand.setConnectTimeout(5000);
            sosXmlCommand.setReadTimeout(5000);
            String response = sosXmlCommand.executeXMLPost(xmlCommand);
            String responseError = sosXmlCommand.getSosxml().selectSingleNodeValue("/spooler/answer/ERROR/@text");
            if (responseError != null && !responseError.isEmpty()) {
                throw new SOSException(responseError);
            }
            System.out.println(getIndentAnswer(answerWithIndent, sosXmlCommand, response));
            System.exit(0);
        } catch (SOSNoResponseException e) {
            if (!schedulerXml.contains("abort_immediately")) {
                System.err.println(e.toString());
                System.exit(8);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            System.exit(8);
        }
    }

    private static String getValueOfCliOption(String arg, String key) {
        if (key != null) {
            arg = arg.substring(key.length());
        }
        return arg.replaceFirst("^\"", "").replaceFirst("\"$", "");
    }

    private static String getPortFromSchedulerXml(String schedulerXml) throws Exception {
        SOSXMLXPath sosXmlPath = new SOSXMLXPath(schedulerXml);
        return sosXmlPath.selectSingleNodeValue("/spooler/config/@http_port", null);
    }
    
    private static String getIndentAnswer(boolean answerWithIndent, SOSXmlCommand sosXmlCommand, String response) {
        if (!answerWithIndent) {
            return response;
        }
        StringWriter writer = new StringWriter();
        try {
            Document doc = sosXmlCommand.getSosxml().getDocument();
            String encoding = doc.getXmlEncoding();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(writer);
            Transformer tformer = TransformerFactory.newInstance().newTransformer();
            tformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //don't work tformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            tformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "script");
            if (encoding != null && !encoding.isEmpty()) {
                tformer.setOutputProperty(OutputKeys.ENCODING, encoding);  
            }
            tformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            return response;
        } finally {
            try {
                writer.close();
            } catch (Exception e) {}
        }
    }

}
