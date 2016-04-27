package sos.scheduler.managed;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.util.SOSArguments;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

public class ManagedConverter {

    private static final Logger LOGGER = Logger.getLogger(ManagedConverter.class);
    private SOSConnection oldConnection;
    private SOSConnection newConnection;
    private SOSConnectionSettings settings;
    private boolean sameConnection = true;
    private SOSLogger logger;

    private final JobChainMapping[] mappings = { new JobChainMapping("executable_files", "launch_executable_file", "executable file orders"),
            new JobChainMapping("database_statements", "launch_database_statement", "database statement orders"),
            new JobChainMapping("database_reports", "launch_database_report", "database report orders"),
            new JobChainMapping("custom_reports", "launch_custom_report", "custom report orders"),
            new JobChainMapping("executable_php_files", "launch_executable_php_file", "executable php file orders") };

    private class JobChainMapping {

        public String managed1Name;
        public String managed2Name;
        public String treeTitle;

        public JobChainMapping(String managed1Name, String managed2Name, String treeTitle) {
            this.managed1Name = managed1Name;
            this.managed2Name = managed2Name;
            this.treeTitle = treeTitle;
        }
    }

    public ManagedConverter(SOSLogger log) {
        logger = log;
    }

    public static void main(String[] args) {
        if (args.length == 0 || "-?".equals(args[0]) || "/?".equals(args[0]) || "-h".equals(args[0])) {
            showUsage();
            System.exit(0);
        }
        try {
            SOSArguments arguments = new SOSArguments(args);
            SOSLogger sosLogger;
            String sourceFile = "";
            String targetFile = "";
            File source = null;
            File target = null;
            int logLevel = 0;
            String folderName = "Import";
            String logFile = "";
            try {
                sourceFile = arguments.as_string("-source=", "");
                targetFile = arguments.as_string("-target=");
                logLevel = arguments.as_int("-v=", SOSStandardLogger.INFO);
                logFile = arguments.as_string("-log=", "");
                folderName = arguments.as_string("-folder=", folderName);
            } catch (Exception e1) {
                LOGGER.debug(e1.getMessage(), e1);
                showUsage();
                System.exit(0);
            }
            if (logLevel == 0) {
                logLevel = SOSLogger.INFO;
            }
            if (!logFile.isEmpty()) {
                sosLogger = new SOSStandardLogger(logFile, logLevel);
            } else {
                sosLogger = new SOSStandardLogger(logLevel);
            }
            arguments.check_all_used();
            ManagedConverter conv = new ManagedConverter(sosLogger);
            target = new File(targetFile);
            if (!sourceFile.isEmpty()) {
                source = new File(sourceFile);
            }
            conv.convert(source, target, folderName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void showUsage() {
        LOGGER.debug("usage:ManagedConverter ");
        LOGGER.debug("Arguments:");
        LOGGER.debug("     -target=         factory.ini file containing the target database connection settings");
        LOGGER.debug("     -source=         factory.ini file containing the source database connection settings");
        LOGGER.debug("                      (only if different from target database)");
        LOGGER.debug("     -v=              loglevel (optional) [0=info] [1=debug1]...[9=debug9]");
        LOGGER.debug("     -log=            log file (optional)");
        LOGGER.debug("     -folder=         folder name in Managed Jobs 2 for the imported jobs (optional)");
    }

    private static void test() {
        try {
            SOSLogger log = new SOSStandardLogger(SOSLogger.DEBUG9);
            ManagedConverter conv = new ManagedConverter(log);
            File source = new File("J:/E/java/al/sos.scheduler/managed.new/factory_scheduler_sos.ini");
            File target = new File("J:/E/java/al/sos.scheduler/managed.new/factory_managed2.ini");
            conv.convert(source, target, "al_test");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void convert(File factoryIniSource, File factoryIniTarget, String branch) {
        try {
            newConnection = getDBConnection(factoryIniTarget.getAbsolutePath(), logger);
            newConnection.connect();
            oldConnection = newConnection;
            if (factoryIniSource != null) {
                oldConnection = getDBConnection(factoryIniSource.getAbsolutePath(), logger);
                oldConnection.connect();
                sameConnection = false;
            }
            settings = new SOSConnectionSettings(newConnection, "SETTINGS", logger);
            if (!sameConnection) {
                convertJobTypes();
                convertConnections();
            }
            int branchId = initTree(branch);
            convertIndependentJobs(branchId);
            convertJobChains(branchId);
            convertDefaultChainOrders(branchId);
            newConnection.commit();
        } catch (Exception e) {
            try {
                logger.error(e.toString());
            } catch (Exception f) {
            }
        } finally {
            try {
                if (newConnection != null) {
                    newConnection.rollback();
                    newConnection.disconnect();
                }
                if (!sameConnection && oldConnection != null) {
                    oldConnection.disconnect();
                }
            } catch (Exception e) {
            }
        }
    }

    protected static SOSConnection getDBConnection(String settingsFile, SOSLogger sosLogger) throws Exception {
        SOSProfileSettings settings = new SOSProfileSettings(settingsFile);
        Properties props = settings.getSection("spooler");
        if (props.isEmpty()) {
            throw new Exception("no settings found in section [spooler] of configuration file: " + settingsFile);
        }
        if (props.getProperty("db") == null || props.getProperty("db").isEmpty()) {
            throw new Exception("no settings found for entry [db] in section [spooler] of configuration file: " + settingsFile);
        }
        if (props.getProperty("db_class") == null || props.getProperty("db_class").isEmpty()) {
            throw new Exception("no settings found for entry [db_class] in section [spooler] of configuration file: " + settingsFile);
        }
        if (sosLogger != null) {
            sosLogger.debug6("connecting to database.. .");
        }
        String dbProperty = props.getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
        dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
        SOSArguments dbArguments = new SOSArguments(dbProperty);
        SOSConnection conn = SOSConnection.createInstance(props.getProperty("db_class"), dbArguments.as_string("-class=", ""),
                dbArguments.as_string("-url=", ""), dbArguments.as_string("-user=", ""), dbArguments.as_string("-password=", ""), sosLogger);
        return conn;
    }

    private int initTree(String branch) throws Exception {
        try {
            String count = newConnection.getSingleValue("SELECT count(*) FROM " + JobSchedulerManagedObject.tableManagedTree
                    + " WHERE \"PARENT\"=1 AND \"TYPE\"='d' AND \"NAME\"='" + branch + "'");
            if ("0".equalsIgnoreCase(count)) {
                return createDirectoryNode(1, branch);
            } else {
                throw new Exception("Node /" + branch + " already exists.");
            }
        } catch (Exception e) {
            throw new Exception("Error initializing tree: " + e);
        }
    }

    private void convertIndependentJobs(int parent) throws Exception {
        logger.info("converting independent jobs...");
        try {
            String sql = "SELECT j.\"ID\", j.\"TITLE\", j.\"JOB_NAME\", j.\"MODEL\", j.\"INPUT_LEVEL\", j.\"SPOOLER_ID\","
                    + " j.\"OUTPUT_LEVEL\", j.\"ERROR_LEVEL\", j.\"JOB_LOCK\", j.\"TIMEOUT\", j.\"PRIORITY\","
                    + " j.\"TASKS\", j.\"IDLE_TIMEOUT\", j.\"MIN_TASKS\", j.\"FORCE_IDLE_TIMEOUT\"," + " j.\"SUSPENDED\", j.\"JOB_TYPE\" FROM  "
                    + JobSchedulerManagedObject.getTableManagedJobs() + " j" + " WHERE \"MODEL\"=0";
            ArrayList independentJobs = oldConnection.getArray(sql);
            if (independentJobs != null && !independentJobs.isEmpty()) {
                logger.debug3("Found " + independentJobs.size() + " independent Jobs.");
                int independParent = createDirectoryNode(parent, "Independent Jobs");
                Iterator iter = independentJobs.iterator();
                while (iter.hasNext()) {
                    HashMap next = (HashMap) iter.next();
                    convertJob(independParent, next);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error converting independant jobs: " + e);
        }
    }

    private void convertJob(int parent, HashMap currJob) throws Exception {
        String name = currJob.get("job_name").toString();
        String title = "";
        String nameInTree = "";
        boolean orderJob = false;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document jobDocument = docBuilder.newDocument();
            Element jobElement = jobDocument.createElement("job");
            if (currJob.get("job_name") != null) {
                jobElement.setAttribute("name", currJob.get("job_name").toString());
                nameInTree = currJob.get("job_name").toString();
            }
            if (currJob.get("title") != null) {
                jobElement.setAttribute("title", currJob.get("title").toString());
                title = currJob.get("title").toString();
                nameInTree = title;
            }
            if (currJob.get("model") == null || currJob.get("model").toString().isEmpty() || "0".equals(currJob.get("model").toString())) {
                jobElement.setAttribute("order", "no");
            } else {
                jobElement.setAttribute("order", "yes");
                orderJob = true;
            }
            if (currJob.get("timeout") != null) {
                long timeout = Long.parseLong(currJob.get("timeout").toString());
                if (timeout > 0) {
                    jobElement.setAttribute("timeout", currJob.get("timeout").toString());
                }
            }
            if (currJob.get("priority") != null) {
                long priority = Long.parseLong(currJob.get("priority").toString());
                if (priority > 0) {
                    jobElement.setAttribute("priority", currJob.get("priority").toString());
                }
            }
            if (currJob.get("tasks") != null) {
                String tasks = currJob.get("tasks").toString();
                if (!tasks.isEmpty()) {
                    jobElement.setAttribute("tasks", tasks);
                }
            }
            if (currJob.get("idle_timeout") != null) {
                long idle_timeout = Long.parseLong(currJob.get("idle_timeout").toString());
                if (idle_timeout > 0) {
                    jobElement.setAttribute("idle_timeout", currJob.get("idle_timeout").toString());
                }
            }
            if (currJob.get("min_tasks") != null) {
                long min_tasks = Long.parseLong(currJob.get("min_tasks").toString());
                if (min_tasks > 0) {
                    jobElement.setAttribute("min_tasks", currJob.get("min_tasks").toString());
                }
            }
            if (currJob.get("force_idle_timeout") != null) {
                int fit = Integer.parseInt(currJob.get("force_idle_timeout").toString());
                if (fit == 0) {
                    jobElement.setAttribute("force_idle_timeout", "no");
                } else {
                    jobElement.setAttribute("force_idle_timeout", "yes");
                }
            }
            String jobDescription = oldConnection.getClob("SELECT \"DESCRIPTION\" FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"ID\"="
                    + currJob.get("id"));
            if (jobDescription != null && !jobDescription.isEmpty()) {
                Element descriptionElement = jobDocument.createElement("description");
                Text textNode = jobDocument.createTextNode(jobDescription);
                descriptionElement.appendChild(textNode);
                jobElement.appendChild(descriptionElement);
            }
            String jobParams = oldConnection.getClob("SELECT \"PARAMS\" FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"ID\"="
                    + currJob.get("id"));
            if (jobParams != null && !jobParams.isEmpty()) {
                Document paramsDocument = docBuilder.parse(new ByteArrayInputStream(jobParams.getBytes()));
                jobElement.appendChild(jobElement.getOwnerDocument().importNode(paramsDocument.getDocumentElement(), true));
            }
            String jobScript = oldConnection.getClob("SELECT \"SCRIPT\" FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"ID\"="
                    + currJob.get("id"));
            if (jobScript == null || jobScript.isEmpty()) {
                logger.warn("no job script found for managed job: " + currJob.get("id"));
                return;
            }
            Document scriptDocument = docBuilder.parse(new ByteArrayInputStream(jobScript.getBytes()));
            jobElement.appendChild(jobElement.getOwnerDocument().importNode(scriptDocument.getDocumentElement(), true));
            String jobMonitor = null;
            try {
                jobMonitor = oldConnection.getClob("SELECT \"MONITOR_SCRIPT\" FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"ID\"="
                        + currJob.get("id"));
            } catch (Exception e) {
                logger.debug9("Table " + JobSchedulerManagedObject.getTableManagedJobs() + " does not have column \"MONITOR_SCRIPT\".");
            }
            if (jobMonitor == null || jobMonitor.isEmpty()) {
                if (logger != null) {
                    logger.debug6("no monitor script found for managed job: " + currJob.get("id"));
                }
            } else {
                Document monitorDocument = docBuilder.parse(new ByteArrayInputStream(jobMonitor.getBytes()));
                Element monitor = monitorDocument.getDocumentElement();
                Node script = monitor.getFirstChild();
                if (script != null && script.getNodeType() == Node.ELEMENT_NODE) {
                    logger.debug9("Found script element.");
                    Element eScript = (Element) script;
                    String language = eScript.getAttribute("language");
                    if (!language.isEmpty()) {
                        logger.debug9("Monitor Script language: " + language);
                    }
                }
                jobElement.appendChild(jobElement.getOwnerDocument().importNode(monitorDocument.getDocumentElement(), true));
            }
            String jobRuntime = oldConnection.getClob("SELECT \"RUN_TIME\" FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"ID\"="
                    + currJob.get("id"));
            if (jobRuntime != null && !jobRuntime.isEmpty()) {
                String dummyRuntime = "<dummy>" + jobRuntime + "</dummy>";
                Document dummyDocument = docBuilder.parse(new ByteArrayInputStream(dummyRuntime.getBytes()));
                Element dummyElement = dummyDocument.getDocumentElement();
                for (Node child = dummyElement.getFirstChild(); child != null; child = child.getNextSibling()) {
                    jobElement.appendChild(jobElement.getOwnerDocument().importNode(child, true));
                }
            }
            jobDocument.appendChild(jobElement);
            String xmlString = dom2String(jobDocument);
            String spoolerID = "NULL";
            if (currJob.get("spooler_id") != null && !currJob.get("spooler_id").toString().isEmpty()) {
                spoolerID = "'" + currJob.get("spooler_id").toString() + "'";
            }
            char managedType = 'i';
            if (orderJob) {
                managedType = 'j';
            }
            int objectId = settings.getLockedSequence("scheduler", "counter", "scheduler_managed_objects.id");
            String sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedObjects
                    + " ( \"ID\", \"NAME\", \"TITLE\", \"TYPE\", \"SUSPENDED\", \"JOB_TYPE\", \"SPOOLER_ID\" )" + "  VALUES (" + objectId + ", '" + name
                    + "', '" + title + "', '" + managedType + "', " + currJob.get("suspended").toString() + ", '" + currJob.get("job_type").toString() + "', "
                    + spoolerID + " )";
            newConnection.executeUpdate(sql);
            newConnection.updateClob(JobSchedulerManagedObject.tableManagedObjects, "XML", xmlString, "\"ID\"=" + objectId);
            createNode(parent, managedType, nameInTree, true, objectId);
        } catch (Exception e) {
            throw new Exception("Error occured initializing job \"" + name + "\": " + e);
        }
    }

    private void convertJobChains(int parent) throws Exception {
        logger.info("converting job chains");
        try {
            String omitChains = "";
            for (int i = 0; i < mappings.length; i++) {
                omitChains += "'" + mappings[i].managed1Name + "'";
                if (i + 1 < mappings.length) {
                    omitChains += ",";
                }
            }
            String sql = "SELECT \"ID\", \"TITLE\", \"NAME\", \"SUSPENDED\", \"SPOOLER_ID\" FROM " + JobSchedulerManagedObject.getTableManagedModels()
                    + " WHERE \"ID\">0 AND " + "\"NAME\" NOT IN (" + omitChains + ") ";
            ArrayList jobChains = oldConnection.getArray(sql);
            if (jobChains != null && !jobChains.isEmpty()) {
                logger.debug3("Found " + jobChains.size() + " job chains.");
                Iterator iter = jobChains.iterator();
                while (iter.hasNext()) {
                    HashMap next = (HashMap) iter.next();
                    convertJobChain(parent, next);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error converting independant jobs: " + e);
        }
    }

    private void convertJobChain(int parent, HashMap chain) throws Exception {
        String jobChainName = chain.get("name").toString();
        String jobChainTitle = jobChainName;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document jobChainDocument = docBuilder.newDocument();
            Element jobChainElement = jobChainDocument.createElement("job_chain");
            jobChainElement.setAttribute("name", chain.get("name").toString());
            String sql = "SELECT j.\"ID\", j.\"TITLE\", j.\"JOB_NAME\", j.\"MODEL\", j.\"INPUT_LEVEL\", j.\"SPOOLER_ID\","
                    + " j.\"OUTPUT_LEVEL\", j.\"ERROR_LEVEL\", j.\"JOB_LOCK\", j.\"TIMEOUT\", j.\"PRIORITY\","
                    + " j.\"TASKS\", j.\"IDLE_TIMEOUT\", j.\"MIN_TASKS\", j.\"FORCE_IDLE_TIMEOUT\"," + " j.\"SUSPENDED\", j.\"JOB_TYPE\" FROM  "
                    + JobSchedulerManagedObject.getTableManagedJobs() + " j" + " WHERE \"MODEL\"=" + chain.get("id").toString();
            ArrayList currJobs = new ArrayList();
            currJobs = oldConnection.getArray(sql);
            if (currJobs.isEmpty()) {
                logger.warn(".. no job found for this workflow model [" + chain.get("id").toString() + "]: " + chain.get("name"));
                return;
            }
            if (chain.get("title") != null && !chain.get("title").toString().isEmpty()) {
                jobChainTitle = chain.get("title").toString();
            }
            int jobChainParent = createDirectoryNode(parent, jobChainTitle);
            Iterator itJobs = currJobs.iterator();
            HashMap currJob = new HashMap();
            Set inputLevels = new HashSet();
            Set outputLevels = new HashSet();
            Set errorLevels = new HashSet();
            while (itJobs.hasNext()) {
                currJob = (HashMap) itJobs.next();
                if (currJob.containsKey("job_name") && currJob.containsKey("input_level") && currJob.containsKey("output_level")
                        && currJob.containsKey("error_level")) {
                    convertJob(jobChainParent, currJob);
                    outputLevels.add(currJob.get("output_level").toString());
                    inputLevels.add(currJob.get("input_level").toString());
                    errorLevels.add(currJob.get("error_level").toString());
                }
            }
            itJobs = currJobs.iterator();
            while (itJobs.hasNext()) {
                currJob = (HashMap) itJobs.next();
                if (currJob.containsKey("job_name") && currJob.containsKey("input_level") && currJob.containsKey("output_level")
                        && currJob.containsKey("error_level")) {
                    Element nodeElement = jobChainDocument.createElement("job_chain_node");
                    nodeElement.setAttribute("state", currJob.get("input_level").toString());
                    nodeElement.setAttribute("job", currJob.get("job_name").toString());
                    if (!currJob.get("output_level").toString().isEmpty() && inputLevels.contains(currJob.get("output_level").toString())) {
                        nodeElement.setAttribute("next_state", currJob.get("output_level").toString());
                    }
                    if (!currJob.get("error_level").toString().isEmpty() && inputLevels.contains(currJob.get("error_level").toString())) {
                        nodeElement.setAttribute("error_state", currJob.get("error_level").toString());
                    }
                    jobChainElement.appendChild(nodeElement);
                }
            }
            jobChainDocument.appendChild(jobChainElement);
            String xmlString = dom2String(jobChainDocument);
            int objectId = settings.getLockedSequence("scheduler", "counter", "scheduler_managed_objects.id");
            String spoolerID = "NULL";
            if (chain.get("spooler_id") != null && !chain.get("spooler_id").toString().isEmpty()) {
                spoolerID = "'" + chain.get("spooler_id").toString() + "'";
            }
            sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedObjects + " ( \"ID\", \"NAME\", \"TITLE\", \"TYPE\", \"SUSPENDED\", \"SPOOLER_ID\" )"
                    + "  VALUES (" + objectId + ", '" + jobChainName + "', '" + jobChainTitle + "', 'c', " + chain.get("suspended").toString() + ", "
                    + spoolerID + ")";
            newConnection.executeUpdate(sql);
            newConnection.updateClob(JobSchedulerManagedObject.tableManagedObjects, "XML", xmlString, "\"ID\"=" + objectId);
            createNode(jobChainParent, 'c', jobChainTitle, true, objectId);
            convertOrders(jobChainParent, jobChainName);
        } catch (Exception e1) {
            throw new Exception("Error occured creating job chain \"" + jobChainName + "\": " + e1);
        }
    }

    private String dom2String(Document dom) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(dom);
        trans.transform(source, result);
        String xmlString = sw.toString();
        return xmlString;
    }

    private void convertOrders(int parent, String model) throws Exception {
        try {
            String query = new String("SELECT o.\"ID\", o.\"SPOOLER_ID\", o.\"JOB_CHAIN\", o.\"ORDER_ID\"" + ", o.\"TITLE\", o.\"SUSPENDED\" " + " FROM "
                    + JobSchedulerManagedObject.getTableManagedOrders() + " o " + " WHERE o.\"JOB_CHAIN\"='" + model + "' ");
            ArrayList orders = oldConnection.getArray(query);
            Iterator iter = orders.iterator();
            while (iter.hasNext()) {
                HashMap next = (HashMap) iter.next();
                convertOrder(parent, next);
            }
        } catch (Exception e) {
            throw new Exception("Error occured converting orders for Job Chain \"" + model + "\": " + e, e);
        }
    }

    private void convertDefaultChainOrders(int parent) throws Exception {
        for (int i = 0; i < mappings.length; i++) {
            JobChainMapping map = mappings[i];
            try {
                String query = new String("SELECT o.\"ID\", o.\"SPOOLER_ID\", o.\"ORDER_ID\"" + ", o.\"TITLE\", 1 as \"SUSPENDED\", '" + map.managed2Name
                        + "' as \"JOB_CHAIN\"" + " FROM " + JobSchedulerManagedObject.getTableManagedOrders() + " o " + " WHERE o.\"JOB_CHAIN\"='"
                        + map.managed1Name + "' ");
                ArrayList orders = oldConnection.getArray(query);
                Iterator iter = orders.iterator();
                if (!orders.isEmpty()) {
                    logger.info("converting " + map.treeTitle + "...");
                    int dir = createDirectoryNode(parent, map.treeTitle);
                    while (iter.hasNext()) {
                        HashMap next = (HashMap) iter.next();
                        convertOrder(dir, next);
                    }
                }
            } catch (Exception e) {
                throw new Exception("Error occured converting orders for Job Chain \"" + map.managed1Name + "\": " + e, e);
            }
        }
    }

    private void convertOrder(int parent, HashMap order) throws Exception {
        String orderId = order.get("order_id").toString();
        String orderTitle = orderId;
        logger.debug3("converting order \"" + orderId + "\"...");
        try {
            String currentWorkflowModelName = order.get("job_chain").toString();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document orderDocument = docBuilder.newDocument();
            Element orderElement = orderDocument.createElement("add_order");
            orderElement.setAttribute("job_chain", currentWorkflowModelName);
            orderElement.setAttribute("id", orderId);
            orderElement.setAttribute("replace", "yes");
            if (order.get("title") != null && !order.get("title").toString().isEmpty()) {
                orderElement.setAttribute("title", order.get("title").toString());
                orderTitle = order.get("title").toString();
            }
            String key = order.get("id").toString();
            String payload = oldConnection.getClob("SELECT \"PARAMS\" FROM " + JobSchedulerManagedObject.getTableManagedOrders() + " WHERE \"ID\"=" + key);
            if (payload != null && !payload.isEmpty()) {
                Document payloadDocument = docBuilder.parse(new ByteArrayInputStream(payload.getBytes()));
                Node node = payloadDocument.getFirstChild();
                while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
                    node = node.getNextSibling();
                }
                if (node == null) {
                    throw new Exception("payload contains no xml elements");
                }
                Element payloadElement = (Element) node;
                if (!"params".equals(payloadElement.getNodeName())) {
                    throw new Exception("element <params> is missing");
                }
                orderElement.appendChild(orderDocument.importNode(payloadElement, true));
            }
            try {
                String runtime = oldConnection
                        .getClob("SELECT \"RUN_TIME\" FROM " + JobSchedulerManagedObject.getTableManagedOrders() + " WHERE \"ID\"=" + key);
                if (runtime != null && !runtime.isEmpty()) {
                    String runtimeXml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "<dummy>" + runtime + "</dummy>";
                    Document dummyDocument = docBuilder.parse(new ByteArrayInputStream(runtimeXml.getBytes()));
                    Node node = dummyDocument.getFirstChild();
                    while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
                        node = node.getNextSibling();
                    }
                    if (node == null) {
                        throw new Exception("runtime contains no xml elements");
                    }
                    Element dummyElement = (Element) node;
                    NodeList children = dummyElement.getChildNodes();
                    Node runtimeNode = null;
                    if (children != null) {
                        for (int i = 0; i < children.getLength() && runtimeNode == null; i++) {
                            Node nextChild = children.item(i);
                            if (nextChild != null && nextChild.getNodeType() == Node.ELEMENT_NODE && "run_time".equalsIgnoreCase(nextChild.getNodeName())) {
                                runtimeNode = nextChild;
                            }
                        }
                    }
                    if (runtimeNode != null) {
                        orderElement.appendChild(orderElement.getOwnerDocument().importNode(runtimeNode, true));
                    }
                }
            } catch (Exception e) {
                throw new Exception("an error occurred setting the runtime: " + e, e);
            }
            orderDocument.appendChild(orderElement);
            String xmlString = dom2String(orderDocument);
            int objectId = settings.getLockedSequence("scheduler", "counter", "scheduler_managed_objects.id");
            String spoolerID = "NULL";
            if (order.get("spooler_id") != null && !order.get("spooler_id").toString().isEmpty()) {
                spoolerID = "'" + order.get("spooler_id").toString() + "'";
            }
            String sql = "INSERT INTO " + JobSchedulerManagedObject.tableManagedObjects
                    + " ( \"ID\", \"NAME\", \"TITLE\", \"TYPE\", \"SUSPENDED\", \"SPOOLER_ID\" )" + "  VALUES (" + objectId + ", '" + orderId + "', '"
                    + orderTitle + "', 'o', " + order.get("suspended").toString() + ", " + spoolerID + ")";
            newConnection.executeUpdate(sql);
            newConnection.updateClob(JobSchedulerManagedObject.tableManagedObjects, "XML", xmlString, "\"ID\"=" + objectId);
            createNode(parent, 'o', orderTitle, true, objectId);
        } catch (Exception e) {
            throw new Exception("Error occured converting order \"" + orderId + "\": " + e, e);
        }
    }

    private int createDirectoryNode(int parent, String name) throws Exception {
        return createNode(parent, 'd', name, false, 0);
    }

    private int createNode(int parent, char type, String name, boolean leaf, int itemID) throws Exception {
        logger.debug1("Creating node \"" + name + "\", type '" + type + "'");
        int iLeaf = leaf ? 1 : 0;
        int treeId = settings.getLockedSequence("scheduler", "counter", "scheduler_managed_tree.id");
        String sql = "INSERT INTO "
                + JobSchedulerManagedObject.tableManagedTree
                + " (\"ID\", \"PARENT\", \"LEAF\", \"TYPE\", \"ITEM_ID\", \"NAME\", \"PERMISSION\", \"CREATED\", \"CREATED_BY\", \"MODIFIED\", \"MODIFIED_BY\")"
                + " VALUES(" + treeId + ", " + parent + "," + iLeaf + ", '" + type + "', " + itemID + ", '" + name
                + "', 777, %now, 'CONVERTER', %now, 'CONVERTER')";
        newConnection.executeUpdate(sql);
        return treeId;
    }

    private void convertJobTypes() throws Exception {
        try {
            logger.info("Converting job types...");
            ArrayList jobTypes = oldConnection.getArray("SELECT \"TYPE\", \"TITLE\" FROM " + JobSchedulerManagedObject.getTableManagedJobTypes());
            Iterator iter = jobTypes.iterator();
            while (iter.hasNext()) {
                HashMap jobType = (HashMap) iter.next();
                String type = jobType.get("type").toString();
                String title = jobType.get("title").toString();
                String count = newConnection.getSingleValue("SELECT COUNT(*) FROM " + JobSchedulerManagedObject.getTableManagedJobTypes() + " WHERE \"TYPE\"='"
                        + type + "'");
                if ("0".equalsIgnoreCase(count)) {
                    logger.debug2("Converting job type " + type);
                    newConnection.executeUpdate("INSERT INTO " + JobSchedulerManagedObject.getTableManagedJobTypes() + "(\"TYPE\", \"TITLE\") VALUES ('" + type
                            + "','" + title + "')");
                    byte[] script = oldConnection.getBlob("SELECT \"SCRIPT\" FROM " + JobSchedulerManagedObject.getTableManagedJobTypes() + " WHERE \"TYPE\"='"
                            + type + "'");
                    newConnection.updateBlob(JobSchedulerManagedObject.getTableManagedJobTypes(), "SCRIPT", script, "\"TYPE\"='" + type + "'");
                } else {
                    logger.debug2("Job type '" + type + "' already exists in target Database");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error occured converting job types: " + e);
        }
    }

    private void convertConnections() throws Exception {
        try {
            logger.info("Converting connections...");
            ArrayList connections = oldConnection.getArray("SELECT \"CONNECTION\", \"TITLE\", \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", "
                    + "\"CREATED\", \"CREATED_BY\", \"MODIFIED\", \"MODIFIED_BY\" FROM " + JobSchedulerManagedObject.getTableManagedConnections());
            Iterator iter = connections.iterator();
            while (iter.hasNext()) {
                HashMap connection = (HashMap) iter.next();
                String name = connection.get("connection").toString();
                String count = newConnection.getSingleValue("SELECT COUNT(*) FROM " + JobSchedulerManagedObject.getTableManagedConnections()
                        + " WHERE \"CONNECTION\"='" + name + "'");
                if ("0".equalsIgnoreCase(count)) {
                    logger.debug2("Converting connection " + name);
                    newConnection
                            .executeUpdate("INSERT INTO "
                                    + JobSchedulerManagedObject.getTableManagedConnections()
                                    + "(\"CONNECTION\", \"TITLE\", \"DRIVER\", \"CLASS\", \"URL\", \"USERNAME\", \"PASSWORD\", \"CREATED\", \"CREATED_BY\", \"MODIFIED\", \"MODIFIED_BY\") VALUES ("
                                    + "'" + name + "'," + "'" + connection.get("title").toString() + "'," + "'" + connection.get("driver").toString() + "',"
                                    + "'" + connection.get("class").toString() + "'," + "'" + connection.get("url").toString() + "'," + "'"
                                    + connection.get("username").toString() + "'," + "'" + connection.get("password").toString() + "'," + "'"
                                    + connection.get("created").toString() + "'," + "'" + connection.get("created_by").toString() + "'," + "'"
                                    + connection.get("modified").toString() + "'," + "'" + connection.get("modified_by").toString() + "')");
                } else {
                    logger.debug2("Connection '" + name + "' already exists in target Database");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error occured converting job types: " + e);
        }
    }

}