package sos.scheduler.cron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sos.scheduler.misc.SchedulerJavaObject;
import sos.spooler.Job;
import sos.spooler.Variable_set;
import sos.util.SOSFile;
import sos.util.SOSFileOperations;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;
import sos.xml.SOSXMLXPath;

public class JobSchedulerCronAdapter extends sos.spooler.Job_impl {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCronAdapter.class);
    private static final String XPATH_SPOOLER_ANSWER_ERROR = "//spooler/answer/ERROR";
    private File schedulerCronConfigurationDir = new File("config/live/cron");
    private String crontab = "";
    private SOSLogger logger;
    private CronConverter converter;
    private boolean useDynamicConfiguration = false;
    private String monitoredLiveDir = null;
    private String[] monitoredLiveDirs = null;
    private final Vector<File> liveDirCrontabs = new Vector<File>();
    private Iterator<File> liveDirCrontabsIterator = null;
    public static final String conAttributeJobNAME = "name";
    public static final String conJobSchedulerJobFileNameExtension = ".job.xml";

    @Override
    public boolean spooler_init() throws Exception {
        File liveFolder = new File(spooler.configuration_directory());
        schedulerCronConfigurationDir = new File(liveFolder, "cron");
        logger = new SOSSchedulerLogger(spooler_log);
        monitoredLiveDir = spooler.variables().value("cron_adapter_dynamic_configuration_dir");
        if (monitoredLiveDir != null && !monitoredLiveDir.isEmpty()) {
            spooler_log.info("parameter cron_adapter_dynamic_configuration_dir: " + monitoredLiveDir);
            monitoredLiveDirs = monitoredLiveDir.split(";");
        } else {
            monitoredLiveDir = null;
        }
        if (spooler_task == null) {
            spooler.variables().set_var("cron_adapter_use_dynamic_configuration", "1");
            if (monitoredLiveDir == null) {
                spooler_log.info("Using dynamic configuration files. Deleting files in " + schedulerCronConfigurationDir.getAbsolutePath());
                SOSFileOperations.removeFile(schedulerCronConfigurationDir, logger);
            } else {
                for (String currentDir : monitoredLiveDirs) {
                    File fDir = new File(currentDir);
                    if (!fDir.exists()) {
                        spooler_log.info(fDir.getAbsolutePath() + " doesn't exist.");
                        continue;
                    }
                    spooler_log.debug5("Looking for " + crontab + " files in " + currentDir);
                    Vector<File> crontabsInCurrentDir = SOSFile.getFilelist(currentDir, "^" + crontab + "$", 0, true);
                    Iterator<File> iter = crontabsInCurrentDir.iterator();
                    while (iter.hasNext()) {
                        File crontabFile = iter.next();
                        spooler_log.info("Deleting configuration files in " + crontabFile.getParent());
                        SOSFileOperations.removeFile(crontabFile.getParentFile(), ".*\\.xml$", SOSFileOperations.GRACIOUS, logger);
                    }
                }
            }
            return true;
        }
        boolean systemCrontab = false;
        boolean oldRunTime = false;
        String systemUser = System.getProperty("user.name");
        String sChangeUser = "";
        String timeout = "";
        try {
            Variable_set params = spooler_task.params();
            crontab = params.value("crontab");
            if (monitoredLiveDir != null && crontab.length() == 0) {
                crontab = "crontab";
            }
            if (crontab.isEmpty()) {
                throw new Exception("missing parameter crontab");
            }
            logger.info("parameter crontab: " + crontab);
            String dc = spooler.variables().var("cron_adapter_use_dynamic_configuration");
            if (dc != null && "1".equals(dc)) {
                useDynamicConfiguration = true;
                if (monitoredLiveDir == null) {
                    logger.info("Using dynamic configuration files in directory " + schedulerCronConfigurationDir.getAbsolutePath());
                }
            }
            if ("/etc/crontab".equalsIgnoreCase(crontab)) {
                systemCrontab = true;
            }
            String sOldRunTime = params.value("old_run_time");
            if ("1".equalsIgnoreCase(sOldRunTime) || "true".equalsIgnoreCase(sOldRunTime) || "yes".equalsIgnoreCase(sOldRunTime)) {
                oldRunTime = true;
                logger.info("parameter old_run_time: true");
            }
            String sSystemCrontab = params.value("systab");
            if (sSystemCrontab != null && !sSystemCrontab.isEmpty()) {
                if ("1".equalsIgnoreCase(sSystemCrontab) || "true".equalsIgnoreCase(sSystemCrontab) || "yes".equalsIgnoreCase(sSystemCrontab)) {
                    systemCrontab = true;
                } else {
                    systemCrontab = false;
                }
                logger.info("parameter systab: " + sSystemCrontab);
            }
            sChangeUser = params.value("changeuser");
            if (sChangeUser != null && !sChangeUser.isEmpty()) {
                logger.info("parameter changeuser: " + sChangeUser);
            } else {
                sChangeUser = "";
            }
            if (systemCrontab && "su".equalsIgnoreCase(sChangeUser) && !"root".equalsIgnoreCase(systemUser)) {
                logger.warn("You are running the Job Scheduler as " + systemUser + " and you are trying to use a system crontab with 'su'. "
                        + "This will not work. Either run the Job Scheduler as root, or use 'sudo' as change_user_command parameter");
            }
            timeout = params.value("timeout");
            if (timeout != null && !timeout.isEmpty()) {
                logger.info("parameter timeout: " + timeout);
            }
        } catch (Exception e) {
            LOGGER.error("Error reading job parameters: " + e.getMessage(), e);
            return false;
        }
        try {
            converter = new CronConverter(logger);
            converter.setOldRunTime(oldRunTime);
            converter.setSystemCronTab(systemCrontab);
            converter.setChangeUserCommand(sChangeUser);
            if (timeout != null && !timeout.isEmpty()) {
                converter.setTimeout(timeout);
            }
            if (monitoredLiveDir != null) {
                for (String currentDir : monitoredLiveDirs) {
                    File fDir = new File(currentDir);
                    if (!fDir.exists()) {
                        spooler_log.info(fDir.getAbsolutePath() + " doesn't exist.");
                        continue;
                    }
                    spooler_log.debug5("Looking for crontab files in " + currentDir);
                    Vector<File> crontabsInCurrentDir = SOSFile.getFilelist(currentDir, "^crontab$", 0, true);
                    Iterator<File> iter = crontabsInCurrentDir.iterator();
                    while (iter.hasNext()) {
                        File crontabFile = iter.next();
                        spooler_log.debug7(" found: " + crontabFile.getAbsolutePath());
                        liveDirCrontabs.add(crontabFile);
                    }
                }
                liveDirCrontabsIterator = liveDirCrontabs.iterator();
                if (!liveDirCrontabsIterator.hasNext()) {
                    spooler_log.info("No crontabs found.");
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error initializing cron converter: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            File crontabFile = new File(crontab);
            if (liveDirCrontabsIterator != null) {
                crontabFile = liveDirCrontabsIterator.next();
                schedulerCronConfigurationDir = crontabFile.getParentFile();
            }
            spooler_job.start_when_directory_changed(crontabFile.getParentFile(), "^" + crontabFile.getName() + "$");
            if (!crontabFile.canRead()) {
                logger.info("Failed to read crontab " + crontabFile.getAbsolutePath());
            }
            HashMap<String, Element> previousMapping = (HashMap<String, Element>) SchedulerJavaObject.getObject(spooler.variables(), spooler_job.name() + "_"
                    + crontabFile.getAbsolutePath() + "_cron2job_mapping");
            if (previousMapping == null) {
                previousMapping = new HashMap<String, Element>();
            }
            HashMap<String, String> previousCommentsMapping = (HashMap<String, String>) SchedulerJavaObject.getObject(spooler.variables(), spooler_job.name()
                    + "_" + crontabFile.getAbsolutePath() + "_cron2comments_mapping");
            if (previousCommentsMapping == null) {
                previousCommentsMapping = new HashMap<String, String>();
            }
            debugHashMap(previousCommentsMapping, "previousCommentsMapping");
            HashMap<String, String> currentCommentsMapping = new HashMap<String, String>();
            HashMap<String, Element> changedJobs = new HashMap<String, Element>();
            HashSet<?> previousEnvVariables = (HashSet<?>) SchedulerJavaObject.getObject(spooler.variables(),
                    spooler_job.name() + "_" + crontabFile.getAbsolutePath() + "_env_variables");
            HashSet<String> currentEnvVariables = new HashSet<String>();
            boolean environmentChanged = false;
            if (previousEnvVariables == null) {
                previousEnvVariables = new HashSet<Object>();
            }
            logger.debug1("Comparing new jobs with jobs of previous run.");
            changedJobs.putAll(previousMapping);
            converter.getReservedJobNames().clear();
            converter.getSkipLines().clear();
            Set<String> previousCronLines = previousMapping.keySet();
            BufferedReader in = new BufferedReader(new StringReader(""));
            if (crontabFile.canRead()) {
                in = new BufferedReader(new FileReader(crontabFile));
            }
            String currentLine = "";
            String currentCommentJobName = "";
            String currentCommentJobTitle = "";
            String currentCommentJobTimeout = "";
            while ((currentLine = in.readLine()) != null) {
                Matcher envMatcher = converter.cronRegExEnvironmentPattern.matcher(currentLine);
                if (envMatcher.matches()) {
                    currentEnvVariables.add(currentLine);
                    if (!previousEnvVariables.contains(currentLine)) {
                        environmentChanged = true;
                    }
                    continue;
                }
                Matcher commentMatcher = converter.cronRegExCommentPattern.matcher(currentLine);
                if (commentMatcher.matches()) {
                    Matcher jobNameMatcher = converter.cronRegExJobNamePattern.matcher(commentMatcher.group(1));
                    Matcher jobTitleMatcher = converter.cronRegExJobTitlePattern.matcher(commentMatcher.group(1));
                    Matcher jobTimeoutMatcher = converter.cronRegExJobTimeoutPattern.matcher(commentMatcher.group(1));
                    if (jobNameMatcher.matches()) {
                        currentCommentJobName = jobNameMatcher.group(1);
                    }
                    if (jobTitleMatcher.matches()) {
                        currentCommentJobTitle = jobTitleMatcher.group(1);
                    }
                    if (jobTimeoutMatcher.matches()) {
                        currentCommentJobTimeout = jobTimeoutMatcher.group(1);
                    }
                    continue;
                }
                if (previousCronLines.contains(currentLine)) {
                    Element jobElement = previousMapping.get(currentLine);
                    String jobName = jobElement.getAttribute(conAttributeJobNAME);
                    String previousCommentForThisJob = previousCommentsMapping.get(currentLine).toString();
                    String commentForThisJob = currentCommentJobName + "-" + currentCommentJobTitle + "-" + currentCommentJobTimeout;
                    if (!previousCommentForThisJob.equals(commentForThisJob)) {
                        logger.debug6("Job-Manipulating comments for current line have changed.");
                    } else {
                        logger.debug6("current line was already submitted in last run: " + currentLine);
                        converter.getSkipLines().add(currentLine);
                        converter.getReservedJobNames().add(jobName);
                        changedJobs.remove(currentLine);
                    }
                }
                Matcher cronMatcher = converter.currentCronPattern.matcher(currentLine);
                if (cronMatcher.matches()) {
                    currentCommentsMapping.put(currentLine, currentCommentJobName + "-" + currentCommentJobTitle + "-" + currentCommentJobTimeout);
                    currentCommentJobName = "";
                    currentCommentJobTitle = "";
                    currentCommentJobTimeout = "";
                }
            }
            if (environmentChanged) {
                if (previousMapping.size() > 0) {
                    logger.info("Environment has changed, all jobs need to be submitted again.");
                }
                converter.getSkipLines().clear();
                converter.getReservedJobNames().clear();
            }
            HashSet<String> changedJobNames = new HashSet<String>();
            Collection<Element> changedJobElements = changedJobs.values();
            Iterator<Element> iter = changedJobElements.iterator();
            while (iter.hasNext()) {
                Element changedJob = iter.next();
                changedJobNames.add(changedJob.getAttribute(conAttributeJobNAME));
            }
            HashMap<String, Element> updatedMapping = new HashMap<String, Element>();
            if (crontabFile.canRead()) {
                converter.cronFile2SchedulerXML(crontabFile, updatedMapping);
            }
            logger.debug1("updating changed jobs");
            Iterator<Element> updatedJobsIterator = updatedMapping.values().iterator();
            while (updatedJobsIterator.hasNext()) {
                Element updatedJob = updatedJobsIterator.next();
                String updatedJobName = updatedJob.getAttribute(conAttributeJobNAME);
                changedJobNames.remove(updatedJobName);
                updateJob(updatedJob);
            }
            if (!changedJobNames.isEmpty()) {
                logger.debug1("removing renamed/deleted jobs");
                Iterator<String> removedJobsIter = changedJobNames.iterator();
                while (removedJobsIter.hasNext()) {
                    String jobName = removedJobsIter.next().toString();
                    removeJob(jobName);
                }
            }
            Iterator<String> changedJobsIter = changedJobs.keySet().iterator();
            while (changedJobsIter.hasNext()) {
                previousMapping.remove(changedJobsIter.next());
            }
            previousMapping.putAll(updatedMapping);
            logger.debug3("Storing mapping to Scheduler variable");
            SchedulerJavaObject.putObject(previousMapping, spooler.variables(), spooler_job.name() + "_" + crontabFile.getAbsolutePath() + "_cron2job_mapping");
            logger.debug3("Storing comments mapping to Scheduler variable");
            debugHashMap(currentCommentsMapping, "currentCommentsMapping");
            SchedulerJavaObject.putObject(currentCommentsMapping, spooler.variables(), spooler_job.name() + "_" + crontabFile.getAbsolutePath()
                    + "_cron2comments_mapping");
            logger.debug3("Storing environment variables to Scheduler variable");
            SchedulerJavaObject
                    .putObject(currentEnvVariables, spooler.variables(), spooler_job.name() + "_" + crontabFile.getAbsolutePath() + "_env_variables");
        } catch (Exception e) {
            LOGGER.error("Error updating Job Scheduler configuration from crontab: " + e.getMessage(), e);
        }
        if (liveDirCrontabsIterator != null) {
            return liveDirCrontabsIterator.hasNext();
        }
        return false;
    }

    private void debugHashMap(final HashMap<String, String> map, final String name) throws Exception {
        logger.debug9(name + ":");
        Set<String> keys = map.keySet();
        if (keys != null) {
            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String key = iter.next().toString();
                String value = map.get(key).toString();
                logger.debug9("[" + key + "]: " + value);
            }
        }
    }

    private void removeJob(final String jobName) {
        try {
            if (useDynamicConfiguration) {
                removeJobFile(jobName);
            } else {
                removeJobXMLCommand(jobName);
            }
            logger.info(String.format("job '%1$s' removed from JS."));
        } catch (Exception e) {
            try {
                logger.warn("Failed to remove job \"" + jobName + "\": " + e);
            } catch (Exception ex) {
            }
        }
    }

    private void removeJobXMLCommand(final String jobName) throws Exception {
        Job currentJob = spooler.job(jobName);
        if (currentJob == null) {
            throw new Exception("Could not find job: " + jobName);
        }
        currentJob.remove();
    }

    private void removeJobFile(final String jobName) throws Exception {
        String fileName = normalizeJobName(jobName);
        File jobFile = new File(schedulerCronConfigurationDir, fileName + conJobSchedulerJobFileNameExtension);
        if (!jobFile.delete()) {
            throw new Exception("Failed to delete file " + jobFile.getAbsolutePath());
        }
    }

    private void updateJob(final Element updatedJob) throws Exception {
        Document jobDocument;
        try {
            jobDocument = converter.getDocBuilder().newDocument();
            jobDocument.appendChild(jobDocument.importNode(updatedJob, true));
        } catch (Exception e) {
            throw new Exception("Error creating DOM Document for job: " + e.getMessage(), e);
        }
        if (useDynamicConfiguration) {
            updateJobFile(jobDocument);
        } else {
            updateJobXMLCommand(jobDocument);
        }
        Element objJob = jobDocument.getDocumentElement();
        String jobName = objJob.getAttribute(conAttributeJobNAME);
        logger.info(String.format("job '%1$s' updated in JS.", jobName));
    }

    private void updateJobXMLCommand(final Document updatedJobDoc) throws Exception {
        String jobName = "";
        try {
            Element updatedJob = updatedJobDoc.getDocumentElement();
            jobName = updatedJob.getAttribute(conAttributeJobNAME);
            StringWriter out = new StringWriter();
            OutputFormat format = new OutputFormat(updatedJobDoc);
            format.setEncoding("UTF-8");
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.setNamespaces(true);
            serializer.serialize(updatedJobDoc);
            logger.info("submitting job...  " + jobName);
            logger.debug9(out.toString());
            String answer = spooler.execute_xml(out.toString());
            logger.debug3("answer from JobScheduler: " + answer);
            SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(answer));
            String errorMsg = "";
            if (xpath.selectSingleNodeValue(XPATH_SPOOLER_ANSWER_ERROR) != null) {
                String errorCode = xpath.selectSingleNodeValue(XPATH_SPOOLER_ANSWER_ERROR + "/@code");
                String errorText = xpath.selectSingleNodeValue(XPATH_SPOOLER_ANSWER_ERROR + "/@text");
                errorMsg = errorCode + ":" + errorText;
            }
            if (!"".equals(errorMsg)) {
                throw new Exception("JobScheduler answer ERROR:" + errorMsg + "\n" + answer);
            }
        } catch (Exception e) {
            throw new Exception("Error occured updating job \"" + jobName + "\": " + e.getMessage(), e);
        }
    }

    private void updateJobFile(final Document updatedJobDoc) throws Exception {
        String jobName = "";
        File jobFile = null;
        try {
            Element updatedJob = updatedJobDoc.getDocumentElement();
            jobName = updatedJob.getAttribute(conAttributeJobNAME);
            String fileName = normalizeJobName(jobName);
            updatedJob.removeAttribute(conAttributeJobNAME);
            jobFile = new File(schedulerCronConfigurationDir, fileName + conJobSchedulerJobFileNameExtension);
            OutputStream fout = new FileOutputStream(jobFile, false);
            OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8");
            OutputFormat format = new OutputFormat(updatedJobDoc);
            format.setEncoding("UTF-8");
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(out, format);
            logger.debug3("Writing file " + jobFile.getAbsolutePath());
            serializer.serialize(updatedJobDoc);
            out.close();
        } catch (Exception e) {
            if (jobFile != null) {
                throw new Exception("Error occured updating job file \"" + jobFile.getAbsolutePath() + "\": " + e.getMessage(), e);
            } else {
                throw new Exception("Error occured updating file for job \"" + jobName + "\": " + e.getMessage(), e);
            }
        }
    }

    private static String normalizeJobName(final String jobName) {
        String fileName = jobName.replaceAll("\\W", "_");
        return fileName;
    }

}
