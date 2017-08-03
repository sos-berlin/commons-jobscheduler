package sos.scheduler.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.scheduler.job.JobSchedulerConstants;
import sos.xml.SOSXMLXPath;

public class JobSchedulerEventXPath {

    public JobSchedulerEventXPath() throws Exception {
    }

    public static String getEventXMLAsString(final String eventXml) throws DOMException, Exception {
        try {
            SOSXMLXPath sosxml = new SOSXMLXPath(new StringBuffer(eventXml.replaceFirst("^[^<]*", "").replaceFirst("[^>]*$", "")));
            NodeList params = sosxml.selectNodeList("/spooler/answer//param[@name='" + JobSchedulerConstants.eventVariableName + "']");
            if (params.item(0) == null) {
                throw new Exception("no event parameters found in Job Scheduler answer");
            }
            NamedNodeMap attrParam = params.item(0).getAttributes();
            String eventString = getText(attrParam.getNamedItem("value"));
            eventString = eventString.replaceAll(String.valueOf((char) 254), "<").replaceAll(String.valueOf((char) 255), ">");
            eventString = eventString.replaceAll("(\\uC3BE|þ|Ã¾)", "<").replaceAll("(\\uC3BF|ÿ|Ã¿)", ">");
            return eventString;
        } catch (Exception e) {
            throw new Exception("error occurred reading Job Scheduler answer: " + e.getMessage());
        }
    }

    public static String getText(final Node node) {
        if (node != null) {
            return node.getNodeValue();
        } else {
            return "";
        }
    }

    public static void main(final String[] args) {
        BufferedReader in = null;
        try {
            if (args.length < 2) {
                throw new Exception("Usage: JobSchedulerEventXPath xmlString  xPathString");
            }
            String eventXml = args[0];
            String eventXPath = args[1];
            if (!eventXml.startsWith("<?xml ")) {
                File xmlFile = new File(eventXml);
                if (!xmlFile.canRead()) {
                    throw new Exception("input file not found: " + xmlFile.getAbsolutePath());
                }
                in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile),"ISO-8859-1"));
                String eventContent = "";
                String line = "";
                while ((line = in.readLine()) != null) {
                    eventContent += line;
                }
                eventXml = eventContent;
            }
            eventXml = JobSchedulerEventXPath.getEventXMLAsString(eventXml);
            SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(eventXml));
            NodeList nl = xPath.selectNodeList(eventXPath);
            if (nl != null) {
                System.out.println(nl.getLength());
            } else {
                System.out.println(0);
            }
        } catch (Exception e) {
            System.out.println(0);
            System.err.println("JobSchedulerEventXPath: " + e.getMessage());
            System.exit(1);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
