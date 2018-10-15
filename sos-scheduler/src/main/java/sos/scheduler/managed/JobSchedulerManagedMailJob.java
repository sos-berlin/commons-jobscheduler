package sos.scheduler.managed;

import java.io.File;
import java.util.Properties;

import sos.net.SOSMail;
import sos.net.SOSMailAttachment;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSFile;

/** @author KB */
public class JobSchedulerManagedMailJob extends JobSchedulerManagedJob {

    @Override
    public boolean spooler_process() {
        orderPayload = null;
        String orderId = "(none)";
        String host = spooler_log.mail().smtp();
        boolean hostChanged = false;
        int port = 25;
        boolean portChanged = false;
        String queueDir = spooler_log.mail().queue_dir();
        boolean queueDirChanged = false;
        String from = spooler_log.mail().from();
        boolean fromChanged = false;
        boolean queueMailOnError = true;
        String fromName = "";
        String replyTo = "";
        String to = "";
        String cc = "";
        String bcc = "";
        String subject = "";
        String body = "";
        String contentType = "";
        String encoding = "";
        String attachmentCharset = "";
        String attachmentContentType = "";
        String attachmentEncoding = "";
        boolean cleanupAttachment = false;
        String[] attachments = {};
        String smtpUser = "";
        String smtpPass = "";
        String securityProtocol = "";
        try {
            try {
                super.prepareParams();
            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }
            if (doSendMail()) {
                try {
                    if (this.getParameters().value("to") != null && !this.getParameters().value("to").isEmpty()) {
                        to = this.getParameters().value("to");
                    } else {
                        throw new Exception("no value was specified for mandatory parameter [to]");
                    }
                    if (this.getParameters().value("subject") != null && !this.getParameters().value("subject").isEmpty()) {
                        subject = this.getParameters().value("subject");
                    } else {
                        throw new Exception("no value was specified for mandatory parameter [subject]");
                    }
                    if (this.getParameters().value("host") != null && !this.getParameters().value("host").isEmpty()) {
                        host = this.getParameters().value("host");
                        hostChanged = true;
                    }
                    if (this.getParameters().value("port") != null && !this.getParameters().value("port").isEmpty()) {
                        try {
                            port = Integer.parseInt(this.getParameters().value("port"));
                            portChanged = true;
                        } catch (Exception e) {
                            throw new Exception(
                                    "illegal, non-numeric value [" + this.getParameters().value("port") + "] for parameter [port]: " + e.getMessage());
                        }
                    }
                    if (this.getParameters().value("smtp_user") != null && !this.getParameters().value("smtp_user").isEmpty()) {
                        smtpUser = this.getParameters().value("smtp_user");
                    }
                    if (this.getParameters().value("smtp_password") != null && !this.getParameters().value("smtp_password").isEmpty()) {
                        smtpPass = this.getParameters().value("smtp_password");
                    }
                    if (this.getParameters().value("queue_directory") != null && !this.getParameters().value("queue_directory").isEmpty()) {
                        queueDir = this.getParameters().value("queue_directory");
                        queueDirChanged = true;
                    }
                    if (this.getParameters().value("from") != null && !this.getParameters().value("from").isEmpty()) {
                        from = this.getParameters().value("from");
                        fromChanged = true;
                    }
                    if (this.getParameters().value("cc") != null && !this.getParameters().value("cc").isEmpty()) {
                        cc = this.getParameters().value("cc");
                    }
                    if (this.getParameters().value("bcc") != null && !this.getParameters().value("bcc").isEmpty()) {
                        bcc = this.getParameters().value("bcc");
                    }
                    if (this.getParameters().value("from_name") != null && !this.getParameters().value("from_name").isEmpty()) {
                        fromName = this.getParameters().value("from_name");
                    }
                    if (this.getParameters().value("reply_to") != null && !this.getParameters().value("reply_to").isEmpty()) {
                        replyTo = this.getParameters().value("reply_to");
                    }
                    if (this.getParameters().value("body") != null && !this.getParameters().value("body").isEmpty()) {
                        body = this.getParameters().value("body");
                    }
                    if (this.getParameters().value("content_type") != null && !this.getParameters().value("content_type").isEmpty()) {
                        contentType = this.getParameters().value("content_type");
                    }
                    if (this.getParameters().value("encoding") != null && !this.getParameters().value("encoding").isEmpty()) {
                        encoding = this.getParameters().value("encoding");
                    }
                    if (this.getParameters().value("attachment_charset") != null && !this.getParameters().value("attachment_charset").isEmpty()) {
                        attachmentCharset = this.getParameters().value("attachment_charset");
                    }
                    if (this.getParameters().value("attachment_content_type") != null && !this.getParameters().value("attachment_content_type").isEmpty()) {
                        attachmentContentType = this.getParameters().value("attachment_content_type");
                    }
                    if (this.getParameters().value("attachment_encoding") != null && !this.getParameters().value("attachment_encoding").isEmpty()) {
                        attachmentEncoding = this.getParameters().value("attachment_encoding");
                    }
                    if (this.getParameters().value("attachment") != null && !this.getParameters().value("attachment").isEmpty()) {
                        attachments = this.getParameters().value("attachment").split(";");
                    }
                    if (this.getParameters().value("security_protocol") != null && !this.getParameters().value("security_protocol").isEmpty()) {
                        securityProtocol = this.getParameters().value("security_protocol");
                    }
                    if (this.getParameters().value("queue_mail_on_error") != null && !this.getParameters().value("queue_mail_on_error").isEmpty()) {
                        queueMailOnError = !"false".equalsIgnoreCase(this.getParameters().value("queue_mail_on_error"));
                    }
                    if (this.getParameters().value("cleanup_attachment") != null && !this.getParameters().value("cleanup_attachment").isEmpty()
                            && ("1".equals(this.getParameters().value("cleanup_attachment"))
                                    || "true".equalsIgnoreCase(this.getParameters().value("cleanup_attachment"))
                                    || "yes".equalsIgnoreCase(this.getParameters().value("cleanup_attachment")))) {
                        cleanupAttachment = true;
                    }

                } catch (Exception e) {
                    throw new Exception("error occurred checking parameters: " + e.getMessage());
                }
                try {
                    SOSMail sosMail;
                    Properties mailSection = null;
                    if (this.getConnectionSettings() != null) {
                        try {
                            mailSection = this.getConnectionSettings().getSection("email", "mail_server");
                            if (mailSection.isEmpty()) {
                                mailSection = null;
                            }
                        } catch (Exception e) {
                            getLogger().debug6("No database settings found, using defaults from factory.ini");
                        }
                    }
                    if (mailSection != null) {
                        sosMail = new SOSMail(getConnectionSettings());
                        if (hostChanged) {
                            sosMail.setHost(host);
                        }
                        if (queueDirChanged) {
                            sosMail.setQueueDir(queueDir);
                        }
                        if (fromChanged) {
                            sosMail.setFrom(from);
                        }
                    } else {
                        sosMail = new SOSMail(host);
                        sosMail.setQueueDir(queueDir);
                        sosMail.setFrom(from);
                        try {
                            SOSSettings smtpSettings = new SOSProfileSettings(spooler.ini_path());
                            Properties smtpProperties = smtpSettings.getSection("smtp");
                            sosMail.setProperties(smtpProperties);

                            if (!smtpProperties.isEmpty()) {
                                if (smtpProperties.getProperty("mail.smtp.user") != null && !smtpProperties.getProperty("mail.smtp.user").isEmpty()) {
                                    sosMail.setUser(smtpProperties.getProperty("mail.smtp.user"));
                                }
                                if (smtpProperties.getProperty("mail.smtp.password") != null && !smtpProperties.getProperty("mail.smtp.password").isEmpty()) {
                                    sosMail.setPassword(smtpProperties.getProperty("mail.smtp.password"));
                                }
                                if (smtpProperties.getProperty("mail.smtp.port") != null && !smtpProperties.getProperty("mail.smtp.port").isEmpty()) {
                                    sosMail.setPort(smtpProperties.getProperty("mail.smtp.port"));
                                }
                            }
                        } catch (Exception e) {
                            // The job is running on an Universal Agent that
                            // does not suppor .ini_path()
                        }
                    }
                    if (portChanged) {
                        sosMail.setPort(Integer.toString(port));
                    }
                    if (!smtpUser.isEmpty()) {
                        sosMail.setUser(smtpUser);
                    }
                    if (!smtpPass.isEmpty()) {
                        sosMail.setPassword(smtpPass);
                    }
                    if (!contentType.isEmpty()) {
                        sosMail.setContentType(contentType);
                    }
                    if (!encoding.isEmpty()) {
                        sosMail.setEncoding(encoding);
                    }
                    if (!attachmentCharset.isEmpty()) {
                        sosMail.setAttachmentCharset(attachmentCharset);
                    }
                    if (!attachmentEncoding.isEmpty()) {
                        sosMail.setAttachmentEncoding(attachmentEncoding);
                    }
                    if (!attachmentContentType.isEmpty()) {
                        sosMail.setAttachmentContentType(attachmentContentType);
                    }
                    if (!fromName.isEmpty()) {
                        sosMail.setFromName(fromName);
                    }
                    sosMail.setSecurityProtocol(securityProtocol);
                    String[] recipientsTo = to.split(";|,");
                    for (int i = 0; i < recipientsTo.length; i++) {
                        if (i == 0) {
                            sosMail.setReplyTo(recipientsTo[i].trim());
                        }
                        sosMail.addRecipient(recipientsTo[i].trim());
                    }
                    if (!replyTo.isEmpty()) {
                        sosMail.setReplyTo(replyTo);
                    }
                    sosMail.addCC(cc);
                    sosMail.addBCC(bcc);
                    sosMail.setSubject(subject);
                    sosMail.setBody(body);
                    for (String attachment2 : attachments) {
                        File attachmentFile = new File(attachment2);
                        SOSMailAttachment attachment = new SOSMailAttachment(sosMail, attachmentFile);
                        attachment.setCharset(sosMail.getAttachmentCharset());
                        attachment.setEncoding(sosMail.getAttachmentEncoding());
                        attachment.setContentType(sosMail.getAttachmentContentType());
                        sosMail.addAttachment(attachment);
                    }
                    sosMail.setSOSLogger(this.getLogger());
                    this.getLogger().info("sending mail: \n" + sosMail.dumpMessageAsString());
                    sosMail.setQueueMailOnError(queueMailOnError);
                    if (!sosMail.send()) {
                        this.getLogger().warn("mail server is unavailable, mail for recipient [" + to + "] is queued in local directory ["
                                + sosMail.getQueueDir() + "]:" + sosMail.getLastError());
                    }
                    if (cleanupAttachment) {
                        for (String attachment : attachments) {
                            File attachmentFile = new File(attachment);
                            if (attachmentFile.exists() && attachmentFile.canWrite()) {
                                SOSFile.deleteFile(attachmentFile);
                            }
                        }
                    }
                    sosMail.clearRecipients();
                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }
            }
            return spooler_task.job().order_queue() != null ? true : false;
        } catch (Exception e) {
            if (queueMailOnError) {
                spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            } else {
                spooler_log.error("error occurred processing order [" + orderId + "]: " + e.getMessage());
            }
            spooler_task.end();
            return false;
        }
    }

    protected boolean doSendMail() {
        return true;
    }

    @Override
    public final Variable_set getParameters() {
        return orderPayload;
    }

}