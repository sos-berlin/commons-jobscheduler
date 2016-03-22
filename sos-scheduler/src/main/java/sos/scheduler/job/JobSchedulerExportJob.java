package sos.scheduler.job;

import java.io.File;
import java.util.HashMap;
import sos.connection.SOSConnection;
import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.marshalling.SOSExport;
import sos.util.SOSStandardLogger;
import sos.util.SOSString;

/** Export Job
 * 
 * @author mueruevet.oeksuez@sos-berlin.com
 *
 *         This job is used to extract records from a database to a XML-file */

public class JobSchedulerExportJob extends JobSchedulerJob {

    /** output path */
    private String exportPath = "";

    /** id = from Table SCHEDULER_SIGNAL_OBJECTS) */
    private String outputFilenameMask = "[dataobject]_[id]_[datetime].xml";

    /** order parameters */
    private Variable_set parameters = null;

    /** datapump query file name */
    private String queryFilename = "";

    /** databse connection and settings */
    private SOSConnection sosConnection = null;

    private String sosSettingsFile = null;

    private SOSString sosString = null;

    /** databse connection and settings */
    private SOSConnection sosUpdateStateConnection = null;

    private String sosSettingsUpdateStateFile = null;

    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        if (rc) {
            try {
                sosString = new SOSString();
                this.setLogger(new sos.util.SOSSchedulerLogger(spooler_log));
                this.setParameters(spooler.create_variable_set());
                // Parameter auslesen
                if (spooler_task.params() != null)
                    this.getParameters().merge(spooler_task.params());
                if (spooler_job.order_queue() != null)
                    this.getParameters().merge(spooler_task.order().params());

                if (this.getParameters().value("export_path") != null && this.getParameters().value("export_path").length() > 0) {
                    exportPath = this.getParameters().value("export_path");
                    getLogger().debug1(".. parameter [export_path]: ");
                } else {
                    // Fehlermeldung oder Warnung?
                    getLogger().warn(".. missing parameter [export_path]: ");
                    throw new Exception(".. missing parameter [export_path]: ");
                }

                if (this.getParameters().value("query_filename") != null && this.getParameters().value("query_filename").length() > 0) {
                    queryFilename = this.getParameters().value("query_filename");
                    getLogger().debug1(".. parameter [query_filename]: " + queryFilename);
                    if (!new File(queryFilename).exists()) {
                        getLogger().warn("..missing query file " + queryFilename);
                        throw new Exception("..missing query file " + queryFilename);
                    }
                } else {
                    // Fehlermeldung oder Warnung?
                    getLogger().warn(".. missing parameter [query_filename] ");
                    throw new Exception(".. missing parameter [query_filename] ");
                }

                if (this.getParameters().value("sos_settings_file") != null && this.getParameters().value("sos_settings_file").length() > 0) {
                    sosSettingsFile = this.getParameters().value("sos_settings_file");
                    getLogger().debug1(".. parameter [sos_settings_file]: " + sosSettingsFile);
                }

                if (this.getParameters().value("sos_setting_file_status_update") != null
                        && this.getParameters().value("sos_setting_file_status_update").length() > 0) {
                    sosSettingsUpdateStateFile = this.getParameters().value("sos_setting_file_status_update");
                    getLogger().debug1(".. parameter [sos_setting_file_status_update]: " + sosSettingsUpdateStateFile);
                }

                // DB Einstellungen aus der sos_settings.file (=jobparameter)
                // oder aus der factory.ini
                sosConnection = getConnections(sosSettingsFile);
                if (sosString.parseToString(sosSettingsFile).equalsIgnoreCase(sosString.parseToString(sosSettingsUpdateStateFile))) {
                    sosUpdateStateConnection = sosConnection;
                } else {
                    sosUpdateStateConnection = getConnections(sosSettingsUpdateStateFile);
                }
            } catch (Exception e) {
                if (this.getLogger() != null)
                    try {
                        this.getLogger().error(e.getMessage());
                    } catch (Exception ex) {
                    }
                return false;
            }
        }
        return rc;
    }

    public boolean spooler_process() {
        long timeInSec = System.currentTimeMillis();
        boolean rc = true;
        String filename = "";
        String sSignalId = ""; // Hilfsvariable
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
                    getLogger().warn("..missing signal_id in Title: " + sSignalId);
                    return false;
                }

            } else {
                getLogger().warn("..missing signal_id in Title: " + sSignalId);
                return false;
            }

            // Parameter auslesen
            if (spooler_task.params() != null)
                this.getParameters().merge(spooler_task.params());
            if (spooler_job.order_queue() != null)
                this.getParameters().merge(spooler_task.order().params());

            if (this.getParameters().value("export_path") != null && this.getParameters().value("export_path").length() > 0) {
                exportPath = this.getParameters().value("export_path");
                getLogger().debug1(".. parameter [export_path]: ");
            } else {
                getLogger().warn(".. missing parameter [export_path]: ");
                throw new Exception(".. missing parameter [export_path]: ");

            }

            if (this.getParameters().value("query_filename") != null && this.getParameters().value("query_filename").length() > 0) {
                queryFilename = this.getParameters().value("query_filename");
                getLogger().debug1(".. parameter [query_filename]: " + queryFilename);
                if (!new File(queryFilename).exists()) {
                    throw new Exception("..missing query file " + queryFilename);
                }
            } else {
                getLogger().warn(".. missing parameter [query_filename] ");
                throw new Exception("..missing query file " + queryFilename);
            }

            if (this.getParameters().value("operation") != null && this.getParameters().value("operation").length() > 0) {
                operation = this.getParameters().value("operation");
                getLogger().debug1(".. parameter [operation]: ");
            }

            jobChainname = spooler_task.order().job_chain().name();

            filename = normalizedPath(exportPath) + getNewFilename(jobChainname, sSignalId);

            export(sosConnection, queryFilename, filename, jobChainname, operation);

            File f = new File(filename);
            File fMove = new File(normalizedPath(exportPath) + f.getName().substring(0, f.getName().length() - 1));
            if (f.renameTo(fMove)) {
                spooler_task.order().params().set_var("filename", fMove.getCanonicalPath());
                getLogger().debug("filename is renamed in " + fMove.getCanonicalPath());
            } else {
                getLogger().warn("filename could not rename to " + fMove.getCanonicalPath());
            }

            // status auf 2 setzen
            String upStr = "UPDATE " + JobSchedulerSignalJob.TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 2 WHERE  \"SIGNAL_ID\" = " + signalId;
            sosUpdateStateConnection.executeUpdate(upStr);
            sosUpdateStateConnection.commit();

            String time = Math.round((System.currentTimeMillis() - timeInSec) / 1000) + "s";
            String stateText = "successfully export Database to XML-File " + filename + " (" + time + ")";

            getLogger().info(stateText);
            spooler_job.set_state_text(stateText);
            // System.out.println("successfully export Database to XML-File " +
            // filename);

            return ((spooler_job.order_queue() != null) ? rc : false);

        } catch (Exception e) {

            if (sosConnection != null) {
                try {
                    // System.out.println("Fehler: " +
                    // SOSClassUtil.getMethodName() + e.toString());
                    sosConnection.rollback();
                } catch (Exception ex) {
                }
                try {
                    String upStr = "UPDATE " + JobSchedulerSignalJob.TABLE_SCHEDULER_SIGNAL_OBECTS + " SET \"STATUS\" = 1002 WHERE  \"SIGNAL_ID\" = "
                            + signalId;
                    sosUpdateStateConnection.executeUpdate(upStr);
                    sosUpdateStateConnection.commit();

                } catch (Exception x) {
                }

            }
            String stateText = "error occurred export filename : " + filename + " cause: " + e.getMessage();
            spooler_job.set_state_text(stateText);
            spooler_log.error(stateText);
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
        SOSStandardLogger sosLogger = null;
        SOSExport export = null;
        try {

            // sosLogger = new SOSStandardLogger(getLogger().getFileName(),
            // getLogger().getLogLevel());
            sosLogger = new SOSStandardLogger(spooler.log().filename(), getLogger().getLogLevel());

            export = new SOSExport(sosConnection, filename, "EXPORT", sosLogger);
            queries = getQueryElement(queryfile, jobChain);

            if (queries != null) {
                exportQueryRecursiv(queries, export, -1, operation);
            }

        } catch (Exception e) {
            spooler_log.warn("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
            throw new Exception("error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

    /** @return Returns the parameters. */
    public Variable_set getParameters() {
        return parameters;
    }

    /** @param parameters The parameters to set. */
    public void setParameters(Variable_set parameters) {
        this.parameters = parameters;
    }

    /** Generiert eine neue Dateiname.
     * 
     * Die neue Dateiname wird gebildet aus: [dataobject]_[id]_[datetime].xml
     * [scheduler_signal_objects
     * .job_chain]_[scheduler_signal_objects.signal_id]_ [yyyymmdd-hhMMss].xml
     * dataobject = job_chainname id = ursprung: Eindeutige SChlüssen der
     * Tabelle SCHEDULER_SIGNAL_OBJECT.ID. Steht hier in der Order Id. datetime
     * = datetime
     * 
     * @return String
     * @throws Exception */
    private String getNewFilename(String jobChainname, String signalId) throws Exception {
        String filename = "";
        filename = outputFilenameMask;
        filename = filename.replaceAll("\\[id\\]", signalId);
        filename = filename.replaceAll("\\[dataobject\\]", jobChainname);

        filename = filename.replaceAll("\\[datetime\\]", SOSDate.getCurrentTimeAsString("yyyymmdd-hhMMss"));
        filename = filename + "~";

        return filename;

    }

    /** Fügt einen Slah hinten einen Verzeichnis, wenn dieser Bereits nicht
     * existiert
     * 
     * @param path
     * @return @throws Exception */
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
        if (sosString.parseToString(settingsfile).length() > 0) {
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
            conn = SOSConnection.createInstance(settingsfile, new sos.util.SOSSchedulerLogger(spooler_log));
            conn.connect();
            spooler_log.debug3("DB Connected");

        } catch (Exception e) {
            throw (new Exception("error in " + SOSClassUtil.getMethodName() + ": connect to database failed: " + e.toString()));
        }
        return conn;
    }

    private org.w3c.dom.Element getQueryElement(String queryfile, String jobChain) throws Exception {
        try {

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            // Using factory get an instance of document builder
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(queryfile);
            // get the root element
            org.w3c.dom.Element docEle = dom.getDocumentElement();

            // get a nodelist of elements
            org.w3c.dom.NodeList nl = docEle.getElementsByTagName("queries");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    // get the employee element
                    org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(i);
                    if (el.getAttribute("name").equalsIgnoreCase(jobChain)) {
                        return el;
                    }
                }
            }

            /*
             * SAXBuilder builder = new SAXBuilder(); Document doc =
             * builder.build( new File( queryfile ) ); Element root =
             * doc.getRootElement(); if(root != null) { listOfQueries =
             * root.getChildren("queries"); for(int i = 0; i <
             * listOfQueries.size(); i++) { Element elem =
             * (Element)listOfQueries.get(i);
             * if(elem.getAttributeValue("name").equalsIgnoreCase(jobChain)) {
             * return elem; } } }
             */
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
            // System.out.println("-------------------------------------- ");
            // System.out.println("before: " + query);
            getLogger().debug("query before replace: " + query);
            for (int i = 0; i < split.length; i++) {
                String name = "\\$\\{" + split[i] + "\\}";
                String value = set.value(split[i]);
                retval = retval.replaceAll(name, value);
                retval = retval.replaceAll(name.toLowerCase(), value);
                retval = retval.replaceAll(name.toUpperCase(), value);

            }
            // System.out.println("after:  " + retval);
            getLogger().debug("query after replace: " + retval);
            // System.out.println("-------------------------------------- ");
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
            // if(queries.getParentNode().getNodeName().equalsIgnoreCase("all_queries"))
            // {
            if (queryId == -1) {
                bDoExport = true;
            }
            if (queries != null) {

                listOfQueries = queries.getChildNodes();

                for (int i = 0; i < listOfQueries.getLength(); i++) {
                    org.w3c.dom.Node child = listOfQueries.item(i);

                    if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {

                        org.w3c.dom.Element elem = (org.w3c.dom.Element) listOfQueries.item(i);
                        if (elem.getTagName().equalsIgnoreCase("query")) {

                            // QUERY MIT PARAMETERN ERSETZEN
                            String query = child.getFirstChild().getNodeValue();
                            query = query.replaceAll("\\n", "");
                            query = query.replaceAll("\\t", "");
                            query = replaceAllParameter(query);

                            String tag = "export";
                            if (sosString.parseToString(elem.getAttribute("tag")).length() > 0) {
                                tag = sosString.parseToString(elem.getAttribute("tag"));
                            }
                            String keys = "";
                            if (sosString.parseToString(elem.getAttribute("keys")).length() > 0) {
                                keys = sosString.parseToString(elem.getAttribute("keys"));
                            }

                            java.util.HashMap ldel = new java.util.HashMap();
                            if (operation != null && operation.equalsIgnoreCase("delete")) {
                                ldel = getKeys(elem.getAttribute("keys"));

                            }

                            // EXPORTIEREN
                            if (queryId == -1 && bDoExport) {
                                queryId = export.query(tag, keys, query, null, operation, ldel, queryId);
                            } else if (queryId > -1 && bDoExport) {
                                // unabhängige Abfrage
                                queryId = export.add(tag, keys, query, "", operation, ldel, queryId);

                            } else {
                                // abhängige Abfrage
                                queryId = export.query(tag, keys, query, "", operation, ldel, queryId);
                            }

                            // REKURSIVE AUFRUF WENN DAS ELEMENT QUERIES BZW.
                            // QUERY WEITERE QUERY KINDKNOTEN HAT
                            newlistOfQueries = elem.getElementsByTagName("query");
                            // int exportid ermitteln und den rekursiven Teil
                            // wiedergeben
                            if (newlistOfQueries.getLength() > 0) {
                                exportQueryRecursiv(elem, export, queryId, operation);

                            }
                        }
                    }
                }

                // EXPORTIEREN
                if (bDoExport) {
                    export.doExport();
                }
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + " :" + e.getMessage());
        }
    }

}