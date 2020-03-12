package sos.scheduler.job;

import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.connection.SOSConnection;
import sos.marshalling.SOSExport;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.util.SOSString;

/** @author mueruevet oeksuez */
public class JobSchedulerExportJob extends JobSchedulerJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerExportJob.class);
    private String exportPath = "";
    private String outputFilenameMask = "[dataobject]_[id]_[datetime].xml";
    private Variable_set parameters = null;
    private String queryFilename = "";
    private SOSConnection sosConnection = null;
    private String sosSettingsFile = null;
    private SOSString sosString = null;
    private SOSConnection sosUpdateStateConnection = null;
    private String sosSettingsUpdateStateFile = null;

    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        if (rc) {
            try {
                sosString = new SOSString();
                this.setParameters(spooler.create_variable_set());
                if (spooler_task.params() != null) {
                    this.getParameters().merge(spooler_task.params());
                }
                if (spooler_job.order_queue() != null) {
                    this.getParameters().merge(spooler_task.order().params());
                }
                if (this.getParameters().value("export_path") != null && !this.getParameters().value("export_path").isEmpty()) {
                    exportPath = this.getParameters().value("export_path");
                    spooler_log.debug1(".. parameter [export_path]: ");
                } else {
                    spooler_log.warn(".. missing parameter [export_path]: ");
                    throw new Exception(".. missing parameter [export_path]: ");
                }
                if (this.getParameters().value("query_filename") != null && !this.getParameters().value("query_filename").isEmpty()) {
                    queryFilename = this.getParameters().value("query_filename");
                    spooler_log.debug1(".. parameter [query_filename]: " + queryFilename);
                    if (!new File(queryFilename).exists()) {
                        spooler_log.warn("..missing query file " + queryFilename);
                        throw new Exception("..missing query file " + queryFilename);
                    }
                } else {
                    spooler_log.warn(".. missing parameter [query_filename] ");
                    throw new Exception(".. missing parameter [query_filename] ");
                }
                if (this.getParameters().value("sos_settings_file") != null && !this.getParameters().value("sos_settings_file").isEmpty()) {
                    sosSettingsFile = this.getParameters().value("sos_settings_file");
                    spooler_log.debug1(".. parameter [sos_settings_file]: " + sosSettingsFile);
                }
                if (this.getParameters().value("sos_setting_file_status_update") != null
                        && !this.getParameters().value("sos_setting_file_status_update").isEmpty()) {
                    sosSettingsUpdateStateFile = this.getParameters().value("sos_setting_file_status_update");
                    spooler_log.debug1(".. parameter [sos_setting_file_status_update]: " + sosSettingsUpdateStateFile);
                }
                sosConnection = getConnections(sosSettingsFile);
                if (sosString.parseToString(sosSettingsFile).equalsIgnoreCase(sosString.parseToString(sosSettingsUpdateStateFile))) {
                    sosUpdateStateConnection = sosConnection;
                } else {
                    sosUpdateStateConnection = getConnections(sosSettingsUpdateStateFile);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                return false;
            }
        }
        return rc;
    }

    public boolean spooler_process() {
        long timeInSec = System.currentTimeMillis();
        boolean rc = true;
        String filename = "";
        String sSignalId = "";
        int signalId = -1;
        String jobChainname = "";
        String operation = null;
        try {
            this.setParameters(spooler.create_variable_set());
            if (spooler_task.order() != null && spooler_task.order().title() != null) {
                sSignalId = spooler_task.order().title();
                int iPos = sSignalId.indexOf(":");
                if (iPos > -1) {
                    sSignalId = sSignalId.substring(iPos + 1);
                }
                sSignalId = sSignalId.trim();
                try {
                    signalId = Integer.parseInt(sSignalId);
                } catch (Exception e) {
                    spooler_log.warn("..missing signal_id in Title: " + sSignalId);
                    return false;
                }
            } else {
                spooler_log.warn("..missing signal_id in Title: " + sSignalId);
                return false;
            }
            if (spooler_task.params() != null) {
                this.getParameters().merge(spooler_task.params());
            }
            if (spooler_job.order_queue() != null) {
                this.getParameters().merge(spooler_task.order().params());
            }
            if (this.getParameters().value("export_path") != null && !this.getParameters().value("export_path").isEmpty()) {
                exportPath = this.getParameters().value("export_path");
                spooler_log.debug1(".. parameter [export_path]: ");
            } else {
                LOGGER.warn(".. missing parameter [export_path]: ");
                throw new Exception(".. missing parameter [export_path]: ");
            }
            if (this.getParameters().value("query_filename") != null && !this.getParameters().value("query_filename").isEmpty()) {
                queryFilename = this.getParameters().value("query_filename");
                spooler_log.debug1(".. parameter [query_filename]: " + queryFilename);
                if (!new File(queryFilename).exists()) {
                    throw new Exception("..missing query file " + queryFilename);
                }
            } else {
                spooler_log.warn(".. missing parameter [query_filename] ");
                throw new Exception("..missing query file " + queryFilename);
            }
            if (this.getParameters().value("operation") != null && !this.getParameters().value("operation").isEmpty()) {
                operation = this.getParameters().value("operation");
                spooler_log.debug1(".. parameter [operation]: ");
            }
            jobChainname = spooler_task.order().job_chain().name();
            filename = normalizedPath(exportPath) + getNewFilename(jobChainname, sSignalId);
            export(sosConnection, queryFilename, filename, jobChainname, operation);
            File f = new File(filename);
            File fMove = new File(normalizedPath(exportPath) + f.getName().substring(0, f.getName().length() - 1));
            if (f.renameTo(fMove)) {
                spooler_task.order().params().set_var("filename", fMove.getCanonicalPath());
                spooler_log.debug1("filename is renamed in " + fMove.getCanonicalPath());
            } else {
                spooler_log.warn("filename could not rename to " + fMove.getCanonicalPath());
            }
            String upStr = "UPDATE " + JobSchedulerSignalJob.TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 2 WHERE  \"SIGNAL_ID\" = " + signalId;
            sosUpdateStateConnection.executeUpdate(upStr);
            sosUpdateStateConnection.commit();
            String time = Math.round((System.currentTimeMillis() - timeInSec) / 1000) + "s";
            String stateText = "successfully export Database to XML-File " + filename + " (" + time + ")";
            spooler_log.info(stateText);
            spooler_job.set_state_text(stateText);
            return spooler_job.order_queue() != null ? rc : false;
        } catch (Exception e) {
            if (sosConnection != null) {
                try {
                    sosConnection.rollback();
                } catch (Exception ex) {
                    //
                }
                try {
                    String upStr =
                            "UPDATE " + JobSchedulerSignalJob.TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 1002 WHERE  \"SIGNAL_ID\" = "
                                    + signalId;
                    sosUpdateStateConnection.executeUpdate(upStr);
                    sosUpdateStateConnection.commit();
                } catch (Exception x) {
                    //
                }
            }
            String stateText = "error occurred export filename : " + filename + " cause: " + e.getMessage();
            spooler_job.set_state_text(stateText);
            LOGGER.error(stateText);
            return false;
        }
    }

    public void spooler_exit() {
        super.spooler_exit();
        try {
            if (sosConnection != null) {
                sosConnection.disconnect();
            }
            if (sosUpdateStateConnection != null) {
                sosUpdateStateConnection.disconnect();
            }
        } catch (Exception e) {
            spooler_log.warn("spooler_exit(): disconnect failed: " + e.toString());
        }
    }

    public void export(SOSConnection sosConnection, String queryfile, String filename, String jobChain, String operation) throws Exception {
        org.w3c.dom.Element queries = null;
        SOSExport export = null;
        try {
            export = new SOSExport(sosConnection, filename, "EXPORT");
            queries = getQueryElement(queryfile, jobChain);
            if (queries != null) {
                exportQueryRecursiv(queries, export, -1, operation);
            }
        } catch (Exception e) {
            spooler_log.warn("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
            throw new Exception("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

    public Variable_set getParameters() {
        return parameters;
    }

    public void setParameters(Variable_set parameters) {
        this.parameters = parameters;
    }

    private String getNewFilename(String jobChainname, String signalId) throws Exception {
        String filename = "";
        filename = outputFilenameMask;
        filename = filename.replaceAll("\\[id\\]", signalId);
        filename = filename.replaceAll("\\[dataobject\\]", jobChainname);
        filename = filename.replaceAll("\\[datetime\\]", SOSDate.getCurrentTimeAsString("yyyymmdd-hhMMss"));
        filename = filename + "~";
        return filename;
    }

    private String normalizedPath(String path) throws Exception {
        try {
            if (path.endsWith("/")) {
                return path;
            } else {
                return path + "/";
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + ": " + e);
        }
    }

    private SOSConnection getConnections(String settingsfile) throws Exception {
        SOSConnection conn = null;
        if (!sosString.parseToString(settingsfile).isEmpty()) {
            conn = getConnectionFromINIFile(settingsfile);
        }
        if (conn == null) {
            conn = getConnection();
        }
        return conn;
    }

    private SOSConnection getConnectionFromINIFile(String settingsfile) throws Exception {
        SOSConnection conn = null;
        try {
            spooler_log.debug3("DB Connecting.. .");
            conn = SOSConnection.createInstance(settingsfile/*, new sos.util.SOSSchedulerLogger(spooler_log)*/);
            conn.connect();
            spooler_log.debug3("DB Connected");
        } catch (Exception e) {
            throw new Exception("error in " + SOSClassUtil.getMethodName() + ": connect to database failed: " + e.getMessage(), e);
        }
        return conn;
    }

    private org.w3c.dom.Element getQueryElement(String queryfile, String jobChain) throws Exception {
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(queryfile);
            org.w3c.dom.Element docEle = dom.getDocumentElement();
            org.w3c.dom.NodeList nl = docEle.getElementsByTagName("queries");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(i);
                    if (el.getAttribute("name").equalsIgnoreCase(jobChain)) {
                        return el;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

    private HashMap getKeys(String keys) throws Exception {
        try {
            HashMap retval = new HashMap();
            Variable_set set = getParameters();
            String[] split = keys.split(",");
            for (int i = 0; i < split.length; i++) {
                String name = split[i];
                String value = set.value(split[i]);
                retval.put(name, value);
            }
            return retval;
        } catch (Exception e) {
            spooler_log.warn("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
            throw new Exception("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

    private String replaceAllParameter(String query) throws Exception {
        try {
            String retval = query;
            Variable_set set = getParameters();
            String[] split = set.names().split(";");
            spooler_log.debug1("query before replace: " + query);
            for (int i = 0; i < split.length; i++) {
                String name = "\\$\\{" + split[i] + "\\}";
                String value = set.value(split[i]);
                retval = retval.replaceAll(name, value);
                retval = retval.replaceAll(name.toLowerCase(), value);
                retval = retval.replaceAll(name.toUpperCase(), value);
            }
            spooler_log.debug1("query after replace: " + retval);
            return retval;
        } catch (Exception e) {
            spooler_log.warn("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
            throw new Exception("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

    private void exportQueryRecursiv(org.w3c.dom.Element queries, SOSExport export, int queryId, String operation) throws Exception {
        org.w3c.dom.NodeList listOfQueries = null;
        org.w3c.dom.NodeList newlistOfQueries = null;
        boolean bDoExport = false;
        try {
            if (queryId == -1) {
                bDoExport = true;
            }
            if (queries != null) {
                listOfQueries = queries.getChildNodes();
                for (int i = 0; i < listOfQueries.getLength(); i++) {
                    org.w3c.dom.Node child = listOfQueries.item(i);
                    if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        org.w3c.dom.Element elem = (org.w3c.dom.Element) listOfQueries.item(i);
                        if ("query".equalsIgnoreCase(elem.getTagName())) {
                            String query = child.getFirstChild().getNodeValue();
                            query = query.replaceAll("\\n", "");
                            query = query.replaceAll("\\t", "");
                            query = replaceAllParameter(query);
                            String tag = "export";
                            if (!sosString.parseToString(elem.getAttribute("tag")).isEmpty()) {
                                tag = sosString.parseToString(elem.getAttribute("tag"));
                            }
                            String keys = "";
                            if (!sosString.parseToString(elem.getAttribute("keys")).isEmpty()) {
                                keys = sosString.parseToString(elem.getAttribute("keys"));
                            }

                            java.util.HashMap ldel = new java.util.HashMap();
                            if (operation != null && "delete".equalsIgnoreCase(operation)) {
                                ldel = getKeys(elem.getAttribute("keys"));
                            }
                            if (queryId == -1 && bDoExport) {
                                queryId = export.query(tag, keys, query, null, operation, ldel, queryId);
                            } else if (queryId > -1 && bDoExport) {
                                queryId = export.add(tag, keys, query, "", operation, ldel, queryId);
                            } else {
                                queryId = export.query(tag, keys, query, "", operation, ldel, queryId);
                            }
                            newlistOfQueries = elem.getElementsByTagName("query");
                            if (newlistOfQueries.getLength() > 0) {
                                exportQueryRecursiv(elem, export, queryId, operation);
                            }
                        }
                    }
                }
                if (bDoExport) {
                    export.doExport();
                }
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

}