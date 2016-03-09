package sos.scheduler.process;

import java.io.File;

import sos.net.SOSMail;
import sos.net.SOSMailAttachment;
import sos.spooler.Order;
import sos.util.SOSClassUtil;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;
import sos.util.SOSFile;

/** @deprecated use sos.scheduler.managed.JobSchedulerManagedMailJob */
public class JobSchedulerProcessSendMailJob extends ProcessOrderJob {

    public boolean spooler_process() {

        Order order = null;
        String orderId = "(none)";

        String host = spooler_log.mail().smtp();
        int port = 25;
        String queueDir = spooler_log.mail().queue_dir();

        String from = spooler_log.mail().from();
        String to = "";
        String cc = "";
        String bcc = "";
        String subject = "";
        String body = "";
        String contentType = "text/plain";
        String encoding = "Base64";

        String attachmentCharset = "iso-8859-1";
        String attachmentContentType = "application/octet-stream";
        String attachmentEncoding = "Base64";
        boolean cleanupAttachment = false;
        String[] attachments = {};

        try {

            try {
                if (spooler_job.order_queue() != null) {
                    order = spooler_task.order();
                    orderId = order.id();

                    if (order.params().value("configuration_path") != null && order.params().value("configuration_path").length() > 0) {
                        this.setConfigurationPath(order.params().value("configuration_path"));
                    } else if (spooler_task.params().value("configuration_path") != null
                            && spooler_task.params().value("configuration_path").length() > 0) {
                        this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                    }

                    if (order.params().value("configuration_file") != null && order.params().value("configuration_file").length() > 0) {
                        this.setConfigurationFilename(order.params().value("configuration_file"));
                    } else if (spooler_task.params().value("configuration_file") != null
                            && spooler_task.params().value("configuration_file").length() > 0) {
                        this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                    }

                    // load and assign configuration
                    this.initConfiguration();
                }

                // prepare parameters and attributes
                this.prepare();

            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }

            if (doSendMail()) {

                try {
                    if (this.getParameters().value("to") != null && this.getParameters().value("to").length() > 0) {
                        to = this.getParameters().value("to");
                    } else {
                        throw new Exception("no value was specified for mandatory parameter [to]");
                    }

                    if (this.getParameters().value("subject") != null && this.getParameters().value("subject").length() > 0) {
                        subject = this.getParameters().value("subject");
                    } else {
                        throw new Exception("no value was specified for mandatory parameter [subject]");
                    }

                    if (this.getParameters().value("host") != null && this.getParameters().value("host").length() > 0) {
                        host = this.getParameters().value("host");
                    }

                    if (this.getParameters().value("port") != null && this.getParameters().value("port").length() > 0) {
                        try {
                            port = Integer.parseInt(this.getParameters().value("port"));
                        } catch (Exception e) {
                            throw new Exception("illegal, non-numeric value [" + this.getParameters().value("port") + "] for parameter [port]: "
                                    + e.getMessage());
                        }
                    }

                    if (this.getParameters().value("queue_directory") != null && this.getParameters().value("queue_directory").length() > 0) {
                        queueDir = this.getParameters().value("queue_directory");
                    }

                    if (this.getParameters().value("from") != null && this.getParameters().value("from").length() > 0) {
                        from = this.getParameters().value("from");
                    }

                    if (this.getParameters().value("cc") != null && this.getParameters().value("cc").length() > 0) {
                        cc = this.getParameters().value("cc");
                    }

                    if (this.getParameters().value("bcc") != null && this.getParameters().value("bcc").length() > 0) {
                        bcc = this.getParameters().value("bcc");
                    }

                    if (this.getParameters().value("body") != null && this.getParameters().value("body").length() > 0) {
                        body = this.getParameters().value("body");
                    }

                    if (this.getParameters().value("content_type") != null && this.getParameters().value("content_type").length() > 0) {
                        contentType = this.getParameters().value("content_type");
                    }

                    if (this.getParameters().value("encoding") != null && this.getParameters().value("encoding").length() > 0) {
                        encoding = this.getParameters().value("encoding");
                    }

                    if (this.getParameters().value("attachment_charset") != null && this.getParameters().value("attachment_charset").length() > 0) {
                        attachmentCharset = this.getParameters().value("attachment_charset");
                    }

                    if (this.getParameters().value("attachment_content_type") != null
                            && this.getParameters().value("attachment_content_type").length() > 0) {
                        attachmentContentType = this.getParameters().value("attachment_content_type");
                    }

                    if (this.getParameters().value("attachment_encoding") != null && this.getParameters().value("attachment_encoding").length() > 0) {
                        attachmentEncoding = this.getParameters().value("attachment_encoding");
                    }

                    if (this.getParameters().value("attachment") != null && this.getParameters().value("attachment").length() > 0) {
                        attachments = this.getParameters().value("attachment").split(";");
                    }

                    if (this.getParameters().value("cleanup_attachment") != null && this.getParameters().value("cleanup_attachment").length() > 0) {
                        if (this.getParameters().value("cleanup_attachment").equals("1")
                                || this.getParameters().value("cleanup_attachment").equalsIgnoreCase("true")
                                || this.getParameters().value("cleanup_attachment").equalsIgnoreCase("yes")) {
                            cleanupAttachment = true;
                        }
                    }

                } catch (Exception e) {
                    throw new Exception("error occurred checking parameters: " + e.getMessage());
                }

                try { // to process order
                    SOSMail sosMail = new SOSMail(host);

                    sosMail.setPort(Integer.toString(port));
                    sosMail.setQueueDir(queueDir);
                    sosMail.setFrom(from);
                    sosMail.setContentType(contentType);
                    sosMail.setEncoding(encoding);

                    String recipientsTo[] = to.split(",");
                    for (int i = 0; i < recipientsTo.length; i++) {
                        if (i == 0)
                            sosMail.setReplyTo(recipientsTo[i].trim());
                        sosMail.addRecipient(recipientsTo[i].trim());
                    }

                    String recipientsCC[] = cc.split(",");
                    for (int i = 0; i < recipientsCC.length; i++) {
                        sosMail.addCC(recipientsCC[i].trim());
                    }

                    String recipientsBCC[] = bcc.split(",");
                    for (int i = 0; i < recipientsBCC.length; i++) {
                        sosMail.addBCC(recipientsBCC[i].trim());
                    }

                    sosMail.setSubject(subject);
                    sosMail.setBody(body);

                    sosMail.setAttachmentCharset(attachmentCharset);
                    sosMail.setAttachmentEncoding(attachmentEncoding);
                    sosMail.setAttachmentContentType(attachmentContentType);

                    for (int i = 0; i < attachments.length; i++) {
                        File attachmentFile = new File(attachments[i]);
                        SOSMailAttachment attachment = new SOSMailAttachment(sosMail, attachmentFile);
                        attachment.setCharset(attachmentCharset);
                        attachment.setEncoding(attachmentEncoding);
                        attachment.setContentType(attachmentContentType);
                        sosMail.addAttachment(attachment);
                    }

                    sosMail.setSOSLogger(this.getLogger());

                    this.getLogger().info("sending mail: \n" + sosMail.dumpMessageAsString());

                    if (!sosMail.send()) {
                        this.getLogger().warn("mail server is unavailable, mail for recipient [" + to + "] is queued in local directory ["
                                + sosMail.getQueueDir() + "]:" + sosMail.getLastError());
                    }

                    if (cleanupAttachment) {

                        for (int i = 0; i < attachments.length; i++) {
                            File attachmentFile = new File(attachments[i]);
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
            return (spooler_task.job().order_queue() != null) ? true : false;

        } catch (Exception e) {
            spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cleanup();
            } catch (Exception e) {
            }
            ;
        }
    }

    /** This function may be overwritten by other classes which may check with
     * other parameters if a mail should be sent or not
     * 
     * @return true if mail should be sent */
    protected boolean doSendMail() {
        return true;
    }

}
