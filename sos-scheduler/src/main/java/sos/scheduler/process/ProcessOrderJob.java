package sos.scheduler.process;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.spooler.Subprocess;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.xml.SOSXMLXPath;

/** @author andreas pueschel
 * @deprecated use sos.scheduler.managed.configuration.ConfigurationOrderJob */
public class ProcessOrderJob extends ProcessJob {

    private String triggerFilename = new String();
    private boolean ignoreError = false;
    private boolean ignoreSignal = false;
    private boolean ignoreStderr = false;
    private String priorityClass = "normal";
    private double timeout = 0;
    private Variable_set parameters = null;
    private Variable_set jobParameters = null;
    private Variable_set orderParameters = null;
    private Document configuration = null;
    private StringBuffer configurationBuffer = null;
    private String configurationPath = "";
    private String configurationFilename = "";
    private String command = "";
    private String commandParameters = "";
    private BufferedReader stdoutStream;
    private BufferedReader stderrStream;
    private Vector orderParameterKeys = null;
    private TreeMap envvars = null;
    private TreeMap additional_envvars = null;

    public boolean spooler_init() {
        try {
            try {
                if (spooler_task.params().value("configuration_path") != null && !spooler_task.params().value("configuration_path").isEmpty()) {
                    this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                } else {
                    this.setConfigurationPath(new File(spooler.ini_path()).getParent());
                }
                if (spooler_task.params().value("configuration_file") != null && !spooler_task.params().value("configuration_file").isEmpty()) {
                    this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                }
            } catch (Exception e) {
                throw new Exception("failed to initialize configuration path: " + e.getMessage());
            }
            try {
                if (spooler_task != null && spooler_task.stdout_path() != null && !spooler_task.stdout_path().isEmpty()
                        && spooler_task.stderr_path() != null && !spooler_task.stderr_path().isEmpty()) {
                    FileReader fisOut = new FileReader(spooler_task.stdout_path());
                    FileReader fisErr = new FileReader(spooler_task.stderr_path());
                    this.stdoutStream = new BufferedReader(fisOut);
                    this.stderrStream = new BufferedReader(fisErr);
                }
            } catch (Exception e) {
                throw new Exception("failed to initialize stdout and stderr streams: " + e.getMessage());
            }
        } catch (Exception e) {
            spooler_log.warn("error occurred on initialization: " + e.getMessage());
        }
        if (!super.spooler_init()) {
            return false;
        }
        return true;
    }

    public Subprocess executeSubprocess() throws Exception {
        return this.executeSubprocess(this.getCommand(), this.getCommandParameters(), null);
    }

    public Subprocess executeSubprocess(HashMap environment) throws Exception {
        return this.executeSubprocess(this.getCommand(), this.getCommandParameters(), environment);
    }

    public Subprocess executeSubprocess(String command) throws Exception {
        return this.executeSubprocess(command, new String(""), null);
    }

    public Subprocess executeSubprocess(String command, String commandParameters) throws Exception {
        return executeSubprocess(command, commandParameters, null);
    }

    public Subprocess executeSubprocess(String command, String commandParameters, HashMap environment) throws Exception {
        Subprocess subprocess = null;
        boolean terminated = true;
        try {
            subprocess = spooler_task.create_subprocess();
            subprocess.set_own_process_group(true);
            subprocess.set_ignore_error(this.isIgnoreError());
            subprocess.set_ignore_signal(this.isIgnoreSignal());
            subprocess.set_priority_class(this.getPriorityClass());
            if (this.getTimeout() > 0) {
                subprocess.set_timeout(this.getTimeout());
            }
            String commandLine = command + " " + commandParameters;
            String[] parameterNames = this.getParameters().names().split(";");
            for (int i = 0; i < parameterNames.length; i++) {
                commandLine =
                        myReplaceAll(commandLine, "\\$\\{" + parameterNames[i] + "\\}", this.getParameters().value(parameterNames[i]).replaceAll(
                                "[\\\\]", "\\\\\\\\"));
            }
            subprocess.set_environment("SCHEDULER_TRIGGER_FILE", this.getTriggerFilename());
            if (environment != null) {
                Iterator envIterator = environment.keySet().iterator();
                while (envIterator.hasNext()) {
                    Object envName = envIterator.next();
                    Object envValue = environment.get(envName.toString());
                    commandLine =
                            myReplaceAll(commandLine, "\\$\\{" + envName.toString() + "\\}", envValue.toString().replaceAll("[\\\\]", "\\\\\\\\"));
                }
            }
            if (this.envvars != null) {
                Iterator envIterator = this.envvars.keySet().iterator();
                while (envIterator.hasNext()) {
                    Object envName = envIterator.next();
                    Object envValue = this.envvars.get(envName.toString());
                    commandLine =
                            myReplaceAll(commandLine, "\\$\\{" + envName.toString() + "\\}", envValue.toString().replaceAll("[\\\\]", "\\\\\\\\"));
                }
            }
            if (this.additional_envvars != null) {
                Iterator envIterator = this.additional_envvars.keySet().iterator();
                while (envIterator.hasNext()) {
                    String envName = (String) envIterator.next();
                    String envValue = (String) this.additional_envvars.get(envName);
                    if (envName == null) {
                        continue;
                    }
                    int varBegin = envValue.indexOf("${");
                    while (varBegin > -1) {
                        int varEnd = envValue.indexOf("}", varBegin + 2);
                        if (varEnd > 0) {
                            String varName = envValue.substring(varBegin + 2, varEnd);
                            boolean hasBasename = varName.startsWith("basename:");
                            if (hasBasename) {
                                varName = varName.substring(9);
                            }
                            if (this.getParameters().value(varName) != null) {
                                if (hasBasename) {
                                    envValue =
                                            myReplaceAll(envValue, "\\$\\{basename:" + varName + "\\}",
                                                    new File(this.getParameters().value(varName)).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                } else {
                                    envValue =
                                            myReplaceAll(envValue, "\\$\\{" + varName + "\\}", this.getParameters().value(varName).replaceAll(
                                                    "[\\\\]", "\\\\\\\\"));
                                }
                                this.getLogger().debug9("environment variable substituted: " + varName);
                            } else {
                                this.getLogger().info("unsubstitutable variable found for environment: " + varName);
                            }
                        }
                        varBegin = envValue.indexOf("${", varEnd + 1);
                    }
                    this.getLogger().debug1(".. setting environment variable: " + envName + "=" + envValue);
                    subprocess.set_environment(envName, envValue);
                }
            }
            this.getLogger().info("executing command: " + commandLine);
            subprocess.start(commandLine);
            if (this.getTimeout() > 0) {
                terminated = subprocess.wait_for_termination(this.getTimeout());
            } else {
                subprocess.wait_for_termination();
            }
            if (!terminated) {
                this.getLogger().warn("timeout reached for subprocess, process will be terminated");
                subprocess.kill();
                subprocess.wait_for_termination();
            }
            boolean stdErrEmpty = true;
            String stdErrString = "";
            String stdOutString = "";
            this.getLogger().info("output reported to stdout for " + commandLine + ":");
            while (this.stdoutStream != null && this.stdoutStream.ready()) {
                String stdOutLine = stdoutStream.readLine();
                this.getLogger().info(stdOutLine);
                stdOutString += stdOutLine + "\n";
            }
            this.getLogger().info("output reported to stderr for " + commandLine + ":");
            while (this.stderrStream != null && this.stderrStream.ready()) {
                String stdErrLine = stderrStream.readLine();
                this.getLogger().info(stdErrLine);
                if (!stdErrLine.trim().isEmpty()) {
                    stdErrEmpty = false;
                }
                stdErrString += stdErrLine + "\n";
            }
            if (spooler_job.order_queue() != null) {
                spooler_task.order().params().set_var("scheduler_order_stderr_output", stdErrString);
                spooler_task.order().params().set_var("scheduler_order_stdout_output", stdOutString);
                spooler_task.order().params().set_var("scheduler_order_exit_code", String.valueOf(subprocess.exit_code()));
                spooler_task.order().params().set_var("scheduler_order_terminated", (terminated ? "true" : "false"));
            }
            if ((subprocess.exit_code() != 0)) {
                if (this.isIgnoreError()) {
                    this.getLogger().info("command terminated with exit code: " + subprocess.exit_code());
                } else {
                    throw new Exception("command terminated with exit code: " + subprocess.exit_code());
                }
            }
            if ((subprocess.termination_signal() != 0)) {
                if (this.isIgnoreSignal()) {
                    this.getLogger().info("command terminated with signal: " + subprocess.termination_signal());
                } else {
                    throw new Exception("command terminated with signal: " + subprocess.termination_signal());
                }
            }
            if (!this.isIgnoreStderr() && !stdErrEmpty) {
                throw new Exception("command terminated with output to stderr:\n" + stdErrString);
            }
            return subprocess;
        } catch (Exception e) {
            this.getLogger().warn("error occurred executing subprocess: " + e.getMessage());
            throw new Exception(this.getLogger().getWarning());
        }
    }

    public void cleanup() throws Exception {
        try {
            if (this.orderParameterKeys != null) {
                Variable_set resultParameters = spooler.create_variable_set();
                String[] parameterNames = spooler_task.order().params().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    if (!this.orderParameterKeys.contains(parameterNames[i])) {
                        resultParameters.set_var(parameterNames[i], spooler_task.order().params().value(parameterNames[i]));
                    }
                }
                spooler_task.order().set_params(resultParameters);
            }
        } catch (Exception e) {
            throw new Exception("error occurred on cleanup: " + e.getMessage());
        }
    }

    public void prepare() throws Exception {
        this.prepareConfiguration();
        this.prepareParameters();
        this.prepareAttributes();
    }

    public void prepareParameters() throws Exception {
        try {
            this.getLogger().debug1("calling " + SOSClassUtil.getMethodName());
            this.setJobParameters(spooler_task.params());
            if (spooler_task.job().order_queue() != null) {
                this.setOrderParameters(spooler_task.order().params());
            } else {
                this.setOrderParameters(spooler.create_variable_set());
            }
            if (this.getJobParameters() != null) {
                this.setParameters(this.getJobParameters());
            } else {
                this.setParameters(spooler.create_variable_set());
            }
            if (this.getOrderParameters() != null) {
                this.getParameters().merge(this.getOrderParameters());
            }
        } catch (Exception e) {
            this.getLogger().warn("error occurred preparing parameters: " + e.getMessage());
            throw new Exception(this.getLogger().getWarning());
        }
    }

    public void prepareAttributes() throws Exception {
        try {
            this.getLogger().debug1("calling " + SOSClassUtil.getMethodName());
            if (this.getParameters().value("configuration_path") != null && !this.getParameters().value("configuration_path").isEmpty()) {
                this.setConfigurationPath(this.getParameters().value("configuration_path"));
                this.getLogger().debug1(".. parameter [configuration_path]: " + this.getConfigurationPath());
            } else {
                this.setConfigurationPath(new File(spooler.ini_path()).getParent());
                this.getLogger().debug1(".. parameter [configuration_path]: " + this.getConfigurationPath());
            }
            if (this.getParameters().value("configuration_file") != null && !this.getParameters().value("configuration_file").isEmpty()) {
                this.setConfigurationFilename(this.getParameters().value("configuration_file"));
                this.getLogger().debug1(".. parameter [configuration_file]: " + this.getConfigurationFilename());
            } else {
                if (spooler_job.order_queue() != null) {
                    this.setConfigurationFilename("scheduler_" + spooler_task.order().job_chain().name() + ".config.xml");
                    this.getLogger().debug1(".. parameter [configuration_file]: " + this.getConfigurationFilename());
                }
            }
            if (this.getParameters().value("scheduler_file_path") != null && !this.getParameters().value("scheduler_file_path").isEmpty()) {
                this.setTriggerFilename(this.getParameters().value("scheduler_file_path"));
                this.getLogger().debug1(".. parameter [scheduler_file_path]: " + this.getTriggerFilename());
            } else {
                this.setTriggerFilename("");
            }
            if (this.getParameters().value("scheduler_order_command") != null && !this.getParameters().value("scheduler_order_command").isEmpty()) {
                this.setCommand(this.getParameters().value("scheduler_order_command"));
                this.getLogger().debug1(".. parameter [scheduler_order_command]: " + this.getCommand());
            } else {
                this.setCommand("");
            }
            if (this.getParameters().value("scheduler_order_command_parameters") != null
                    && !this.getParameters().value("scheduler_order_command_parameters").isEmpty()) {
                this.setCommandParameters(this.getParameters().value("scheduler_order_command_parameters"));
                this.getLogger().debug1(".. parameter [scheduler_order_command_parameters]: " + this.getCommandParameters());
            } else {
                this.setCommandParameters("");
            }
            if (this.getParameters().value("scheduler_order_ignore_stderr") != null
                    && !this.getParameters().value("scheduler_order_ignore_stderr").isEmpty()) {
                if ("yes".equalsIgnoreCase(this.getParameters().value("scheduler_order_ignore_stderr"))
                        || "true".equalsIgnoreCase(this.getParameters().value("scheduler_order_ignore_stderr"))
                        || "1".equals(this.getParameters().value("scheduler_order_ignore_stderr"))) {
                    this.setIgnoreStderr(true);
                } else {
                    this.setIgnoreStderr(false);
                }
                this.getLogger().debug1(".. parameter [scheduler_order_ignore_stderr]: " + this.isIgnoreStderr());
            } else {
                this.setIgnoreStderr(false);
            }
            if (this.getParameters().value("scheduler_order_ignore_error") != null
                    && !this.getParameters().value("scheduler_order_ignore_error").isEmpty()) {
                if ("yes".equalsIgnoreCase(this.getParameters().value("scheduler_order_ignore_error"))
                        || "true".equalsIgnoreCase(this.getParameters().value("scheduler_order_ignore_error"))
                        || "1".equals(this.getParameters().value("scheduler_order_ignore_error"))) {
                    this.setIgnoreError(true);
                } else {
                    this.setIgnoreError(false);
                }
                this.getLogger().debug1(".. parameter [scheduler_order_ignore_error]: " + this.isIgnoreError());
            } else {
                this.setIgnoreError(false);
            }
            if (this.getParameters().value("scheduler_order_ignore_signal") != null
                    && !this.getParameters().value("scheduler_order_ignore_signal").isEmpty()) {
                if ("yes".equalsIgnoreCase(this.getParameters().value("scheduler_order_ignore_signal"))
                        || "true".equalsIgnoreCase(this.getParameters().value("scheduler_order_ignore_signal"))
                        || "1".equals(this.getParameters().value("scheduler_order_ignore_signal"))) {
                    this.setIgnoreSignal(true);
                } else {
                    this.setIgnoreSignal(false);
                }
                this.getLogger().debug1(".. parameter [scheduler_order_ignore_signal]: " + this.isIgnoreSignal());
            } else {
                this.setIgnoreSignal(false);
            }
            if (this.getParameters().value("scheduler_order_priority_class") != null
                    && !this.getParameters().value("scheduler_order_priority_class").isEmpty()) {
                this.setPriorityClass(this.getParameters().value("scheduler_order_priority_class"));
                this.getLogger().debug1(".. parameter [scheduler_order_priority_class]: " + this.getPriorityClass());
            } else {
                this.setPriorityClass("normal");
            }
            try {
                if (this.getParameters().value("scheduler_order_timeout") != null && !this.getParameters().value("scheduler_order_timeout").isEmpty()) {
                    this.setTimeout(Double.parseDouble(this.getParameters().value("scheduler_order_timeout")));
                    this.getLogger().debug1(".. parameter [scheduler_order_timeout]: " + this.getTimeout());
                } else {
                    this.setTimeout(0);
                }
            } catch (Exception e) {
                throw new Exception("illegal value for parameter [scheduler_order_timeout]: " + this.getParameters().value("scheduler_order_timeout"));
            }
        } catch (Exception e) {
            this.getLogger().warn("error occurred processing attributes: " + e.getMessage());
            throw new Exception(this.getLogger().getWarning());
        }
    }

    public Variable_set getParameters() {
        return parameters;
    }

    public void setParameters(Variable_set parameters) {
        this.parameters = parameters;
    }

    public Variable_set getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(Variable_set jobParameters) {
        this.jobParameters = jobParameters;
    }

    public Variable_set getOrderParameters() {
        return orderParameters;
    }

    public void setOrderParameters(Variable_set orderParameters) {
        this.orderParameters = orderParameters;
    }

    public void initConfiguration() throws Exception {
        if (spooler_job.order_queue() != null && spooler_task.order().params() != null) {
            if (spooler_task.order().params().value("configuration_path") != null
                    && !spooler_task.order().params().value("configuration_path").isEmpty()) {
                this.setConfigurationPath(spooler_task.order().params().value("configuration_path"));
            }
            if (spooler_task.order().params().value("configuration_file") != null
                    && !spooler_task.order().params().value("configuration_file").isEmpty()) {
                this.setConfigurationFilename(spooler_task.order().params().value("configuration_file"));
            }
        }
        this.initConfiguration(this.getConfigurationPath(), this.getConfigurationFilename());
    }

    public void initConfiguration(String configurationPath, String configurationFilename) throws Exception {
        if (spooler_job.order_queue() != null && spooler_task.order().params() != null) {
            if (spooler_task.order().params().value("configuration_path") != null
                    && !spooler_task.order().params().value("configuration_path").isEmpty()) {
                this.setConfigurationPath(spooler_task.order().params().value("configuration_path"));
            }
        }
        if (configurationFilename.startsWith(".") || configurationFilename.startsWith("/") || configurationFilename.startsWith("\\")
                || configurationPath == null || configurationPath.isEmpty()) {
            this.initConfiguration(configurationFilename);
        } else {
            this.initConfiguration(configurationPath + ((!configurationPath.endsWith("/") && !configurationPath.endsWith("\\")) ? "/" : "")
                    + configurationFilename);
        }
    }

    public void initConfiguration(String configurationFilename) throws Exception {
        FileInputStream fis = null;
        try {
            if (spooler_task.job().order_queue() != null) {
                if (configurationFilename == null || configurationFilename.isEmpty()) {
                    throw new Exception("no configuration filename was specified");
                }
                this.getLogger().debug1(".. config file " + configurationFilename);
                File configurationFile = new File(configurationFilename);
                if (!configurationFile.exists()) {
                    throw new Exception("configuration file not found: " + configurationFile.getCanonicalPath());
                } else if (!configurationFile.canRead()) {
                    throw new Exception("configuration file is not accessible: " + configurationFile.getCanonicalPath());
                }
                fis = new FileInputStream(configurationFile);
                BufferedInputStream in = new BufferedInputStream(fis);
                byte inBuffer[] = new byte[1024];
                int inBytesRead;
                this.configurationBuffer = new StringBuffer();
                while ((inBytesRead = in.read(inBuffer)) != -1) {
                    this.configurationBuffer.append(new String(inBuffer, 0, inBytesRead));
                }
                spooler_task.order().set_xml_payload(this.configurationBuffer.toString());
                spooler_task.order().params().set_var("scheduler_order_configuration_loaded", "true");
                spooler_task.order().params().set_var("configuration_file", configurationFilename);
            }
        } catch (Exception e) {
            this.getLogger().warn("error occurred initializing configuration: " + e.getMessage());
            throw new Exception(this.getLogger().getWarning());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (Exception ex) {
            }
        }
    }

    public Document prepareConfiguration() throws Exception {
        String nodeQuery = "";
        String payload = "";
        try {
            this.getLogger().debug1("calling " + SOSClassUtil.getMethodName());
            this.orderParameterKeys = new Vector();
            if (spooler_task.job().order_queue() != null) {
                if (spooler_task.order().xml_payload() == null || spooler_task.order().xml_payload().isEmpty()
                        || spooler_task.order().params() == null
                        || spooler_task.order().params().value("scheduler_order_configuration_loaded") == null
                        || spooler_task.order().params().value("scheduler_order_configuration_loaded").isEmpty()) {
                    this.initConfiguration();
                }
                if (spooler_task.order().xml_payload() == null) {
                    throw new Exception("no configuration was specified for this order: " + spooler_task.order().id());
                }
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(false);
                docFactory.setValidating(false);
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                payload = spooler_task.order().xml_payload();
                if (!payload.startsWith("<?xml ")) {
                    payload = "<?xml version='1.0' encoding='ISO-8859-1'?>" + payload;
                }
                this.setConfiguration(docBuilder.parse(new ByteArrayInputStream(payload.getBytes())));
                SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(payload));
                NodeList nodeList = null;
                NamedNodeMap nodeMapSettings = null;
                nodeQuery = "//settings/log_level";
                Node nodeSettings = xpath.selectSingleNode(nodeQuery);
                if (nodeSettings != null) {
                    nodeMapSettings = nodeSettings.getAttributes();
                    if (nodeMapSettings != null && nodeMapSettings.getNamedItem("value") != null) {
                        this.getLogger().debug1(
                                "Log Level is: " + nodeMapSettings.getNamedItem("value").getNodeValue() + "("
                                        + this.logLevel2Int(nodeMapSettings.getNamedItem("value").getNodeValue()) + ")");
                        this.getLogger().setLogLevel(this.logLevel2Int(nodeMapSettings.getNamedItem("value").getNodeValue()));
                    }
                }
                this.setEnvVars();
                String env = "";
                boolean globalEnv = false;
                nodeQuery = "//job_chain[@name='" + spooler_task.order().job_chain().name() + "']/order";
                this.getLogger().debug9("lookup order query for job chain: " + nodeQuery);
                Node nodeParams = xpath.selectSingleNode(nodeQuery + "/params");
                if (nodeParams == null || !nodeParams.hasChildNodes()) {
                    nodeQuery = "//application[@name='" + spooler_task.order().job_chain().name() + "']/order";
                    this.getLogger().debug9("lookup order query for application: " + nodeQuery);
                    nodeParams = xpath.selectSingleNode(nodeQuery + "/params");
                }
                if (nodeParams != null && nodeParams.hasAttributes()) {
                    NamedNodeMap nodeMapParams = nodeParams.getAttributes();
                    if (nodeMapParams != null && nodeMapParams.getNamedItem("env") != null) {
                        env = nodeMapParams.getNamedItem("env").getNodeValue();
                        this.getLogger().debug3(".. parameter section with env=" + env + " found");
                        globalEnv = "yes".equalsIgnoreCase(env) || "1".equals(env) || "on".equalsIgnoreCase(env) || "true".equalsIgnoreCase(env);
                    }
                }
                nodeList = xpath.selectNodeList(nodeQuery + "/params/param");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    String nodeValue = "";
                    String nodeName = "";
                    if ("param".equalsIgnoreCase(node.getNodeName())) {
                        NamedNodeMap nodeMap = node.getAttributes();
                        if (nodeMap != null && nodeMap.getNamedItem("name") != null) {
                            nodeName = nodeMap.getNamedItem("name").getNodeValue();
                            if (nodeMap.getNamedItem("value") != null) {
                                nodeValue = nodeMap.getNamedItem("value").getNodeValue();
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
                            this.getLogger().debug1(".. global configuration parameter [" + nodeName + "]: " + nodeValue);
                            spooler_task.order().params().set_var(nodeName, nodeValue);
                            if (globalEnv || nodeMap.getNamedItem("env") != null) {
                                if (nodeMap.getNamedItem("env") != null) {
                                    env = nodeMap.getNamedItem("env").getNodeValue();
                                }
                                boolean setEnv =
                                        globalEnv || "yes".equalsIgnoreCase(env) || "1".equals(env) || "on".equalsIgnoreCase(env)
                                                || "true".equalsIgnoreCase(env);
                                if (setEnv) {
                                    if (this.additional_envvars == null) {
                                        this.additional_envvars = new TreeMap();
                                    }
                                    this.additional_envvars.put(nodeName, nodeValue);
                                }
                            }
                        }
                    }
                }
                nodeQuery =
                        "//job_chain[@name='" + spooler_task.order().job_chain().name() + "']/order/process[@state='" + spooler_task.order().state()
                                + "']";
                this.getLogger().debug9("lookup order node query: " + nodeQuery + "/params/param");
                nodeList = xpath.selectNodeList(nodeQuery + "/params/param");
                if (nodeList == null || nodeList.getLength() == 0) {
                    nodeQuery =
                            "//application[@name='" + spooler_task.order().job_chain().name() + "']/order/process[@state='"
                                    + spooler_task.order().state() + "']";
                    this.getLogger().debug9("lookup order node query: " + nodeQuery + "/params/param");
                    nodeList = xpath.selectNodeList(nodeQuery + "/params/param");
                }
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    this.getLogger().debug1("---->" + node.getNodeName());
                    if ("param".equalsIgnoreCase(node.getNodeName())) {
                        NamedNodeMap nodeMap = node.getAttributes();
                        if (nodeMap != null && nodeMap.getNamedItem("name") != null) {
                            String value = "";
                            if (nodeMap.getNamedItem("value") != null) {
                                value = nodeMap.getNamedItem("value").getNodeValue();
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
                            this.getLogger().debug1(".. configuration parameter [" + nodeMap.getNamedItem("name").getNodeValue() + "]: " + value);
                            spooler_task.order().params().set_var(nodeMap.getNamedItem("name").getNodeValue(), value);
                            this.orderParameterKeys.add(nodeMap.getNamedItem("name").getNodeValue());
                        }
                    }
                }
                String[] parameterNames = spooler_task.order().params().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    String currParam = String.valueOf(spooler_task.order().params().value(parameterNames[i]));
                    if (currParam != null && currParam.matches("^.*\\$\\{.*\\}.*$")) {
                        boolean parameterFound = false;
                        boolean envFound = false;
                        boolean additionalEnvFound = false;
                        String parameterValue = spooler_task.order().params().value(parameterNames[i]);
                        int trials = 0;
                        while (parameterValue.indexOf("${") != -1 && trials <= 1) {
                            this.getLogger().debug1("substitution trial:" + trials + " --> " + parameterValue);
                            for (int j = 0; j < parameterNames.length; j++) {
                                this.getLogger().debug9(
                                        "parameterNames[j]=" + parameterNames[j] + " -->" + parameterValue.indexOf("${" + parameterNames[j] + "}"));
                                if (!parameterNames[i].equals(parameterNames[j])
                                        && (parameterValue.indexOf("${" + parameterNames[j] + "}") != -1 || parameterValue.indexOf("${basename:"
                                                + parameterNames[j] + "}") != -1)) {
                                    if (parameterValue.indexOf("${basename:") != -1) {
                                        parameterValue =
                                                myReplaceAll(parameterValue, "\\$\\{basename:" + parameterNames[j] + "\\}", new File(
                                                        spooler_task.order().params().value(parameterNames[j])).getName().replaceAll("[\\\\]",
                                                        "\\\\\\\\"));
                                    } else {
                                        parameterValue =
                                                myReplaceAll(parameterValue, "\\$\\{" + parameterNames[j] + "\\}",
                                                        spooler_task.order().params().value(parameterNames[j]).replaceAll("[\\\\]", "\\\\\\\\"));
                                    }
                                    parameterFound = true;
                                    trials = 0;
                                }
                            }
                            trials++;
                        }
                        if (this.envvars != null) {
                            Iterator envIterator = this.envvars.keySet().iterator();
                            while (envIterator.hasNext()) {
                                Object envName = envIterator.next();
                                Object envValue = this.envvars.get(envName.toString());
                                if (parameterValue.indexOf("${" + envName.toString() + "}") != -1) {
                                    parameterValue =
                                            myReplaceAll(parameterValue, "\\$\\{" + envName.toString() + "\\}", envValue.toString().replaceAll(
                                                    "[\\\\]", "\\\\\\\\"));
                                    envFound = true;
                                } else if (parameterValue.indexOf("${basename:" + envName.toString() + "}") != -1) {
                                    parameterValue =
                                            myReplaceAll(parameterValue, "\\$\\{basename:" + envName.toString() + "\\}",
                                                    new File(envValue.toString()).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                    envFound = true;
                                }
                            }
                        }
                        if (this.additional_envvars != null) {
                            Iterator envIterator = this.additional_envvars.keySet().iterator();
                            while (envIterator.hasNext()) {
                                Object envName = envIterator.next();
                                Object envValue = this.additional_envvars.get(envName.toString());
                                if (parameterValue.indexOf("${" + envName + "}") != -1) {
                                    parameterValue =
                                            myReplaceAll(parameterValue, "\\$\\{" + envName.toString() + "\\}", envValue.toString().replaceAll(
                                                    "[\\\\]", "\\\\\\\\"));
                                    additionalEnvFound = true;
                                } else if (parameterValue.indexOf("${basename:" + envName.toString() + "}") != -1) {
                                    parameterValue =
                                            myReplaceAll(parameterValue, "\\$\\{basename:" + envName.toString() + "\\}",
                                                    new File(envValue.toString()).getName().replaceAll("[\\\\]", "\\\\\\\\"));
                                    additionalEnvFound = true;
                                }
                            }
                        }
                        if (parameterFound) {
                            this.getLogger().debug3("parameter substitution [" + parameterNames[i] + "]: " + parameterValue);
                            spooler_task.order().params().set_var(parameterNames[i], parameterValue);
                        }
                        if (envFound) {
                            this.getLogger().debug3("environment variable substitution [" + parameterNames[i] + "]: " + parameterValue);
                            spooler_task.order().params().set_var(parameterNames[i], parameterValue);
                        }
                        if (additionalEnvFound) {
                            this.getLogger().debug3("additional environment variable substitution [" + parameterNames[i] + "]: " + parameterValue);
                            spooler_task.order().params().set_var(parameterNames[i], parameterValue);
                        }
                    }
                }
            }
            return this.getConfiguration();
        } catch (Exception e) {
            this.getLogger().warn("error occurred preparing configuration: " + e.getMessage());
            throw new Exception(this.getLogger().getWarning());
        }
    }

    public Document getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Document configuration) {
        this.configuration = configuration;
    }

    public void setTriggerFilename(String triggerFilename) {
        this.triggerFilename = triggerFilename;
    }

    public String getTriggerFilename() {
        return triggerFilename;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public boolean isIgnoreError() {
        return ignoreError;
    }

    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    public boolean isIgnoreSignal() {
        return ignoreSignal;
    }

    public void setIgnoreSignal(boolean ignoreSignal) {
        this.ignoreSignal = ignoreSignal;
    }

    public boolean isIgnoreStderr() {
        return ignoreStderr;
    }

    public void setIgnoreStderr(boolean ignoreStderr) {
        this.ignoreStderr = ignoreStderr;
    }

    public String getPriorityClass() {
        return priorityClass;
    }

    public void setPriorityClass(String priorityClass) {
        this.priorityClass = priorityClass;
    }

    public double getTimeout() {
        return timeout;
    }

    public void setTimeout(double timeout) {
        this.timeout = timeout;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandParameters() {
        return commandParameters;
    }

    public void setCommandParameters(String commandParameters) {
        this.commandParameters = commandParameters;
    }

    public String normalizedPath(String path) throws Exception {
        return normalizedPath(path, true);
    }

    public String normalizedPath(String path, boolean addSeparator) throws Exception {
        if (addSeparator) {
            if (!path.endsWith("/") && !path.endsWith("\\")) {
                path = path + File.separatorChar;
            }
        } else {
            if (path.endsWith("/") || path.endsWith("\\")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    public String getConfigurationFilename() {
        return configurationFilename;
    }

    public void setConfigurationFilename(String configurationFilename) {
        this.configurationFilename = configurationFilename;
    }

    private void setEnvVars() throws Exception {
        String OS = System.getProperty("os.name").toLowerCase();
        boolean win = false;
        this.envvars = new TreeMap();
        if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1)) {
            win = true;
        }
        Variable_set env = spooler_task.create_subprocess().env();
        this.getLogger().debug9(env.names());
        StringTokenizer t = new StringTokenizer(env.names(), ";");
        while (t.hasMoreTokens()) {
            String envname = t.nextToken();
            if (envname != null) {
                String envvalue = env.value(envname);
                if (win) {
                    this.getLogger().debug9(envname.toUpperCase() + "=" + envvalue);
                    this.envvars.put(envname.toUpperCase(), envvalue);
                } else {
                    this.envvars.put(envname, envvalue);
                }
            }
        }
    }

    private int logLevel2Int(String l) {
        HashMap levels = new HashMap();
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

    private String myReplaceAll(String source, String what, String replacement) {
        String newReplacement = replacement.replaceAll("\\$", "\\\\\\$");
        newReplacement = newReplacement.replaceAll("\"", "");
        return source.replaceAll(what, newReplacement);
    }

    public TreeMap getEnvvars() {
        return envvars;
    }

}