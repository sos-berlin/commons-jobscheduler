package sos.scheduler.live;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import sos.connection.SOSConnection;

import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class JobSchedulerSynchronizeConfiguration extends JobSchedulerJob {

    private SOSConnection conn = null;
    private boolean orderJob = true;
    private Order order = null;
    private String file_path;
    private String operation = "";
    private String type = "";
    private String live_object_id = "";
    private String history_id = "";
    private LinkedHashSet listOfElements = null;

    public boolean spooler_process() throws Exception {
        this.conn = this.getConnection();
        orderJob = !(spooler_task.job().order_queue() == null);
        Variable_set params = null;
        if (orderJob) {
            spooler_log.debug3("working as order job");
            order = spooler_task.order();
            params = order.params();
        } else {
            spooler_log.debug3("working as stand alone job");
            params = spooler_task.params();
        }
        file_path = params.value("path");
        if ("".equals(file_path)) {
            file_path = spooler.configuration_directory();
        }
        operation = params.value("operation");
        type = params.value("type");
        live_object_id = params.value("id");
        history_id = params.value("history_id");
        if ("".equals(operation)) {
            livePoll();
        }
        return false;
    }

    public void spooler_close() {
        spooler_log.debug3("spooler_close");
    }

    private void livePoll() throws Exception {
        Iterator resultset = null;
        HashMap rec;
        spooler_log.debug3("Exporting configurations");
        String selStr = "SELECT oh.\"OBJECT_ID\" , oh.\"PK_ID\" AS \"HISTORY_ID\"," + " oh.\"OPERATION\", o.\"NAME\" , o.\"TYPE\""
                + " FROM LIVE_OBJECTS o, LIVE_OBJECT_HISTORY oh" + " WHERE o.\"PK_ID\"=oh.\"OBJECT_ID\" AND oh.\"IN_SYNC\" = 0";
        spooler_log.debug3(selStr);
        ArrayList arrayList = new ArrayList();
        arrayList = conn.getArray(selStr);
        resultset = arrayList.iterator();
        spooler_log.debug3("spooler_process");
        while (resultset.hasNext()) {
            rec = (HashMap) resultset.next();
            spooler_log.debug1("-->" + "object_id:" + getLiveValue(rec, "object_id") + "   " + "HISTORY_ID:" + getLiveValue(rec, "history_id") + "   "
                    + "operation:" + getLiveValue(rec, "operation") + "   " + "type:" + getLiveValue(rec, "type"));
            liveExport(rec);
        }
    }

    private void liveExport(HashMap rec) throws Exception {
        String object_name = getLiveValue(rec, "name");
        String object_type = getLiveValue(rec, "type");
        listOfElements = new LinkedHashSet();
        String selStr = "SELECT r.\"PK_ID\", r.\"PARENT_ID\", m.\"ELEMENT_PATH\", m.\"TABLE_NAME\", m.\"ELEMENT_NAME\", m.\"NESTING\" "
                + "FROM LIVE_OBJECT_METADATA m LEFT JOIN LIVE_OBJECT_REFERENCES r " + "ON m.\"ELEMENT_PATH\"=r.\"OBJECT_PATH\"  " + "WHERE r.\"OBJECT_ID\"="
                + getLiveValue(rec, "object_id") + " " + "ORDER BY m.\"ORDERING\"";
        ArrayList arrayList = new ArrayList();
        arrayList = conn.getArray(selStr);
        Iterator xml_elements = arrayList.iterator();
        spooler_log.debug3(selStr);
        while (xml_elements.hasNext()) {
            rec = (HashMap) xml_elements.next();
            JobSchedulerMetadataElement element = new JobSchedulerMetadataElement(rec);
            element.attributes = new HashMap();
            listOfElements.add(element);
            if (element.attributes == null) {
                spooler_log.debug3("Hashmap is null");
            }
        }
        String xml = getLiveXml();
        spooler_log.debug3(xml);
        File f = new File(file_path, object_name + "." + object_type + ".xml");
        spooler_log.debug3("Writing to " + f.getAbsolutePath());
        Writer fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(xml);
        } catch (IOException e) {
            System.err.println("File could not be created");
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private String getLiveXml() throws Exception {
        String erg = "";
        JobSchedulerMetadataElement element = null;
        JobSchedulerMetadataElement rootElement = null;
        Iterator it = listOfElements.iterator();
        while (it.hasNext()) {
            element = (JobSchedulerMetadataElement) it.next();
            if (rootElement == null) {
                rootElement = element;
            }
            element.attribute = getLiveAttributes(element);
        }
        String encoding = "ISO-8859-1";
        SAXBuilder builder = new org.jdom.input.SAXBuilder();
        String xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?> ";
        xml += "<" + rootElement.element_name + " " + rootElement.attribute + "></" + rootElement.element_name + ">";
        spooler_log.debug3(xml);
        Document parentDoc = builder.build(new java.io.StringReader(xml));
        Element root = parentDoc.getRootElement();
        it = listOfElements.iterator();
        element = (JobSchedulerMetadataElement) it.next();
        String s = "";
        Element aktEle = root;
        while (it.hasNext()) {
            element = (JobSchedulerMetadataElement) it.next();
            aktEle = liveAddMissingNodes(root, element);
            s = element.element_name;
            Element ele = new Element(s);
            setLiveXmlAttribute(element, ele);
            aktEle.addContent(ele);
        }
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        erg = output.outputString(root);
        return erg;
    }

    private Element liveAddMissingNodes(Element root, JobSchedulerMetadataElement element) {
        Element aktEle = root;
        String s = "";
        for (int i = 1; i < element.nesting; i++) {
            s = element.elements.get(i).toString();
            spooler_log.debug3("checking " + s);
            spooler_log.debug3(aktEle.toString());
            if (aktEle.getChild(s) == null) {
                spooler_log.debug3("adding:" + s);
                Element ele = new Element(s);
                aktEle = aktEle.addContent(ele);
                spooler_log.debug3(aktEle.getChildren().toString());
                if (aktEle.getChild(s) == null) {
                    spooler_log.debug3("Error. Must contain" + s);
                }
                aktEle = ele;
            } else {
                aktEle = aktEle.getChild(s);
            }
        }
        return aktEle;
    }

    private void setLiveXmlAttribute(JobSchedulerMetadataElement element, Element ele) {
        Iterator attrs = element.attributes.keySet().iterator();
        while (attrs.hasNext()) {
            String k = attrs.next().toString();
            spooler_log.debug3(k + "=" + element.attributes.get(k).toString());
            ele.setAttribute(k, element.attributes.get(k).toString());
        }
    }

    private String getLiveAttributes(JobSchedulerMetadataElement element) throws Exception {
        HashMap rec = null;
        String attr = "";
        if ("".equals(element.table_name)) {
            return null;
        } else {
            String selStr = "SELECT * " + " FROM " + element.table_name + " WHERE \"PK_ID\" = " + element.pkid;
            ArrayList arrayList = new ArrayList();
            arrayList = conn.getArray(selStr);
            Iterator resultset = arrayList.iterator();
            if (resultset.hasNext()) {
                rec = (HashMap) resultset.next();
                Iterator fieldnames = rec.keySet().iterator();
                while (fieldnames.hasNext()) {
                    String f = fieldnames.next().toString();
                    if (!"object_path".equals(f) && !"cdata".equals(f) && !"parent_id".equals(f) && !"pk_id".equals(f) && !"object_id".equals(f)
                            && rec.get(f) != null && !"".equals(rec.get(f))) {
                        attr += f;
                        attr += "=\"" + rec.get(f) + "\"" + " ";
                        element.attributes.put(f, rec.get(f).toString());
                    }
                }
            }
        }
        return attr.trim();
    }

    private String getLiveValue(HashMap h, String k) {
        String erg = "";
        try {
            if (h.containsKey(k) && h.get(k) == null) {
                erg = "";
            } else {
                erg = h.get(k).toString();
            }
            return erg;
        } catch (Exception e) {
            return "";
        }
    }

}