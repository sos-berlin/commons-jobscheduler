package sos.scheduler.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.w3c.dom.DOMException;
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
            String eventString = sosxml.selectSingleNodeValue("//param[@name='" + JobSchedulerConstants.EVENTS_VARIABLE_NAME + "']/@value");
            if (eventString == null) {
                throw new Exception("no event parameters found in Job Scheduler answer");
            }
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
    
    public static String buildXPath(String attr, String value, String xPath) {
        String[] values = value.trim().split("\\s+");
        String xPathOr = "";
        for (int i = 0; i < values.length; i++) {
           if (i == 0) {
               xPathOr += "@"+attr+"='"+values[i]+"'";
           } else {
               xPathOr += "or @"+attr+"='"+values[i]+"'";
           }
        }
        if (values.length > 1) {
            xPathOr = "(" + xPathOr + ")";
        }
        if (values.length > 0) {
            if (!xPath.isEmpty()) {
                xPathOr = " and " + xPathOr;
            }
            xPath += xPathOr;
        }
        return xPath;
    }

    public static void main(final String[] args) {
        BufferedReader in = null;
        try {
            if (args.length < 2) {
                throw new Exception("Usage: JobSchedulerEventXPath xmlString  xPathString");
            }
            String eventXml = args[0];
            String eventXPath = args[1];
            String eventClass = null;
            String eventId = null;
            String eventExitCode = null;
            String eventXPath2 = "";
            if (args.length > 2 && eventXPath.isEmpty()) {
                eventClass = args[2];
                if (eventClass != null && !eventClass.isEmpty()) {
                    eventXPath2 = JobSchedulerEventXPath.buildXPath("event_class", eventClass, eventXPath2);
                }
            }
            if (args.length > 3 && eventXPath.isEmpty()) {
                eventId = args[3];
                if (eventId != null && !eventId.isEmpty()) {
                    eventXPath2 = JobSchedulerEventXPath.buildXPath("event_id", eventId, eventXPath2);
                }
            }
            if (args.length > 4 && eventXPath.isEmpty()) {
                eventExitCode = args[4];
                if (eventExitCode != null && !eventExitCode.isEmpty()) {
                    eventXPath2 = JobSchedulerEventXPath.buildXPath("exit_code", eventExitCode, eventXPath2);
                }
            }
            if (!eventXPath2.isEmpty()) {
                eventXPath2 = "//events/event[" + eventXPath2 + "]";
            } else {
                eventXPath2 = "//events/event";
            }
            if (eventXPath.isEmpty()) {
                eventXPath = eventXPath2; 
            }
            
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
