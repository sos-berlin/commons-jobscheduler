package sos.scheduler.job;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import sos.connection.SOSConnection;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.settings.SOSProfileSettings;
import sos.spooler.Job_impl;
import sos.util.SOSArguments;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;

/** @author andreas pueschel */
public class JobSchedulerTriggerSanityCheck extends Job_impl {

    protected SOSProfileSettings jobSettings = null;
    private SOSSchedulerLogger sosLogger = null;
    private SOSConnection sosConnection = null;
    private Connection connection = null;
    private Properties jobProperties = null;
    private Properties checkCategories = null;
    private Vector checkSchedulers = null;
    private String jobName = "scheduler_check_sanity";

    public boolean spooler_init() {
        ArrayList references = null;
        try {
            this.setLogger(new SOSSchedulerLogger(this.spooler.log()));
            try {
                this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
                this.setJobProperties(this.jobSettings.getSection("spooler"));
                if (this.getJobProperties().isEmpty()) {
                    throw new Exception("no settings found in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (this.getJobProperties().getProperty("db") == null || this.getJobProperties().getProperty("db").isEmpty()) {
                    throw new Exception("no settings found for entry [db] in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (this.getJobProperties().getProperty("db_class") == null || this.getJobProperties().getProperty("db_class").isEmpty()) {
                    throw new Exception("no settings found for entry [db_class] in section [spooler] of configuration file: " + spooler.ini_path());
                }
                if (this.getLogger() != null) {
                    sosLogger.debug6("connecting to database.. .");
                }
                String dbProperty = this.getJobProperties().getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
                dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
                SOSArguments arguments = new SOSArguments(dbProperty);
                this.setConnection(SOSConnection.createInstance(this.getJobProperties().getProperty("db_class"), arguments.as_string("-class=", ""),
                        arguments.as_string("-url=", ""), arguments.as_string("-user=", ""), arguments.as_string("-password=", ""),
                        (SOSLogger) new SOSSchedulerLogger(this.spooler_log)));
                this.getConnection().connect();
                if (this.getLogger() != null) {
                    this.getLogger().debug6("..successfully connected to " + arguments.as_string("-url=", ""));
                }
            } catch (Exception e) {
                throw new Exception("connect to database failed: " + e.getMessage(), e);
            }
            try {
                this.checkSchedulers = new Vector();
                HashMap schedulerEntry = new HashMap();
                schedulerEntry.put("host", "localhost");
                schedulerEntry.put("port", "4444");
                this.checkSchedulers.add(schedulerEntry);
            } catch (Exception e) {
                throw new Exception("retrieval of schedulers for sanity check failed: " + e.getMessage());
            }
            try {
                if (spooler_task.params().var("check_scheduler_host") != null && !spooler_task.params().var("check_scheduler_host").isEmpty()) {
                    if ("true".equalsIgnoreCase(spooler_task.params().var("check_scheduler_host"))
                            || "yes".equalsIgnoreCase(spooler_task.params().var("check_scheduler_host"))
                            || "1".equalsIgnoreCase(spooler_task.params().var("check_scheduler_host"))) {
                        this.checkCategories.put("check_scheduler_host", "yes");
                        if (this.getLogger() != null) {
                            this.getLogger().info(".. job parameter [check_scheduler_host]: yes");
                        }
                    } else {
                        this.checkCategories.put("check_scheduler_host", "no");
                        if (this.getLogger() != null) {
                            this.getLogger().info(".. job parameter [check_scheduler_host]: no");
                        }
                    }
                }
                if (spooler_task.params().var("check_disk_space") != null && !spooler_task.params().var("check_disk_space").isEmpty()) {
                    if ("true".equalsIgnoreCase(spooler_task.params().var("check_disk_space"))
                            || "yes".equalsIgnoreCase(spooler_task.params().var("check_disk_space"))
                            || "1".equalsIgnoreCase(spooler_task.params().var("check_disk_space"))) {
                        this.checkCategories.put("check_disk_space", "yes");
                        if (this.getLogger() != null) {
                            this.getLogger().info(".. job parameter [check_disk_space]: yes");
                        }
                    } else {
                        this.checkCategories.put("check_disk_space", "no");
                        if (this.getLogger() != null) {
                            this.getLogger().info(".. job parameter [check_disk_space]: no");
                        }
                    }
                }
                if (spooler_task.params().var("check_memory_size") != null && !spooler_task.params().var("check_memory_size").isEmpty()) {
                    if ("true".equalsIgnoreCase(spooler_task.params().var("check_memory_size"))
                            || "yes".equalsIgnoreCase(spooler_task.params().var("check_memory_size"))
                            || "1".equalsIgnoreCase(spooler_task.params().var("check_memory_size"))) {
                        this.checkCategories.put("check_memory_size", "yes");
                        if (this.getLogger() != null) {
                            this.getLogger().info(".. job parameter [check_memory_size]: yes");
                        }
                    } else {
                        this.checkCategories.put("check_memory_size", "no");
                        if (this.getLogger() != null) {
                            this.getLogger().info(".. job parameter [check_memory_size]: no");
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("parameter check failed: " + e.getMessage(), e);
            }
            return true;
        } catch (Exception e) {
            if (this.getLogger() != null) {
                try {
                    this.getLogger().error(e.getMessage());
                } catch (Exception ex) {
                }
            }
            return false;
        }
    }

    public boolean spooler_process() {
        String jobContent = "";
        try {
            this.setLogger(new SOSSchedulerLogger(this.spooler.log()));
            try {
                jobContent = "<start_job job=\"" + this.jobName + "\"><params>";
                Iterator categoryIterator = checkCategories.keySet().iterator();
                while (categoryIterator.hasNext()) {
                    String category = (String) categoryIterator.next();
                    if (category != null && !category.isEmpty() && checkCategories.containsKey(category)) {
                        jobContent += "<param name=\"" + category + "\" value=\"" + checkCategories.get(category).toString() + "\"/>";
                    }
                }
                jobContent += "</params></start_job>";
                Iterator schedulerIterator = this.checkSchedulers.iterator();
                while (schedulerIterator.hasNext()) {
                    HashMap checkScheduler = (HashMap) schedulerIterator.next();
                    SOSSchedulerCommand command = new SOSSchedulerCommand(checkScheduler.get("host").toString(), Integer.parseInt(checkScheduler.get("port")
                            .toString()), "udp");
                    command.connect();
                    command.sendRequest(jobContent);
                    command.disconnect();
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage(), e);
            }
        } catch (Exception e) {
            spooler_log.error("error occurred starting sanity check for remote scheduler: " + e.getMessage());
        }
        return false;
    }

    public Properties getJobProperties() {
        return this.jobProperties;
    }

    public void setJobProperties(Properties jobProperties) {
        this.jobProperties = jobProperties;
    }

    public SOSProfileSettings getJobSettings() {
        return this.jobSettings;
    }

    public void setJobSettings(SOSProfileSettings jobSettings) {
        this.jobSettings = jobSettings;
    }

    public SOSConnection getConnection() {
        return this.sosConnection;
    }

    public void setConnection(SOSConnection sosConnection) {
        this.sosConnection = sosConnection;
    }

    public SOSSchedulerLogger getLogger() {
        return sosLogger;
    }

    public void setLogger(SOSSchedulerLogger sosLogger) {
        this.sosLogger = sosLogger;
    }

}