package sos.scheduler.live;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.connection.SOSConnection;

public class JobSchedulerLiveXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerLiveXml.class);
    private File xmlFile;
    private Document doc;
    private HashMap tableNames;
    private LinkedHashSet listOfElements;
    private SOSConnection conn = null;
  
    public JobSchedulerLiveXml(SOSConnection conn_, File xmlFile_) throws Exception {
        this.xmlFile = xmlFile_;
        this.conn = conn_;
        getXml();
        listOfElements = new LinkedHashSet();
        tableNames = new HashMap();
        fillTableNames();
    }

    private String getParentId(HashMap parents, JobSchedulerMetadataElement element) {
        String erg = "";
        int nesting = element.nesting;
        while ("".equals(erg) && nesting > 0) {
            String p = "";
            for (int i = 0; i < nesting; i++) {
                p += element.elements.get(i) + "/";
            }
            if (p.length() > 0) {
                p = p.substring(0, p.length() - 1);
            }
            if (!"".equals(getValue(parents, p))) {
                erg = getValue(parents, p);
            }
            nesting--;
        }
        return erg;
    }

    public void store(String config_path) throws Exception {
        HashMap parents = new HashMap();
        String parent_id = "";
        Element root = doc.getRootElement();
        log("Reading configuration for " + root.getName());
        String type = root.getName();
        String fileName = xmlFile.getName();
        String jobChainName = "";
        if ("order".equalsIgnoreCase(type)) {
            String[] t3 = fileName.split("\\,");
            jobChainName = t3[0];
            fileName = t3[1];
            LOGGER.debug("jobChainName: " + jobChainName + " fileName: " + fileName);
        }
        String[] t = fileName.split("\\.");
        String objectName = "";
        for (int i = 0; i < t.length - 2; i++) {
            if (objectName.length() > 0) {
                objectName += "." + t[i];
            } else {
                objectName = t[i];
            }
        }
        String[] t2 = config_path.split("\\/");
        String objectPath = "";
        for (int i = 0; i < t2.length - 1; i++) {
            if (objectPath.length() > 0) {
                objectPath += "/" + t2[i];
            } else {
                objectPath = t2[i];
            }
        }
        String sqlStmt = "DELETE FROM LIVE_OBJECTS WHERE \"TYPE\"='" + type + "' AND \"PATH\"='" + objectPath + "' AND \"NAME\"='" + objectName + "'";
        conn.executeUpdate(sqlStmt);
        sqlStmt =
                "INSERT INTO LIVE_OBJECTS (" + "\"TYPE\", \"PATH\", \"NAME\", \"CREATED\", \"MODIFIED\" )" + "VALUES (" + "'" + type + "','"
                        + objectPath + "','" + objectName + "', GETDATE(), GETDATE())";
        log(sqlStmt);
        conn.execute(sqlStmt);
        String object_id = conn.getSingleValue("SELECT @@IDENTITY");
        log("Object_id:" + object_id);
        readNode(0, "", root);
        Iterator i = listOfElements.iterator();
        while (i.hasNext()) {
            JobSchedulerMetadataElement element = (JobSchedulerMetadataElement) i.next();
            if (!"".equals(element.table_name)) {
                String s = "";
                String sv = "";
                if ("".equals(parent_id)) {
                    s = "\"OBJECT_ID\" ";
                    sv = object_id;
                } else {
                    s = "\"OBJECT_ID\", \"PARENT_ID\", \"OBJECT_PATH\"";
                    sv = object_id + "," + getParentId(parents, element) + ",'" + element.element_path + "'";
                }
                sqlStmt =
                        "Insert into " + element.table_name + " (" + s
                                + ((jobChainName.length() > 0) ? element.fieldnames() + ", \"JOB_CHAIN\" " : element.fieldnames()) + ") values ("
                                + sv + ((jobChainName.length() > 0) ? element.fieldvalues() + ", '" + jobChainName + "'" : element.fieldvalues())
                                + ")";
                log(sqlStmt);
                conn.execute(sqlStmt);
                jobChainName = "";
                parent_id = conn.getSingleValue("SELECT @@IDENTITY");
                parents.put(element.element_path, parent_id);
                log("Parent_id(" + element.element_path + ")=" + parent_id);
            }
        }
        LOGGER.info("File has been imported: " + xmlFile.getAbsolutePath());
    }

    private void log(String s) throws Exception {
        LOGGER.debug(s);
    }

    private void fillTableNames() throws Exception {
        String selStr = "SELECT \"TABLE_NAME\" , \"ELEMENT_PATH\"" + " FROM LIVE_OBJECT_METADATA";
        List<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
        arrayList = conn.getArray(selStr);
        Iterator<Map<String, String>> resultset = arrayList.iterator();
        while (resultset.hasNext()) {
            Map<String, String> rec = resultset.next();
            log(getValue(rec, "element_path") + " --> " + getValue(rec, "table_name"));
            tableNames.put(getValue(rec, "element_path"), getValue(rec, "table_name"));
        }
    }

    private String getAttributes(JobSchedulerMetadataElement element, Element node) {
        String erg = "";
        List l = node.getAttributes();
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Attribute attr = (Attribute) i.next();
            erg += attr.getName() + "=" + attr.getValue() + " ";
            element.attributes.put(attr.getName(), attr.getValue());
        }
        return erg;
    }

    private void readNode(int nesting, String path, Element node) throws Exception {
        path += node.getName() + "/";
        HashMap rec = new HashMap();
        rec.put("pkid", "");
        rec.put("parent_id", "");
        rec.put("nesting", String.valueOf(nesting));
        rec.put("element_name", node.getName());
        rec.put("element_path", path);
        JobSchedulerMetadataElement element = new JobSchedulerMetadataElement(rec);
        if (element.element_path.charAt(element.element_path.length() - 1) == '/') {
            element.element_path = element.element_path.substring(0, element.element_path.length() - 1);
        }
        element.table_name = getValue(tableNames, element.element_path);
        element.attributes = new HashMap();
        String attribute = getAttributes(element, node);
        log(nesting + "-->" + path + " -- " + node.getName() + " " + attribute);
        element.attribute = attribute;
        listOfElements.add(element);
        List l = node.getChildren();
        if (!l.isEmpty()) {
            Iterator i = l.iterator();
            while (i.hasNext()) {
                nesting++;
                Element ele = (Element) i.next();
                readNode(nesting, path, ele);
                nesting--;
            }
        }
    }

    private void getXml() {
        try {
            // ---- Read XML file ----
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(xmlFile);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setFormat(Format.getPrettyFormat());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    private String getValue(Map<String, String> h, String k) {
        String erg = "";
        try {
            if (h.containsKey(k) && h.get(k) == null) {
                erg = "";
            } else {
                erg = h.get(k);
            }
            return erg;
        } catch (Exception e) {
            return "";
        }
    }

}
