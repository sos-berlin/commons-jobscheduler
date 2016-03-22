/*
 * ManagedReporter.java Created on 26.10.2005
 */
package sos.scheduler.managed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import sos.net.SOSMail;
import sos.net.SOSMailOrder;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSSettings;
import sos.spooler.Task;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.util.SOSLogger;

/** Helper class for jobs which send out reports
 *
 * @author Andreas Liebert */
public class ManagedReporter {

    // Job settings, used by the reporter
    private SOSSettings jobSettings;

    // mail settings from the database
    private SOSSettings sosMailSettings;

    protected String tableMailSettings = "SETTINGS";

    /** application name for mail settings */
    protected String applicationMail = "email";

    /** section name for mail settings */
    protected String sectionMail = "mail_server";

    // Logger object of the job
    private SOSLogger logger;

    // Mail Object for sending out the report
    private SOSMail mail;

    // Task object of the calling job
    private Task spooler_task;

    // calling job
    private JobSchedulerManagedJob job;

    // is the job running as an order job?
    private boolean orderJob;

    // Hashmap containing replacements for body, subject...
    private HashMap replacements = new HashMap();

    // body template
    private String body = "";

    // subject template
    private String subject = "Report [taskid]";

    // Payload of the order
    private Variable_set orderPayload;

    // Report file
    private File attach;

    // delete attachFile?
    private boolean deleteAttach = false;

    // should reports be sent even if there is no result?
    private boolean sendIfNoResult = true;

    // should reports be sent if there is a result?
    private boolean sendIfResult = true;

    // does the current report have a result?
    private boolean hasResult = true;

    // log dir (order or job parameter)
    private String logDirectory;

    /** instantiates a reporter object
     * 
     * @param job the job which sends out the report
     * @throws Exception */
    public ManagedReporter(JobSchedulerManagedJob job) throws Exception {
        this.setJobSettings(job.getJobSettings());
        this.setLogger(job.getLogger());
        this.job = job;
        spooler_task = job.spooler_task;

        // sosMailSettings lesen: (wenn vorhanden)
        if (spooler_task.params().var("application_mail") != null) {
            if (spooler_task.params().var("application_mail").length() > 0) {
                this.setApplicationMail(spooler_task.params().var("application_mail"));
                getLogger().debug6(".. job parameter [application_mail]: " + this.getApplicationMail());
            }
        }

        if (spooler_task.params().var("section_mail") != null) {
            if (spooler_task.params().var("section_mail").length() > 0) {
                this.setSectionMail(spooler_task.params().var("section_mail"));
                getLogger().debug6(".. job parameter [section_mail]: " + this.getSectionMail());
            }
        }
        if (spooler_task.params().var("table_settings") != null) {
            if (spooler_task.params().var("table_settingss").length() > 0) {
                this.setTableMailSettings(spooler_task.params().var("table_settings"));
                getLogger().debug6(".. job parameter [table_settings]: " + this.getTableMailSettings());
            }
        }

        try {
            sosMailSettings = new SOSConnectionSettings(job.getConnection(), this.getTableMailSettings(), this.getApplicationMail(), this.getSectionMail(), getLogger());

            getLogger().debug3("MailSettings: " + sosMailSettings.getSection().size());
        } catch (Exception e) {
            getLogger().debug3("MailSettings were not found.");
            sosMailSettings = null;
        }
        String mailServer = "";
        String logMailFrom = "";
        String mailUser = "";
        String mailPassword = "";
        try {
            Properties spoolProp = getJobSettings().getSection("spooler");
            Properties smtpProp = getJobSettings().getSection("smtp");
            mailServer = spoolProp.getProperty("smtp");
            logMailFrom = spoolProp.getProperty("log_mail_from");

            mailUser = smtpProp.getProperty("mail.smtp.user");
            if (mailUser.equals("")) {
                mailUser = spoolProp.getProperty("mail.smtp.user");
            }
            mailPassword = smtpProp.getProperty("mail.smtp.password");
            if (mailPassword.equals("")) {
                mailPassword = spoolProp.getProperty("mail.smtp.password");
            }

        } catch (Exception e) {
        }
        boolean hasSOSMailOrder = false;
        if (job.getConnection() != null) {
            try {
                String result = job.getConnection().getSingleValue("SELECT COUNT(*) FROM MAILS");
                if (result.length() > 0)
                    hasSOSMailOrder = true;
            } catch (Exception e) {
                getLogger().debug3("Table MAILS was not found.");
            }
        }
        try {
            if (job.getConnection() == null) {
                getLogger().debug7("Initializing SOSMail without database.");
                mail = new SOSMail(mailServer);
                getLogger().debug9("Setting mail sender: " + logMailFrom);
                mail.setFrom(logMailFrom);
                mail.setUser(mailUser);
                mail.setPassword(mailPassword);
                mail.setCharset("ISO-8859-1");
                mail.setQueueDir(job.spooler_log.mail().queue_dir());
            } else if (sosMailSettings != null && sosMailSettings.getSection() != null && sosMailSettings.getSection().size() > 0) {
                if (hasSOSMailOrder) {
                    getLogger().debug7("Initializing SOSMailOrder with Mail Settings");
                    mail = new SOSMailOrder(sosMailSettings, job.getConnection());
                } else {
                    getLogger().debug7("Initializing SOSMail with Mail Settings");
                    mail = new SOSMail(sosMailSettings);
                }
            } else {
                if (hasSOSMailOrder) {
                    getLogger().debug7("Initializing SOSMailOrder without Mail Settings");
                    mail = new SOSMailOrder(mailServer, job.getConnection());
                } else {
                    getLogger().debug7("Initializing SOSMail without Mail Settings");
                    mail = new SOSMail(mailServer);
                }
                getLogger().debug9("Setting mail sender: " + logMailFrom);
                mail.setFrom(logMailFrom);
                if (mailUser != null) {
                    getLogger().debug9("Setting mail user: " + mailUser);
                    mail.setUser(mailUser);
                }
                if (mailPassword != null) {
                    getLogger().debug9("Setting mail password xxxxxxxx");
                    mail.setPassword(mailPassword);
                }
                mail.setCharset("ISO-8859-1");
                if (job.spooler_log.mail() != null && job.spooler_log.mail().queue_dir() != null) {
                    getLogger().debug9("Setting mail queue dir: " + job.spooler_log.mail().queue_dir());
                    mail.setQueueDir(job.spooler_log.mail().queue_dir());
                }
            }
            mail.setSOSLogger(getLogger());
        } catch (Exception e) {
            throw new Exception("Failed to initialize SOSMail class." + e, e);
        }
    }

    /** generates a file object for the report. The filename is generated using
     * configured replacements. The report has to be written to the file after
     * calling this function and before calling report()
     * 
     * @see ManagedReporter#report()
     * @return file object for the report
     * @throws Exception */
    public File getReportFile() throws Exception {
        orderJob = !(spooler_task.job().order_queue() == null);

        String fileName = "report_[date]_[taskid].xml";
        orderPayload = job.getOrderPayload();

        try {

            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_mailto") != null
                    && getOrderPayload().var("scheduler_order_report_mailto").length() > 0) {
                String[] mails = getOrderPayload().var("scheduler_order_report_mailto").split(",");
                for (int i = 0; i < mails.length; i++) {
                    mail.addRecipient(mails[i].trim());
                }
                job.debugParamter(getOrderPayload(), "scheduler_order_report_mailto");
                if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_mailcc") != null
                        && getOrderPayload().var("scheduler_order_report_mailcc").length() > 0) {
                    String[] mailscc = getOrderPayload().var("scheduler_order_report_mailcc").split(",");
                    for (int i = 0; i < mailscc.length; i++) {
                        mail.addCC(mailscc[i].trim());
                    }
                    job.debugParamter(getOrderPayload(), "scheduler_order_report_mailcc");
                }
                if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_mailbcc") != null
                        && getOrderPayload().var("scheduler_order_report_mailbcc").length() > 0) {
                    String[] mailsbcc = getOrderPayload().var("scheduler_order_report_mailbcc").split(",");
                    for (int i = 0; i < mailsbcc.length; i++) {
                        mail.addBCC(mailsbcc[i].trim());
                    }
                    job.debugParamter(getOrderPayload(), "scheduler_order_report_mailbcc");
                }
            } else {
                getLogger().debug6("Using mail recipients from job.");
                mail.addRecipient(job.spooler_log.mail().to());
                if (job.spooler_log.mail().cc() != null && job.spooler_log.mail().cc().length() > 0) {
                    mail.addCC(job.spooler_log.mail().cc());
                }
                if (job.spooler_log.mail().bcc() != null && job.spooler_log.mail().bcc().length() > 0) {
                    mail.addBCC(job.spooler_log.mail().bcc());
                }
            }

            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_subject") != null
                    && getOrderPayload().var("scheduler_order_report_subject").length() > 0) {
                subject = (getOrderPayload().var("scheduler_order_report_subject"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_subject");
            }
            subject = replace(subject);
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_body") != null
                    && getOrderPayload().var("scheduler_order_report_body").length() > 0) {
                body = (getOrderPayload().var("scheduler_order_report_body"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_body");
            }
            body = replace(body);
            logDirectory = "";
            if (getOrderPayload() != null && getOrderPayload().var("log_directory") != null && getOrderPayload().var("log_directory").length() > 0) {
                logDirectory = getOrderPayload().var("log_directory");
                job.debugParamter(getOrderPayload(), "log_directory");
            }

            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_filename") != null
                    && getOrderPayload().var("scheduler_order_report_filename").length() > 0) {
                fileName = getOrderPayload().var("scheduler_order_report_filename");
                job.debugParamter(getOrderPayload(), "scheduler_order_report_filename");
            }

            fileName = replace(fileName);
            getLogger().debug6("Output filename: " + fileName);
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_path") != null
                    && getOrderPayload().var("scheduler_order_report_path").length() > 0) {
                String path = getOrderPayload().var("scheduler_order_report_path");
                job.debugParamter(getOrderPayload(), "scheduler_order_report_path");
                if (!path.endsWith("/") && !path.endsWith("\\") && path.length() > 0)
                    path = path + "/";
                path = replace(path);
                File reportPath = new File(path);
                if (!reportPath.exists())
                    reportPath.mkdirs();
                attach = new File(path + fileName);
                deleteAttach = false;
            } else {
                String path = System.getProperty("java.io.tmpdir");
                if (!path.endsWith("/") && !path.endsWith("\\") && path.length() > 0)
                    path = path + "/";
                attach = new File(path + fileName);
                deleteAttach = true;
            }
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_send_if_no_result") != null
                    && getOrderPayload().var("scheduler_order_report_send_if_no_result").length() > 0) {
                sendIfNoResult = (getOrderPayload().var("scheduler_order_report_send_if_no_result").equals("1") || getOrderPayload().var("scheduler_order_report_send_if_no_result").equalsIgnoreCase("true"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_send_if_no_result");
            }
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_send_if_result") != null
                    && getOrderPayload().var("scheduler_order_report_send_if_result").length() > 0) {
                sendIfResult = (getOrderPayload().var("scheduler_order_report_send_if_result").equals("1") || getOrderPayload().var("scheduler_order_report_send_if_result").equalsIgnoreCase("true"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_send_if_result");
            }

            return attach;
        } catch (Exception e) {
            mail.clearAttachments();
            mail.clearRecipients();
            clearReplacements();
            throw new Exception("error occurred preparing report file: " + e, e);
        }
    }

    public void report(File[] files) throws Exception {
        try {
            if (hasResult() && !sendIfResult) {
                getLogger().info("Query returned a result. No report will be sent because " + "scheduler_order_send_if_result=false");
                return;
            }
            if (!hasResult() && !sendIfNoResult) {
                getLogger().info("Query returned no result. No report will be sent because " + "scheduler_order_send_if_no_result=false");
                return;
            }

            boolean asbody = false;

            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_asbody") != null) {
                asbody = (getOrderPayload().var("scheduler_order_report_asbody").equals("1") || getOrderPayload().var("scheduler_order_report_asbody").equalsIgnoreCase("true"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_asbody");
            }
            // wenn mit files!=null aufgerufen wurde, wurde nicht
            // getReportFile()
            // aufgerufen
            if (files != null) {
                getReportFile();
                deleteAttach = false;
                attach = files[0];
            }
            if (files != null && files.length > 1)
                asbody = false;
            if (asbody) {
                BufferedReader reader = new BufferedReader(new FileReader(attach));
                StringBuffer buffer = new StringBuffer();
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    buffer.append('\n');
                    line = reader.readLine();
                }
                reader.close();
                body = buffer.toString();
                mail.setContentType("text/html");
            }
            if (body.trim().startsWith("<html"))
                mail.setContentType("text/html");
            mail.setBody(body);
            mail.setSubject(subject);
            if (files != null) {
                if (!asbody) {
                    for (int i = 0; i < files.length; i++) {
                        mail.addAttachment(files[i].getAbsolutePath());
                    }
                }
            } else if (!asbody)
                mail.addAttachment(attach.getAbsolutePath());
            if (logDirectory.length() == 0 && job.spooler_log.level() == -9) {
                logDirectory = job.spooler.log_dir();
            }
            if (logDirectory.length() > 0)
                dumpMessage();
            getLogger().info("Sending report email to: " + mail.getRecipientsAsString() + ", subject: " + mail.getSubject());
            if (mail instanceof SOSMailOrder) {
                SOSMailOrder mo = (SOSMailOrder) mail;
                mo.setJobId(job.spooler_task.id());
            }
            mail.send();
        } catch (Exception e) {
            throw new Exception("Error occurred sending report: " + e, e);
        } finally {
            if (mail instanceof SOSMailOrder) {
                SOSMailOrder mo = (SOSMailOrder) mail;
                mo.initOrder();
            }
            clearReplacements();
            if (deleteAttach) {
                try {
                    attach.delete();
                    attach = null;
                } catch (SecurityException e) {
                    getLogger().warn("Failed to delete temporary attachment file: " + e);
                }
            }
        }
    }

    /** Sends the report
     * 
     * @throws Exception */
    public void report() throws Exception {
        report(null);
    }

    private String replace(String source) {
        String target;
        try {
            target = source.replaceAll("\\[date\\]", SOSDate.getCurrentDateAsString());
            target = target.replaceAll("\\[datetime\\]", SOSDate.getCurrentTimeAsString());
            target = target.replaceAll("\\[date_german\\]", SOSDate.getCurrentDateAsString("dd.MM.yyyy"));
            target = target.replaceAll("\\[datetime_german\\]", SOSDate.getCurrentTimeAsString("dd.MM.yyyy HH:mm:ss"));
            target = target.replaceAll("\\[subject\\]", this.getSubject());
            target = target.replaceAll("\\[nl\\]", "\n");
            if (isOrderJob())
                target = target.replaceAll("\\[orderid\\]", spooler_task.order().id());
            target = target.replaceAll("\\[jobname\\]", spooler_task.job().name());
            target = target.replaceAll("\\[taskid\\]", Integer.toString(spooler_task.id()));
            /*
             * target = target.replaceAll("\\[sql\\]",sql); target =
             * target.replaceAll("\\[xml\\]",xml);
             */
            Set keys = replacements.keySet();
            Iterator keysIt = keys.iterator();
            while (keysIt.hasNext()) {
                String key = keysIt.next().toString();
                String repl = replacements.get(key).toString();
                target = target.replaceAll(key, repl);
            }

        } catch (Exception e) {
            try {
                getLogger().warn("An error occurred replacing fields in String \"" + source + "\"");
            } catch (Exception ex) {
            }
            return source;
        }
        return target;
    }

    /** Adds a replacement
     * 
     * @param regex regular expression which will be replaced
     * @param replacement String containing the replacement text */
    public void addReplacement(String regex, String replacement) {
        replacements.put(regex, replacement);
    }

    /** clears replacements */
    public void clearReplacements() {
        replacements.clear();
    }

    /** @return Returns the logger. */
    public SOSLogger getLogger() {
        return logger;
    }

    /** @param logger The logger to set. */
    public void setLogger(SOSLogger logger) {
        this.logger = logger;
    }

    /** @return Returns the jobSettings. */
    public SOSSettings getJobSettings() {
        return jobSettings;
    }

    /** @param jobSettings The jobSettings to set. */
    public void setJobSettings(SOSSettings jobSettings) {
        this.jobSettings = jobSettings;
    }

    /** @return Returns the orderJob. */
    public boolean isOrderJob() {
        return orderJob;
    }

    /** @return Returns the body. */
    public String getBody() {
        return body;
    }

    /** Sets the body for the mail. May contain replacement variables
     * 
     * @param body The body to set. */
    public void setBody(String body) {
        this.body = body;
    }

    /** @return Returns the subject. */
    public String getSubject() {
        return subject;
    }

    /** Sets the subject for the mail. May contain replacement variables
     * 
     * @param subject The subject to set. */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /** @return Returns the orderPayload. */
    public Variable_set getOrderPayload() {
        return orderPayload;
    }

    /** @return Returns the hasResult. */
    public boolean hasResult() {
        return hasResult;
    }

    /** Sets if the report has a result */
    public void setHasResult(boolean hasResult) {
        this.hasResult = hasResult;
    }

    public String getTableMailSettings() {
        return tableMailSettings;
    }

    public void setTableMailSettings(String tableMailSettings) {
        this.tableMailSettings = tableMailSettings;
    }

    public String getApplicationMail() {
        return applicationMail;
    }

    public void setApplicationMail(String applicationMail) {
        this.applicationMail = applicationMail;
    }

    public String getSectionMail() {
        return sectionMail;
    }

    public void setSectionMail(String sectionMail) {
        this.sectionMail = sectionMail;
    }

    private void dumpMessage() {
        String queuePattern = "yyyy-MM-dd.HHmmss.S";
        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(queuePattern);

        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);

        String dumpFileName = logDirectory + "/" + "sos." + b + ".email";
        try {
            mail.dumpMessageToFile(dumpFileName, true);
        } catch (Exception e) {
            try {
                getLogger().warn(e.toString());
            } catch (Exception ex) {
            }
        }
    }
}
