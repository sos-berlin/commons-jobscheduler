package sos.scheduler.managed.configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.net.SOSMail;
import sos.scheduler.misc.ParameterSubstitutor;
import sos.spooler.Monitor_impl;
import sos.spooler.Variable_set;
import sos.util.SOSFile;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;

/** <p>
 * ConfigurationBaseMonitor provides functions for Monitor-Scripts
 * </p>
 *
 * !!!! Attention: this program is using the xml-paylod for making the changes
 * persistent !!!!
 * 
 * die payload wird u.A. verwendet, um die configuration auch für agenten
 * verfügbar zu machen
 *
 *
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 */
public class ConfigurationBaseMonitor extends Monitor_impl {

    protected static final String conParamNameCONFIGURATION_FILE = "ConfigurationBaseMonitor.configuration_file";
    protected static final String conParamNameCONFIGURATION_PATH = "ConfigurationBaseMonitor.configuration_path";
    protected static final String conFileNameExtensionCONFIG_XML = ".config.xml";
    protected static final String conDefaultFileName4CACHE = "cache";
    private static final Logger LOGGER = Logger.getLogger(ConfigurationBaseMonitor.class);
    private static final String CLASSNAME = "ConfigurationBaseMonitor";
    private static final String XML_VERSION_1_0_ENCODING_ISO_8859_1 = "<?xml version='1.0' encoding='ISO-8859-1'?>";
    private static final String ATTRIBUTE_NAME_HIDDEN = "hidden";
    private static final String ATTRIBUTE_NAME_NAME = "name";
    private static final String ATTRIBUTE_NAME_VALUE = "value";
    private static final String VARIABLE_SCHEDULER_ORDER_ADDITIONAL_ENVVARS = "scheduler_order_additional_envvars";
    private static final String VARIABLE_NAME_SCHEDULER_ORDER_CONFIGURATION_LOADED = "ConfigurationBaseMonitor.scheduler_order_configuration_loaded";
    private static final String VARIABLE_NAME_GLOBAL_CONFIGURATION_PARAMS = "global_configuration_params";
    private static final String TAG_NAME_PARAM = "param";
    private static final String XPATH_PARAMS = "/params";
    private static final String XPATH_PARAMS_PARAM = XPATH_PARAMS + "/" + TAG_NAME_PARAM;
    private static final String SETTINGS_LOG_LEVEL = "//settings/log_level";
    private static final String SYSTEMPROPERTY_OS_NAME = "os.name";
    private static final String TRUE = "true";
    private static final String NAMED_ITEM_ENV = "env";
    private SOSSchedulerLogger sosLogger = null;
    private Document configuration = null;
    private String configurationPath = "";
    private String configurationFilename = "";
    private Vector<String> orderParameterKeys = null;
    private TreeMap<String, String> envvars = null;
    private TreeMap<String, String> additional_envvars = null;
    private boolean envVarsCaseSensitive = true;
    public static final String conVariableStartString = "${";
    public static final String conTypeBASENAME = "basename:";
    public static final String conVariableTypeBASENAME = conVariableStartString + conTypeBASENAME;
    public static final String conTypeFILE_CONTENT = "file_content:";
    public static final String conVariableTypeFILE_CONTENT = conVariableStartString + conTypeFILE_CONTENT;

    private String getOrderParam(final String pstrParamName) {
        if (spooler_task.order() != null) {
            String strT = spooler_task.order().params().value(pstrParamName);
            spooler_log.debug9(String.format("...getting %1$s = %2$s", pstrParamName, strT));
            if (strT != null) {
                return strT;
            }
        }
        return null;
    }

    private String setOrderParam(final String pstrParamName, final String pstrParamValue) {
        if (spooler_task.order() != null) {
            spooler_task.order().params().set_var(pstrParamName, pstrParamValue);
            spooler_log.debug3(String.format("...Setting %1$s = %2$s", pstrParamName, pstrParamValue));
        }
        return pstrParamValue;
    }

    public void initConfiguration() throws Exception {
        spooler_log.debug9("..initConfiguration");
        String paramNameCONFIGURATION_PATH = getOrderParam(conParamNameCONFIGURATION_PATH);
        String paramNameCONFIGURATION_FILE = getOrderParam(conParamNameCONFIGURATION_FILE);
        if (paramNameCONFIGURATION_PATH != null && !paramNameCONFIGURATION_PATH.isEmpty()) {
            this.setConfigurationPath(paramNameCONFIGURATION_PATH);
        }
        if (paramNameCONFIGURATION_FILE != null && !paramNameCONFIGURATION_FILE.isEmpty()) {
            this.setConfigurationFilename(paramNameCONFIGURATION_FILE);
        }
        this.initConfiguration(this.getConfigurationPath(), this.getConfigurationFilename());
    }

    private void initConfiguration(final String configurationPath1, final String configurationFilename1) throws Exception {
        this.setConfigurationPath(getOrderParam(conParamNameCONFIGURATION_PATH));
        spooler_log.debug3(String.format("--> configurationFilename1:%s ", configurationFilename1));
        spooler_log.debug3(String.format("--> configurationPath1:%s ", configurationPath1));
        if (configurationFilename1.startsWith(".") || configurationFilename1.startsWith("/") || configurationFilename1.startsWith("\\")
                || configurationFilename1.indexOf(":") > -1 || configurationPath1 == null || configurationPath1.isEmpty()) {
            this.initConfiguration(configurationFilename1);
        } else {
            this.initConfiguration(configurationPath1 + (!configurationPath1.endsWith("/") && !configurationPath1.endsWith("\\") ? "/" : "")
                    + configurationFilename1);
        }
    }

    public void initConfiguration(final String configurationFilename1) throws Exception {
        FileInputStream fis = null;
        try {
            if (configurationFilename1 == null || configurationFilename1.isEmpty()) {
                throw new JobSchedulerException(CLASSNAME + ": no configuration filename was specified");
            }
            JSFile objConfigFile = new JSFile(configurationFilename1);
            spooler_log.debug7(String.format("opened config file: %s", configurationFilename1));
            if (objConfigFile.exists()) {
                objConfigFile.canRead();
                String configurationBuffer = objConfigFile.getContent();
                spooler_log.debug9(String.format("...with content %s", configurationBuffer));
                objConfigFile.close();
                // Damit die Agenten auch eine Konfiguration haben. Evtl.
                // Dummy-Step als ersten Node einfügen.
                spooler_task.order().set_xml_payload(configurationBuffer);
            } else {
                // Es ist ok, dass die Datei fehlt
                this.getLogger().debug3(String.format(CLASSNAME + ": error occurred initializing configuration: File %s not found", configurationFilename1));
            }
        } catch (Exception e) {
            this.getLogger().warn(CLASSNAME + ": error occurred initializing configuration: " + e.getMessage());
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private String getJobChainName() {
        String strT = spooler_task.order().job_chain().name();
        return strT;
    }

    public Document prepareConfiguration() throws Exception {
        String nodeQuery = "";
        String payload = "";
        additional_envvars = null;
        try {
            orderParameterKeys = new Vector<String>();
            if (spooler_task.job().order_queue() != null) {
                this.setEnvVars();
                String env = "";
                boolean globalEnv = false;
                if (spooler_task.order().xml_payload() == null || spooler_task.order().xml_payload().isEmpty() || spooler_task.order().params() == null
                        || getOrderParam(VARIABLE_NAME_SCHEDULER_ORDER_CONFIGURATION_LOADED) == null
                        || getOrderParam(VARIABLE_NAME_SCHEDULER_ORDER_CONFIGURATION_LOADED).isEmpty()) {
                    spooler_log.debug3(".. call init from prepare");
                }
                this.initConfiguration();
                if (spooler_task.order().xml_payload() == null) {
                    throw new JobSchedulerException(CLASSNAME + ": monitor: no configuration was specified for this order: " + spooler_task.order().id());
                }
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(false);
                docFactory.setValidating(false);
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                payload = spooler_task.order().xml_payload();
                if (!payload.startsWith("<?xml ")) {
                    debugx(3, "Payload not starting with <?xml: " + payload);
                    debugx(3, String.format("Will add %s to payload", XML_VERSION_1_0_ENCODING_ISO_8859_1));
                    payload = XML_VERSION_1_0_ENCODING_ISO_8859_1 + payload;
                }
                debugx(3, "Payload: " + payload);
                if (!XML_VERSION_1_0_ENCODING_ISO_8859_1.equals(payload)) {
                    this.setConfiguration(docBuilder.parse(new ByteArrayInputStream(payload.getBytes())));
                    SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(payload));
                    NodeList nodeList = null;
                    NamedNodeMap nodeMapSettings = null;
                    nodeQuery = SETTINGS_LOG_LEVEL;
                    Node nodeSettings = xpath.selectSingleNode(nodeQuery);
                    if (nodeSettings != null) {
                        nodeMapSettings = nodeSettings.getAttributes();
                        if (nodeMapSettings != null && nodeMapSettings.getNamedItem(ATTRIBUTE_NAME_VALUE) != null) {
                            debugx(1, "Log Level is: " + nodeMapSettings.getNamedItem(ATTRIBUTE_NAME_VALUE).getNodeValue());
                            this.getLogger().setLogLevel(this.logLevel2Int(nodeMapSettings.getNamedItem(ATTRIBUTE_NAME_VALUE).getNodeValue()));
                        }
                    }
                    nodeQuery = "//job_chain[@name='" + getJobChainName() + "']/order";
                    debugx(9, "monitor: lookup order for job chain: " + nodeQuery + XPATH_PARAMS_PARAM);
                    Node nodeParams = xpath.selectSingleNode(nodeQuery + XPATH_PARAMS);
                    if (nodeParams == null || !nodeParams.hasChildNodes()) {
                        nodeQuery = "//application[@name='" + getJobChainName() + "']/order";
                        debugx(9, "lookup order query for application: " + nodeQuery);
                        nodeParams = xpath.selectSingleNode(nodeQuery + XPATH_PARAMS);
                    }
                    if (nodeParams != null && nodeParams.hasAttributes()) {
                        NamedNodeMap nodeMapParams = nodeParams.getAttributes();
                        if (nodeMapParams != null && nodeMapParams.getNamedItem(NAMED_ITEM_ENV) != null) {
                            env = nodeMapParams.getNamedItem(NAMED_ITEM_ENV).getNodeValue();
                            debugx(3, ".. parameter section with env=" + env + " found");
                            globalEnv = isTrue(env);
                        }
                    }
                    nodeList = xpath.selectNodeList(nodeQuery + XPATH_PARAMS_PARAM);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        String nodeValue = "";
                        String nodeName = "";
                        if (TAG_NAME_PARAM.equalsIgnoreCase(node.getNodeName())) {
                            NamedNodeMap nodeMap = node.getAttributes();
                            if (nodeMap != null && nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME) != null) {
                                nodeName = nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue();
                                if (nodeMap.getNamedItem(ATTRIBUTE_NAME_VALUE) != null) {
                                    nodeValue = nodeMap.getNamedItem(ATTRIBUTE_NAME_VALUE).getNodeValue();
                                } else {
                                    NodeList children = node.getChildNodes();
                                    for (int j = 0; j < children.getLength(); j++) {
                                        Node item = children.item(j);
                                        switch (item.getNodeType()) {
                                        case Node.TEXT_NODE:
                                        case Node.CDATA_SECTION_NODE:
                                            nodeValue += item.getNodeValue();
                                        }
                                    }
                                }
                                debugx(3, ".. monitor: global configuration parameter [" + nodeName + "]: " + nodeValue);
                                setOrderParam(nodeName, nodeValue);
                                if (globalEnv || nodeMap.getNamedItem(NAMED_ITEM_ENV) != null) {
                                    if (nodeMap.getNamedItem(NAMED_ITEM_ENV) != null) {
                                        env = nodeMap.getNamedItem(NAMED_ITEM_ENV).getNodeValue();
                                    }
                                    boolean setEnv = globalEnv || isTrue(env);
                                    if (setEnv) {
                                        if (additional_envvars == null) {
                                            additional_envvars = new TreeMap<String, String>();
                                        }
                                        additional_envvars.put(nodeName, nodeValue);
                                    }
                                }
                            }
                        }
                    }
                    if (additional_envvars != null) {
                        Iterator iter = additional_envvars.keySet().iterator();
                        String envNames = "";
                        while (iter.hasNext()) {
                            String envName = (String) iter.next();
                            envNames += envName;
                            if (iter.hasNext()) {
                                envNames += ";";
                            }
                        }
                        orderParameterKeys.add(VARIABLE_SCHEDULER_ORDER_ADDITIONAL_ENVVARS);
                        setOrderParam(VARIABLE_SCHEDULER_ORDER_ADDITIONAL_ENVVARS, envNames);
                    }
                    nodeQuery = "//job_chain[@name='" + spooler_task.order().job_chain().name() + "']/order/process[@state='" + spooler_task.order().state()
                            + "']";
                    debugx(9, "monitor: lookup order node query for job chain: " + nodeQuery + XPATH_PARAMS_PARAM);
                    nodeList = xpath.selectNodeList(nodeQuery + XPATH_PARAMS_PARAM);
                    nodeParams = xpath.selectSingleNode(nodeQuery + XPATH_PARAMS);
                    if (nodeList == null || nodeList.getLength() == 0) {
                        nodeQuery = "//application[@name='" + spooler_task.order().job_chain().name() + "']/order/process[@state='"
                                + spooler_task.order().state() + "']";
                        debugx(9, "monitor: lookup order node query for application: " + nodeQuery + XPATH_PARAMS_PARAM);
                        nodeList = xpath.selectNodeList(nodeQuery + XPATH_PARAMS_PARAM);
                        nodeParams = xpath.selectSingleNode(nodeQuery + XPATH_PARAMS);
                    }
                    globalEnv = false;
                    if (nodeParams != null && nodeParams.hasAttributes()) {
                        NamedNodeMap nodeMapParams = nodeParams.getAttributes();
                        if (nodeMapParams != null && nodeMapParams.getNamedItem(NAMED_ITEM_ENV) != null) {
                            env = nodeMapParams.getNamedItem(NAMED_ITEM_ENV).getNodeValue();
                            debugx(3, ".. parameter section with env=" + env + " found");
                            globalEnv = isTrue(env);
                        }
                    }
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        this.getLogger().debug7("---->" + node.getNodeName());
                        if (TAG_NAME_PARAM.equalsIgnoreCase(node.getNodeName())) {
                            NamedNodeMap nodeMap = node.getAttributes();
                            boolean hidden = false;
                            if (nodeMap.getNamedItem(ATTRIBUTE_NAME_HIDDEN) != null
                                    && TRUE.equalsIgnoreCase(nodeMap.getNamedItem(ATTRIBUTE_NAME_HIDDEN).getNodeValue())) {
                                hidden = true;
                            }
                            if (nodeMap != null && nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME) != null) {
                                String value = "";
                                if (nodeMap.getNamedItem(ATTRIBUTE_NAME_VALUE) != null) {
                                    value = nodeMap.getNamedItem(ATTRIBUTE_NAME_VALUE).getNodeValue();
                                } else {
                                    NodeList children = node.getChildNodes();
                                    for (int j = 0; j < children.getLength(); j++) {
                                        Node item = children.item(j);
                                        switch (item.getNodeType()) {
                                        case Node.TEXT_NODE:
                                        case Node.CDATA_SECTION_NODE:
                                            value += item.getNodeValue();
                                        }
                                    }
                                }
                                if (hidden) {
                                    debugx(3, ".. configuration parameter [" + nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue() + "]: *****");
                                } else {
                                    debugx(3, ".. configuration parameter [" + nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue() + "]: " + value);
                                }
                                setOrderParam(nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue(), value);
                                orderParameterKeys.add(nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue());
                                if (globalEnv || nodeMap.getNamedItem(NAMED_ITEM_ENV) != null) {
                                    if (nodeMap.getNamedItem(NAMED_ITEM_ENV) != null) {
                                        env = nodeMap.getNamedItem(NAMED_ITEM_ENV).getNodeValue();
                                    }
                                    boolean setEnv = globalEnv || isTrue(env);
                                    if (setEnv) {
                                        if (additional_envvars == null) {
                                            additional_envvars = new TreeMap<String, String>();
                                        }
                                        additional_envvars.put(nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue(), value);
                                    }
                                }
                            }
                        }
                    }
                }
                debugx(3, "Looking for order state parameters from order params");
                String state = spooler_task.order().state();
                String[] parameterNames = spooler_task.order().params().names().split(";");
                for (String parameterName : parameterNames) {
                    if (parameterName.startsWith(state + "/")) {
                        String parName = parameterName.replaceAll("^" + state + "/", "");
                        setOrderParam(parName, getOrderParam(parameterName));
                        orderParameterKeys.add(parName);
                    }
                }
                debugx(3, "Looking for global parameters from scheduler variables");
                Variable_set globalVariables = spooler.create_variable_set();
                globalVariables.merge(spooler.variables());
                globalVariables.merge(spooler_task.params());
                if (globalVariables.value(VARIABLE_NAME_GLOBAL_CONFIGURATION_PARAMS) != null
                        && !globalVariables.value(VARIABLE_NAME_GLOBAL_CONFIGURATION_PARAMS).isEmpty()) {
                    String globalFile = globalVariables.value(VARIABLE_NAME_GLOBAL_CONFIGURATION_PARAMS);
                    JSFile objFile = new JSFile(globalFile);
                    if (objFile.canRead()) {
                        getLogger().debug3("Reading global parameters from " + globalFile);
                        SOSXMLXPath globalXPath = new SOSXMLXPath(globalFile);
                        NodeList globalParams = globalXPath.selectNodeList("//params/param");
                        for (int i = 0; i < globalParams.getLength(); i++) {
                            Node node = globalParams.item(i);
                            if (TAG_NAME_PARAM.equalsIgnoreCase(node.getNodeName())) {
                                NamedNodeMap nodeMap = node.getAttributes();
                                boolean hidden = false;
                                if (nodeMap.getNamedItem(ATTRIBUTE_NAME_HIDDEN) != null
                                        && TRUE.equalsIgnoreCase(nodeMap.getNamedItem(ATTRIBUTE_NAME_HIDDEN).getNodeValue())) {
                                    hidden = true;
                                }
                                if (nodeMap != null && nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME) != null) {
                                    String value = "";
                                    if (nodeMap.getNamedItem(ATTRIBUTE_NAME_VALUE) != null) {
                                        value = nodeMap.getNamedItem(ATTRIBUTE_NAME_VALUE).getNodeValue();
                                    } else {
                                        NodeList children = node.getChildNodes();
                                        for (int j = 0; j < children.getLength(); j++) {
                                            Node item = children.item(j);
                                            switch (item.getNodeType()) {
                                            case Node.TEXT_NODE:
                                            case Node.CDATA_SECTION_NODE:
                                                value += item.getNodeValue();
                                            }
                                        }
                                    }
                                    String strT = nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue();
                                    if (hidden) {
                                        debugx(3, ".. configuration parameter [" + strT + "]: *****");
                                    } else {
                                        debugx(3, ".. configuration parameter [" + strT + "]: " + value);
                                    }
                                    globalVariables.set_var(strT, value);
                                    if (globalEnv || nodeMap.getNamedItem(NAMED_ITEM_ENV) != null) {
                                        if (nodeMap.getNamedItem(NAMED_ITEM_ENV) != null) {
                                            env = nodeMap.getNamedItem(NAMED_ITEM_ENV).getNodeValue();
                                        }
                                        boolean setEnv = globalEnv || isTrue(env);
                                        if (setEnv) {
                                            if (additional_envvars == null) {
                                                additional_envvars = new TreeMap();
                                            }
                                            additional_envvars.put(nodeMap.getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue(), value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                substituteOrderParamsInTaskParams();
                substitute(spooler_task.params(), globalVariables);
                substitute(spooler_task.order().params(), globalVariables);
                debugx(1, "Merged order parameters after substitutions:");
                ConfigurationBaseMonitor.logParameters(spooler_task.order().params(), this.getLogger());
            }
            return this.getConfiguration();
        } catch (Exception e) {
            this.getLogger().warn(CLASSNAME + ": Monitor: error occurred preparing configuration: " + e.getMessage());
            throw new JobSchedulerException(this.getLogger().getWarning(), e);
        }
    }

    private void substituteOrderParamsInTaskParams() {
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();
        String[] parameterNames = spooler_task.order().params().names().split(";");
        for (String paramName : parameterNames) {
            String paramValue = String.valueOf(getOrderParam(paramName));
            debugx(3, "addKey " + paramName + "=" + paramValue);
            parameterSubstitutor.addKey(paramName, paramValue);
        }
        parameterNames = spooler_task.params().names().split(";");
        for (String parameterName : parameterNames) {
            String paramName = parameterName;
            String paramValue = String.valueOf(spooler_task.params().value(paramName));
            paramValue = parameterSubstitutor.replace(paramValue);
            if (!"".trim().equals(paramName)) {
                debugx(3, "set " + paramName + "=" + paramValue);
                spooler_task.params().set_var(paramName, paramValue);
            }
        }
    }

    private void substitute(final Variable_set objParams, final Variable_set globalVariables) throws Exception {
        String[] parameterNames = objParams.names().split(";");
        Pattern paramPattern = Pattern.compile("(?m)^.*\\$\\{.*\\}.*$");
        int metaTrials = 0;
        while (metaTrials <= 1) {
            for (int i = 0; i < parameterNames.length; i++) {
                String currParamValue = String.valueOf(objParams.value(parameterNames[i]));
                if (currParamValue != null && paramPattern.matcher(currParamValue).find()) {
                    boolean parameterFound = false;
                    boolean globalParameterFound = false;
                    boolean envFound = false;
                    boolean additionalEnvFound = false;
                    String parameterValue = objParams.value(parameterNames[i]);
                    int trials = 0;
                    while (parameterValue.indexOf(conVariableStartString) != -1 && trials <= 1) {
                        debugx(6, "substitution trials: " + trials + " --> " + parameterValue);
                        for (int j = 0; j < parameterNames.length; j++) {
                            this.getLogger().debug9("parameterNames[j]=" + parameterNames[j] + " -->"
                                    + contains(parameterValue, conVariableStartString + parameterNames[j] + "}", false));
                            if (!parameterNames[i].equals(parameterNames[j])
                                    && (contains(parameterValue, conVariableStartString + parameterNames[j].toUpperCase() + "}", false)
                                            || contains(parameterValue, conVariableTypeBASENAME + parameterNames[j] + "}", false) || contains(parameterValue, conVariableTypeFILE_CONTENT
                                            + parameterNames[j] + "}", false))) {
                                String jParameterValue = objParams.value(parameterNames[j]);
                                if (parameterValue.indexOf(conVariableTypeBASENAME + parameterNames[j] + "}") != -1) {
                                    parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{basename:" + parameterNames[j] + "\\}", new File(objParams.value(parameterNames[j])).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                    parameterFound = true;
                                    trials = 0;
                                } else if (parameterValue.indexOf(conVariableTypeFILE_CONTENT + parameterNames[j] + "}") != -1) {
                                    if (jParameterValue.indexOf(conVariableStartString) != -1) {
                                        getLogger().debug9("file_content parameter still contains other parameters.");
                                        metaTrials = 0;
                                    } else {
                                        File contentFile = new File(objParams.value(parameterNames[j]));
                                        String fileContent = "";
                                        try {
                                            fileContent = SOSFile.readFile(contentFile);
                                        } catch (Exception e) {
                                            getLogger().warn(CLASSNAME + ": Failed to read file: " + contentFile.getAbsolutePath());
                                        }
                                        parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{file_content:" + parameterNames[j] + "\\}", fileContent.replaceAll("[\\\\]", "\\\\\\\\"));
                                        parameterFound = true;
                                        trials = 0;
                                    }
                                } else {
                                    parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{" + parameterNames[j] + "\\}", objParams.value(parameterNames[j]).replaceAll("[\\\\]", "\\\\\\\\"));
                                    parameterFound = true;
                                    trials = 0;
                                }
                            }
                        }
                        trials++;
                    }
                    if (globalVariables.count() > 0) {
                        String[] globalNames = globalVariables.names().split(";");
                        for (String globalName : globalNames) {
                            debugx(9, "globalNames[j]=" + globalName + " -->" + contains(parameterValue, conVariableStartString + globalName + "}", false));
                            String jParameterValue = globalVariables.value(globalName);
                            if (contains(parameterValue, conVariableStartString + globalName.toUpperCase() + "}", false)
                                    || contains(parameterValue, conVariableTypeBASENAME + globalName + "}", false)
                                    || contains(parameterValue, conVariableTypeFILE_CONTENT + globalName + "}", false)) {
                                if (parameterValue.indexOf(conVariableTypeBASENAME) != -1) {
                                    parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{basename:" + globalName + "\\}", new File(globalVariables.value(globalName)).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                } else if (parameterValue.indexOf(conVariableTypeFILE_CONTENT + globalName + "}") != -1) {
                                    if (jParameterValue.indexOf(conVariableStartString) != -1) {
                                        getLogger().debug9("file_content parameter still contains other parameters.");
                                        metaTrials = 0;
                                    } else {
                                        File contentFile = new File(globalVariables.value(globalName));
                                        String fileContent = "";
                                        try {
                                            fileContent = SOSFile.readFile(contentFile);
                                        } catch (Exception e) {
                                            getLogger().warn(CLASSNAME + ": Failed to read file: " + contentFile.getAbsolutePath());
                                        }
                                        parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{file_content:" + globalName + "\\}", fileContent.replaceAll("[\\\\]", "\\\\\\\\"));
                                    }
                                } else {
                                    parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{" + globalName + "\\}", globalVariables.value(globalName).replaceAll("[\\\\]", "\\\\\\\\"));
                                }
                                globalParameterFound = true;
                            }
                        }
                    }
                    String casePrefix = "";
                    if (!envVarsCaseSensitive) {
                        casePrefix = "(?i)";
                    }
                    if (envvars != null) {
                        Iterator envIterator = envvars.keySet().iterator();
                        while (envIterator.hasNext()) {
                            try {
                                Object envName = envIterator.next();
                                Object envValue = envvars.get(envName.toString());
                                if (contains(parameterValue, conVariableStartString + envName.toString() + "}", envVarsCaseSensitive)) {
                                    parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{" + envName.toString() + "\\}", envValue.toString().replaceAll("[\\\\]", "\\\\\\\\"));
                                    envFound = true;
                                } else if (contains(parameterValue, conVariableTypeBASENAME + envName.toString() + "}", envVarsCaseSensitive)) {
                                    parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{basename:" + envName.toString() + "\\}", new File(envValue.toString()).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                    envFound = true;
                                }
                            } catch (Exception e) {
                                getLogger().warn(CLASSNAME + ": Error reading envar");
                            }
                        }
                    }
                    if (additional_envvars != null) {
                        Iterator envIterator = additional_envvars.keySet().iterator();
                        while (envIterator.hasNext()) {
                            Object envName = envIterator.next();
                            Object envValue = additional_envvars.get(envName.toString());
                            if (contains(parameterValue, conVariableStartString + envName + "}", envVarsCaseSensitive)) {
                                parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{" + envName.toString() + "\\}", envValue.toString().replaceAll("[\\\\]", "\\\\\\\\"));
                                additionalEnvFound = true;
                            } else if (contains(parameterValue, conVariableTypeBASENAME + envName.toString() + "}", envVarsCaseSensitive)) {
                                parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{basename:" + envName.toString() + "\\}", new File(envValue.toString()).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                additionalEnvFound = true;
                            } else if (contains(parameterValue, conVariableTypeFILE_CONTENT + envName.toString() + "}", envVarsCaseSensitive)) {
                                if (envValue.toString().indexOf(conVariableStartString) != -1) {
                                    getLogger().debug9("file_content parameter still contains other parameters.");
                                    metaTrials = 0;
                                } else {
                                    File contentFile = new File(envValue.toString());
                                    String fileContent = "";
                                    try {
                                        fileContent = SOSFile.readFile(contentFile);
                                    } catch (Exception e) {
                                        getLogger().warn(CLASSNAME + ": Failed to read file: " + contentFile.getAbsolutePath());
                                    }
                                    parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{file_content:" + envName.toString() + "\\}", fileContent.replaceAll("[\\\\]", "\\\\\\\\"));
                                    additionalEnvFound = true;
                                }
                            }
                        }
                    }
                    Variable_set objOrderParams = objParams;
                    if (parameterFound) {
                        debugx(3, "parameter substitution [" + parameterNames[i] + "]: " + parameterValue);
                        objOrderParams.set_var(parameterNames[i], parameterValue);
                    }
                    if (globalParameterFound) {
                        debugx(3, "global parameter substitution [" + parameterNames[i] + "]: " + parameterValue);
                        objOrderParams.set_var(parameterNames[i], parameterValue);
                    }
                    if (envFound) {
                        debugx(3, "environment variable substitution [" + parameterNames[i] + "]: " + parameterValue);
                        objOrderParams.set_var(parameterNames[i], parameterValue);
                    }
                    if (additionalEnvFound) {
                        debugx(3, "additional environment variable substitution [" + parameterNames[i] + "]: " + parameterValue);
                        objOrderParams.set_var(parameterNames[i], parameterValue);
                    }
                }
            }
            metaTrials++;
        }
    }

    public static void logParameters(final Variable_set params, final SOSLogger logger) {
        String[] names = params.names().split(";");
        for (String name : names) {
            try {
                logger.debug1(".. parameter [" + name + "]: " + params.value(name));
            } catch (Exception e) {
                // no exception handling here
            }
        }
    }

    public void cleanupConfiguration() throws Exception {
        try {
            if (orderParameterKeys != null) {
                Variable_set resultParameters = spooler.create_variable_set();
                String[] parameterNames = spooler_task.order().params().names().split(";");
                for (String strParamName : parameterNames) {
                    if (!orderParameterKeys.contains(strParamName) && !"".equals(strParamName)) {
                        String strValue = getOrderParam(strParamName);
                        debugx(9, String.format("set '%1$s' to value '%2$s'", strParamName, strValue));
                        resultParameters.set_var(strParamName, strValue);
                    }
                }
                if (spooler_task.order() != null) {
                    spooler_task.order().set_params(resultParameters);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(CLASSNAME + ": error occurred in monitor on cleanup: " + e.getMessage(), e);
        }
    }

    public void sendMail(final String recipient, final String recipientCC, final String recipientBCC, final String subject, final String body) throws Exception {
        try {
            SOSMail sosMail = new SOSMail(spooler_log.mail().smtp());
            sosMail.setQueueDir(spooler_log.mail().queue_dir());
            sosMail.setFrom(spooler_log.mail().from());
            sosMail.setContentType("text/plain");
            sosMail.setEncoding("Base64");
            String recipients[] = recipient.split(",");
            for (int i = 0; i < recipients.length; i++) {
                if (i == 0) {
                    sosMail.setReplyTo(recipients[i].trim());
                }
                sosMail.addRecipient(recipients[i].trim());
            }
            String recipientsCC[] = recipientCC.split(",");
            for (String element : recipientsCC) {
                sosMail.addCC(element.trim());
            }
            String recipientsBCC[] = recipientBCC.split(",");
            for (String element : recipientsBCC) {
                sosMail.addBCC(element.trim());
            }
            sosMail.setSubject(subject);
            sosMail.setBody(body);
            sosMail.setSOSLogger(this.getLogger());
            this.getLogger().info("sending mail: \n" + sosMail.dumpMessageAsString());
            if (!sosMail.send()) {
                this.getLogger().warn("mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory ["
                        + sosMail.getQueueDir() + "]:" + sosMail.getLastError());
            }
            sosMail.clearRecipients();
        } catch (Exception e) {
            throw new JobSchedulerException(CLASSNAME + ": error occurred in monitor sending mail: " + e.getMessage(), e);
        }
    }

    public SOSSchedulerLogger getLogger() {
        return sosLogger;
    }

    public void setLogger(final SOSSchedulerLogger sosLogger1) {
        sosLogger = sosLogger1;
    }

    public Document getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Document configuration1) {
        configuration = configuration1;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public void setConfigurationPath(final String configurationPath) {
        if (configurationPath != null) {
            this.configurationPath = configurationPath;
        }
    }

    public String getConfigurationFilename() {
        return configurationFilename;
    }

    public void setConfigurationFilename(final String configurationFilename) {
        if (configurationFilename != null) {
            spooler_log.debug9("...setConfigurationFilename:" + configurationFilename);
            this.configurationFilename = configurationFilename;
        }
    }

    private void setEnvVars() throws Exception {
        String OS = System.getProperty(SYSTEMPROPERTY_OS_NAME).toLowerCase();
        boolean win = false;
        envvars = new TreeMap();
        if (OS.indexOf("nt") > -1 || OS.indexOf("windows") > -1) {
            win = true;
            envVarsCaseSensitive = false;
        }
        Variable_set env = spooler_task.create_subprocess().env();
        debugx(9, "environment variable names: " + env.names());
        StringTokenizer t = new StringTokenizer(env.names(), ";");
        while (t.hasMoreTokens()) {
            String envname = t.nextToken();
            if (envname != null) {
                String envvalue = env.value(envname);
                if (win) {
                    debugx(9, "set environment variable: " + envname.toUpperCase() + "=" + envvalue);
                    envvars.put(envname.toUpperCase(), envvalue);
                } else {
                    debugx(9, "set environment variable: " + envname + "=" + envvalue);
                    envvars.put(envname, envvalue);
                }
            }
        }
    }

    private int logLevel2Int(final String l) {
        HashMap<String, String> levels = new HashMap<String, String>();
        if (l == null) {
            return this.getLogger().getLogLevel();
        } else {
            levels.put("info", "10");
            levels.put("warn", "11");
            levels.put("error", "12");
            levels.put("debug1", "1");
            levels.put("debug2", "2");
            levels.put("debug3", "3");
            levels.put("debug4", "4");
            levels.put("debug5", "5");
            levels.put("debug6", "6");
            levels.put("debug7", "7");
            levels.put("debug8", "8");
            levels.put("debug9", "9");
            if (levels.get(l) != null) {
                return Integer.parseInt(levels.get(l).toString());
            } else {
                return this.getLogger().getLogLevel();
            }
        }
    }

    public static String myReplaceAll(final String source, final String what, final String replacement) {
        String newReplacement = replacement.replaceAll("\\$", "\\\\\\$");
        newReplacement = newReplacement.replaceAll("\"", "");
        return source.replaceAll("(?m)" + what, newReplacement);
    }

    public static boolean contains(final String searchString, final String otherString, final boolean caseSensitive) {
        if (caseSensitive) {
            return searchString.indexOf(otherString) != -1;
        } else {
            return searchString.toUpperCase().indexOf(otherString.toUpperCase()) != -1;
        }
    }

    private void debug3(final String pstrDebugMessage) {
        final String conMethodName = CLASSNAME + "::debug3";
        debugx(3, pstrDebugMessage);
    }

    private void debugx(final int intDebugLevel, final String pstrMsg) {
        try {
            switch (intDebugLevel) {
            case 1:
                this.getLogger().debug1(CLASSNAME + ": " + pstrMsg);
                break;
            case 2:
                this.getLogger().debug2(CLASSNAME + ": " + pstrMsg);
                break;
            case 3:
                this.getLogger().debug3(CLASSNAME + ": " + pstrMsg);
                break;
            case 6:
                this.getLogger().debug6(CLASSNAME + ": " + pstrMsg);
                break;
            case 9:
                this.getLogger().debug9(CLASSNAME + ": " + pstrMsg);
                break;
            default:
                this.getLogger().debug(CLASSNAME + ": " + pstrMsg);
                break;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private boolean isTrue(final String pstrValue) {
        return "yes".equalsIgnoreCase(pstrValue) || "1".equals(pstrValue) || "on".equalsIgnoreCase(pstrValue) || TRUE.equalsIgnoreCase(pstrValue);
    }

}
