package com.sos.scheduler.plugins.globalmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Document;

public class JobConfigurationFileChanger {

    private Document jobToChange;
    private HashMap<String, JobSchedulerFileElement> listOfMonitors;

    public JobConfigurationFileChanger(Document jobToChange_) {
        super();
        this.jobToChange = jobToChange_;
    }

    public void setListOfMonitors(HashMap<String, JobSchedulerFileElement> listOfMonitors) {
        this.listOfMonitors = listOfMonitors;
    }

    private static Document convertJdomElement2W3Document(org.jdom.Element jdomElement) throws JDOMException {
        DOMOutputter domOut = new DOMOutputter();
        jdomElement.detach();
        org.jdom.Document jdomDoc = new org.jdom.Document(jdomElement);
        return domOut.output(jdomDoc);
    }

    public Document addMonitorUse() throws JDOMException {
        DOMBuilder domBuilder = new DOMBuilder();
        org.jdom.Element aktJob = domBuilder.build(jobToChange).getRootElement();
        List<org.jdom.Element> monitorUseList = aktJob.getChildren("monitor.use");
        Iterator<String> listOfMonitorNames = listOfMonitors.keySet().iterator();
        while (listOfMonitorNames.hasNext()) {
            String monitorName = listOfMonitorNames.next();
            org.jdom.Element monitorUse = new org.jdom.Element("monitor.use");
            monitorUse.setAttribute("monitor", monitorName);
            monitorUseList.add(monitorUse);
        }
        reorderDOM(aktJob);
        return convertJdomElement2W3Document(aktJob);
    }

    protected void reorderDOM(Element element) {
        String[] ordering4Elements = { "settings", "description", "lock.use", "params", "environment", "script", "process", "monitor.use", "monitor",
                "start_when_directory_changed", "delay_after_error", "delay_order_after_setback", "run_time", "commands" };
        for (int i = 0; i < ordering4Elements.length; i++) {
            List<Element> listOfElements = new ArrayList<Element>(element.getChildren(ordering4Elements[i]));
            if (!listOfElements.isEmpty()) {
                element.removeChildren(ordering4Elements[i]);
                for (Iterator<Element> iterator = listOfElements.iterator(); iterator.hasNext();) {
                    Element child = (Element) iterator.next();
                    element.addContent(child);
                }
            }
        }
    }

}