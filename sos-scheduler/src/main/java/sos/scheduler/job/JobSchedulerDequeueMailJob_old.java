package sos.scheduler.job;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import sos.net.SOSMail;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSFile;

/**
 * process mails
 * 
 * job parameters:
 * table_mails
 * table_mail_attachments
 * table_settings
 * application_mail
 * section_mail
 * application_mail_templates
 * section_mail_templates
 * mail_to
 * log_directory
 * log_only
 * queue_directory
 * max_delivery
 * 
 * order parameters:
 * id order identification
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-01-03 
 */
public class JobSchedulerDequeueMailJob_old extends JobSchedulerMailJob {


    protected Vector mailOrders                     = null;
    
    protected Iterator mailOrderIterator            = null;

    protected boolean hasDatabase                   = false;

	
    @Override
	public boolean spooler_open() {
        
        try {
            // classic or order driven
            if (spooler_task.job().order_queue() == null) {
                spooler_log.debug6("retrieving mail files from directory: " + this.getQueueDirectory() + " for file specification: " + this.getQueuePrefixSpec());
            
                mailOrders = SOSFile.getFilelist(this.getQueueDirectory(), this.getQueuePrefixSpec(), 0);
                mailOrderIterator = mailOrders.iterator();
                if (mailOrders.size() > 0) spooler_log.info(mailOrders.size() + " mail files found");
            
                return mailOrderIterator.hasNext();
            } else {
                return true;
            }
            
        } catch (Exception e){
            spooler_log.warn("failed to retrieve mail files from directory [" + this.getQueueDirectory() + ", " + this.getQueuePrefixSpec() + "]: " + e.getMessage());
            return false;
        }
    }
    
    
    /**
     * process single mail order
     */
    @Override
	public boolean spooler_process() {

        Order order = null;
        Variable_set parameters = null;
        File listFile                         = null;
        File messageFile                      = null;
        File failedFile                       = null;
        String messageId                      = "";
        long mailOrderId                      = 0;
        long curDeliveryCounter               = 1;
        
        try {
            // classic or order driven
            if (spooler_task.job().order_queue() == null) {
                parameters = spooler_task.params();
                if (!mailOrderIterator.hasNext()) return true;
                listFile = (File) mailOrderIterator.next();

            } else {
                order = spooler_task.order();
                parameters = (Variable_set) spooler_task.order().payload();
                
                if ( parameters.var("file") == null || parameters.var("file").toString().length() == 0)
                    throw new Exception("no filename was given in payload");
                
                listFile = new File(this.getQueueDirectory() 
                        + (!this.getQueueDirectory().endsWith("/") && !this.getQueueDirectory().endsWith("\\") ? "/" : "") 
                        + parameters.var("file"));
            }

            File workFile = new File(listFile.getAbsolutePath());
            if (this.getLogger() != null) this.getLogger().info("processing mail file: " + workFile.getAbsolutePath());

            if (workFile == null) {
                throw new Exception("empty file name found.");
            } else if (!workFile.exists()) {
                throw new Exception("mail file [" + workFile.getAbsolutePath() + "] does not exist.");
            } else if (!workFile.canRead()) {
                throw new Exception("cannot read from mail file [" + workFile.getAbsolutePath() + "]");
            }

            // prepare a file for later rename operations in case of error
            String failedPath = "";
            if (workFile.getAbsolutePath().lastIndexOf("/") > 0) {
                failedPath = workFile.getAbsolutePath().substring(0, workFile.getAbsolutePath().lastIndexOf("/"));
            } else if (workFile.getAbsolutePath().lastIndexOf("\\") > 0) {
                failedPath = workFile.getAbsolutePath().substring(0, workFile.getAbsolutePath().lastIndexOf("\\"));
            }
            if (!failedPath.endsWith("/") && !failedPath.endsWith("\\"))
                failedPath += "/";

            String failedName = workFile.getName();
            if (failedName.endsWith("~")) {
                failedName = failedName.substring(0, failedName.length()-1);
            }
            failedFile = new File(failedPath + this.getFailedPrefix() + failedName);
            
            messageFile = new File(workFile.getAbsolutePath() + "~");
            if (messageFile.exists()) messageFile.delete();
            workFile.renameTo(messageFile);
            
            // populate mailer with settings
            if (sosMailSettings != null) {
                sosMail = new SOSMail( sosMailSettings );
            } else {
                sosMail = new SOSMail(spooler_log.mail().smtp());
            }

            if (this.getLogger() != null) sosMail.setSOSLogger(this.getLogger());

            // job scheduler sets mail host if no default was specified by settings
            if (sosMail.getHost() == null || sosMail.getHost().length() == 0 || sosMail.getHost().equalsIgnoreCase("-queue")) {
                if (!spooler_log.mail().smtp().equalsIgnoreCase("-queue")) {
                    sosMail.setHost(spooler_log.mail().smtp());
                } else {
                    throw new Exception("no SMTP host was configured, global settings contain smtp=-queue");
                }
            }
            
            // job scheduler sets mail queue directory if no default was specified by parameters or settings
            if (sosMail.getQueueDir() == null || sosMail.getQueueDir().length() == 0)
                sosMail.setQueueDir(this.getQueueDirectory());

            // set queue prefix "sos" to enable dequeueing by JobSchedulerMailDequeueJob
            sosMail.setQueuePraefix(this.getQueuePrefix());

            SOSSettings smtpSettings = new SOSProfileSettings(spooler.ini_path());
            Properties smtpProperties = smtpSettings.getSection("smtp");

            if (!smtpProperties.isEmpty()) {
                if (smtpProperties.getProperty("mail.smtp.user") != null && smtpProperties.getProperty("mail.smtp.user").length() > 0) {
                    sosMail.setUser(smtpProperties.getProperty("mail.smtp.user"));
                }
                if (smtpProperties.getProperty("mail.smtp.password") != null && smtpProperties.getProperty("mail.smtp.password").length() > 0) { 
                    sosMail.setPassword(smtpProperties.getProperty("mail.smtp.password"));
                }
                if (smtpProperties.getProperty("mail.smtp.port") != null && smtpProperties.getProperty("mail.smtp.port").length() > 0) { 
                    sosMail.setPort(smtpProperties.getProperty("mail.smtp.port"));
                }
            }

            try { // to load mail and check delivery counter
                sosMail.loadFile(messageFile);
                messageId = sosMail.getLoadedMessageId();
            } catch (Exception e) {
                throw new Exception("mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
            }


            try { // to retrieve mail order from database
                if (this.getConnection() != null && this.hasDatabase()) {
                    String id = this.getConnection().getSingleValue("SELECT \"ID\" FROM " + this.getTableMails()  
                        + " WHERE \"MESSAGE_ID\"='" + messageId + "'");
                    if (id == null || id.length() == 0) {
                        this.getLogger().debug1("no entry found in database for message id [" + messageId + "] of mail file: " + workFile.getAbsolutePath()); 
                    } else {
                        mailOrderId = Integer.parseInt(id);
                    }
                }
            } catch (Exception e) {
                throw new Exception("error occurred retrieving entry in database for message id [" + messageId + "] of mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
            }
            
            
            try { // to check the delivery counter
                if (sosMail.getMessage().getHeader("X-SOSMail-delivery-counter") != null && sosMail.getMessage().getHeader("X-SOSMail-delivery-counter").length > 0) {
                    try {
                        curDeliveryCounter = Integer.parseInt(sosMail.getMessage().getHeader("X-SOSMail-delivery-counter")[0].toString().trim());
                    } catch (Exception ex) {
                        throw new Exception("illegal header value for X-SOSMail-delivery-counter: " + sosMail.getMessage().getHeader("X-SOSMail-delivery-counter")[0].toString());
                    }
                    if (++curDeliveryCounter > this.getMaxDeliveryCounter() && this.getMaxDeliveryCounter() > 0) {
                        if (this.getLogger() != null) this.getLogger().debug3("mail file [" + workFile.getAbsolutePath() + "] exceeds number of trials [" + this.getMaxDeliveryCounter() + "] to send mail and will not be dequeued");
                        sosMail.setQueueDir("");
                    }
                }
                sosMail.getMessage().setHeader("X-SOSMail-delivery-counter", String.valueOf(curDeliveryCounter) );
                sosMail.getMessage().saveChanges();
            } catch (Exception e) {
                throw new Exception("mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
            }

            
            try { // to send
                boolean shouldSend = true;
                File mailFile = null;
                String message = "";

                if (this.getLogDirectory() != null && this.getLogDirectory().length() > 0) {
                    // dump message with attachments
                    mailFile = this.getMailFile(this.getLogDirectory());
                    sosMail.dumpMessageToFile(mailFile, true);
                }
                
                if (this.isLogOnly()) {
                    message = "mail was NOT sent" + (this.getMaxDeliveryCounter() > 0 ? " (trial " + curDeliveryCounter + " of " + this.getMaxDeliveryCounter() + ")" : "") +  " but stored to file: " + mailFile.getAbsolutePath();
                    if (this.getLogger() != null) this.getLogger().info(message);
                    message = message.length() > 250 ? message.substring(message.length()-250) : message;
                    message = message.replaceAll("'", "''");
                    shouldSend = false;
                } else if (!sosMail.send()) {
                    message = "mail was NOT sent but stored for later dequeueing" + (this.getMaxDeliveryCounter() > 0 ? " (trial " + curDeliveryCounter + " of " + this.getMaxDeliveryCounter() + ")" : "") + ", reason was: " + sosMail.getLastError();
                    if (this.getLogger() != null) this.getLogger().info(message);
                    message = message.length() > 250 ? message.substring(message.length()-250) : message;
                    message = message.replaceAll("'", "''");
                    shouldSend = false;
                }


                try { // to check the delivery counter if mail could not be sent
                    if (!shouldSend && curDeliveryCounter > this.getMaxDeliveryCounter() && this.getMaxDeliveryCounter() > 0) {
                        throw new Exception("number of trials [" + this.getMaxDeliveryCounter() + "] exceeded to send mail from file: " + workFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }
                
                
                if (this.getLogger() != null) {
                    if (this.isLogOnly()) {
                        this.getLogger().info("mail was processed from file [" + workFile.getAbsolutePath() + "] to: " + sosMail.getRecipientsAsString() + " into: " + mailFile.getAbsolutePath());
                    } else {
                        this.getLogger().info("mail was sent from file [" + workFile.getAbsolutePath() + "] to: " + sosMail.getRecipientsAsString());
                    }
                }
                if (this.getConnection() != null && this.hasDatabase() && mailOrderId > 0) {
                    this.getConnection().executeUpdate("UPDATE " + this.getTableMails() + " SET \"JOB_ID\"=" + spooler_task.id() + ", \"STATUS\"=" + STATE_SUCCESS + ", \"STATUS_TEXT\"='" + message + "'"
                        + ", \"MESSAGE_ID\"='" + sosMail.getMessage().getMessageID() + "'"
                        + ", \"MODIFIED\"=%now, \"MODIFIED_BY\"='" + spooler_task.job().name() + "', \"DELIVERED\"=%now"
                        + " WHERE \"ID\"=" + mailOrderId);
                    this.getConnection().commit();
                }
                if (this.getLogger() != null) this.getLogger().debug3("mail was sent with headers: " + sosMail.dumpHeaders());
            } catch (Exception ex) {
                throw new Exception("mail was NOT sent from file [" + workFile.getAbsolutePath() + "]" + (mailOrderId > 0 ? " for order [" + mailOrderId + "]" : "") + ": " + ex.getMessage());
            }

            messageFile.delete();

            // return value for classic and order driven processing
            if (spooler_task.job().order_queue() == null) {
                return mailOrderIterator.hasNext();
            } else {
                return true;
            }
        }    
        catch (Exception e) {
            String message = ""; 
            if (spooler_task.job().order_queue() != null) {
                message = "error occurred processing mail [" + mailOrderId + "] : " + e.getMessage();
            } else {
                message = "error occurred processing mail: " + e.getMessage();
            }
            
            // do not create warnings (that are sent by mail) if the number of dequeue trials has exceeded
            // no: always create warnings
            if (curDeliveryCounter > this.getMaxDeliveryCounter() && this.getMaxDeliveryCounter() > 0 
                    && (sosMail.getQueueDir() == null || sosMail.getQueueDir().length() == 0)) {
                spooler_log.warn(message);
            } else {
                spooler_log.warn(message);
            }

            try { // to update database if mail order has been identified
                if (this.getConnection() != null && this.hasDatabase() && mailOrderId > 0) {
                    message = e.getMessage().length() > 250 ? e.getMessage().substring(e.getMessage().length()-250) : e.getMessage();
                    message = message.replaceAll("'", "''");
                    this.getConnection().executeUpdate("UPDATE " + this.getTableMails() + " SET \"JOB_ID\"=" + spooler_task.id() + ", \"STATUS\"=" + STATE_ERROR + ", \"STATUS_TEXT\"='" + message + "',"
                            + "\"MODIFIED\"=%now, \"MODIFIED_BY\"='" + spooler_task.job().name() + "', \"DELIVERED\"=NULL"
                            + " WHERE \"ID\"=" + mailOrderId);
                    this.getConnection().commit();
                }
            } catch (Exception ex) {
                spooler_log.warn("error occurred updating mail order: " + ex.getMessage());
            }
            
            // return value for classic and order driven processing
            if (spooler_task.job().order_queue() == null) {
                return mailOrderIterator.hasNext();
            } else {
                return false;
            }
        } 
        finally {
            try {
                // this file should only exist in case of errors, we rename it with a prefix to prevent further processing
                if (messageFile.exists()) {
                    spooler_log.info("mail file is renamed to exclude it from further processing: " + failedFile.getAbsolutePath());
                    messageFile.renameTo(failedFile);
                }
            } 
            catch (Exception ex) {} // gracefully ignore this error to preserve the original exception

            try {
                if (this.getConnection() != null) this.getConnection().rollback();
            } 
            catch (Exception ex) {} // gracefully ignore this error to preserve the original exception
        }
	}

}