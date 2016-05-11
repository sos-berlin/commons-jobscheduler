package sos.scheduler.cron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSDate;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author Andreas Liebert */
public class CrontabFileConverter extends JSToolBox {

    protected Pattern cronRegExPattern;
    protected Pattern cronRegExSystemPattern;
    protected Pattern cronRegExAliasPattern;
    protected Pattern cronRegExCommentPattern;
    protected Pattern cronRegExJobNamePattern;
    protected Pattern cronRegExJobTitlePattern;
    protected Pattern cronRegExJobTimeoutPattern;
    protected Pattern cronRegExEnvironmentPattern;
    protected Pattern currentCronPattern;
    protected String strBaseDirectory = "CronConverter";
    protected String strMockCommand = "ping -n 20 localhost";
    private static final Logger LOGGER = Logger.getLogger(CrontabFileConverter.class);
    private static final String TRUE = "true";
    private static final String SYSTEM_CRONTAB_NAME = "/etc/crontab";
    private static final String STATE_ERROR = "error";
    private static final String STATE_SUCCESS = "success";
    private static final String ATTRIBUTE_ERROR_STATE = "error_state";
    private static final String ATTRIBUTE_NEXT_STATE = "next_state";
    private static final String TAG_JOB_CHAIN_NODE = "job_chain_node";
    private static final String ATTRIBUTE_JOB_CHAIN = "job_chain";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_STATE = "state";
    private static final String TAG_ADD_ORDER = "add_order";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String TAG_JOB = "job";
    private static final String ATTRIBUTE_ORDER = "order";
    private static final String ATTRIBUTE_TITLE = "title";
    private static final String TAG_RUN_TIME = "run_time";
    private static final String OPTION_CREATE_MOCK = "createMock";
    private static final String OPTION_CREATE_JOBCHAINS = "createJobChains";
    private static final String NEWLINE = "\n";
    private static final String ATTRIBUTE_LANGUAGE = "language";
    private static final String TAG_SCRIPT = "script";
    private static final String TAG_SPOOLER = "spooler";
    private static final String OPTION_TIMEOUT = "timeout";
    private static final String OPTION_CHANGEUSER = "changeuser";
    private static final String OPTION_VERBOSE = "v";
    private static final String OPTION_TARGET = "target";
    private static final String OPTIONS_SYSTAB = "systab";
    private static final String OPTION_CRONTAB = "crontab";
    private static final String CRON_REGEX_ALIAS = "(@reboot|@yearly|@annually|@monthly|@weekly|@daily|@midnight|@hourly)\\s+(.+)$";
    private static final String CRON_REGEX_COMMENT = "^\\s*#\\s*(.+)";
    private static final String CRON_REGEX_JOBNAME = "\\s*job_name\\s*=\\s*(.+)";
    private static final String CRON_REGEX_JOBTITLE = "\\s*job_title\\s*=\\s*(.+)";
    private static final String CRON_REGEX_JOB_TIMEOUT = "\\s*job_timeout\\s*=\\s*(.+)";
    private static final String CRON_REGEX_ENVIRONMENT = "^\\s*(\\w+)\\s*=\\s*(.+)";
    private static final String COMMAND_REGEX = "[^\\s]*/[^\\s]*";
    private static final String JOBNAME_REGEX = "(.*)_(\\d*)$";
    private static final String CRON_REGEX = "-?([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.+)$";
    private static final String CRON_REGEX_SYSTEM = "-?([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.+)$";
    private final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private final DocumentBuilder docBuilder;
    private final Pattern commandRegExPattern;
    private final Pattern jobNameRegExPattern;
    private final boolean flgCreateSubFolderStructure = true;
    private boolean systemCronTab = false;
    private boolean oldRunTime = false;
    private boolean usedNewRunTime = false;
    private boolean flgCreateAMock = false;
    private boolean flgCreateJobChainJobs = false;
    private String changeUserCommand = "";
    private String strCronLine = "";
    private HashSet<String> skipLines = new HashSet<String>();
    private HashSet<String> reservedJobNames = new HashSet<String>();
    private String timeout = "600";
    private String lastComment = "";

    public boolean isCreateAMock() {
        return flgCreateAMock;
    }

    public void setCreateAMock(final boolean createAMok) {
        flgCreateAMock = createAMok;
    }

    public boolean isCreateJobChainJobs() {
        return flgCreateJobChainJobs;
    }

    public void setCreateJobChainJobs(final boolean createJobChainJobs) {
        flgCreateJobChainJobs = createJobChainJobs;
    }

    public static void main(final String[] args) {
        LOGGER.info("SOS CronConverter - Main");
        try {
            SOSLogger sosLogger;
            String sourceFile = "";
            String targetFile = "";
            String changeUser = "";
            File source = null;
            File target = null;
            int logLevel = 0;
            boolean sysTab = false;
            boolean useOldRunTime = false;
            String jobTimeout = "";
            Options options = new Options();
            OptionBuilder.withArgName("0|1");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("set to 1 if source is the system crontab (with user field)");
            Option optSysTab = OptionBuilder.create(OPTIONS_SYSTAB);
            OptionBuilder.withArgName("file");
            OptionBuilder.hasArgs();
            OptionBuilder.isRequired();
            OptionBuilder.withDescription("crontab file");
            Option optSourceFile = OptionBuilder.create(OPTION_CRONTAB);
            OptionBuilder.withArgName("file");
            OptionBuilder.hasArgs();
            OptionBuilder.isRequired();
            OptionBuilder.withDescription("xml configuration file");
            Option optTargetFile = OptionBuilder.create(OPTION_TARGET);
            OptionBuilder.withArgName("level");
            OptionBuilder.hasArg();
            OptionBuilder.withType(new Integer(0));
            OptionBuilder.withDescription("loglevel [0=info] [1=debug1]...[9=debug]");
            Option optLogLevel = OptionBuilder.create(OPTION_VERBOSE);
            OptionBuilder.withArgName("command");
            OptionBuilder.hasArgs();
            OptionBuilder.withDescription("change user command for -systab=1. 'su' or 'sudo' or define your own command using $SCHEDULER_CRONTAB_USER.");
            Option optChangeUser = OptionBuilder.create(OPTION_CHANGEUSER);
            OptionBuilder.withArgName("seconds");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("job timeout (0 for unlimited");
            OptionBuilder.withArgName("true|false");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("set to true if script has to be mok'ed");
            Option optCreateMok = OptionBuilder.create(OPTION_CREATE_MOCK);
            OptionBuilder.withArgName("true|false");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("set to true if you want to create jobChains and jobs");
            Option optCreateJobChains = OptionBuilder.create(OPTION_CREATE_JOBCHAINS);
            options.addOption(optSysTab);
            options.addOption(optSourceFile);
            options.addOption(optTargetFile);
            options.addOption(optLogLevel);
            options.addOption(optChangeUser);
            options.addOption(optCreateMok);
            options.addOption(optCreateJobChains);
            CommandLineParser parser = new GnuParser();
            CommandLine line = null;
            try {
                line = parser.parse(options, args);
            } catch (Exception e) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("cronconverter", options, true);
                System.exit(0);
            }
            sourceFile = getWholeArgument(line.getOptionValues(OPTION_CRONTAB));
            if (sourceFile.equalsIgnoreCase(SYSTEM_CRONTAB_NAME)) {
                sysTab = true;
            }
            targetFile = getWholeArgument(line.getOptionValues(OPTION_TARGET));
            String ll = line.getOptionValue(OPTION_VERBOSE, "" + SOSStandardLogger.INFO);
            logLevel = Integer.parseInt(ll);
            if (line.hasOption(optSysTab.getOpt())) {
                sysTab = "1".equals(line.getOptionValue(optSysTab.getOpt()).trim());
            }
            useOldRunTime = line.hasOption("oldRunTime");
            changeUser = "";
            if (line.hasOption(OPTION_CHANGEUSER)) {
                changeUser = getWholeArgument(line.getOptionValues(OPTION_CHANGEUSER));
            }
            jobTimeout = line.getOptionValue(OPTION_TIMEOUT);
            if (logLevel == 0) {
                logLevel = SOSLogger.INFO;
            }
            sosLogger = new SOSStandardLogger(logLevel);
            target = new File(targetFile);
            source = new File(sourceFile);
            CrontabFileConverter cc = new CrontabFileConverter(sosLogger);
            if (jobTimeout != null && !jobTimeout.isEmpty()) {
                cc.setTimeout(jobTimeout);
            }
            cc.setChangeUserCommand(changeUser);
            if (line.hasOption(OPTION_CREATE_MOCK)) {
                cc.setCreateAMock(line.getOptionValue(OPTION_CREATE_MOCK).equalsIgnoreCase(TRUE));
            }
            if (line.hasOption(OPTION_CREATE_JOBCHAINS)) {
                cc.setCreateJobChainJobs(line.getOptionValue(OPTION_CREATE_JOBCHAINS).equalsIgnoreCase(TRUE));
            }
            cc.setSystemCronTab(sysTab);
            cc.oldRunTime = useOldRunTime;
            cc.cronFile2SchedulerXMLFile(source, target);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
        System.exit(0);
    }

    private static String getWholeArgument(final String[] optionValues) {
        String value = "";
        for (int i = 0; i < optionValues.length; i++) {
            value += optionValues[i];
            if (i + 1 < optionValues.length) {
                value += " ";
            }
        }
        return value;
    }

    public CrontabFileConverter(final SOSLogger log) throws Exception {
        docBuilder = docFactory.newDocumentBuilder();
        cronRegExAliasPattern = Pattern.compile(CRON_REGEX_ALIAS);
        cronRegExPattern = Pattern.compile(CRON_REGEX);
        cronRegExSystemPattern = Pattern.compile(CRON_REGEX_SYSTEM);
        cronRegExCommentPattern = Pattern.compile(CRON_REGEX_COMMENT);
        cronRegExEnvironmentPattern = Pattern.compile(CRON_REGEX_ENVIRONMENT);
        commandRegExPattern = Pattern.compile(COMMAND_REGEX);
        jobNameRegExPattern = Pattern.compile(JOBNAME_REGEX);
        cronRegExJobNamePattern = Pattern.compile(CRON_REGEX_JOBNAME);
        cronRegExJobTitlePattern = Pattern.compile(CRON_REGEX_JOBTITLE);
        cronRegExJobTimeoutPattern = Pattern.compile(CRON_REGEX_JOB_TIMEOUT);
        currentCronPattern = cronRegExPattern;
    }

    public void cronFile2SchedulerXMLFile(final File cronFile, final File schedulerXML) throws Exception {
        try {
            Document configurationDocument = cronFile2SchedulerXML(cronFile, new HashMap<String, Element>());
            LOGGER.debug("writing " + schedulerXML.getAbsolutePath());
            OutputStream fout = new FileOutputStream(schedulerXML, false);
            OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8");
            OutputFormat format = new OutputFormat(configurationDocument);
            format.setEncoding("UTF-8");
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.setNamespaces(true);
            serializer.serialize(configurationDocument);
            out.close();
        } catch (Exception e) {
            throw new JobSchedulerException("Error writing JobScheduler configuration file: " + e, e);
        }
    }

    public Document cronFile2SchedulerXML(final File cronFile, final HashMap<String, Element> cron2jobMapping) throws Exception {
        try {
            HashSet<String> jobNames = new HashSet<String>();
            if (reservedJobNames != null) {
                jobNames.addAll(reservedJobNames);
            }
            HashMap<String, String> environmentVariables = new HashMap<String, String>();
            Document configurationDoc = docBuilder.newDocument();
            Element spoolerElement = configurationDoc.createElement(TAG_SPOOLER);
            configurationDoc.appendChild(spoolerElement);
            Element configElement = configurationDoc.createElement("config");
            spoolerElement.appendChild(configElement);
            Element jobsElement = configurationDoc.createElement("jobs");
            configElement.appendChild(jobsElement);
            BufferedReader in = new BufferedReader(new FileReader(cronFile));
            Vector<Element> vecJobs = new Vector<Element>();
            Vector<String> vecCronRecords = new Vector<String>();
            lastComment = "";
            String lastCommentJobName = "";
            String lastCommentJobTitle = "";
            String lastCommentJobTimeout = "";
            while ((strCronLine = in.readLine()) != null) {
                if (strCronLine.trim().isEmpty()) {
                    lastComment = "";
                    continue;
                }
                if (skipLines != null && skipLines.contains(strCronLine)) {
                    LOGGER.debug("Skipping line " + strCronLine);
                    lastComment = "";
                    lastCommentJobName = "";
                    lastCommentJobTitle = "";
                    lastCommentJobTimeout = "";
                    continue;
                }
                Matcher commentMatcher = cronRegExCommentPattern.matcher(strCronLine);
                if (commentMatcher.matches()) {
                    Matcher jobNameMatcher = cronRegExJobNamePattern.matcher(commentMatcher.group(1));
                    Matcher jobTitleMatcher = cronRegExJobTitlePattern.matcher(commentMatcher.group(1));
                    Matcher jobTimeoutMatcher = cronRegExJobTimeoutPattern.matcher(commentMatcher.group(1));
                    if (jobNameMatcher.matches()) {
                        lastCommentJobName = jobNameMatcher.group(1);
                        LOGGER.debug("Found job name in comment: " + lastCommentJobName);
                        continue;
                    }
                    if (jobTitleMatcher.matches()) {
                        lastCommentJobTitle = jobTitleMatcher.group(1);
                        LOGGER.debug("Found job title in comment: " + lastCommentJobTitle);
                        continue;
                    }
                    if (jobTimeoutMatcher.matches()) {
                        lastCommentJobTimeout = jobTimeoutMatcher.group(1);
                        LOGGER.debug("Found job timeout in comment: " + lastCommentJobTimeout);
                        continue;
                    }
                    if (!lastComment.isEmpty()) {
                        lastComment += NEWLINE;
                    }
                    lastComment += commentMatcher.group(1);
                    continue;
                }
                Matcher environmentMatcher = cronRegExEnvironmentPattern.matcher(strCronLine);
                if (environmentMatcher.matches()) {
                    String envName = environmentMatcher.group(1);
                    String envValue = environmentMatcher.group(2);
                    LOGGER.debug("Found environment variable [" + envName + "]: " + envValue);
                    if (envValue.startsWith("\"") && envValue.endsWith("\"")) {
                        envValue = envValue.substring(1, envValue.length() - 1);
                    }
                    environmentVariables.put(envName, envValue);
                    lastComment = "";
                }
                Matcher cronMatcher = currentCronPattern.matcher(strCronLine);
                Matcher cronAliasMatcher = cronRegExAliasPattern.matcher(strCronLine);
                if (cronMatcher.matches() || cronAliasMatcher.matches()) {
                    vecCronRecords.add(strCronLine);
                    Document jobDocument = createJobElement(strCronLine, environmentVariables);
                    Element jobElement = jobDocument.getDocumentElement();
                    boolean jobNameChanged = false;
                    if (!lastCommentJobName.isEmpty()) {
                        jobElement.setAttribute(ATTRIBUTE_NAME, lastCommentJobName.replaceAll("/", "_"));
                        lastCommentJobName = "";
                    }
                    if (!lastCommentJobTitle.isEmpty()) {
                        jobElement.setAttribute(ATTRIBUTE_TITLE, lastCommentJobTitle);
                        lastCommentJobTitle = "";
                    }
                    if (!lastCommentJobTimeout.isEmpty()) {
                        jobElement.setAttribute(OPTION_TIMEOUT, lastCommentJobTimeout);
                        lastCommentJobTimeout = "";
                    }
                    String jobName = jobElement.getAttribute(ATTRIBUTE_NAME);
                    while (jobNames.contains(jobName)) {
                        jobName = incrementJobName(jobName);
                        jobNameChanged = true;
                    }
                    if (jobNameChanged) {
                        LOGGER.debug("Setting new job name \"" + jobName + "\"");
                        jobElement.setAttribute(ATTRIBUTE_NAME, jobName);
                    }
                    jobNames.add(jobName);
                    Node importedJob = configurationDoc.importNode(jobElement, true);
                    cron2jobMapping.put(strCronLine, jobElement);
                    if (!lastComment.isEmpty()) {
                        lastComment = lastComment.replaceAll("--", "__");
                        Comment jobComment = configurationDoc.createComment(lastComment);
                        jobsElement.appendChild(jobComment);
                    }
                    jobsElement.appendChild(importedJob);
                    lastComment = "";
                    vecJobs.add(jobElement);
                }
            }
            if (isCreateJobChainJobs()) {
                Element jobChainsElement = configurationDoc.createElement("job_chains");
                configElement.appendChild(jobChainsElement);
                int i = 0;
                for (Element objJobElement : vecJobs) {
                    strCronLine = vecCronRecords.get(i++);
                    Document objJCD = createJobChainElement(objJobElement);
                    Element objJC = objJCD.getDocumentElement();
                    jobChainsElement.appendChild(configurationDoc.importNode(objJC, true));
                }
                Element commandsElement = configurationDoc.createElement("commands");
                configElement.appendChild(commandsElement);
                i = 0;
                for (Element objJobElement : vecJobs) {
                    strCronLine = vecCronRecords.get(i++);
                    Document objJCD = createOrderElement(objJobElement);
                    Element objJC = objJCD.getDocumentElement();
                    commandsElement.appendChild(configurationDoc.importNode(objJC, true));
                }
            }
            in.close();
            return configurationDoc;
        } catch (Exception e) {
            throw new JobSchedulerException("Error converting file " + cronFile.getAbsolutePath() + " to JobScheduler XML: " + e, e);
        }
    }

    private String incrementJobName(String jobName) {
        Matcher jobNameMatcher = jobNameRegExPattern.matcher(jobName);
        if (jobNameMatcher.matches()) {
            String baseName = jobNameMatcher.group(1);
            String counter = jobNameMatcher.group(2);
            int iCounter = Integer.parseInt(counter);
            iCounter++;
            jobName = baseName + "_" + iCounter;
        } else {
            jobName = jobName + "_1";
        }
        return jobName;
    }

    public Document createJobElement(final String cronline) throws Exception {
        return createJobElement(cronline, new HashMap<String, String>());
    }

    public Document createJobChainElement(final Element pobjJobElement) throws DOMException, Exception {
        Document jobchain = docBuilder.newDocument();
        Element jobChainElement = jobchain.createElement(ATTRIBUTE_JOB_CHAIN);
        jobChainElement.setAttribute(ATTRIBUTE_NAME, pobjJobElement.getAttribute(ATTRIBUTE_NAME));
        jobChainElement.appendChild(createExtension(jobchain));
        Element jobChainNode = jobchain.createElement(TAG_JOB_CHAIN_NODE);
        jobChainNode.setAttribute(ATTRIBUTE_STATE, "100");
        jobChainNode.setAttribute(TAG_JOB, getNameWithoutPath(pobjJobElement.getAttribute(ATTRIBUTE_NAME)));
        jobChainNode.setAttribute(ATTRIBUTE_NEXT_STATE, STATE_SUCCESS);
        jobChainNode.setAttribute(ATTRIBUTE_ERROR_STATE, STATE_ERROR);
        jobChainElement.appendChild(jobChainNode);
        jobChainNode = jobchain.createElement(TAG_JOB_CHAIN_NODE);
        jobChainNode.setAttribute(ATTRIBUTE_STATE, STATE_SUCCESS);
        jobChainElement.appendChild(jobChainNode);
        jobChainNode = jobchain.createElement(TAG_JOB_CHAIN_NODE);
        jobChainNode.setAttribute(ATTRIBUTE_STATE, STATE_ERROR);
        jobChainElement.appendChild(jobChainNode);
        jobchain.appendChild(jobChainElement);
        return jobchain;
    }

    private Element createExtension(final Document pobjDoc) throws DOMException, Exception {
        Element objExtensions = pobjDoc.createElement("extensions");
        Element objExtension = pobjDoc.createElementNS("www.sos-berlin.com/schema/joe", "extension");
        Element objGenerator = pobjDoc.createElement("generator");
        objExtension.appendChild(objGenerator);
        objGenerator.setAttribute("name", "CronConverter");
        objGenerator.setAttribute("date", SOSDate.getCurrentTimeAsString());
        objGenerator.setAttribute("vendor", "www.sos-berlin.com");
        Element objComments = pobjDoc.createElement("comment");
        Node objCommentNode = pobjDoc.createCDATASection(lastComment);
        objComments.appendChild(objCommentNode);
        objExtension.appendChild(objComments);
        Element objDocu = pobjDoc.createElement("docu");
        Node objDocuNode = pobjDoc.createCDATASection(strCronLine);
        objDocu.appendChild(objDocuNode);
        objGenerator.appendChild(objDocu);
        objExtensions.appendChild(objExtension);
        return objExtensions;
    }

    public Document createOrderElement(final Element pobjJobElement) throws Exception {
        Document objOrderDocument = docBuilder.newDocument();
        Element addOrderElement = objOrderDocument.createElement(TAG_ADD_ORDER);
        String strOrderID = pobjJobElement.getAttribute(ATTRIBUTE_NAME);
        addOrderElement.setAttribute(ATTRIBUTE_ID, getNameWithoutPath(strOrderID));
        addOrderElement.setAttribute(ATTRIBUTE_TITLE, pobjJobElement.getAttribute(ATTRIBUTE_NAME));
        addOrderElement.setAttribute(ATTRIBUTE_JOB_CHAIN, pobjJobElement.getAttribute(ATTRIBUTE_NAME));
        addOrderElement.appendChild(createExtension(objOrderDocument));
        Element runTimeElement = objOrderDocument.createElement(TAG_RUN_TIME);
        LOGGER.debug(strCronLine);
        cronRegExPattern = Pattern.compile(CRON_REGEX);
        Matcher cronRegExMatcher = cronRegExPattern.matcher(strCronLine);
        createRunTime(cronRegExMatcher, runTimeElement);
        addOrderElement.appendChild(runTimeElement);
        objOrderDocument.appendChild(addOrderElement);
        return objOrderDocument;
    }

    private String getNameWithoutPath(final String pstrName) {
        String strRet = pstrName;
        int i = pstrName.lastIndexOf("/");
        if (i != -1) {
            strRet = pstrName.substring(i + 1);
        }
        return strRet;
    }

    public Document createJobElement(String cronline, final HashMap<String, String> environmentVariables) throws Exception {
        try {
            LOGGER.info("processing line: " + cronline);
            Document eleJob = docBuilder.newDocument();
            Element jobElement = eleJob.createElement(TAG_JOB);
            Matcher cronRegExAliasMatcher = cronRegExAliasPattern.matcher(cronline);
            if (cronRegExAliasMatcher.matches()) {
                LOGGER.debug("Current line matches pattern " + CRON_REGEX_ALIAS);
                cronline = convertAlias(cronRegExAliasMatcher);
            }
            Matcher cronRegExMatcher = cronRegExPattern.matcher(cronline);
            int commandIndex = 6;
            if (isSystemCronTab()) {
                commandIndex = 7;
                cronRegExMatcher = cronRegExSystemPattern.matcher(cronline);
            }
            if (!cronRegExMatcher.matches()) {
                throw new JobSchedulerException("Fail to parse cron line \"" + cronline + "\"");
            }
            String jobname = getJobName(cronRegExMatcher.group(commandIndex));
            jobElement.setAttribute(ATTRIBUTE_NAME, jobname);
            if (isCreateJobChainJobs()) {
                jobElement.setAttribute(ATTRIBUTE_ORDER, "yes");
            } else {
                jobElement.setAttribute(ATTRIBUTE_ORDER, "no");
            }
            jobElement.setAttribute(ATTRIBUTE_TITLE, "Cron Job " + cronRegExMatcher.group(commandIndex).trim());
            if (timeout != null && !"0".equals(timeout)) {
                jobElement.setAttribute(OPTION_TIMEOUT, timeout);
            }
            String schedulerUser = "";
            String command = cronRegExMatcher.group(commandIndex);
            if (isSystemCronTab()) {
                schedulerUser = cronRegExMatcher.group(6);
                command = (changeUserCommand + " " + command).trim();
            }
            jobElement.appendChild(createExtension(eleJob));
            if (!isCreateJobChainJobs()) {
                LOGGER.debug("Creating params element");
                Element paramsElement = eleJob.createElement("params");
                LOGGER.debug("Creating param element (command)");
                Element paramCommandElement = eleJob.createElement("param");
                paramCommandElement.setAttribute("name", "command");
                paramCommandElement.setAttribute("value", command);
                paramsElement.appendChild(paramCommandElement);
                jobElement.appendChild(paramsElement);
            }
            LOGGER.debug("Creating script element");
            Element scriptElement = eleJob.createElement(TAG_SCRIPT);
            scriptElement.setAttribute(ATTRIBUTE_LANGUAGE, "shell");
            String script = NEWLINE;
            if (!schedulerUser.isEmpty()) {
                script += "export SCHEDULER_CRONTAB_USER=" + schedulerUser + NEWLINE;
            }
            Iterator<String> envIter = environmentVariables.keySet().iterator();
            // set environment variables on job
            while (envIter.hasNext()) {
                String envName = envIter.next().toString();
                String envValue = environmentVariables.get(envName).toString();
                script += envName + "=" + envValue + NEWLINE;
                script += "export " + envName;
            }
            script += "echo created by CrontabFileConverter, at " + SOSDate.getCurrentTimeAsString() + NEWLINE;
            if (isCreateAMock()) {
                script += "echo mock-mode: " + command + NEWLINE;
                if (!strMockCommand.isEmpty()) {
                    script += strMockCommand + NEWLINE;
                }
                script += "exit 0" + NEWLINE;
            } else {
                script += command;
            }
            Node scriptData = eleJob.createCDATASection(script);
            scriptElement.appendChild(scriptData);
            jobElement.appendChild(scriptElement);
            if (!isCreateJobChainJobs()) {
                Element runTimeElement = eleJob.createElement(TAG_RUN_TIME);
                createRunTime(cronRegExMatcher, runTimeElement);
                if (usedNewRunTime && oldRunTime) {
                    // workaround while <month> Element is not available
                    // can later be deleted (keep only else branch)
                    usedNewRunTime = false;
                    Document runTimeDocument = docBuilder.newDocument();
                    runTimeDocument.appendChild(runTimeDocument.importNode(runTimeElement, true));
                    StringWriter out = new StringWriter();
                    OutputFormat format = new OutputFormat(runTimeDocument);
                    format.setIndenting(true);
                    format.setIndent(2);
                    format.setOmitXMLDeclaration(true);
                    XMLSerializer serializer = new XMLSerializer(out, format);
                    serializer.serialize(runTimeDocument);
                    Comment runTimeComment = eleJob.createComment("This run_time is currently not supported:\n" + out.toString());
                    jobElement.appendChild(runTimeComment);
                } else {
                    jobElement.appendChild(runTimeElement);
                }
            }
            eleJob.appendChild(jobElement);
            return eleJob;
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured creating job from cron line: " + cronline, e);
        }
    }

    private void createRunTime(final Matcher pcronRegExMatcher, final Element runTimeElement) throws Exception {
        try {
            if (!pcronRegExMatcher.matches()) {
                throw new JobSchedulerException("Fail to parse cron line \"" + strCronLine + "\", regexp is " + pcronRegExMatcher.toString());
            }
            String minutes = pcronRegExMatcher.group(1);
            String hours = pcronRegExMatcher.group(2);
            String days = pcronRegExMatcher.group(3);
            String months = pcronRegExMatcher.group(4);
            String weekdays = pcronRegExMatcher.group(5);
            if ("@reboot".equals(minutes)) {
                runTimeElement.setAttribute("once", "yes");
                return;
            }
            Vector<Element> childElements = new Vector<Element>();
            Element periodElement = runTimeElement.getOwnerDocument().createElement("period");
            LOGGER.debug("processing hours [" + hours + "] and minutes [" + minutes + "]");
            if (minutes.startsWith("*")) {
                if ("*".equalsIgnoreCase(minutes)) {
                    periodElement.setAttribute("repeat", "60");
                } else {
                    String repeat = minutes.substring(2);
                    repeat = formatTwoDigits(repeat);
                    periodElement.setAttribute("repeat", "00:" + repeat);
                }
                if (hours.startsWith("*")) {
                    if (!"*".equalsIgnoreCase(hours)) {
                        throw new JobSchedulerException("Combination of minutes and hours not supported: " + minutes + " " + hours);
                    }
                    childElements.add(periodElement);
                } else {
                    LOGGER.debug("Found specific hours, creating periods with begin and end.");
                    String[] hourArray = hours.split(",");
                    for (int i = 0; i < hourArray.length; i++) {
                        String currentHour = hourArray[i];
                        if (currentHour.indexOf("/") != -1) {
                            String[] additionalHours = getArrayFromColumn(currentHour);
                            hourArray = combineArrays(hourArray, additionalHours);
                            continue;
                        }
                        String[] currentHourArray = currentHour.split("-");
                        Element currentPeriodElement = (Element) periodElement.cloneNode(true);
                        String beginHour = currentHourArray[0];
                        int iEndHour = (Integer.parseInt(beginHour) + 1) % 24;
                        if (iEndHour == 0) {
                            iEndHour = 24;
                        }
                        String endHour = "" + iEndHour;
                        if (currentHourArray.length > 1) {
                            endHour = currentHourArray[1];
                        }
                        beginHour = formatTwoDigits(beginHour);
                        endHour = formatTwoDigits(endHour);
                        currentPeriodElement.setAttribute("begin", beginHour + ":00");
                        currentPeriodElement.setAttribute("end", endHour + ":00");
                        childElements.add(currentPeriodElement);
                    }
                }
            } else {
                String[] minutesArray = getArrayFromColumn(minutes);
                for (String element : minutesArray) {
                    Element currentPeriodElement = (Element) periodElement.cloneNode(true);
                    String currentMinute = element;
                    currentMinute = formatTwoDigits(currentMinute);
                    if (hours.startsWith("*")) {
                        currentPeriodElement.setAttribute("absolute_repeat", "01:00");
                        usedNewRunTime = true;
                        if (!"*".equalsIgnoreCase(hours)) {
                            String repeat = hours.substring(2);
                            repeat = formatTwoDigits(repeat);
                            currentPeriodElement.setAttribute("absolute_repeat", repeat + ":00");
                        }
                        currentPeriodElement.setAttribute("begin", "00:" + currentMinute);
                        childElements.add(currentPeriodElement);
                    } else {
                        String[] hourArray = hours.split(",");
                        for (String element2 : hourArray) {
                            currentPeriodElement = (Element) periodElement.cloneNode(true);
                            String currentHour = element2;
                            if (currentHour.indexOf("-") == -1) {
                                currentHour = formatTwoDigits(currentHour);
                                currentPeriodElement.setAttribute("single_start", currentHour + ":" + currentMinute);
                            } else {
                                String[] currentHourArray = currentHour.split("[-/]");
                                int beginHour = Integer.parseInt(currentHourArray[0]);
                                int endHour = Integer.parseInt(currentHourArray[1]);
                                int beginMinute = Integer.parseInt(currentMinute);
                                int endMinute = beginMinute + 1;
                                endMinute = beginMinute;
                                if (endMinute == 60) {
                                    endMinute = 0;
                                    endHour = endHour + 1;
                                }
                                endHour = endHour % 24;
                                if (endHour == 0) {
                                    endHour = 24;
                                }
                                String stepSize = "1";
                                if (currentHourArray.length == 3) {
                                    stepSize = formatTwoDigits(currentHourArray[2]);
                                }
                                currentPeriodElement.setAttribute("absolute_repeat", stepSize + ":00");
                                usedNewRunTime = true;
                                currentPeriodElement.setAttribute("begin", formatTwoDigits(beginHour) + ":" + formatTwoDigits(beginMinute));
                                currentPeriodElement.setAttribute("end", formatTwoDigits(endHour) + ":" + formatTwoDigits(endMinute));
                            }
                            childElements.add(currentPeriodElement);
                        }
                    }
                }
            }
            LOGGER.debug("processing days [" + days + "]");
            boolean monthDaysSet = false;
            if (days.startsWith("*")) {
                if (!"*".equals(days)) {
                    Element monthDaysElement = runTimeElement.getOwnerDocument().createElement("monthdays");
                    String repeat = days.substring(2);
                    int iRepeat = Integer.parseInt(repeat);
                    for (int i = 1; i <= 30; i = i + iRepeat) {
                        String day = "" + i;
                        addDay(day, monthDaysElement, childElements);
                    }
                    childElements.clear();
                    childElements.add(monthDaysElement);
                    monthDaysSet = true;
                }
            } else {
                Element monthDaysElement = runTimeElement.getOwnerDocument().createElement("monthdays");
                String[] daysArray = getArrayFromColumn(days);
                for (String day : daysArray) {
                    addDay(day, monthDaysElement, childElements);
                }
                childElements.clear();
                childElements.add(monthDaysElement);
                monthDaysSet = true;
            }
            if (!"*".equals(weekdays) && monthDaysSet) {
                LOGGER.info("Weekdays will not be processed as days are already set in current line.");
            } else {
                LOGGER.debug("processing weekdays [" + weekdays + "]");
                weekdays = replaceDayNames(weekdays);
                if (weekdays.startsWith("*/")) {
                    throw new JobSchedulerException("Repeat intervals for the weekdays column [" + weekdays
                            + "] are not supported. Please use the days column.");
                }
                if (!"*".equals(weekdays)) {
                    Element weekDaysElement = runTimeElement.getOwnerDocument().createElement("weekdays");
                    String[] daysArray = getArrayFromColumn(weekdays);
                    for (String day : daysArray) {
                        addDay(day, weekDaysElement, childElements);
                    }
                    childElements.clear();
                    childElements.add(weekDaysElement);
                }
            }
            LOGGER.debug("processing months [" + months + "]");
            if (months.startsWith("*")) {
                if (!"*".equals(months)) {
                    months = replaceMonthNames(months);
                    Vector<Element> newChildElements = new Vector<Element>();
                    String repeat = months.substring(2);
                    int iRepeat = Integer.parseInt(repeat);
                    for (int i = 1; i <= 12; i = i + iRepeat) {
                        String month = "" + i;
                        Element monthElement = runTimeElement.getOwnerDocument().createElement("month");
                        usedNewRunTime = true;
                        monthElement.setAttribute("month", month);
                        Iterator<Element> iter = childElements.iterator();
                        while (iter.hasNext()) {
                            Element child = iter.next();
                            monthElement.appendChild(child.cloneNode(true));
                        }
                        newChildElements.add(monthElement);
                    }
                    childElements = newChildElements;
                }
            } else {
                Vector<Element> newChildElements = new Vector<Element>();
                String[] monthArray = getArrayFromColumn(months);
                for (String month : monthArray) {
                    Element monthElement = runTimeElement.getOwnerDocument().createElement("month");
                    usedNewRunTime = true;
                    monthElement.setAttribute("month", month);
                    Iterator<Element> iter = childElements.iterator();
                    while (iter.hasNext()) {
                        Element child = iter.next();
                        monthElement.appendChild(child.cloneNode(true));
                    }
                    newChildElements.add(monthElement);
                }
                childElements = newChildElements;
            }
            Iterator<Element> iter = childElements.iterator();
            while (iter.hasNext()) {
                Element someElement = iter.next();
                runTimeElement.appendChild(someElement);
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Error creating run time: " + e, e);
        }
    }

    private static String[] combineArrays(final String[] hourArray, final String[] additionalHours) {
        String[] newArray = new String[hourArray.length + additionalHours.length];
        for (int i = 0; i < hourArray.length; i++) {
            newArray[i] = hourArray[i];
        }
        for (int i = 0; i < additionalHours.length; i++) {
            newArray[i + hourArray.length] = additionalHours[i];
        }
        return newArray;
    }

    private void addDay(final String day, final Element parentDaysElement, final Vector<Element> childElements) throws Exception {
        LOGGER.debug("adding day: " + day);
        Element dayElement = parentDaysElement.getOwnerDocument().createElement("day");
        dayElement.setAttribute("day", day);
        Iterator<Element> iter = childElements.iterator();
        while (iter.hasNext()) {
            Element child = iter.next();
            dayElement.appendChild(child.cloneNode(true));
        }
        parentDaysElement.appendChild(dayElement);
    }

    private String[] getArrayFromColumn(final String column) {
        String[] elements = column.split(",");
        Vector<String> result = new Vector<String>();
        for (String element : elements) {
            if (element.indexOf("-") == -1) {
                result.add(element);
            } else {
                String[] range = element.split("[-/]");
                if (range.length < 2 || range.length > 3) {
                    try {
                        LOGGER.warn("unknown crontab synthax: " + element);
                    } catch (Exception e) {
                        //
                    }
                } else {
                    int from = Integer.parseInt(range[0]);
                    int to = Integer.parseInt(range[1]);
                    int stepSize = 1;
                    if (range.length == 3) {
                        stepSize = Integer.parseInt(range[2]);
                    }
                    for (int j = from; j <= to; j = j + stepSize) {
                        result.add("" + j);
                    }
                }
            }
        }
        String[] dummy = new String[1];
        return result.toArray(dummy);
    }

    private static String replaceDayNames(String element) {
        element = element.replaceAll("(?i)mon", "1");
        element = element.replaceAll("(?i)tue", "2");
        element = element.replaceAll("(?i)wed", "3");
        element = element.replaceAll("(?i)thu", "4");
        element = element.replaceAll("(?i)fri", "5");
        element = element.replaceAll("(?i)sat", "6");
        element = element.replaceAll("(?i)sun", "7");
        return element;
    }

    private static String replaceMonthNames(String element) {
        element = element.replaceAll("(?i)jan", "1");
        element = element.replaceAll("(?i)feb", "2");
        element = element.replaceAll("(?i)mar", "3");
        element = element.replaceAll("(?i)apr", "4");
        element = element.replaceAll("(?i)may", "5");
        element = element.replaceAll("(?i)jun", "6");
        element = element.replaceAll("(?i)jul", "7");
        element = element.replaceAll("(?i)aug", "8");
        element = element.replaceAll("(?i)sep", "9");
        element = element.replaceAll("(?i)oct", "10");
        element = element.replaceAll("(?i)nov", "11");
        element = element.replaceAll("(?i)dec", "12");
        return element;
    }

    private String getJobName(String pstrCommand) {
        Matcher commandMatcher = commandRegExPattern.matcher(pstrCommand);
        if (commandMatcher.find()) {
            pstrCommand = commandMatcher.group();
        } else {
            int space = pstrCommand.indexOf(" ");
            if (space != -1) {
                pstrCommand = pstrCommand.substring(0, space);
            }
        }
        if (pstrCommand.startsWith("\"")) {
            pstrCommand = pstrCommand.substring(1);
        }
        if (pstrCommand.endsWith("\"")) {
            pstrCommand = pstrCommand.substring(0, pstrCommand.length() - 1);
        }
        if (pstrCommand.startsWith("/") || pstrCommand.startsWith("\\")) {
            pstrCommand = pstrCommand.substring(1);
        }
        int i = pstrCommand.indexOf("}");
        if (i >= 0) {
            pstrCommand = pstrCommand.substring(i + 1);
        }
        if (!flgCreateSubFolderStructure) {
            pstrCommand = pstrCommand.replaceAll("/", "_");
            if (pstrCommand.startsWith("_")) {
                pstrCommand = pstrCommand.substring(1);
            }
        } else if (pstrCommand.startsWith("/")) {
            pstrCommand = pstrCommand.substring(1);
        }
        if (!strBaseDirectory.isEmpty()) {
            pstrCommand = strBaseDirectory + "/" + pstrCommand;
        }
        return pstrCommand;
    }

    private String convertAlias(final Matcher matcher) throws Exception {
        LOGGER.debug("Converting alias...");
        try {
            String alias = matcher.group(1);
            String rest = matcher.group(2);
            String result = "";
            if ("@yearly".equals(alias) || "@annually".equals(alias)) {
                result = "0 0 1 1 * ";
            } else if ("@monthly".equals(alias)) {
                result = "0 0 1 * * ";
            } else if ("@weekly".equals(alias)) {
                result = "0 0 * * 0 ";
            } else if ("@daily".equals(alias) || "@midnight".equals(alias)) {
                result = "0 0 * * * ";
            } else if ("@hourly".equals(alias)) {
                result = "0 * * * * ";
            } else if ("@reboot".equals(alias)) {
                result = "@reboot @reboot @reboot @reboot @reboot";
            }
            result += rest;
            LOGGER.debug("Alias converted to " + result);
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException("Error converting alias: " + e, e);
        }
    }

    private static String formatTwoDigits(final String number) {
        if (number.length() == 1) {
            return "0" + number;
        }
        return number;
    }

    private static String formatTwoDigits(final int number) {
        return formatTwoDigits("" + number);
    }

    public boolean isSystemCronTab() {
        return systemCronTab;
    }

    public void setSystemCronTab(final boolean systemCronTab) {
        if (systemCronTab) {
            currentCronPattern = cronRegExSystemPattern;
        } else {
            currentCronPattern = cronRegExPattern;
        }
        this.systemCronTab = systemCronTab;
    }

    protected boolean isOldRunTime() {
        return oldRunTime;
    }

    protected void setOldRunTime(final boolean oldRunTime) {
        this.oldRunTime = oldRunTime;
    }

    public HashSet<String> getSkipLines() {
        return skipLines;
    }

    public void setSkipLines(final HashSet<String> skipLines) {
        this.skipLines = skipLines;
    }

    public HashSet<String> getReservedJobNames() {
        return reservedJobNames;
    }

    public void setReservedJobNames(final HashSet<String> reservedJobNames) {
        this.reservedJobNames = reservedJobNames;
    }

    protected DocumentBuilder getDocBuilder() {
        return docBuilder;
    }

    public String getChangeUserCommand() {
        return changeUserCommand;
    }

    public void setChangeUserCommand(String changeUserCommand) {
        if ("su".equalsIgnoreCase(changeUserCommand)) {
            changeUserCommand = "su $SCHEDULER_CRONTAB_USER -c";
        } else if ("sudo".equalsIgnoreCase(changeUserCommand)) {
            changeUserCommand = "sudo -u $SCHEDULER_CRONTAB_USER";
        }
        this.changeUserCommand = changeUserCommand;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

}