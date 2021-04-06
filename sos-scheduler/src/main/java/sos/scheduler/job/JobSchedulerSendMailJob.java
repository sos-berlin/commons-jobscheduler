package sos.scheduler.job;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.net.SOSMailOrder;
import sos.spooler.Order;
import sos.spooler.Variable_set;
//import sos.textprocessor.SOSDocumentFactoryTextProcessor;
import sos.textprocessor.SOSPlainTextProcessor;
import sos.textprocessor.SOSTextProcessor;
import sos.util.SOSDate;

/** @author andreas pueschel */
public class JobSchedulerSendMailJob extends JobSchedulerMailJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerSendMailJob.class);

    protected List<String> mailOrders = null;
    protected Iterator<String> mailOrderIterator = null;
    protected String encoding = "7bit";
    protected String charset = "iso-8859-1";
    protected String attachmentEncoding = "Base64";
    protected String attachmentCharset = "iso-8859-1";
    protected SOSTextProcessor mailTextProcessor = null;
    protected SOSPlainTextProcessor mailPlainTextProcessor = null;
//    protected SOSDocumentFactoryTextProcessor mailDocumentFactoryTextProcessor = null;
    protected boolean hasDatabase = true;

    public boolean spooler_init() {
        if (!super.spooler_init()) {
            return false;
        }
        return true;
    }

    public boolean spooler_open() {
        try {
            if (spooler_task.job().order_queue() == null) {
                this.mailOrders = this.getConnection().getArrayValue("SELECT \"ID\" FROM " + this.getTableMails() + " WHERE \"STATUS\"=" + STATE_READY
                        + " AND (\"TARGETED\" IS NULL OR \"TARGETED\"<=%now) ORDER BY \"ID\" ASC");
                this.mailOrderIterator = this.mailOrders.iterator();
                return this.mailOrderIterator.hasNext();
            } else {
                return true;
            }
        } catch (Exception e) {
            spooler_log.warn("failed to retrieve mail orders: " + e.getMessage());
            return false;
        } finally {
            try {
                if (this.getConnection() != null) {
                    this.getConnection().rollback();
                }
            } catch (Exception ex) {
                // gracefully ignore this error to preserve the original
                // exception
            }
        }
    }

    public boolean spooler_process() {
        Order order = null;
        Variable_set orderData = null;
        int mailOrderId = 0;
        boolean rc = true;
        try {
            mailOrderId = Integer.parseInt(this.mailOrderIterator.next());
            SOSMailOrder mailOrder = new SOSMailOrder(this.sosMailSettings, this.getConnection());

            if (mailOrder.getHost() == null || mailOrder.getHost().isEmpty()) {
                if (!"-queue".equalsIgnoreCase(spooler_log.mail().smtp())) {
                    mailOrder.setHost(spooler_log.mail().smtp());
                } else {
                    throw new Exception("no SMTP host was configured, global settings contain smtp=-queue");
                }
            }
            if (mailOrder.getQueueDir() == null || mailOrder.getQueueDir().isEmpty()) {
                mailOrder.setQueueDir(this.getQueueDirectory());
            }
            mailOrder.setQueuePraefix(this.getQueuePrefix());
            if (spooler_task.job().order_queue() == null) {
                mailOrder.load(mailOrderId);
                rc = this.mailOrderIterator.hasNext();
            } else {
                order = spooler_task.order();
                orderData = (Variable_set) spooler_task.order().payload();
                if (orderData.var("id") == null || orderData.var("id").toString().isEmpty()) {
                    mailOrder.initOrder();
                    if (orderData.var("mail_from") != null && !orderData.var("mail_from").isEmpty()) {
                        mailOrder.setFrom(orderData.var("mail_from"));
                    }
                    if (orderData.var("mail_to") != null && !orderData.var("mail_to").isEmpty()) {
                        mailOrder.addRecipient(orderData.var("mail_to"));
                    }
                    if (orderData.var("mail_cc") != null && !orderData.var("mail_cc").isEmpty()) {
                        mailOrder.addCC(orderData.var("mail_cc"));
                    }
                    if (orderData.var("mail_bcc") != null && !orderData.var("mail_bcc").isEmpty()) {
                        mailOrder.addBCC(orderData.var("mail_bcc"));
                    }
                    if (orderData.var("mail_subject") != null && !orderData.var("mail_subject").isEmpty()) {
                        mailOrder.setSubject(orderData.var("mail_subject"));
                    }
                    if (orderData.var("mail_body") != null && !orderData.var("mail_body").isEmpty()) {
                        mailOrder.setBody(orderData.var("mail_body"));
                    }
                    if (orderData.var("mail_attachment") != null && !orderData.var("mail_attachment").isEmpty()) {
                        mailOrder.addAttachment(orderData.var("mail_attachment"));
                    }
                } else {
                    try {
                        mailOrder.load(mailOrderId);
                    } catch (Exception e) {
                        LOGGER.info("failed to load order [" + orderData.var("id") + "]: " + e.getMessage());
                        return false;
                    }
                }
                if (mailOrder.getTargeted() != null) {
                    Date currentDate = SOSDate.getTime();
                    Date targetDate = mailOrder.getTargeted();
                    if (targetDate.after(currentDate)) {
                        Calendar target = Calendar.getInstance();
                        target.setTime(targetDate);
                        spooler_task.job().set_delay_order_after_setback(1, (target.getTimeInMillis() - System.currentTimeMillis()) / 1000);
                        spooler_task.order().setback();
                        LOGGER.info("order is set back for target date: " + mailOrder.getTargeted().toString());
                        return false;
                    }
                }
            }
            try {
                switch (mailOrder.getStatus()) {
                case STATE_SUCCESS:
                    throw new Exception("mail order has already been successfully processed");
                case STATE_CANCEL:
                    throw new Exception("mail order has already been cancelled");
                }
            } catch (Exception ex) {
                LOGGER.info("mail status [" + mailOrder.getStatus() + "] not applicable for order [" + mailOrder.getId() + "]: " + ex.getMessage());
                if (spooler_task.job().order_queue() == null) {
                    return this.mailOrderIterator.hasNext();
                } else {
                    return false;
                }
            }
            try {
                File mailFile = null;
                String message = "";
                if (this.getMailTo() != null && !this.getMailTo().isEmpty()) {
                    LOGGER.info("mail recipients [" + mailOrder.getRecipientsAsString() + "] are replaced by settings: " + this.getMailTo());
                    mailOrder.clearRecipients();
                    mailOrder.addRecipient(this.getMailTo());
                }
                if (this.getLogDirectory() != null && !this.getLogDirectory().isEmpty()) {
                    mailFile = this.getMailFile(this.getLogDirectory());
                    mailOrder.dumpMessageToFile(mailFile, true);
                }
                mailOrder.setModifiedBy(spooler_task.job().name());
                mailOrder.setJobId(spooler_task.id());
                if (this.isLogOnly()) {
                    message = "mail was NOT sent but stored to file: " + mailFile.getAbsolutePath();
                    LOGGER.info(message);
                    message = (message.length() > 250 ? message.substring(message.length() - 250) : message);
                    message = message.replaceAll("'", "''");
                } else {
                    mailOrder.send();
                }
                LOGGER.info("mail was " + (this.isLogOnly() ? "processed" : "sent") + " for order " + mailOrderId + " to: " + mailOrder
                        .getRecipientsAsString());
                LOGGER.debug("mail was sent with headers: " + mailOrder.dumpHeaders());

            } catch (Exception ex) {
                throw new Exception("mail was NOT sent for order " + mailOrderId + ": " + ex.getMessage());
            }
            if (spooler_task.job().order_queue() == null) {
                return this.mailOrderIterator.hasNext();
            } else {
                return rc;
            }
        } catch (Exception e) {
            if (spooler_task.job().order_queue() != null) {
                spooler_log.warn("error occurred processing mail [" + ((order != null) ? "job chain: " + order.job_chain().name() + ", order: "
                        + order.id() : "(none)") + "]: " + e.getMessage());
            } else {
                spooler_log.warn("error occurred processing mail: " + e.getMessage());
            }
            if (spooler_task.job().order_queue() != null) {
                spooler_task.end();
                return false;
            } else {
                return this.mailOrderIterator.hasNext();
            }
        } finally {
            try {
                if (this.getConnection() != null) {
                    this.getConnection().rollback();
                }
            } catch (Exception ex) {
                // gracefully ignore this error to preserve the original
                // exception
            }
        }
    }

    public boolean hasDatabase() {
        return true;
    }

}