package com.sos.scheduler.plugins.globalmonitor;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JobConfigurationFileChanger {

    private Document jobToChange;
    private Map<String, JobSchedulerFileElement> listOfMonitors;

    public JobConfigurationFileChanger(Document jobToChange_) {
        super();
        this.jobToChange = jobToChange_;
    }

    public void setListOfMonitors(Map<String, JobSchedulerFileElement> listOfMonitors) {
        this.listOfMonitors = listOfMonitors;
    }

    private String logXml(Document doc) {
        String output = "";
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();

            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return output;
    }

    public Document addMonitorUse() {
        Element aktJob = jobToChange.getDocumentElement();

        Iterator<String> listOfMonitorNames = listOfMonitors.keySet().iterator();
        while (listOfMonitorNames.hasNext()) {
            String monitorName = listOfMonitorNames.next();
            Element monitorUse = jobToChange.createElement("monitor.use");
            monitorUse.setAttribute("monitor", monitorName);
            aktJob.appendChild(monitorUse);
        }
        reorderDOM(aktJob);
        return jobToChange;
    }

    protected void reorderDOM(Element element) {

        String[] ordering4Elements = { "settings", "description", "lock.use", "params", "environment", "script", "process", "monitor.use", "monitor",
                "start_when_directory_changed", "delay_after_error", "delay_order_after_setback", "run_time", "commands" };
        NodeList listOfElements = element.getChildNodes();

        for (String elementName : ordering4Elements) {
            for (int i = 0; i < listOfElements.getLength(); i++) {
                Node node = listOfElements.item(i);
                if (elementName.equals(node.getNodeName())) {
                    element.appendChild(node);
                }
            }
        }

    }

}