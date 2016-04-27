package sos.scheduler.job;

import java.io.File;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import sos.net.SOSMail;
import sos.settings.SOSConnectionSettings;

/** @author andreas pueschel
 * @author ghassan beydoun */
public class JobSchedulerMailJob extends JobSchedulerJob {

    protected SOSMail sosMail = null;
    protected SOSConnectionSettings sosMailSettings = null;
    protected boolean hasDatabase = false;
    protected String tableMails = "MAILS";
    protected String tableMailAttachments = "MAIL_ATTACHMENTS";
    protected String tableMailSettings = "SETTINGS";
    protected String applicationMail = "email";
    protected String sectionMail = "mail_server";
    protected String applicationMailTemplates = "email_templates";
    protected String sectionMailTemplates = "mail_templates";
    protected String applicationMailTemplatesFactory = "email_templates_factory";
    protected String sectionMailTemplatesFactory = "mail_templates";
    protected String applicationMailScripts = "email";
    protected String sectionMailScripts = "mail_start_scripts_factory";
    protected boolean hasLocalizedTemplates = true;
    protected String queueDirectory = "";
    protected String failedPrefix = "failed.";
    protected String queuePrefix = "sos.";
    protected String queuePattern = "yyyy-MM-dd.HHmmss.S";
    protected String queuePrefixSpec = "^(sos.*)(?<!\\~)$";
    protected String mailTo = "";
    protected String logDirectory = "";
    protected boolean logOnly = false;
    private String tableMailBouncePatterns = "MAIL_BOUNCE_PATTERNS";
    private String tableMailBounces = "MAIL_BOUNCES";
    private String tableMailBounceDeliveries = "MAIL_BOUNCE_DELIVERIES";
    private String tableMailBouncePatternRetries = "MAIL_BOUNCE_PATTERN_RETRIES";
    public static final int STATE_READY = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_CANCEL = 1001;
    public static final int STATE_ERROR = 1002;
    long maxDeliveryCounter = 0;

    public boolean spooler_init() {
        try {
            if (!super.spooler_init()) {
                return false;
            }
            this.setQueueDirectory(spooler_log.mail().queue_dir());
            try {
                if (spooler_task != null) {
                    if (spooler_task.params().var("db") != null
                            && !spooler_task.params().var("db").isEmpty()
                            && ("true".equalsIgnoreCase(spooler_task.params().var("db")) || "yes".equalsIgnoreCase(spooler_task.params().var("db")) || "1"
                                    .equals(spooler_task.params().var("db")))) {
                        this.setHasDatabase(true);
                        spooler_log.debug6(".. job parameter [db]: " + this.hasDatabase());
                    }
                    if (spooler_task.params().var("table_mails") != null && !spooler_task.params().var("table_mails").isEmpty()) {
                        this.setTableMails(spooler_task.params().var("table_mails"));
                        spooler_log.debug6(".. job parameter [table_mails]: " + this.getTableMails());
                    }
                    if (spooler_task.params().var("table_mail_attachments") != null && !spooler_task.params().var("table_mail_attachments").isEmpty()) {
                        this.setTableMailAttachments(spooler_task.params().var("table_mail_attachments"));
                        spooler_log.debug6(".. job parameter [table_mail_attachments]: " + this.getTableMailAttachments());
                    }
                    if (spooler_task.params().var("table_settings") != null && !spooler_task.params().var("table_settingss").isEmpty()) {
                        this.setTableMailSettings(spooler_task.params().var("table_settings"));
                        spooler_log.debug6(".. job parameter [table_settings]: " + this.getTableMailSettings());
                    }
                    if (spooler_task.params().var("application_mail") != null && !spooler_task.params().var("application_mail").isEmpty()) {
                        this.setApplicationMail(spooler_task.params().var("application_mail"));
                        spooler_log.debug6(".. job parameter [application_mail]: " + this.getApplicationMail());
                    }
                    if (spooler_task.params().var("section_mail") != null && !spooler_task.params().var("section_mail").isEmpty()) {
                        this.setSectionMail(spooler_task.params().var("section_mail"));
                        spooler_log.debug6(".. job parameter [section_mail]: " + this.getSectionMail());
                    }
                    if (spooler_task.params().var("application_mail_templates") != null && !spooler_task.params().var("application_mail_templates").isEmpty()) {
                        this.setApplicationMailTemplates(spooler_task.params().var("application_mail_templates"));
                        spooler_log.debug6(".. job parameter [application_mail_templates]: " + this.getApplicationMailTemplates());
                    }
                    if (spooler_task.params().var("section_mail_templates") != null && !spooler_task.params().var("section_mail_templates").isEmpty()) {
                        this.setSectionMailTemplates(spooler_task.params().var("section_mail_templates"));
                        spooler_log.debug6(".. job parameter [section_mail_templates]: " + this.getSectionMailTemplates());
                    }
                    if (spooler_task.params().var("application_mail_templates_factory") != null
                            && !spooler_task.params().var("application_mail_templates_factory").isEmpty()) {
                        this.setApplicationMailTemplatesFactory(spooler_task.params().var("application_mail_templates_factory"));
                        spooler_log.debug6(".. job parameter [application_mail_templates_factory]: " + this.getApplicationMailTemplatesFactory());
                    }
                    if (spooler_task.params().var("section_mail_templates_factory") != null
                            && !spooler_task.params().var("section_mail_templates_factory").isEmpty()) {
                        this.setSectionMailTemplatesFactory(spooler_task.params().var("section_mail_templates_factory"));
                        spooler_log.debug6(".. job parameter [section_mail_templates_factory]: " + this.getSectionMailTemplatesFactory());
                    }
                    if (spooler_task.params().var("mail_to") != null && !spooler_task.params().var("mail_to").isEmpty()) {
                        this.setMailTo(spooler_task.params().var("mail_to"));
                        spooler_log.debug6(".. job parameter [mail_to]: " + this.getMailTo());
                    }
                    if (spooler_task.params().var("queue_prefix") != null && !spooler_task.params().var("queue_prefix").isEmpty()) {
                        this.setQueuePrefix(spooler_task.params().var("queue_prefix"));
                        spooler_log.debug6(".. job parameter [queue_prefix]: " + this.getQueuePrefix());
                    }
                    if (spooler_task.params().var("queue_prefix_spec") != null && !spooler_task.params().var("queue_prefix_spec").isEmpty()) {
                        this.setQueuePrefixSpec(spooler_task.params().var("queue_prefix_spec"));
                        spooler_log.debug6(".. job parameter [queue_prefix_spec]: " + this.getQueuePrefixSpec());
                    }
                    if (spooler_task.params().var("queue_directory") != null && !spooler_task.params().var("queue_directory").isEmpty()) {
                        this.setQueueDirectory(spooler_task.params().var("queue_directory"));
                        spooler_log.debug6(".. job parameter [queue_directory]: " + this.getQueueDirectory());
                    }
                    if (spooler_task.params().var("log_directory") != null && !spooler_task.params().var("log_directory").isEmpty()) {
                        this.setLogDirectory(spooler_task.params().var("log_directory"));
                        spooler_log.debug6(".. job parameter [log_directory]: " + this.getLogDirectory());
                    }
                    if (spooler_task.params().var("log_only") != null && !spooler_task.params().var("log_only").isEmpty()) {
                        this.setLogOnly("1".equals(spooler_task.params().var("log_only")) || "true".equalsIgnoreCase(spooler_task.params().var("log_only"))
                                || "yes".equalsIgnoreCase(spooler_task.params().var("log_only")) ? true : false);
                        spooler_log.debug6(".. job parameter [log_only]: " + this.isLogOnly());
                    }
                    if (spooler_task.params().var("max_delivery") != null && !spooler_task.params().var("max_delivery").isEmpty()) {
                        this.setMaxDeliveryCounter(Long.parseLong(spooler_task.params().var("max_delivery")));
                        spooler_log.debug6(".. job parameter [max_delivery]: " + this.getMaxDeliveryCounter());
                    }
                }
            } catch (Exception e) {
                throw new Exception("an error occurred processing job parameters: " + e.getMessage());
            }
            try {
                if (this.getConnection() != null && this.hasDatabase()) {
                    this.sosMailSettings = new SOSConnectionSettings(this.getConnection(), this.getTableMailSettings(), this.getApplicationMail(),
                            this.getSectionMail(), this.getLogger());
                    if (this.sosMailSettings.getSectionEntry("mail_to") != null && !this.sosMailSettings.getSectionEntry("mail_to").isEmpty()) {
                        this.setMailTo(this.sosMailSettings.getSectionEntry("mail_to"));
                        spooler_log.debug6(".. job settings [mail_to]: " + this.getMailTo());
                    }
                    if (this.sosMailSettings.getSectionEntry("mail_queue_prefix") != null
                            && !this.sosMailSettings.getSectionEntry("mail_queue_prefix").isEmpty()) {
                        this.setQueuePrefix(this.sosMailSettings.getSectionEntry("mail_queue_prefix"));
                        spooler_log.debug6(".. job settings [mail_queue_prefix]: " + this.getQueuePrefix());
                    }
                    if (this.sosMailSettings.getSectionEntry("queue_prefix") != null && !this.sosMailSettings.getSectionEntry("queue_prefix").isEmpty()) {
                        this.setQueuePrefix(this.sosMailSettings.getSectionEntry("queue_prefix"));
                        spooler_log.debug6(".. job settings [queue_prefix]: " + this.getQueuePrefix());
                    }
                    if (this.sosMailSettings.getSectionEntry("mail_queue_prefix_spec") != null
                            && !this.sosMailSettings.getSectionEntry("mail_queue_prefix_spec").isEmpty()) {
                        this.setQueuePrefixSpec(this.sosMailSettings.getSectionEntry("mail_queue_prefix_spec"));
                        spooler_log.debug6(".. job settings [mail_queue_prefix_spec]: " + this.getQueuePrefixSpec());
                    }
                    if (this.sosMailSettings.getSectionEntry("queue_prefix_spec") != null
                            && !this.sosMailSettings.getSectionEntry("queue_prefix_spec").isEmpty()) {
                        this.setQueuePrefixSpec(this.sosMailSettings.getSectionEntry("queue_prefix_spec"));
                        spooler_log.debug6(".. job settings [queue_prefix_spec]: " + this.getQueuePrefix());
                    }
                    if (this.sosMailSettings.getSectionEntry("mail_queue_directory") != null
                            && !this.sosMailSettings.getSectionEntry("mail_queue_directory").isEmpty()) {
                        this.setQueueDirectory(this.sosMailSettings.getSectionEntry("mail_queue_directory"));
                        spooler_log.debug6(".. job settings [mail_queue_directory]: " + this.getQueueDirectory());
                    }
                    if (this.sosMailSettings.getSectionEntry("queue_directory") != null && !this.sosMailSettings.getSectionEntry("queue_directory").isEmpty()) {
                        this.setQueueDirectory(this.sosMailSettings.getSectionEntry("queue_directory"));
                        spooler_log.debug6(".. job settings [queue_directory]: " + this.getQueueDirectory());
                    }
                    if (this.sosMailSettings.getSectionEntry("mail_log_directory") != null
                            && !this.sosMailSettings.getSectionEntry("mail_log_directory").isEmpty()) {
                        this.setLogDirectory(this.sosMailSettings.getSectionEntry("mail_log_directory"));
                        spooler_log.debug6(".. job settings [mail_log_directory]: " + this.getLogDirectory());
                    }
                    if (this.sosMailSettings.getSectionEntry("log_directory") != null && !this.sosMailSettings.getSectionEntry("log_directory").isEmpty()) {
                        this.setLogDirectory(this.sosMailSettings.getSectionEntry("log_directory"));
                        spooler_log.debug6(".. job settings [log_directory]: " + this.getLogDirectory());
                    }
                    if (this.sosMailSettings.getSectionEntry("mail_log_only") != null && !this.sosMailSettings.getSectionEntry("mail_log_only").isEmpty()) {
                        this.setLogOnly(("1".equalsIgnoreCase(this.sosMailSettings.getSectionEntry("mail_log_only"))
                                || "true".equalsIgnoreCase(this.sosMailSettings.getSectionEntry("mail_log_only")) || "yes"
                                .equalsIgnoreCase(this.sosMailSettings.getSectionEntry("mail_log_only"))) ? true : false);
                        spooler_log.debug6(".. job settings [mail_log_only]: " + this.isLogOnly());
                    }
                    if (this.sosMailSettings.getSectionEntry("log_only") != null && !this.sosMailSettings.getSectionEntry("log_only").isEmpty()) {
                        this.setLogOnly(("1".equalsIgnoreCase(this.sosMailSettings.getSectionEntry("log_only"))
                                || "true".equalsIgnoreCase(this.sosMailSettings.getSectionEntry("log_only")) || "yes".equalsIgnoreCase(this.sosMailSettings
                                .getSectionEntry("log_only"))) ? true : false);
                        spooler_log.debug6(".. job settings [log_only]: " + this.isLogOnly());
                    }
                    if (this.sosMailSettings.getSectionEntry("max_delivery") != null && !this.sosMailSettings.getSectionEntry("max_delivery").isEmpty()) {
                        this.setMaxDeliveryCounter(Long.parseLong(this.sosMailSettings.getSectionEntry("max_delivery")));
                        spooler_log.debug6(".. job settings [max_delivery]: " + this.getMaxDeliveryCounter());
                    }
                }
            } catch (Exception e) {
                throw new Exception("could not process settings: " + e.getMessage());
            }
            return true;
        } catch (Exception e) {
            spooler_log.warn("failed to initialize job: " + e.getMessage());
            return false;
        }
    }

    public File getMailFile(String path) throws Exception {
        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(this.getQueuePattern());
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }
        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);
        String lastGeneratedFilename = path + this.getQueuePrefix() + b + ".email";
        File f = new File(lastGeneratedFilename);
        while (f.exists()) {
            b = s.format(d, bb, fp);
            lastGeneratedFilename = path + this.getQueuePrefix() + b + ".email";
            f = new File(lastGeneratedFilename);
        }
        return f;
    }

    public String getApplicationMail() {
        return applicationMail;
    }

    public void setApplicationMail(String applicationMail) {
        this.applicationMail = applicationMail;
    }

    public String getApplicationMailTemplates() {
        return applicationMailTemplates;
    }

    public void setApplicationMailTemplates(String applicationMailTemplates) {
        this.applicationMailTemplates = applicationMailTemplates;
    }

    public String getSectionMail() {
        return sectionMail;
    }

    public void setSectionMail(String sectionMail) {
        this.sectionMail = sectionMail;
    }

    public String getSectionMailTemplates() {
        return sectionMailTemplates;
    }

    public void setSectionMailTemplates(String sectionMailTemplates) {
        this.sectionMailTemplates = sectionMailTemplates;
    }

    public String getTableMailAttachments() {
        return tableMailAttachments;
    }

    public void setTableMailAttachments(String tableMailAttachments) {
        this.tableMailAttachments = tableMailAttachments;
    }

    public String getTableMails() {
        return tableMails;
    }

    public void setTableMails(String tableMails) {
        this.tableMails = tableMails;
    }

    public String getTableMailSettings() {
        return tableMailSettings;
    }

    public void setTableMailSettings(String tableMailSettings) {
        this.tableMailSettings = tableMailSettings;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }

    public boolean isLogOnly() {
        return logOnly;
    }

    public void setLogOnly(boolean logOnly) {
        this.logOnly = logOnly;
    }

    public String getQueueDirectory() {
        return queueDirectory;
    }

    public void setQueueDirectory(String queueDirectory) {
        this.queueDirectory = queueDirectory;
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    public void setQueuePrefix(String queuePrefix) {
        this.queuePrefix = queuePrefix;
    }

    public String getQueuePrefixSpec() {
        return queuePrefixSpec;
    }

    public void setQueuePrefixSpec(String queuePrefixSpec) {
        this.queuePrefixSpec = queuePrefixSpec;
    }

    public long getMaxDeliveryCounter() {
        return maxDeliveryCounter;
    }

    public void setMaxDeliveryCounter(long maxDeliveryCounter) {
        this.maxDeliveryCounter = maxDeliveryCounter;
    }

    public String getFailedPrefix() {
        return failedPrefix;
    }

    public void setFailedPrefix(String failedPrefix) {
        this.failedPrefix = failedPrefix;
    }

    public String getQueuePattern() {
        return queuePattern;
    }

    public void setQueuePattern(String queuePattern) {
        this.queuePattern = queuePattern;
    }

    public String getApplicationMailTemplatesFactory() {
        return applicationMailTemplatesFactory;
    }

    public void setApplicationMailTemplatesFactory(String applicationMailTemplatesFactory) {
        this.applicationMailTemplatesFactory = applicationMailTemplatesFactory;
    }

    public String getSectionMailTemplatesFactory() {
        return sectionMailTemplatesFactory;
    }

    public void setSectionMailTemplatesFactory(String sectionMailTemplatesFactory) {
        this.sectionMailTemplatesFactory = sectionMailTemplatesFactory;
    }

    public boolean hasLocalizedTemplates() {
        return hasLocalizedTemplates;
    }

    public void setHasLocalizedTemplates(boolean hasLocalizedTemplates) {
        this.hasLocalizedTemplates = hasLocalizedTemplates;
    }

    public String getApplicationMailScripts() {
        return applicationMailScripts;
    }

    public void setApplicationMailScripts(String applicationMailScripts) {
        this.applicationMailScripts = applicationMailScripts;
    }

    public String getSectionMailScripts() {
        return sectionMailScripts;
    }

    public void setSectionMailScripts(String sectionMailScripts) {
        this.sectionMailScripts = sectionMailScripts;
    }

    public boolean hasDatabase() {
        return hasDatabase;
    }

    public void setHasDatabase(boolean hasDatabase) {
        this.hasDatabase = hasDatabase;
    }

    public String getTableMailBouncePatterns() {
        return tableMailBouncePatterns;
    }

    public void setTableMailBouncePatterns(String tableMailBouncePatterns) {
        this.tableMailBouncePatterns = tableMailBouncePatterns;
    }

    public String getTableMailBounces() {
        return tableMailBounces;
    }

    public void setTableMailBounces(String tableMailBounces) {
        this.tableMailBounces = tableMailBounces;
    }

    public String getTableMailBounceDeliveries() {
        return tableMailBounceDeliveries;
    }

    public void setTableMailBounceDeliveries(String tableMailBounceDeliveries) {
        this.tableMailBounceDeliveries = tableMailBounceDeliveries;
    }

    public String getTableMailBouncePatternRetries() {
        return tableMailBouncePatternRetries;
    }

    public void setTableMailBouncePatternRetries(String tableMailBouncePatternRetries) {
        this.tableMailBouncePatternRetries = tableMailBouncePatternRetries;
    }

}