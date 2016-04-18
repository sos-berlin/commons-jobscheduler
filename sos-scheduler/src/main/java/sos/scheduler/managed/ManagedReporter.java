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

/** @author Andreas Liebert */
public class ManagedReporter {

    protected String tableMailSettings = "SETTINGS";
    protected String applicationMail = "email";
    protected String sectionMail = "mail_server";
    private SOSSettings jobSettings;
    private SOSSettings sosMailSettings;
    private SOSLogger logger;
    private SOSMail mail;
    private Task spooler_task;
    private JobSchedulerManagedJob job;
    private boolean orderJob;
    private HashMap replacements = new HashMap();
    private String body = "";
    private String subject = "Report [taskid]";
    private Variable_set orderPayload;
    private File attach;
    private boolean deleteAttach = false;
    private boolean sendIfNoResult = true;
    private boolean sendIfResult = true;
    private boolean hasResult = true;
    private String logDirectory;
    private String mailServer = "";
    private String logMailFrom = "";
    private String mailUser = "";
    private String mailPassword = "";
    private String mailPort = "";
    private String securityProtocol = "";
    private boolean isUniversalAgent;

    public ManagedReporter(JobSchedulerManagedJob job) throws Exception {
        this.setJobSettings(job.getJobSettings());
        this.setLogger(job.getLogger());
        this.job = job;
        try {
            isUniversalAgent = false;
            job.spooler.ini_path();
        } catch (Exception e) {
            isUniversalAgent = true;
        }
        spooler_task = job.spooler_task;
        if (spooler_task.params().var("application_mail") != null && !spooler_task.params().var("application_mail").isEmpty()) {
            this.setApplicationMail(spooler_task.params().var("application_mail"));
            getLogger().debug6(".. job parameter [application_mail]: " + this.getApplicationMail());
        }
        if (spooler_task.params().var("section_mail") != null && !spooler_task.params().var("section_mail").isEmpty()) {
            this.setSectionMail(spooler_task.params().var("section_mail"));
            getLogger().debug6(".. job parameter [section_mail]: " + this.getSectionMail());
        }
        if (spooler_task.params().var("table_settings") != null && !spooler_task.params().var("table_settingss").isEmpty()) {
            this.setTableMailSettings(spooler_task.params().var("table_settings"));
            getLogger().debug6(".. job parameter [table_settings]: " + this.getTableMailSettings());
        }
        try {
            sosMailSettings = new SOSConnectionSettings(job.getConnection(), this.getTableMailSettings(), this.getApplicationMail(), this.getSectionMail(),
                    getLogger());
            getLogger().debug3("MailSettings: " + sosMailSettings.getSection().size());
        } catch (Exception e) {
            getLogger().debug3("MailSettings were not found.");
            sosMailSettings = null;
        }
        if (!isUniversalAgent) {
            try {
                Properties spoolProp = getJobSettings().getSection("spooler");
                Properties smtpProp = getJobSettings().getSection("smtp");
                mailServer = spoolProp.getProperty("smtp");
                logMailFrom = spoolProp.getProperty("log_mail_from");
                mailUser = smtpProp.getProperty("mail.smtp.user");
                if ("".equals(mailUser)) {
                    mailUser = spoolProp.getProperty("mail.smtp.user");
                }
                mailPassword = smtpProp.getProperty("mail.smtp.password");
                if ("".equals(mailPassword)) {
                    mailPassword = spoolProp.getProperty("mail.smtp.password");
                }
                securityProtocol = smtpProp.getProperty("mail.smtp.security_protocol");
                if ("".equals(securityProtocol)) {
                    securityProtocol = spoolProp.getProperty("mail.smtp.security_protocol");
                }
            } catch (Exception e) {
            }
        }
        if (mailServer == null) {
            mailServer = "";
        }
        if (mailPort == null) {
            mailPort = "";
        }
        if (logMailFrom == null) {
            logMailFrom = "";
        }
        if (mailUser == null) {
            mailUser = "";
        }
        if (mailPassword == null) {
            mailPassword = "";
        }
        if (securityProtocol == null) {
            securityProtocol = "";
        }
        if (!spooler_task.params().var("smtp_port").isEmpty()) {
            mailPort = spooler_task.params().var("smtp_port");
        }
        if (!spooler_task.params().var("from").isEmpty()) {
            logMailFrom = spooler_task.params().var("from");
        }
        if (!spooler_task.params().var("smtp_user").isEmpty()) {
            mailUser = spooler_task.params().var("smtp_user");
        }
        if (spooler_task.params().var("smtp_password").isEmpty()) {
            mailPassword = spooler_task.params().var("smtp_password");
        }
        if (spooler_task.params().var("security_protocol").isEmpty()) {
            securityProtocol = spooler_task.params().var("security_protocol");
        }
        if (!spooler_task.params().var("host").isEmpty()) {
            mailServer = spooler_task.params().var("host");
        }
        if (!spooler_task.params().var("smtp_port").isEmpty()) {
            mailPort = spooler_task.params().var("smtp_port");
        }
        if (!spooler_task.params().var("from").isEmpty()) {
            logMailFrom = spooler_task.params().var("from");
        }
        if (!spooler_task.params().var("smtp_user").isEmpty()) {
            mailUser = spooler_task.params().var("smtp_user");
        }
        if (!spooler_task.params().var("smtp_password").isEmpty()) {
            mailPassword = spooler_task.params().var("smtp_password");
        }
        if (!spooler_task.params().var("security_protocol").isEmpty()) {
            securityProtocol = spooler_task.params().var("security_protocol");
        }
        boolean hasSOSMailOrder = false;
        if (job.getConnection() != null) {
            try {
                String result = job.getConnection().getSingleValue("SELECT COUNT(*) FROM MAILS");
                if (!result.isEmpty()) {
                    hasSOSMailOrder = true;
                }
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
            } else if (sosMailSettings != null && sosMailSettings.getSection() != null && !sosMailSettings.getSection().isEmpty()) {
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
                if (!mailPort.isEmpty()) {
                    getLogger().debug9("Setting mail smtp port:" + mailPort);
                    mail.setPort(mailPort);
                }
                if (!securityProtocol.isEmpty()) {
                    getLogger().debug9("Setting mail securityProtocol:" + securityProtocol);
                    mail.setSecurityProtocol(securityProtocol);
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
                    && !getOrderPayload().var("scheduler_order_report_mailto").isEmpty()) {
                String[] mails = getOrderPayload().var("scheduler_order_report_mailto").split(",");
                for (int i = 0; i < mails.length; i++) {
                    mail.addRecipient(mails[i].trim());
                }
                job.debugParamter(getOrderPayload(), "scheduler_order_report_mailto");
                if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_mailcc") != null
                        && !getOrderPayload().var("scheduler_order_report_mailcc").isEmpty()) {
                    String[] mailscc = getOrderPayload().var("scheduler_order_report_mailcc").split(",");
                    for (int i = 0; i < mailscc.length; i++) {
                        mail.addCC(mailscc[i].trim());
                    }
                    job.debugParamter(getOrderPayload(), "scheduler_order_report_mailcc");
                }
                if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_mailbcc") != null
                        && !getOrderPayload().var("scheduler_order_report_mailbcc").isEmpty()) {
                    String[] mailsbcc = getOrderPayload().var("scheduler_order_report_mailbcc").split(",");
                    for (int i = 0; i < mailsbcc.length; i++) {
                        mail.addBCC(mailsbcc[i].trim());
                    }
                    job.debugParamter(getOrderPayload(), "scheduler_order_report_mailbcc");
                }
            } else {
                getLogger().debug6("Using mail recipients from job.");
                mail.addRecipient(job.spooler_log.mail().to());
                if (job.spooler_log.mail().cc() != null && !job.spooler_log.mail().cc().isEmpty()) {
                    mail.addCC(job.spooler_log.mail().cc());
                }
                if (job.spooler_log.mail().bcc() != null && !job.spooler_log.mail().bcc().isEmpty()) {
                    mail.addBCC(job.spooler_log.mail().bcc());
                }
            }
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_subject") != null
                    && !getOrderPayload().var("scheduler_order_report_subject").isEmpty()) {
                subject = (getOrderPayload().var("scheduler_order_report_subject"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_subject");
            }
            subject = replace(subject);
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_body") != null
                    && !getOrderPayload().var("scheduler_order_report_body").isEmpty()) {
                body = (getOrderPayload().var("scheduler_order_report_body"));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_body");
            }
            body = replace(body);
            logDirectory = "";
            if (getOrderPayload() != null && getOrderPayload().var("log_directory") != null && !getOrderPayload().var("log_directory").isEmpty()) {
                logDirectory = getOrderPayload().var("log_directory");
                job.debugParamter(getOrderPayload(), "log_directory");
            }
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_filename") != null
                    && !getOrderPayload().var("scheduler_order_report_filename").isEmpty()) {
                fileName = getOrderPayload().var("scheduler_order_report_filename");
                job.debugParamter(getOrderPayload(), "scheduler_order_report_filename");
            }
            fileName = replace(fileName);
            getLogger().debug6("Output filename: " + fileName);
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_path") != null
                    && !getOrderPayload().var("scheduler_order_report_path").isEmpty()) {
                String path = getOrderPayload().var("scheduler_order_report_path");
                job.debugParamter(getOrderPayload(), "scheduler_order_report_path");
                if (!path.endsWith("/") && !path.endsWith("\\") && !path.isEmpty()) {
                    path = path + "/";
                }
                path = replace(path);
                File reportPath = new File(path);
                if (!reportPath.exists()) {
                    reportPath.mkdirs();
                }
                attach = new File(path + fileName);
                deleteAttach = false;
            } else {
                String path = System.getProperty("java.io.tmpdir");
                if (!path.endsWith("/") && !path.endsWith("\\") && !path.isEmpty()) {
                    path = path + "/";
                }
                attach = new File(path + fileName);
                deleteAttach = true;
            }
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_send_if_no_result") != null
                    && !getOrderPayload().var("scheduler_order_report_send_if_no_result").isEmpty()) {
                sendIfNoResult = ("1".equals(getOrderPayload().var("scheduler_order_report_send_if_no_result")) 
                        || "true".equalsIgnoreCase(getOrderPayload().var("scheduler_order_report_send_if_no_result")));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_send_if_no_result");
            }
            if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_send_if_result") != null
                    && !getOrderPayload().var("scheduler_order_report_send_if_result").isEmpty()) {
                sendIfResult = ("1".equals(getOrderPayload().var("scheduler_order_report_send_if_result")) 
                        || "true".equalsIgnoreCase(getOrderPayload().var("scheduler_order_report_send_if_result")));
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
                asbody = ("1".equals(getOrderPayload().var("scheduler_order_report_asbody")) 
                        || "true".equalsIgnoreCase(getOrderPayload().var("scheduler_order_report_asbody")));
                job.debugParamter(getOrderPayload(), "scheduler_order_report_asbody");
            }
            if (files != null) {
                getReportFile();
                deleteAttach = false;
                attach = files[0];
            }
            if (files != null && files.length > 1) {
                asbody = false;
            }
            if (asbody) {
                BufferedReader reader = new BufferedReader(new FileReader(attach));
                StringBuilder builder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line);
                    builder.append('\n');
                    line = reader.readLine();
                }
                reader.close();
                body = builder.toString();
                mail.setContentType("text/html");
            }
            if (body.trim().startsWith("<html")) {
                mail.setContentType("text/html");
            }
            mail.setBody(body);
            mail.setSubject(subject);
            if (files != null) {
                if (!asbody) {
                    for (int i = 0; i < files.length; i++) {
                        mail.addAttachment(files[i].getAbsolutePath());
                    }
                }
            } else if (!asbody) {
                mail.addAttachment(attach.getAbsolutePath());
            }
            if (!isUniversalAgent && logDirectory.isEmpty() && job.spooler_log.level() == -9) {
                logDirectory = job.spooler.log_dir();
            }
            if (!logDirectory.isEmpty()) {
                dumpMessage();
            }
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
            if (isOrderJob()) {
                target = target.replaceAll("\\[orderid\\]", spooler_task.order().id());
            }
            target = target.replaceAll("\\[jobname\\]", spooler_task.job().name());
            target = target.replaceAll("\\[taskid\\]", Integer.toString(spooler_task.id()));
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

    public void addReplacement(String regex, String replacement) {
        replacements.put(regex, replacement);
    }

    public void clearReplacements() {
        replacements.clear();
    }

    public SOSLogger getLogger() {
        return logger;
    }

    public void setLogger(SOSLogger logger) {
        this.logger = logger;
    }

    public SOSSettings getJobSettings() {
        return jobSettings;
    }

    public void setJobSettings(SOSSettings jobSettings) {
        this.jobSettings = jobSettings;
    }

    public boolean isOrderJob() {
        return orderJob;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Variable_set getOrderPayload() {
        return orderPayload;
    }

    public boolean hasResult() {
        return hasResult;
    }

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