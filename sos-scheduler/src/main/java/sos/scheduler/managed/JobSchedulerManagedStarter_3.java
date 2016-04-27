package sos.scheduler.managed;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sos.connection.SOSOracleConnection;
import sos.scheduler.command.RemoteScheduler;
import sos.scheduler.job.JobSchedulerJob;
import sos.scheduler.live.JobSchedulerLiveXml;
import sos.scheduler.live.JobSchedulerMetadataElement;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSCrypt;
import sos.util.SOSFile;
import sos.util.SOSFileOperations;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;
import sos.xml.SOSXMLXPath;

/** @author Andreas Liebert */
public class JobSchedulerManagedStarter_3 extends JobSchedulerJob {

    protected Pattern hotFolderRegExPattern;
    protected Pattern noSchedulerFileRegExPattern;
    private static final String HOT_FOLDER_REG_EX = "(?:(.*),)?(.*)\\.(.*)\\.xml$";
    private static final String NO_SCHEDULER_FILE_REG_EX = "^.*(?<!\\.(job|order|lock|job_chain|process_class|params|schedule)\\.xml)$";
    private boolean startscript = true;
    private File liveFolder;
    private File remoteFolder;
    private boolean isDatabaseInterfaceSupported = false;
    private LinkedHashSet listOfElements = null;
    private DocumentBuilderFactory docFactory;
    private DocumentBuilder docBuilder;
    private HashMap dbCache;
    public static final int ACTION_SUBMIT = 1;
    public static final int ACTION_SUBMIT_AND_START = 2;
    public static final int ACTION_TRY_OUT = 3;
    public static final int ACTION_REMOVE = 4;
    public static final int ACTION_REMOVE_ORDERS = 5;
    public static final int ACTION_REMOVE_DIR = 6;
    public static final int ACTION_MOVE = 7;
    public static final int ACTION_MOVE_DIR = 8;
    Variable_set orderParams = null;

    public static final int getAction(String action) throws Exception {
        if ("submit".equalsIgnoreCase(action)) {
            return ACTION_SUBMIT;
        } else if ("submit_and_start".equalsIgnoreCase(action)) {
            return ACTION_SUBMIT_AND_START;
        } else if ("remove".equalsIgnoreCase(action)) {
            return ACTION_REMOVE;
        } else if ("remove_orders".equalsIgnoreCase(action)) {
            return ACTION_REMOVE_ORDERS;
        } else if ("remove_dir".equalsIgnoreCase(action)) {
            return ACTION_REMOVE_DIR;
        } else if ("try_out".equalsIgnoreCase(action)) {
            return ACTION_TRY_OUT;
        } else if ("move".equalsIgnoreCase(action)) {
            return ACTION_MOVE;
        } else if ("move_dir".equalsIgnoreCase(action)) {
            return ACTION_MOVE_DIR;
        }
        throw new Exception("unknown action: \"" + action + "\"");
    }

    private final String getWhat(String type) {
        String what = "unknown type: " + type;
        if ("o".equalsIgnoreCase(type)) {
            what = "order";
        } else if ("j".equalsIgnoreCase(type)) {
            what = "orderjob";
        } else if ("i".equalsIgnoreCase(type)) {
            what = "job";
        } else if ("f".equalsIgnoreCase(type)) {
            what = "job.global";
        } else if ("l".equalsIgnoreCase(type)) {
            what = "lock";
        } else if ("c".equalsIgnoreCase(type)) {
            what = "job_chain";
        } else if ("p".equalsIgnoreCase(type)) {
            what = "process_class";
        } else if ("k".equalsIgnoreCase(type)) {
            what = "documentation";
        } else if ("s".equalsIgnoreCase(type)) {
            what = "schedule";
        } else if ("t".equalsIgnoreCase(type)) {
            what = "schedule.substitute";
        } else if ("v".equalsIgnoreCase(type)) {
            what = "params";
        } else if ("m".equalsIgnoreCase(type)) {
            what = "misc";
        }
        return what;
    }

    private final String getType(String what) {
        String type = "unknown type: " + what;
        if ("order".equalsIgnoreCase(what)) {
            type = "o";
        } else if ("orderjob".equalsIgnoreCase(what)) {
            type = "j";
        } else if ("job".equalsIgnoreCase(what)) {
            type = "i";
        } else if ("job.global".equalsIgnoreCase(what)) {
            type = "f";
        } else if ("lock".equalsIgnoreCase(what)) {
            type = "l";
        } else if ("job_chain".equalsIgnoreCase(what)) {
            type = "c";
        } else if ("process_class".equalsIgnoreCase(what)) {
            type = "p";
        } else if ("documentation".equalsIgnoreCase(what)) {
            type = "k";
        } else if ("schedule".equalsIgnoreCase(what)) {
            type = "s";
        } else if ("schedule.substitute".equalsIgnoreCase(what)) {
            type = "t";
        } else if ("params".equalsIgnoreCase(what)) {
            type = "v";
        } else if ("misc".equalsIgnoreCase(what)) {
            type = "m";
        }
        return type;
    }

    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        hotFolderRegExPattern = Pattern.compile(HOT_FOLDER_REG_EX);
        noSchedulerFileRegExPattern = Pattern.compile(NO_SCHEDULER_FILE_REG_EX);
        try {
            String available = this.getConnection().getSingleValue("SELECT COUNT(*) FROM " + JobSchedulerManagedObject.getTableLiveObjects() + " WHERE 1=0");
            if (available != null && "0".equals(available)) {
                isDatabaseInterfaceSupported = true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                getConnection().rollback();
            } catch (Exception e) {
                try {
                    getLog().error("Error rolling back after checking live objects: " + e);
                } catch (Exception ex) {
                }
            }
        }
        liveFolder = new File(spooler.configuration_directory());
        remoteFolder = new File(liveFolder.getParentFile(), "remote");
        startscript = spooler_job == null;
        if (!rc) {
            return false || startscript;
        }
        if (startscript) {
            try {
                setLogger(new SOSSchedulerLogger(spooler.log()));
                getLog().info("JobSchedulerManagedStarter is running as startscript");
                spooler.set_var("scheduler_managed_jobs_version", "3");
            } catch (Exception e2) {
            }
        } else {
            try {
                setLogger(new SOSSchedulerLogger(spooler_log));
                getLog().info("JobSchedulerManagedStarter is running as job");
            } catch (Exception e3) {
            }
        }
        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
        } catch (Exception e) {
            spooler_log.error("Failed to initialize DocumentBuilderFactory: " + e);
            return false;
        }
        if (startscript) {
            try {
                processLive();
                processSubmits();
                processDirs();
                processDatabase();
                return true;
            } catch (Exception e) {
                try {
                    getLog().error("error occurred in initialization: " + e.getMessage());
                } catch (Exception e1) {
                }
                return false || startscript;
            } finally {
                if (this.getConnection() != null) {
                    try {
                        this.getConnection().rollback();
                    } catch (Exception ex) {
                        // no error handling
                    }
                    try {
                        this.getConnection().disconnect();
                    } catch (Exception ex) {
                        // no error handling
                    }
                }
            }
        }
        return true;
    }

    private void processLive() throws Exception {
        if (!isDatabaseInterfaceSupported) {
            return;
        }
        try {
            Iterator resultset = null;
            HashMap rec;
            spooler_log.debug3("Exporting configurations from database interface");
            String selStr = "SELECT oh.\"PK_ID\", oh.\"OBJECT_ID\", " + " oh.\"NAME\" AS \"NEW_NAME\", oh.\"PATH\" AS \"NEW_PATH\", "
                    + " oh.\"OPERATION\", o.\"NAME\", o.\"PATH\", o.\"TYPE\"" + " FROM " + JobSchedulerManagedObject.getTableLiveObjectHistory()
                    + " oh LEFT OUTER JOIN " + JobSchedulerManagedObject.getTableLiveObjects() + " o" + " ON oh.\"OBJECT_ID\"=o.\"PK_ID\""
                    + " WHERE oh.\"IN_SYNC\"=0";
            if (!startscript && orderParams.value("history_id") != null && !orderParams.value("history_id").isEmpty()) {
                selStr += " AND oh.\"PK_ID\"=" + orderParams.value("history_id");
            }
            spooler_log.debug3("processLive: " + selStr);
            ArrayList arrayList = new ArrayList();
            arrayList = this.getConnection().getArray(selStr);
            resultset = arrayList.iterator();
            while (resultset.hasNext()) {
                rec = (HashMap) resultset.next();
                if (rec.get("name") != null && !rec.get("name").toString().isEmpty()) {
                    if ("delete".equalsIgnoreCase(getLiveValue(rec, "operation")) || "rename".equalsIgnoreCase(getLiveValue(rec, "operation"))) {
                        File currentFile = null;
                        File newFile = null;
                        String jobChainName = "";
                        if ("order".equalsIgnoreCase(getLiveValue(rec, "type"))) {
                            jobChainName = this.getConnection().getSingleValue(
                                    "SELECT \"JOB_CHAIN\" FROM " + JobSchedulerManagedObject.getTableLiveOrders() + " WHERE \"OBJECT_ID\"="
                                            + getLiveValue(rec, "object_id"));
                            if (jobChainName != null && jobChainName.length() > 0) {
                                currentFile = new File(liveFolder + "/" + getLiveValue(rec, "path"), jobChainName + "," + getLiveValue(rec, "name")
                                        + ".order.xml");
                            } else {
                                currentFile = new File(liveFolder + "/" + getLiveValue(rec, "path"), getLiveValue(rec, "name") + "."
                                        + getLiveValue(rec, "type") + ".xml");
                            }
                        } else {
                            currentFile = new File(liveFolder + "/" + getLiveValue(rec, "path"), getLiveValue(rec, "name") + "." + getLiveValue(rec, "type")
                                    + ".xml");
                        }
                        getLogger().debug7("current file: " + currentFile.getAbsolutePath());
                        if (currentFile.exists()) {
                            boolean rc = false;
                            if ("delete".equalsIgnoreCase(getLiveValue(rec, "operation"))) {
                                getLogger().debug("deleting file " + currentFile.getAbsolutePath());
                                rc = currentFile.delete();
                                if (!rc) {
                                    getLogger().warn("failed to delete file " + currentFile.getAbsolutePath());
                                }
                            } else if ("rename".equalsIgnoreCase(getLiveValue(rec, "operation"))) {
                                if ("order".equalsIgnoreCase(getLiveValue(rec, "type"))) {
                                    if (jobChainName != null && jobChainName.length() > 0) {
                                        newFile = new File(liveFolder + "/" + getLiveValue(rec, "new_path"), jobChainName + "," + getLiveValue(rec, "new_name")
                                                + ".order.xml");
                                    } else {
                                        newFile = new File(liveFolder + "/" + getLiveValue(rec, "new_path"), getLiveValue(rec, "new_name") + "."
                                                + getLiveValue(rec, "type") + ".xml");
                                    }
                                } else {
                                    newFile = new File(liveFolder + "/" + getLiveValue(rec, "new_path"), getLiveValue(rec, "new_name") + "."
                                            + getLiveValue(rec, "type") + ".xml");
                                }
                                getLogger().debug("renaming file " + currentFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
                                rc = currentFile.renameTo(newFile);
                                if (!rc) {
                                    getLogger().warn("failed to rename file " + currentFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
                                }
                            }
                            if (rc && !isWindows() && !startscript) {
                                spooler.execute_xml("<check_folders/>");
                            }
                        }
                    } else {
                        liveExport(rec);
                    }
                }
                String updString = "UPDATE " + JobSchedulerManagedObject.getTableLiveObjectHistory()
                        + " SET \"SYNCHRONIZED\"=%now, \"IN_SYNC\"=1 WHERE \"PK_ID\"=" + getLiveValue(rec, "pk_id");
                this.getConnection().executeUpdate(updString);
                if ("delete".equalsIgnoreCase(getLiveValue(rec, "operation"))) {
                    updString = "DELETE FROM " + JobSchedulerManagedObject.getTableLiveObjects() + " WHERE \"PK_ID\"=" + getLiveValue(rec, "object_id");
                    this.getConnection().executeUpdate(updString);
                }
                this.getConnection().commit();
            }
        } catch (Exception e) {
            throw new Exception("Error processing configuration from database interface: " + e, e);
        }
    }

    private void processSubmits() throws Exception {
        try {
            String query = "SELECT \"ID\", \"OBJECT_ID\", \"PATH\", \"OLD_PATH\", \"HOST\", \"PORT\", \"ACTION\", \"HASH\" FROM "
                    + JobSchedulerManagedObject.tableManagedSubmits + " WHERE \"STATE\"='ordered' AND \"SPOOLER_ID\"='" + spooler.id()
                    + "' ORDER BY \"ID\" ASC";
            ArrayList submits = getConnection().getArray(query);
            getLogger().debug1("Found " + submits.size() + " submits.");
            Iterator submitIterator = submits.iterator();
            while (submitIterator.hasNext()) {
                HashMap submit = (HashMap) submitIterator.next();
                String submitID = submit.get("id").toString();
                String xml = getConnection().getClob("SELECT \"XML\" FROM " + JobSchedulerManagedObject.tableManagedSubmits + " WHERE \"ID\"=" + submitID);
                submit.put("xml", xml);
                try {
                    processSubmit(submit);
                } catch (Exception e) {
                    getLogger().warn("Error processing submit: " + e);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error processing previously submitted commands: " + e, e);
        }
    }

    private final static String getPathWithoutSupervisor(String path) {
        int index = path.indexOf('/', 1);
        if (index == -1) {
            return "/";
        }
        String pathWithoutSup = path.substring(index);
        return pathWithoutSup;
    }

    private void processSubmit(HashMap submit) throws Exception {
        String state = "stored";
        try {
            logSubmit(submit);
            if (!submit.get("path").toString().isEmpty()) {
                submit.put("path", getPathWithoutSupervisor(submit.get("path").toString()));
            }
            if (!submit.get("old_path").toString().isEmpty()) {
                submit.put("old_path", getPathWithoutSupervisor(submit.get("old_path").toString()));
            }
            String host = submit.get("host").toString();
            String port = submit.get("port").toString();
            File currentRoot = liveFolder.getParentFile();
            getLogger().debug7("current root folder: " + currentRoot.getAbsolutePath());
            File currentFile = new File(currentRoot, submit.get("path").toString());
            getLogger().debug7("current file: " + currentFile.getAbsolutePath());
            File oldFile = currentFile;
            if (!submit.get("old_path").toString().isEmpty()) {
                oldFile = new File(currentRoot, submit.get("old_path").toString());
            }
            String action = submit.get("action").toString();
            if (action == null || action.isEmpty()) {
                throw new Exception("submit has no action!");
            }
            switch (getAction(action)) {
            case ACTION_SUBMIT:
                processActionSubmit(currentFile, submit);
                break;
            case ACTION_SUBMIT_AND_START:
                processActionSubmit(currentFile, submit);
                processActionAndStart(submit, host, port);
                break;
            case ACTION_TRY_OUT:
                processActionTryOut(submit, host, port);
                break;
            case ACTION_REMOVE:
                processActionRemove(currentFile, submit);
                break;
            case ACTION_REMOVE_ORDERS:
                processActionRemoveOrders(currentFile, submit);
                break;
            case ACTION_REMOVE_DIR:
                processActionRemoveDir(currentFile.getAbsolutePath());
                break;
            case ACTION_MOVE:
                processActionMove(oldFile, currentFile, submit);
                break;
            case ACTION_MOVE_DIR:
                processActionMoveDir(oldFile, currentFile, submit);
            }
        } catch (Exception e) {
            state = "error";
            throw new Exception("Error executing MANAGED_SUBMIT[" + submit.get("id").toString() + "]: " + e, e);
        } finally {
            try {
                String sql = "UPDATE " + JobSchedulerManagedObject.tableManagedSubmits + " SET \"STATE\"='" + state + "', "
                        + "\"MODIFIED\"=%now, \"MODIFIED_BY\"='JobSchedulerManagedStarter' WHERE \"ID\"=" + submit.get("id").toString();
                getConnection().executeUpdate(sql);
                getConnection().commit();
            } catch (Exception e) {
                throw new Exception("Error updating MANAGED_SUBMISSION[" + submit.get("id").toString() + "]: " + e, e);
            }
        }
    }

    private void processActionSubmit(File file, HashMap submit) throws Exception {
        try {
            getLogger().debug9("processActionSubmit()");
            String hash = submit.get("hash").toString();
            String fileHash = "";
            if (file.exists()) {
                fileHash = SOSCrypt.MD5encrypt(file);
            } else if (!file.getParentFile().exists()) {
                getLogger().debug1("Creating directory " + file.getParent());
                if (!file.getParentFile().mkdirs()) {
                    throw new Exception("Failed to create directory " + file.getParent());
                }
            }
            if (!hash.isEmpty() && !hash.equals(fileHash)) {
                getLogger().debug1("File has changed, possible conflict. Database wins.");
            }
            String xml = submit.get("xml").toString();
            String path = submit.get("path").toString();
            updateLiveFile(xml, path, file, submit.get("object_id").toString());
        } catch (Exception e) {
            throw new Exception("Error in submit action: " + e);
        }
    }

    private void updateLiveFile(String xml, String currentPath, File resultFile, String objectID) throws Exception {
        try {
            getLogger().debug6("updating live file " + resultFile.getAbsolutePath());
            Matcher mat = noSchedulerFileRegExPattern.matcher(resultFile.getName());
            if (mat.matches()) {
                getLogger().debug5(resultFile.getAbsolutePath() + " is no scheduler configuration file.");
                FileWriter fstream = new FileWriter(resultFile);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xml);
                out.close();
            } else {
                ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
                Document xmlDoc = docBuilder.parse(bis);
                writeXMLFile(xmlDoc, resultFile);
            }
            if (objectID != null && !objectID.isEmpty()) {
                String fileHash = SOSCrypt.MD5encrypt(resultFile);
                String sql = "UPDATE " + JobSchedulerManagedObject.tableManagedObjects + " SET \"HASH\"='" + fileHash + "', \"STATE\"=10 " + " WHERE \"ID\"="
                        + objectID;
                getConnection().executeUpdate(sql);
                getConnection().commit();
            }
        } catch (Exception e) {
            throw new Exception("Error updating live file: " + e.getMessage(), e);
        }
    }

    private void processActionRemove(File file, HashMap submit) throws Exception {
        try {
            String hash = submit.get("hash").toString();
            String fileHash = "";
            if (file.exists()) {
                fileHash = SOSCrypt.MD5encrypt(file);
            }
            if (!hash.isEmpty() && !hash.equals(fileHash)) {
                getLogger().debug1("File has changed, possible conflict. Database wins.");
            }
            if (file.exists()) {
                getLogger().debug("deleting file " + file.getAbsolutePath());
                boolean rc = file.delete();
                if (!file.getParentFile().equals(liveFolder) && !file.getParentFile().getParentFile().equals(remoteFolder)
                        && file.getParentFile().list().length == 0) {
                    getLogger().debug("Parent directory is now empty, deleting parent directory...");
                    deleteDirectory(file.getParentFile());
                }
                if (!rc) {
                    getLogger().warn("failed to delete file " + file.getAbsolutePath());
                } else if (!isWindows() && !startscript) {
                    spooler.execute_xml("<check_folders/>");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in remove action: " + e);
        }
    }

    private void processActionRemoveOrders(File file, HashMap submit) throws Exception {
        try {
            String orderRegEx = file.getName().replaceAll("\\.job_chain\\.xml", ",.*\\.order\\.xml");
            Vector orderFiles = SOSFile.getFilelist(file.getParent(), orderRegEx, 0);
            Iterator iter = orderFiles.iterator();
            boolean dirChanged = false;
            while (iter.hasNext()) {
                File orderFile = (File) iter.next();
                if (orderFile.exists()) {
                    getLogger().debug("deleting file " + orderFile.getAbsolutePath());
                    boolean rc = orderFile.delete();
                    if (!rc) {
                        getLogger().warn("failed to delete file " + orderFile.getAbsolutePath());
                    } else {
                        dirChanged = true;
                    }
                }
            }
            if (!isWindows() && !startscript && dirChanged) {
                spooler.execute_xml("<check_folders/>");
            }
        } catch (Exception e) {
            throw new Exception("Error in remove orders action: " + e);
        }
    }

    private void processActionRemoveDir(String dir) throws Exception {
        try {
            getLogger().debug6("remove directory: " + dir);
            File d = new File(dir);
            boolean dirChanged = deleteDirectory(d);
            if (!isWindows() && !startscript && dirChanged) {
                spooler.execute_xml("<check_folders/>");
            }
            if (!dirChanged) {
                throw new Exception("Directory " + d.getAbsolutePath() + " could not be removed.");
            }
        } catch (Exception e) {
            throw new Exception("Error in remove directory action: " + e.getMessage(), e);
        }
    }

    private void processActionMove(File oldFile, File newFile, HashMap submit) throws Exception {
        try {
            boolean dirChanged = false;
            getLogger().debug1("Moving " + oldFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
            SOSFileOperations.renameFile(oldFile, newFile, getLogger());
            dirChanged = true;
            String sql = "UPDATE " + JobSchedulerManagedObject.tableManagedObjects + " SET \"STATE\"=10 " + " WHERE \"ID\"="
                    + submit.get("object_id").toString();
            getConnection().executeUpdate(sql);
            getConnection().commit();
            if (!isWindows() && !startscript && dirChanged) {
                spooler.execute_xml("<check_folders/>");
            }
        } catch (Exception e) {
            throw new Exception("Error in move action: " + e);
        }
    }

    private void moveDir(File source, File target, SOSLogger log) throws Exception {
        try {
            SOSFileOperations.renameFile(source, target, SOSFileOperations.GRACIOUS | SOSFileOperations.CREATE_DIR | SOSFileOperations.RECURSIVE, log);
            deleteDirectory(source);
        } catch (Exception e) {
            throw new Exception("Failed to move " + source.getAbsolutePath() + " to " + target.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private void processActionMoveDir(File oldFile, File newFile, HashMap submit) throws Exception {
        try {
            boolean dirChanged = false;
            getLogger().debug1("Moving " + oldFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
            moveDir(oldFile, newFile, getLogger());
            dirChanged = true;
            String relativePath = submit.get("path").toString();
            if (!relativePath.endsWith("/")) {
                relativePath += "/";
            }
            String sql = "SELECT \"ITEM_ID\" FROM " + JobSchedulerManagedObject.tableManagedTree + " WHERE \"PATH\" LIKE " + "'/" + spooler.id() + relativePath
                    + "%'";
            ArrayList objectIDs = getConnection().getArrayValue(sql);
            if (!objectIDs.isEmpty()) {
                Iterator iter = objectIDs.iterator();
                String comma = "";
                StringBuilder objectIDList = new StringBuilder();
                while (iter.hasNext()) {
                    String objectID = (String) iter.next();
                    objectIDList.append(comma);
                    objectIDList.append(objectID);
                    comma = ", ";
                }
                sql = "UPDATE " + JobSchedulerManagedObject.tableManagedObjects + " SET \"STATE\"=10 " + " WHERE \"ID\" IN (" + objectIDList.toString()
                        + ") AND \"STATE\"=5";
                getConnection().executeUpdate(sql);
                getConnection().commit();
            }
            if (!isWindows() && !startscript && dirChanged) {
                spooler.execute_xml("<check_folders/>");
            }
        } catch (Exception e) {
            throw new Exception("Error in move action: " + e);
        }
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                boolean rc = true;
                String what = "File ";
                if (files[i].isDirectory()) {
                    rc = deleteDirectory(files[i]);
                } else {
                    rc = files[i].delete();
                    what = "Directory";
                }
                if (!rc) {
                    try {
                        getLogger().warn(what + files[i].getAbsolutePath() + " could not be deleted");
                    } catch (Exception e) {
                    }
                }
            }
        }
        return path.delete();
    }

    private void processActionAndStart(HashMap submit, String host, String port) throws Exception {
        try {
            if (startscript) {
                getLogger().debug1("Start request is skipped, because this is a startscript.");
                return;
            }
            SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(submit.get("xml").toString()));
            String path = submit.get("path").toString();
            path = extractRelativePath(path);
            String endOfPath = new File(path).getName();
            String startPath = new File(path).getParent().replaceAll("\\\\", "/");
            if ("/".equals(startPath)) {
                startPath = "";
            }
            Matcher mat = hotFolderRegExPattern.matcher(endOfPath);
            mat.matches();
            String type = mat.group(3);
            String name = mat.group(2);
            boolean start = true;
            String nodeQuery = "//add_order[@at]";
            Node runtime;
            if ("order".equals(type)) {
                Node atNode = xpath.selectSingleNode(nodeQuery);
                nodeQuery = "//run_time";
                runtime = xpath.selectSingleNode(nodeQuery);
                if (atNode == null && runtime == null) {
                    start = false;
                    getLog().debug1(
                            "processActionAndStart:" + name
                                    + " once=yes. Order will not be modified because 'no at and no run_time' is specified by <add_order>");
                }
            }
            nodeQuery = "//run_time[@repeat]";
            runtime = xpath.selectSingleNode(nodeQuery);
            if (runtime != null) {
                start = false;
                getLog().debug1("processActionAndStart:" + name + " once=yes. " + type + " will not be modified because 'repeat' is specified by <run_time>");
            }
            nodeQuery = "//run_time/period[@repeat]";
            runtime = xpath.selectSingleNode(nodeQuery);
            if (runtime != null && start) {
                start = false;
                getLog().debug1("processActionAndStart:" + name + " once=yes. " + type + " will not be started because 'repeat' is specified by <run_time>");
            }
            if (start) {
                String command = "";
                if ("order".equalsIgnoreCase(type)) {
                    String jobChain = startPath + "/" + mat.group(1);
                    command = "<modify_order at=\"now\" job_chain=\"" + jobChain + "\"  order=\"" + name + "\"/>";
                } else {
                    command = "<start_job job=\"" + name + "\" at=\"now\"/>";
                }
                executeCommandOnOneOrAllSchedulers(host, port, command);
            }
        } catch (Exception e) {
            throw new Exception("Error processing start request: " + e, e);
        }
    }

    private void executeCommandOnOneOrAllSchedulers(String host, String port, String command) throws Exception {
        try {
            ArrayList remoteSchedulers = new ArrayList();
            if ("0".equalsIgnoreCase(port) && "self".equalsIgnoreCase(host)) {
                try {
                    spooler.execute_xml(command);
                } catch (Exception e) {
                    getLogger().warn("Failed to send command: " + e);
                }
            } else if ("0".equalsIgnoreCase(port) && "all".equalsIgnoreCase(host)) {
                String answer = spooler.execute_xml("<show_state what=\"remote_schedulers\"/>");
                SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(answer));
                NodeList remoteSchedulerList = xpath.selectNodeList("/spooler/answer/state/remote_schedulers/remote_scheduler[@connected='yes']");
                for (int i = 0; i < remoteSchedulerList.getLength(); i++) {
                    Element rem = (Element) remoteSchedulerList.item(i);
                    String currHost = rem.getAttribute("hostname");
                    String currPort = rem.getAttribute("tcp_port");
                    int iCurrPort = Integer.parseInt(currPort);
                    RemoteScheduler sched = new RemoteScheduler(currHost, iCurrPort);
                    remoteSchedulers.add(sched);
                }
            } else {
                int iPort = Integer.parseInt(port);
                RemoteScheduler sched = new RemoteScheduler(host, iPort);
                remoteSchedulers.add(sched);
            }
            Iterator iter = remoteSchedulers.iterator();
            while (iter.hasNext()) {
                RemoteScheduler scheduler = (RemoteScheduler) iter.next();
                try {
                    scheduler.sendCommand(command);
                } catch (Exception e) {
                    getLogger().warn("Failed to send command: " + e);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error executing command: " + e, e);
        }
    }

    private void processActionTryOut(HashMap submit, String host, String port) throws Exception {
        try {
            if (startscript) {
                getLogger().debug1("TryOut request is skipped, because this is a startscript.");
                return;
            }
            String path = submit.get("path").toString();
            path = extractRelativePath(path);
            String endOfPath = new File(path).getName();
            String startPath = new File(path).getParent().replaceAll("\\\\", "/");
            if ("/".equals(startPath)) {
                startPath = "";
            }
            Matcher mat = hotFolderRegExPattern.matcher(endOfPath);
            mat.matches();
            String jobChain = startPath + "/" + mat.group(1);
            ByteArrayInputStream bai = new ByteArrayInputStream(submit.get("xml").toString().getBytes());
            InputSource source = new InputSource(bai);
            Document orderDoc = docBuilder.parse(source);
            Element orderElement = orderDoc.getDocumentElement();
            orderElement.removeAttribute("at");
            orderElement.removeAttribute("id");
            orderElement.setAttribute("job_chain", jobChain);
            NodeList orderChildren = orderElement.getChildNodes();
            for (int i = 0; i < orderChildren.getLength(); i++) {
                Node child = orderChildren.item(i);
                if ("run_time".equals(child.getNodeName())) {
                    orderElement.removeChild(child);
                }
            }
            StringWriter out = new StringWriter();
            OutputFormat format = new OutputFormat(orderDoc);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(orderDoc);
            out.close();
            executeCommandOnOneOrAllSchedulers(host, port, out.toString());
        } catch (Exception e) {
            throw new Exception("Error processing tryout request: " + e, e);
        }
    }

    private void writeXMLFile(Document xml, File file) throws Exception {
        try {
            getLogger().debug("writing " + file.getAbsolutePath());
            OutputStream fout = new FileOutputStream(file, false);
            OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8");
            OutputFormat format = new OutputFormat(xml);
            format.setEncoding("UTF-8");
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(xml);
            out.close();
            if (!isWindows() && !startscript) {
                spooler.execute_xml("<check_folders/>");
            }
        } catch (Exception e) {
            throw new Exception("Error writing xml file: " + e, e);
        }
    }

    private static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        boolean win = false;
        if (OS.indexOf("windows") > -1) {
            win = true;
        }
        return win;
    }

    private static final String extractRelativePath(String path) throws Exception {
        String relativePath = "";
        if (path.startsWith("/live/")) {
            relativePath = path.substring(4);
        }
        if (path.startsWith("/remote/")) {
            int index = path.indexOf('/', 8);
            relativePath = path.substring(index);
        }
        return relativePath;
    }

    private void logSubmit(HashMap submit) {
        try {
            String id = "";
            if (submit.get("id") != null) {
                id = submit.get("id").toString();
            }
            getLogger().debug7("Processing submit[" + id + "]:");
            Iterator iter = submit.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next().toString();
                if ("xml".equals(key)) {
                    getLogger().debug9("  " + key + ": " + submit.get(key));
                } else if (!"id".equals(key)) {
                    getLogger().debug7("  " + key + ": " + submit.get(key));
                }
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private void processDirs() throws Exception {
        dbCache = new HashMap();
        try {
            String sql = "SELECT o.\"ID\", o.\"SUSPENDED\", o.\"HASH\", t.\"PATH\", t.\"TYPE\", t.\"OWNER\", t.\"GROUP\", "
                    + "t.\"ID\" AS \"TREE_ID\", t.\"PERMISSION\" ";
            if (!(getConnection() instanceof SOSOracleConnection)) {
                sql += " FROM " + JobSchedulerManagedObject.tableManagedTree + " t LEFT OUTER JOIN " + " " + JobSchedulerManagedObject.tableManagedObjects
                        + " o ON o.\"ID\"=t.\"ITEM_ID\"" + " WHERE (t.\"LINK_ID\"=0 OR t.\"LINK_ID\" IS NULL) ";
            } else {
                sql += " FROM " + JobSchedulerManagedObject.tableManagedTree + " t," + " " + JobSchedulerManagedObject.tableManagedObjects + " o"
                        + " WHERE o.\"ID\"(+)=t.\"ITEM_ID\" ";
            }
            sql += "AND (t.\"PATH\" LIKE '/" + spooler.id() + "/%' OR t.\"PATH\"='/" + spooler.id() + "')";
            ArrayList dbContent = getConnection().getArray(sql);
            if (dbContent.isEmpty()) {
                createDBSupervisorDir();
            }
            Iterator dbIterator = dbContent.iterator();
            while (dbIterator.hasNext()) {
                HashMap node = (HashMap) dbIterator.next();
                String path = node.get("path").toString();
                String type = node.get("type").toString();
                char cType = type.charAt(0);
                switch (cType) {
                case 'd':
                case 'm':
                    break;
                case 'o':
                case 'i':
                case 'l':
                case 'c':
                case 'p':
                case 's':
                case 'v':
                    path = path + "." + getWhat(type) + ".xml";
                    break;
                case 'j':
                    path = path + ".job.xml";
                    break;
                case 't':
                    path = path + ".schedule.xml";
                    break;
                default:
                    continue;
                }
                dbCache.put(getPathWithoutSupervisor(path), node);
            }
        } catch (Exception e) {
            throw new Exception("Error creating local cache of managed objects in database: " + e, e);
        }
        try {
            processDir(remoteFolder, "/remote");
            processDir(liveFolder, "/live");
        } catch (Exception e) {
            throw new Exception("Error processing directories: " + e, e);
        }
    }

    private void processDir(File directory, String dbDir) {
        try {
            getLogger().debug7("Processing directoy " + dbDir);
            Object obj = dbCache.get(dbDir);
            if (obj == null) {
                getLogger().info("Directory " + dbDir + " does not exist in database. Creating directory...");
                createDBDir(dbDir);
            }
            Vector fileList = SOSFile.getFilelist(directory.getAbsolutePath(), ".*", java.util.regex.Pattern.CASE_INSENSITIVE);
            Iterator fileIterator = fileList.iterator();
            while (fileIterator.hasNext()) {
                File confFile = (File) fileIterator.next();
                processFile(confFile, dbDir + "/" + confFile.getName());
            }
            Vector folderList = SOSFile.getFolderlist(directory.getAbsolutePath(), ".*", 0, false);
            Iterator folderIterator = folderList.iterator();
            while (folderIterator.hasNext()) {
                File currentDir = (File) folderIterator.next();
                if (!currentDir.isDirectory()) {
                    continue;
                }
                processDir(currentDir, dbDir + "/" + currentDir.getName());
            }
        } catch (Exception e) {
            try {
                getLogger().warn("Failed to process directory " + directory.getAbsolutePath() + ": " + e);
            } catch (Exception f) {
            }
        }
    }

    private void processFile(File confFile, String dbPath) {
        try {
            getLogger().debug7("Processing file " + dbPath);
            Matcher mat = noSchedulerFileRegExPattern.matcher(confFile.getName());
            boolean schedulerFile = !mat.matches();
            if (schedulerFile || !isBinary(confFile)) {
                Object obj = dbCache.get(dbPath);
                if (obj == null) {
                    getLogger().info("Configuration file " + dbPath + " does not exist in database. Adding file...");
                    createDBFile(confFile, dbPath, schedulerFile);
                } else {
                    String fileHash = SOSCrypt.MD5encrypt(confFile);
                    HashMap dbObject = (HashMap) obj;
                    String dbHash = dbObject.get("hash").toString();
                    if (dbHash.equalsIgnoreCase(fileHash)) {
                        getLogger().debug9("File is unchanged: " + dbPath);
                    } else {
                        if (dbHash.isEmpty()) {
                            getLogger().debug1("File has been added to folder and database. Conflict->folder wins");
                        }
                        getLogger().info("Configuration file " + dbPath + " has changed. Updating database.");
                        updateDBFile(confFile, dbPath, fileHash, dbObject, schedulerFile);
                    }
                }
            }
        } catch (Exception e) {
            try {
                getLogger().warn("Failed to process file " + confFile.getAbsolutePath() + ": " + e);
            } catch (Exception f) {
            }
        }
    }

    private boolean isBinary(File file) throws Exception {
        try {
            boolean result = false;
            InputStream in = new FileInputStream(file);
            int curByte = in.read();
            while (curByte != -1 && !result) {
                if (curByte == 0) {
                    result = true;
                    break;
                }
                curByte = in.read();
            }
            in.close();
            if (result) {
                getLogger().debug5("File " + file.getAbsolutePath() + " is binary.");
            }
            return result;
        } catch (Exception e) {
            throw new Exception("Failed to check if file " + file.getAbsolutePath() + " is binary: " + e, e);
        }
    }

    private void updateDBFile(File confFile, String path, String hash, HashMap dbObject, boolean isSchedulerFile) throws Exception {
        try {
            String type = "";
            if (isSchedulerFile) {
                type = getType(confFile);
            } else {
                type = "m";
            }
            String sql = "UPDATE " + JobSchedulerManagedObject.tableManagedObjects + " SET \"TYPE\"='" + type + "', \"HASH\"='" + hash
                    + "', \"STATE\"=10 WHERE \"ID\"=" + dbObject.get("id").toString();
            getConnection().execute(sql);
            String fileContent = SOSFile.readFileUnicode(confFile);
            getConnection().updateClob(JobSchedulerManagedObject.tableManagedObjects, "XML", fileContent, "\"ID\"=" + dbObject.get("id").toString());
            if (isDatabaseInterfaceSupported) {
                spooler_log.debug1("live confFile: " + confFile.getAbsolutePath());
                spooler_log.debug1("live path: " + path);
                JobSchedulerLiveXml liveXml = new JobSchedulerLiveXml(this.getConnection(), this.getLogger(), confFile);
                liveXml.store(path.substring(6));
            }
            String treeID = (dbObject.get("tree_id") != null) ? dbObject.get("tree_id").toString() : "";
            if ("".equals(treeID)) {
                String name = path.replaceAll("\\\\", "/");
                if (isSchedulerFile) {
                    name = name.replaceAll("\\..*\\.xml$", "");
                }
                String dbPath = "/" + spooler.id() + name;
                treeID = this.getConnection().getSingleValue(
                        "SELECT \"ID\" FROM " + JobSchedulerManagedObject.tableManagedTree + " WHERE \"PATH\"='" + dbPath + "'");
            }
            if ("".equals(treeID)) {
                throw new Exception("element not found in tree: " + path);
            }
            sql = "UPDATE " + JobSchedulerManagedObject.tableManagedTree + " SET \"TYPE\"='" + type
                    + "', \"MODIFIED\"=%now, \"MODIFIED_BY\"='Managed Starter 3' WHERE \"ID\"=" + treeID;
            getConnection().execute(sql);
            getConnection().commit();
            int nextSubmissionID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_submissions.id");
            sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedSubmits
                    + " (\"ID\", \"OBJECT_ID\", \"TREE_ID\", \"PATH\", \"OLD_PATH\", \"SPOOLER_ID\", \"ACTION\", \"STATE\",  \"MODIFIED\", \"MODIFIED_BY\", "
                    + "\"HASH\") VALUES(" + nextSubmissionID + ", " + dbObject.get("id").toString() + ", " + treeID + ", '" + path + "', '" + path + "', '"
                    + spooler.id() + "', 'submit', 'stored', %now, 'Managed Starter 3', '" + hash + "')";
            getConnection().executeUpdate(sql);
            getConnection().commit();
            dbObject.put("hash", hash);
            dbObject.put("type", type);
        } catch (Exception e) {
            throw new Exception("Failed to update file in database: " + e, e);
        }
    }

    private String getType(File configFile) throws Exception {
        String type = "";
        try {
            Matcher mat = hotFolderRegExPattern.matcher(configFile.getName());
            mat.matches();
            String longType = mat.group(3);
            type = getType(longType);
            if (type.length() > 1) {
                throw new Exception(type);
            }
            if ("job".equals(longType) && isOrderJob(configFile)) {
                type = "j";
            }
            if ("schedule".equals(longType) && isSubstituteSchedule(configFile)) {
                type = "t";
            }
        } catch (Exception e) {
            throw new Exception("Failed to get type for file [" + configFile.getAbsolutePath() + "]: " + e);
        }
        return type;
    }

    private boolean isOrderJob(File configFile) throws Exception {
        SOSXMLXPath xp = new SOSXMLXPath(configFile.getAbsolutePath());
        String orderAtt = xp.selectSingleNodeValue("/job/@order");
        if (orderAtt != null && "yes".equalsIgnoreCase(orderAtt)) {
            getLogger().debug8("File " + configFile + " is orderjob.");
            return true;
        }
        getLogger().debug8("File " + configFile + " is independent job.");
        return false;
    }

    private boolean isSubstituteSchedule(File configFile) throws Exception {
        SOSXMLXPath xp = new SOSXMLXPath(configFile.getAbsolutePath());
        String orderAtt = xp.selectSingleNodeValue("/schedule/@substitute");
        if (orderAtt != null && !orderAtt.isEmpty()) {
            getLogger().debug8("File " + configFile + " is substitute schedule.");
            return true;
        }
        getLogger().debug8("File " + configFile + " is ordinary schedule.");
        return false;
    }

    private void createDBFile(File confFile, String path, boolean isSchedulerFile) throws Exception {
        try {
            String parentID = "0";
            if (confFile == null) {
                throw new Exception("why is confFile null?");
            }
            String name = confFile.getName();
            if (isSchedulerFile) {
                name = name.replaceAll("\\..*\\.xml$", "");
            }
            String owner = "0";
            String group = "0";
            String permission = "774";
            String fileHash = SOSCrypt.MD5encrypt(confFile);
            File dirFile = new File(path);
            String parentPath = dirFile.getParent().replaceAll("\\\\", "/");
            String dbPath = "/" + spooler.id() + parentPath + "/" + name;
            HashMap parent = (HashMap) dbCache.get(parentPath);
            if (parent == null) {
                throw new Exception("failed to find parent directory of " + path + " in database.");
            }
            parentID = (parent.get("tree_id") != null) ? parent.get("tree_id").toString() : "";
            if ("".equals(parentID)) {
                parentID = this.getConnection().getSingleValue(
                        "SELECT \"ID\" FROM " + JobSchedulerManagedObject.tableManagedTree + " WHERE \"PATH\"='" + "/" + spooler.id() + parentPath + "'");
            }
            owner = (parent.get("owner") != null) ? parent.get("owner").toString() : "";
            group = (parent.get("group") != null) ? parent.get("group").toString() : "";
            permission = (parent.get("permission") != null) ? parent.get("permission").toString() : "";
            int nextID = 0;
            int nextObjectID = 0;
            String type = "";
            if (isSchedulerFile) {
                type = getType(confFile);
            } else {
                type = "m";
            }
            nextID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_tree.id");
            nextObjectID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_objects.id");
            String sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedObjects + " (\"ID\", \"NAME\", \"TYPE\", \"HASH\", \"STATE\", \"REFERENCE\") "
                    + "VALUES(" + nextObjectID + ", '" + name + "', '" + type + "', '" + fileHash + "', 10, 0)";
            getConnection().execute(sql);
            String fileContent = SOSFile.readFileUnicode(confFile);
            getConnection().updateClob(JobSchedulerManagedObject.tableManagedObjects, "XML", fileContent, "\"ID\"=" + nextObjectID);
            if (isDatabaseInterfaceSupported) {
                spooler_log.debug1("live confFile: " + confFile.getAbsolutePath());
                spooler_log.debug1("live path: " + path);
                JobSchedulerLiveXml liveXml = new JobSchedulerLiveXml(this.getConnection(), this.getLogger(), confFile);
                liveXml.store(path.substring(6));
            }
            sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedTree
                    + " (\"ID\", \"PARENT\", \"LEAF\", \"TYPE\", \"ITEM_ID\", \"NAME\", \"OWNER\", \"GROUP\", \"PERMISSION\", \"CREATED\", \"CREATED_BY\", "
                    + "\"MODIFIED\", \"MODIFIED_BY\", \"PATH\") VALUES(" + nextID + ", " + parentID + ", 1, '" + type + "', " + nextObjectID + ", '" + name
                    + "', " + owner + ", " + group + ", " + permission + ", %now, 'Managed Starter 3', %now, 'Managed Starter 3', '" + dbPath + "')";
            getConnection().execute(sql);
            getConnection().commit();
            int nextSubmissionID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_submissions.id");
            sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedSubmits
                    + " (\"ID\", \"OBJECT_ID\", \"TREE_ID\", \"PATH\", \"OLD_PATH\", \"SPOOLER_ID\", \"ACTION\", \"STATE\",  \"MODIFIED\", \"MODIFIED_BY\", "
                    + "\"HASH\") VALUES(" + nextSubmissionID + ", " + nextObjectID + ", " + nextID + ", '" + path + "', '" + path + "', '" + spooler.id()
                    + "', 'submit', 'stored', %now, 'Managed Starter 3', '" + fileHash + "')";
            getConnection().executeUpdate(sql);
            getConnection().commit();
            HashMap newFile = new HashMap();
            newFile.put("id", "" + nextObjectID);
            newFile.put("suspended", "0");
            newFile.put("hash", fileHash);
            newFile.put("path", dbPath);
            newFile.put("type", type);
            newFile.put("owner", owner);
            newFile.put("group", group);
            newFile.put("tree_id", "" + nextID);
            newFile.put("permission", permission);
            dbCache.put(path, newFile);
        } catch (Exception e) {
            throw new Exception("Failed to create file in database: " + e, e);
        }
    }

    private void createDBSupervisorDir() throws Exception {
        try {
            getLogger().info("creating supervisor root dir...");
            String parentID = "1";
            String dir = "/" + spooler.id();
            String name = "";
            String owner = "0";
            String group = "0";
            String permission = "774";
            File dirFile = new File(dir);
            name = dirFile.getName();
            int nextID = 0;
            while (nextID < 50) {
                nextID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_tree.id");
            }
            String sql = "INSERT INTO "
                    + JobSchedulerManagedObject.tableManagedTree
                    + "(\"ID\", \"PARENT\", \"TYPE\", \"NAME\", \"OWNER\", \"GROUP\", \"PERMISSION\", \"CREATED\", \"CREATED_BY\", \"MODIFIED\", \"MODIFIED_BY\", "
                    + "\"PATH\") VALUES (" + nextID + ", " + parentID + ", 'd', '" + name + "', " + owner + ", " + group + ", " + permission
                    + ", %now, 'Managed Starter 3', %now, 'Managed Starter 3', '" + dir + "')";
            getConnection().execute(sql);
            getConnection().commit();
            HashMap newDir = new HashMap();
            newDir.put("id", "");
            newDir.put("suspended", "0");
            newDir.put("hash", "");
            newDir.put("path", dir);
            newDir.put("type", "d");
            newDir.put("owner", owner);
            newDir.put("group", group);
            newDir.put("tree_id", "" + nextID);
            newDir.put("permission", permission);
            dbCache.put("/", newDir);
        } catch (Exception e) {
            throw new Exception("Failed to create supervisor root directory: " + e.getMessage(), e);
        }
    }

    private void createDBDir(String dir) throws Exception {
        try {
            String parentID = "0";
            String name = dir;
            String owner = "0";
            String group = "0";
            String permission = "774";
            if (!"/".equals(dir)) {
                File dirFile = new File(dir);
                String parentPath = dirFile.getParent().replaceAll("\\\\", "/");
                name = dirFile.getName();
                HashMap parent = (HashMap) dbCache.get(parentPath);
                if (parent == null) {
                    throw new Exception("failed to find parent directory of " + dir + " in database.");
                }
                parentID = (parent.get("tree_id") != null) ? parent.get("tree_id").toString() : "";
                if ("".equals(parentID)) {
                    parentID = this.getConnection().getSingleValue(
                            "SELECT \"ID\" FROM " + JobSchedulerManagedObject.tableManagedTree + " WHERE \"PATH\"='" + "/" + spooler.id() + parentPath + "'");
                }
                if ("".equals(parentID)) {
                    parentID = "0";
                }
                owner = (parent.get("owner") != null) ? parent.get("owner").toString() : "0";
                group = (parent.get("group") != null) ? parent.get("group").toString() : "0";
                permission = (parent.get("permission") != null) ? parent.get("permission").toString() : "774";
            }
            int nextID = 0;
            nextID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_tree.id");
            String sql = "INSERT INTO "
                    + JobSchedulerManagedObject.tableManagedTree
                    + "(\"ID\", \"PARENT\", \"TYPE\", \"NAME\", \"OWNER\", \"GROUP\", \"PERMISSION\", \"CREATED\", \"CREATED_BY\", \"MODIFIED\", \"MODIFIED_BY\", "
                    + "\"PATH\") VALUES(" + nextID + ", " + parentID + ", 'd', '" + name + "', " + owner + ", " + group + ", " + permission
                    + ", %now, 'Managed Starter 3', %now, 'Managed Starter 3', '/" + spooler.id() + dir + "')";
            getConnection().execute(sql);
            getConnection().commit();
            HashMap newDir = new HashMap();
            newDir.put("id", "");
            newDir.put("suspended", "0");
            newDir.put("hash", "");
            newDir.put("path", "/" + spooler.id() + dir);
            newDir.put("type", "d");
            newDir.put("owner", owner);
            newDir.put("group", group);
            newDir.put("tree_id", "" + nextID);
            newDir.put("permission", permission);
            dbCache.put(dir, newDir);
        } catch (Exception e) {
            throw new Exception("Failed to create directory: " + e, e);
        }
    }

    private void processDatabase() throws Exception {
        try {
            Iterator iter = dbCache.keySet().iterator();
            while (iter.hasNext()) {
                String submitPath = (String) iter.next();
                File realPath = new File(liveFolder.getParentFile(), submitPath);
                if (!realPath.exists()) {
                    HashMap dbObject = (HashMap) dbCache.get(submitPath);
                    if (dbObject.get("hash") != null && !dbObject.get("hash").toString().isEmpty()) {
                        getLogger().debug1("File " + submitPath + " has been deleted in file system and will be deleted from database.");
                        deleteDBFile(dbObject, submitPath);
                        iter.remove();
                    } else {
                        getLogger().debug1("File " + submitPath + " is in database but has not yet been submitted.");
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error processing objects in database: " + e.getMessage(), e);
        }
    }

    private void deleteDBFile(HashMap dbObject, String submitPath) {
        try {
            String treeID = (dbObject.get("tree_id") != null) ? dbObject.get("tree_id").toString() : "0";
            if ("0".equals(treeID)) {
                spooler_log.debug1("no object found in database, ignoring delete request for file: " + submitPath);
                return;
            }
            String objectID = (dbObject.get("id") != null) ? dbObject.get("id").toString() : "";
            String action = "remove_dir";
            String sql = "DELETE FROM " + JobSchedulerManagedObject.tableManagedTree + " WHERE \"ID\"=" + treeID;
            getConnection().execute(sql);
            if (!objectID.isEmpty()) {
                action = "remove";
                sql = "DELETE FROM " + JobSchedulerManagedObject.tableManagedObjects + " WHERE \"ID\"=" + objectID;
                getConnection().execute(sql);
            }
            getConnection().commit();
            int nextSubmissionID = getConnectionSettings().getLockedSequence("scheduler", "counter", "scheduler_managed_submissions.id");
            sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedSubmits
                    + " (\"ID\", \"OBJECT_ID\", \"TREE_ID\", \"PATH\", \"OLD_PATH\", \"SPOOLER_ID\", \"ACTION\", \"STATE\",  \"MODIFIED\", \"MODIFIED_BY\") "
                    + " VALUES(" + nextSubmissionID + ", " + objectID + ", " + treeID + ", '" + submitPath + "', '" + submitPath + "', '" + spooler.id()
                    + "', '" + action + "', 'stored', %now, 'Managed Starter 3')";
            getConnection().executeUpdate(sql);
            getConnection().commit();
        } catch (Exception e) {
            String path = "";
            if (dbObject != null && dbObject.get("path") != null) {
                path = dbObject.get("path").toString();
            }
            try {
                getLogger().warn("Failed to delete file [" + path + "] from database: " + e);
            } catch (Exception f) {
            }
        }
    }

    public SOSLogger getLog() {
        return getLogger();
    }

    public boolean spooler_process() throws Exception {
        boolean orderJob = true;
        boolean includeInterface = false;
        try {
            if (spooler_task.params().value("include_interface") != null
                    && ("yes".equalsIgnoreCase(spooler_task.params().value("include_interface"))
                            || "true".equalsIgnoreCase(spooler_task.params().value("include_interface")) || "1".equals(spooler_task.params().value(
                            "include_interface")))) {
                includeInterface = true;
            }
            if (spooler_job.order_queue() != null) {
                Order order = spooler_task.order();
                orderParams = order.params();
                String action = orderParams.value("action");
                if (action == null) {
                    action = "";
                }
                if (orderParams.value("include_interface") != null
                        && ("yes".equalsIgnoreCase(orderParams.value("include_interface")) || "true".equalsIgnoreCase(orderParams.value("include_interface")) || "1"
                                .equals(orderParams.value("include_interface")))) {
                    includeInterface = true;
                }
                if ("read_submits".equalsIgnoreCase(action)) {
                    processSubmits();
                } else if ("read_interface".equalsIgnoreCase(action)) {
                    processLive();
                } else {
                    if (includeInterface) {
                        processLive();
                    }
                    processSubmits();
                    processDirs();
                    processDatabase();
                }
            } else {
                orderJob = false;
            }
            return orderJob;
        } catch (Exception e) {
            getLog().warn("Error occurred processing order: " + e.getMessage());
            spooler_task.order().setback();
            spooler_task.order().params().set_var("message", e.getMessage());
            spooler_task.end();
            return false;
        } finally {
            if (this.getConnection() != null) {
                try {
                    this.getConnection().rollback();
                } catch (Exception ex) {
                    // no error handling
                }
            }
        }
    }

    private void liveExport(HashMap rec) throws Exception {
        String object_name = getLiveValue(rec, "name");
        String object_path = getLiveValue(rec, "path");
        String object_type = getLiveValue(rec, "type");
        File currentFile = null;
        listOfElements = new LinkedHashSet();
        String selStr = "SELECT r.\"PK_ID\", r.\"PARENT_ID\", m.\"ELEMENT_PATH\", m.\"TABLE_NAME\", m.\"ELEMENT_NAME\", m.\"NESTING\"" + " FROM "
                + JobSchedulerManagedObject.getTableLiveObjectMetadata() + " m LEFT JOIN " + JobSchedulerManagedObject.getTableLiveObjectReferences() + " r"
                + " ON m.\"ELEMENT_PATH\"=r.\"OBJECT_PATH\" " + " WHERE r.\"OBJECT_ID\"=" + getLiveValue(rec, "object_id") + " ORDER BY m.\"ORDERING\"";
        ArrayList arrayList = new ArrayList();
        arrayList = this.getConnection().getArray(selStr);
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
        spooler_log.debug3("liveExport: " + xml);
        SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(xml));
        String jobChainName = xpath.selectSingleNodeValue("//order/@job_chain");
        File currentRoot = liveFolder.getParentFile();
        getLogger().debug7("current root folder: " + currentRoot.getAbsolutePath());
        if (jobChainName != null && !jobChainName.isEmpty()) {
            xml = xml.replaceFirst(" job_chain=\"" + jobChainName + "\"", "");
            currentFile = new File(liveFolder + "/" + object_path, jobChainName + "," + object_name + "." + object_type + ".xml");
        } else {
            currentFile = new File(liveFolder + "/" + object_path, object_name + "." + object_type + ".xml");
        }
        getLogger().debug7("current file: " + currentFile.getAbsolutePath());
        if (!currentFile.exists() && !currentFile.getParentFile().exists()) {
            getLogger().debug1("Creating directory " + currentFile.getParent());
            if (!currentFile.getParentFile().mkdirs()) {
                throw new Exception("Failed to create directory " + currentFile.getParent());
            }
        }
        updateLiveFile(xml, object_path, currentFile, "");
        getLog().info("Object synchronized from database interface: " + object_path + "/" + currentFile.getName());
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
        org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        String xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?> ";
        xml += "<" + rootElement.element_name + " " + rootElement.attribute + "></" + rootElement.element_name + ">";
        spooler_log.debug3(xml);
        org.jdom.Document parentDoc = builder.build(new java.io.StringReader(xml));
        org.jdom.Element root = parentDoc.getRootElement();
        it = listOfElements.iterator();
        element = (JobSchedulerMetadataElement) it.next();
        String s = "";
        org.jdom.Element aktEle = root;
        while (it.hasNext()) {
            element = (JobSchedulerMetadataElement) it.next();
            if (element.nesting != -1) {
                aktEle = liveAddMissingNodes(root, element);
                s = element.element_name;
                org.jdom.Element ele = new org.jdom.Element(s);
                setLiveXmlAttribute(element, ele);
                aktEle.addContent(ele);
                element.nesting = -1;
                liveAddDependendNodes(element, root, aktEle);
            }
        }
        org.jdom.output.XMLOutputter output = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
        erg = output.outputString(root);
        erg = erg.replaceAll("pk_id=\"[0-9]*\"", "");
        return erg;
    }

    private boolean livePathContains(JobSchedulerMetadataElement element, JobSchedulerMetadataElement el) {
        boolean erg = true;
        int eSize = element.elements.size();
        int pSize = el.elements.size();
        int minSize = pSize;
        if (eSize < pSize) {
            minSize = eSize;
        }
        for (int i = 0; i < minSize; i++) {
            if (!element.elements.get(i).toString().equals(el.elements.get(i).toString())) {
                spooler_log.debug3("--->" + element.elements.get(i) + "<>" + el.elements.get(i));
                erg = false;
            }
        }
        return erg;
    }

    private void liveAddDependendNodes(JobSchedulerMetadataElement parent, org.jdom.Element root, org.jdom.Element aktEle) {
        spooler_log.debug3("looking for depending nodes of:" + aktEle.getName());
        Iterator it = listOfElements.iterator();
        JobSchedulerMetadataElement element = (JobSchedulerMetadataElement) it.next();
        String s = "";
        while (it.hasNext()) {
            element = (JobSchedulerMetadataElement) it.next();
            if (element.nesting != -1 && element.parent_id.equals(parent.pkid) && livePathContains(element, parent)) {
                aktEle = liveAddMissingNodes(root, element);
                s = element.element_name;
                spooler_log.debug3("adding depending node:" + s + "-->" + aktEle.getName());
                org.jdom.Element ele = new org.jdom.Element(s);
                setLiveXmlAttribute(element, ele);
                aktEle.addContent(ele);
                element.nesting = -1;
                liveAddDependendNodes(element, root, ele);
            }
        }
    }

    private org.jdom.Element liveAddMissingNodes(org.jdom.Element root, JobSchedulerMetadataElement element) {
        org.jdom.Element aktEle = root;
        String s = "";
        for (int i = 1; i < element.nesting; i++) {
            s = element.elements.get(i).toString();
            spooler_log.debug3("checking " + s);
            if (aktEle.getChild(s) == null) {
                spooler_log.debug3("adding:" + s);
                org.jdom.Element ele = new org.jdom.Element(s);
                aktEle.addContent(ele);
                aktEle = ele;
            } else {
                List children = aktEle.getChildren(s);
                if (children.size() == 1) {
                    aktEle = aktEle.getChild(s);
                } else {
                    Iterator itChildren = children.iterator();
                    while (itChildren.hasNext()) {
                        org.jdom.Element node = (org.jdom.Element) itChildren.next();
                        List l = node.getAttributes();
                        Iterator itAttr = l.iterator();
                        while (itAttr.hasNext()) {
                            org.jdom.Attribute attr = (org.jdom.Attribute) itAttr.next();
                            if ("pk_id".equals(attr.getName()) && (attr.getValue().equals(element.parent_id))) {
                                aktEle = node;
                            }
                        }
                    }
                }
            }
        }
        return aktEle;
    }

    private void setLiveXmlAttribute(JobSchedulerMetadataElement element, org.jdom.Element ele) {
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
            String selStr = "SELECT * FROM " + element.table_name + " WHERE \"PK_ID\" = " + element.pkid;
            ArrayList arrayList = new ArrayList();
            arrayList = this.getConnection().getArray(selStr);
            Iterator resultset = arrayList.iterator();
            if (resultset.hasNext()) {
                rec = (HashMap) resultset.next();
                Iterator fieldnames = rec.keySet().iterator();
                while (fieldnames.hasNext()) {
                    String f = fieldnames.next().toString();
                    if (!"object_path".equals(f) && !"cdata".equals(f) && !"parent_id".equals(f) && !"object_id".equals(f) && rec.get(f) != null
                            && !"".equals(rec.get(f))) {
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

    public static void main(String args[]) throws Exception {
        // nothing
    }

}