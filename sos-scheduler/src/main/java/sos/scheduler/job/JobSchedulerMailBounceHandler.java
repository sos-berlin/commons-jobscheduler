package sos.scheduler.job;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.SearchTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.net.SOSMail;
import sos.net.SOSMailAttachment;
import sos.net.SOSMailReceiver;
import sos.net.SOSMimeMessage;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSString;

/** @author ghassan beydoun */
public class JobSchedulerMailBounceHandler extends JobSchedulerMailJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerMailBounceHandler.class);

    private static final String X_SOSMAIL_DELIVERY_COUNTER_HEADER = "X-SOSMail-delivery-counter";
    private List<Map<String, String>> mailBouncePatternTableList = new ArrayList<Map<String, String>>();
    private SOSMailReceiver receiver = null;
    private LinkedHashMap<String, Pattern> patternMap = null;
    private Map<String, String> patternActions = null;
    private String patternId = null;
    private String forwardSubject = "Here is the original message:\n\n";
    private boolean handleBouncedMailOnly = true;
    private String bounceDirectory;
    private int retryInterval = -1;
    private String xSOSMailDeliveryCounterHeader;
    private String jobChainName = "scheduler_send_bounced_mails";
    private String mailId = null;
    SOSMail forwardMessage = null;
    SOSMimeMessage sosMimeMessage = null;
    String smtpHost = "";
    String smtpPort = "25";
    String receiverHost = "";
    String receiverPort = "110";
    String protocol = "imap";
    String pop3_user = "";
    String pop3_password = "";
    boolean mailUseSsl = false;
    String[] toArray = null;
    String[] ccArray = null;
    String[] bccArray = null;
    String from = "";
    String fromName = "";

    public boolean spooler_init() {
        try {
            if (!super.spooler_init()) {
                return false;
            }
            LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
            this.getJobSettings().setKeysToLowerCase();
            setJobSettings();
            getMailBouncePatterns();
            setMailBouncePatterns();
            setPatternActions();
            if (spooler_task != null) {
                this.setJobId(spooler_task.id());
            }
            if (spooler_job != null) {
                this.setJobName(spooler_job.name());
            }
            if (spooler_job != null) {
                this.setJobTitle(spooler_job.title());
            }
            if (spooler_task.params().var("mail_use_ssl") != null && !spooler_task.params().var("mail_use_ssl").isEmpty()) {
                if ("true".equalsIgnoreCase(spooler_task.params().var("mail_use_ssl"))) {
                    mailUseSsl = true;
                }
                spooler_log.debug6(".. job parameter [mail_use_ssl]: [" + spooler_task.params().var("mail_use_ssl") + "]");
            }
            if (spooler_task.params().var("handle_bounced_mail_only") != null && !spooler_task.params().var("handle_bounced_mail_only").isEmpty()) {
                if ("true".equalsIgnoreCase(spooler_task.params().var("handle_bounced_mail_only"))) {
                    setHandleBouncedMailOnly(true);
                }
                spooler_log.debug6(".. job parameter [handle_bounced_mail_only]: [" + spooler_task.params().var("handle_bounced_mail_only") + "]");
            }
            if (spooler_task.params().var("bounce_directory") == null || spooler_task.params().var("bounce_directory").isEmpty()) {
                throw new Exception("No bounce directory specified in entry [bounce_directory] of job section in file " + spooler.ini_path());
            }
            setBounceDirectory(spooler_task.params().var("bounce_directory"));
            spooler_log.debug6(".. job parameter [bounce_directory]: [" + spooler_task.params().var("bounce_directory") + "]");
            new File(getBounceDirectory()).mkdirs();
        } catch (Exception e) {
            try {
                LOGGER.error(SOSClassUtil.getMethodName() + ": " + e.getMessage(), e);
            } catch (Exception ex) {
                //
            }
            return false;
        }
        return true;
    }

    public boolean spooler_process() throws Exception {
        boolean expunge = false;
        Folder folder = null;
        Message[] messages = {};
        SOSMimeMessage sosMimeMessage = null;
        try {
            LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
            receiver = new SOSMailReceiver(receiverHost, receiverPort, pop3_user, pop3_password, mailUseSsl, protocol);
            receiver.connect(protocol);
            folder = receiver.openFolder("INBOX", receiver.READ_WRITE);
            if (this.isHandleBouncedMessagesOnly()) {
                messages = folder.search(getBounceSerachTerm());
            } else {
                messages = folder.getMessages();
            }
            LOGGER.debug("total found messages: " + messages.length);
            for (int i = 0; i < messages.length; i++) {
                sosMimeMessage = new SOSMimeMessage(messages[i]);
                if (sosMimeMessage.isBounce()) {
                    handleBounceMessage(sosMimeMessage);
                }
            }
            this.getConnection().commit();
            expunge = true;
        } finally {
            try {
                receiver.closeFolder(expunge);
            } catch (Exception e) {
                //
            }
            try {
                receiver.disconnect();
            } catch (Exception e) {
                //
            }
            try {
                this.getConnection().disconnect();
            } catch (Exception e) {
                //
            }
        }
        return false;
    }

    private void setJobSettings() throws Exception {
        this.setJobProperties(this.getJobSettings().getSection("job " + spooler_job.name()));
        Properties ini = this.getJobProperties();
        if (ini.getProperty("protocol") == null) {
            throw new Exception("[protocol] missing!!");
        }
        protocol = ini.getProperty("protocol");
        LOGGER.debug("..protocol [" + protocol + "]");
        if (ini.getProperty("pop3_host") == null) {
            throw new Exception("[pop3_host] missing!!");
        }
        receiverHost = ini.getProperty("pop3_host");
        LOGGER.debug("..receiver host [" + receiverHost + "]");
        if (ini.getProperty("smtp_host") == null) {
            throw new Exception("[smtp host] missing!!");
        }
        smtpHost = ini.getProperty("smtp_host");
        LOGGER.debug("..smtp host [" + smtpHost + "]");
        if (ini.getProperty("smtp_port") == null) {
            throw new Exception("[smtp port] missing!!");
        }
        smtpPort = ini.getProperty("smtp_port");
        LOGGER.debug("..smtp port [" + smtpPort + "]");
        if (ini.getProperty("pop3_user") == null) {
            throw new Exception("[pop3_user] missing!!");
        }
        pop3_user = ini.getProperty("pop3_user");
        LOGGER.debug("..pop3 user [" + pop3_user + "]");
        if (ini.getProperty("pop3_password") == null) {
            throw new Exception("[pop3_password] missing!!");
        }
        pop3_password = ini.getProperty("pop3_password");
        LOGGER.debug("..pop3 password [****]");
        if (ini.getProperty("forward_subject") == null) {
            throw new Exception("[forward_subject] missing!!");
        }
        forwardSubject = ini.getProperty("forward_subject");
        LOGGER.debug("..forward subject [" + forwardSubject + "]");
    }

    // private void setProxy() {
    // // set proxy if needed
    // }

    public final SearchTerm getBounceSerachTerm() throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        return new AndTerm(new HeaderTerm("Return-Path", "<>"), new HeaderTerm("Content-Type", "multipart/report"));
    }

    public String getPatternAction(SOSMimeMessage sosMimeMessage) throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        String actionPattern = null;
        Matcher matcher = null;
        String inputString = null;
        Pattern pattern;
        inputString = sosMimeMessage.getPlainTextBody();
        for (Entry<String, Pattern> entry : patternMap.entrySet()) {
            patternId = entry.getKey();
            pattern = entry.getValue();
            matcher = pattern.matcher(inputString);
            LOGGER.debug("..try find string matched [" + inputString + "]" + pattern.pattern());
            if (matcher.find()) {
                LOGGER.debug("string matched [" + inputString + "]" + pattern.pattern());
                LOGGER.info("..pattern captured \"" + pattern.pattern() + "\"");
                break;
            }
        }
        for (Map<String, String> patternEntry : mailBouncePatternTableList) {
            if (patternEntry.get("pattern_id").equals(patternId)) {
                actionPattern = patternEntry.get("action");
                break;
            }
        }
        return patternActions.get(actionPattern);
    }

    private void getMailBouncePatterns() throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        StringBuilder query = new StringBuilder();
        query.append("SELECT \"PATTERN_ID\",\"PATTERN\",\"ACTION\",\"MAX_RETRIES\",");
        query.append("\"RETRY_INTERVAL\",\"MAIL_TO\",\"CC_TO\",\"BCC_TO\",\"REPLY_TO\",");
        query.append("\"SUBJECT\",\"SUBJECT_TEMPLATE\",\"SUBJECT_TEMPLATE_TYPE\" FROM ");
        query.append(this.getTableMailBouncePatterns());
        query.append(" ORDER BY \"ORDERING\" ASC");
        LOGGER.debug("..query [" + query + "]");
        mailBouncePatternTableList.addAll(this.getConnection().getArray(query.toString()));
        if (mailBouncePatternTableList.isEmpty()) {
            throw new Exception(SOSClassUtil.getMethodName() + ": No entries found!!");
        }
    }

    private void setMailBouncePatterns() throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        patternMap = new LinkedHashMap<String, Pattern>();
        String patternId = "";
        String patternString = "";
        Pattern pattern = null;
        for (Map<String, String> record : mailBouncePatternTableList) {
            if ((record.get("pattern_id") != null) && (record.get("pattern") != null)) {
                LOGGER.debug(record.get("pattern_id") + "=" + "=" + record.get("pattern"));
                patternId = record.get("pattern_id");
                patternString = record.get("pattern");
                pattern = Pattern.compile(patternString, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                LOGGER.trace("..pattern \"" + record.get("pattern") + "\" compiled successfully.");
                patternMap.put(patternId, pattern);
            }
        }
    }

    public void handleBounceMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        String bouncedMailAction = "";
        int bouncedMailStatus = -1;
        Vector<SOSMailAttachment> sosMailAttachmentList = sosMimeMessage.getSosMailAttachments();
        LOGGER.info("..bounced message found:");
        Iterator<SOSMailAttachment> it = sosMailAttachmentList.iterator();
        SOSMailAttachment sosMailAttachment = null;
        for (; it.hasNext();) {
            sosMailAttachment = it.next();
            if ("message/rfc822".equalsIgnoreCase(sosMailAttachment.getContentType())) {
                SOSMimeMessage originalMessage = sosMimeMessage.getAttachedSosMimeMessage(receiver.getSession(), sosMailAttachment.getContent());
                this.setXSOSMailDeliveryCounterHeader(getXSOSMailDeliveryCounterHeader(originalMessage));
                LOGGER.info("....originally message attributes:");
                LOGGER.info("....message id [" + originalMessage.getMessageId() + "]");
                LOGGER.info("....from [" + originalMessage.getFromAddress() + "]");
                LOGGER.info("....sent date [" + originalMessage.getSentDateAsString("yyyy-MM-dd HH:mm:ss") + "]");
                setMailId(getMailId(originalMessage.getMessageId()));
                if (SOSString.isEmpty(getMailId())) {
                    continue;
                }
                this.updateTableMails(originalMessage.getMessageId());
                if (!SOSString.isEmpty(getXSOSMailDeliveryCounterHeader())) {
                    LOGGER.info("...current " + JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER + " [" + this
                            .getXSOSMailDeliveryCounterHeader() + "]");
                }
                bouncedMailAction = this.getPatternAction(sosMimeMessage);
                LOGGER.info("..available pattern action for this bounce [" + bouncedMailAction + "]");
                if ("drop".equalsIgnoreCase(bouncedMailAction)) {
                    LOGGER.info("..Message with ID [" + sosMimeMessage.getMessageId() + "] is marked for delete.");
                    sosMimeMessage.deleteMessage();
                    bouncedMailStatus = BounceMailStatus.CANCELLED;
                } else if ("store".equalsIgnoreCase(bouncedMailAction)) {
                    String fileName = getLogDirectory() + File.separator + sosMimeMessage.getMessageId();
                    sosMimeMessage.dumpMessageToFile(fileName, true, false);
                    bouncedMailStatus = BounceMailStatus.REQUESTED;
                } else if ("retry".equalsIgnoreCase(bouncedMailAction)) {
                    if (isRetrySendingAllowed(originalMessage.getMessageId())) {
                        setRetryInterval(getRetryInterval(originalMessage));
                        int currentHeaderValue;
                        if ((currentHeaderValue = originalMessage.incrementHeader(
                                JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER)) == -1) {
                            currentHeaderValue = 1;
                            originalMessage.setHeader(JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER, "1");
                            LOGGER.debug(".. [" + JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER + "] set.");
                        }
                        LOGGER.debug("..current value of \"" + JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER + "\" ["
                                + currentHeaderValue + "]");
                        LOGGER.debug("..query directory [" + bounceDirectory + " ] set.");
                        originalMessage.setQueueDir(bounceDirectory);
                        originalMessage.dumpMessageToFile(true, false);
                        LOGGER.debug("..currently dumped message [" + originalMessage.getDumpedFileName() + "]");
                        if (!orderRetrySend(originalMessage)) {
                            continue;
                        }
                    }
                    bouncedMailStatus = BounceMailStatus.DELIVERED;
                } else if ("forward".equalsIgnoreCase(bouncedMailAction)) {
                    forwardMessage(sosMimeMessage.getMessage(), receiver.getSession());
                    bouncedMailStatus = BounceMailStatus.DELIVERED;
                }
                updateMailBouncesTable(originalMessage, bouncedMailStatus);
            }
        }
    }

    public void updateMailBouncesTable(SOSMimeMessage sosMimeMessage, int bouncedMailStatusCode) throws Exception {
        StringBuilder query = new StringBuilder();
        String bouncedMailStatusText = "";
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        if (bouncedMailStatusCode == BounceMailStatus.REQUESTED) {
            bouncedMailStatusText = "requested";
        } else if (bouncedMailStatusCode == BounceMailStatus.CANCELLED) {
            bouncedMailStatusText = "CANCELLED";
        } else if (bouncedMailStatusCode == BounceMailStatus.DELIVERED) {
            bouncedMailStatusText = "DELIVERED";
        } else if (bouncedMailStatusCode == BounceMailStatus.HAS_ERRORS) {
            bouncedMailStatusText = "HAS_ERRORS";
        }
        query = new StringBuilder();
        query.append("INSERT INTO ");
        query.append(this.getTableMailBounces());
        query.append(" (\"JOB_ID\",\"INBOUND_ID\",\"MESSAGE_ID\",");
        query.append("\"RECEIVED\",\"PATTERN_ID\",\"OUTBOUND_ID\",\"STATUS\",");
        query.append("\"STATUS_TEXT\",\"CREATED\",\"CREATED_BY\",\"MODIFIED\",\"MODIFIED_BY\")");
        query.append(" VALUES (");
        query.append(this.getJobId());
        query.append(",");
        query.append(SOSString.isEmpty(getMailId()) ? "0" : getMailId());// inbound_id=mails.id
        query.append(",'");
        query.append(sosMimeMessage.getMessageId());
        query.append("',%timestamp('");
        query.append(sosMimeMessage.getSentDateAsString());
        query.append("'),");
        query.append(patternId);
        query.append(",");
        query.append(SOSString.isEmpty(getMailId()) ? "0" : getMailId());// inbound_id=mails.id
        query.append(",");
        query.append(bouncedMailStatusCode);
        query.append(",'");
        query.append(bouncedMailStatusText);
        query.append("',");
        query.append("%now,'");
        query.append(this.getJobName());
        query.append("',%now,'");
        query.append(this.getJobName());
        query.append("')");
        LOGGER.debug("update MAIL_BOUNCES_TABLE: " + query);
        this.getConnection().execute(query.toString());
        this.getConnection().commit();
    }

    public boolean isHandleBouncedMessagesOnly() {
        return handleBouncedMailOnly;
    }

    public void setHandleBouncedMailOnly(boolean handleBouncedMailOnly) {
        this.handleBouncedMailOnly = handleBouncedMailOnly;
    }

    public void setPatternActions() throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        this.patternActions = new HashMap<String, String>();
        this.patternActions.put("0", "drop");
        this.patternActions.put("10", "store");
        this.patternActions.put("100", "retry");
        this.patternActions.put("200", "forward");
    }

    public int getRetryCounter(String messageId) throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        Map<String, String> result = new HashMap<String, String>();
        StringBuffer query = new StringBuffer();
        query.append("SELECT \"RETRY_COUNT\" FROM ");
        query.append(this.getTableMails());
        query.append(" WHERE \"MESSAGE_ID\" ='");
        query.append(messageId);
        query.append("'");
        LOGGER.debug("..query[" + query.toString() + "]");
        result = getConnection().getSingle(query.toString());
        if (result.get("retry_count") != null && !SOSString.isEmpty(result.get("retry_count"))) {
            try {
                LOGGER.info("..message with ID [" + messageId + "] is already delivered [" + result.get("retry_count") + "] time(s)");
            } catch (Exception e) {
                //
            }
            return Integer.parseInt(result.get("retry_count"));
        }
        return -1;
    }

    public void updateTableMails(String messageId) throws Exception {
        StringBuilder query = new StringBuilder();
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        query.append("UPDATE ");
        query.append(this.getTableMails());
        query.append(" SET \"STATUS\"=");
        query.append(BounceMailStatus.REQUESTED);
        query.append(",\"STATUS_TEXT\"='requested',");
        query.append("\"MODIFIED\"=%now,\"MODIFIED_BY\"='");
        query.append(this.getJobName());
        query.append("' WHERE \"MESSAGE_ID\"='");
        query.append(messageId);
        query.append("'");
        LOGGER.debug("update table \"MAILS\": " + query);
        this.getConnection().execute(query.toString());
    }

    private String getMailId(String messageId) throws Exception {
        StringBuilder query = new StringBuilder();
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        query.append("SELECT \"ID\" FROM ");
        query.append(this.getTableMails());
        query.append(" WHERE \"MESSAGE_ID\"='");
        query.append(messageId);
        query.append("'");
        LOGGER.debug(".. query: " + query);
        return this.getConnection().getSingleValue(query.toString());
    }

    public boolean isRetrySendingAllowed(String messageId) throws Exception {
        int retryCounter = -1;
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        retryCounter = this.getRetryCounter(messageId);
        if (retryCounter <= 0 && !SOSString.isEmpty(this.getXSOSMailDeliveryCounterHeader())) {
            retryCounter = Integer.parseInt(this.getXSOSMailDeliveryCounterHeader());
        }
        LOGGER.debug(".. current retry counter [" + retryCounter + "]");
        if (retryCounter > 0) {
            for (Map<String, String> patternEntry : this.mailBouncePatternTableList) {
                if (patternEntry.get("pattern_id") != null && patternEntry.get("pattern_id").equals(patternId) && patternEntry.get(
                        "max_retries") != null) {
                    return Integer.parseInt(patternEntry.get("max_retries").toString()) > retryCounter;
                }
            }
        }
        return false;
    }

    public int getRetryInterval(SOSMimeMessage sosMimeMessage) throws Exception {
        StringBuilder query = new StringBuilder();
        String retryItervalString = null;
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        int atIndex = sosMimeMessage.getFromAddress().indexOf('@');
        String domain = sosMimeMessage.getFromAddress().substring(atIndex + 1, sosMimeMessage.getFromAddress().length());
        retryInterval = -1;
        query.append("SELECT \"RETRY_INTERVAL\" FROM ");
        query.append(this.getTableMailBounceDeliveries());
        query.append(" WHERE \"DOMAIN\"='");
        query.append(domain);
        query.append("'");
        LOGGER.debug("..query [" + query + "]");
        retryItervalString = getConnection().getSingleValue(query.toString());
        if (!SOSString.isEmpty(retryItervalString)) {
            retryInterval = Integer.parseInt(retryItervalString);
        }
        if (retryInterval <= 0) {
            query = new StringBuilder();
            query.append("SELECT \"RETRY_INTERVAL\" FROM ");
            query.append(this.getTableMailBouncePatternRetries());
            query.append(" WHERE \"PATTERN_ID\"=");
            query.append(patternId);
            LOGGER.debug("..query [" + query + "]");
            retryItervalString = getConnection().getSingleValue(query.toString());
            if (!SOSString.isEmpty(retryItervalString)) {
                retryInterval = Integer.parseInt(retryItervalString);
            }
        }
        LOGGER.debug("..current retry interval [" + retryInterval + "]");
        return retryInterval;
    }

    public String getBounceDirectory() {
        return bounceDirectory;
    }

    public void setBounceDirectory(String bounceDirectory) {
        this.bounceDirectory = bounceDirectory;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getXSOSMailDeliveryCounterHeader() {
        return xSOSMailDeliveryCounterHeader;
    }

    private String getXSOSMailDeliveryCounterHeader(SOSMimeMessage sosMimeMessage) throws Exception {
        return sosMimeMessage.getHeaderValue(X_SOSMAIL_DELIVERY_COUNTER_HEADER);
    }

    private void setXSOSMailDeliveryCounterHeader(String mailDeliveryCounterHeader) {
        xSOSMailDeliveryCounterHeader = mailDeliveryCounterHeader;
    }

    public boolean orderRetrySend(SOSMimeMessage sosMimeMessage) throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        Variable_set orderData = spooler.create_variable_set();
        orderData.set_var("file", new File(sosMimeMessage.getDumpedFileName()).getName());
        if (!SOSString.isEmpty(getMailId())) {
            orderData.set_var("id", getMailId());
        }
        Order order = spooler.create_order();
        order.set_title(this.getJobTitle() + "." + jobChainName);
        order.set_payload(orderData);
        if (!spooler.job_chain_exists(jobChainName)) {
            LOGGER.warn("could not find job chain: " + jobChainName);
            return false;
        }
        spooler.job_chain(jobChainName).add_or_replace_order(order);
        spooler_task.job().set_delay_order_after_setback(1, this.getRetryInterval());
        LOGGER.debug(".. order " + order.title() + " added.");
        return true;
    }

    public String getJobChainName() {
        return jobChainName;
    }

    public void setJobChainName(String jobChainName) {
        this.jobChainName = jobChainName;
    }

    // private void createJobChain() throws Exception {
    // LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
    // if (spooler.job_chain_exists(jobChainName)) {
    // return;
    // }
    // Job_chain jobChain = spooler.create_job_chain();
    // LOGGER.debug(".. job chain [" + jobChainName + "] created.");
    // jobChain.set_name(this.getJobName() + "." + jobChainName);
    // jobChain.add_job(jobChainName, "0", "100", "1100");
    // LOGGER.debug(".. job [" + jobChainName + "] added.");
    // jobChain.add_end_state("100");
    // jobChain.add_end_state("1100");
    // spooler.add_job_chain(jobChain);
    // LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
    // }

    public void forwardMessage(MimeMessage message, Session session) throws Exception {
        LOGGER.debug("Calling " + SOSClassUtil.getMethodName());
        String[] toArray = {};
        String[] ccArray = {};
        String[] bccArray = {};
        String mailFrom = "";
        for (Map<String, String> patternEntry : this.mailBouncePatternTableList) {
            if (patternEntry.get("pattern_id") != null && patternEntry.get("pattern_id").equals(patternId)) {
                if (patternEntry.get("mail_to") == null) {
                    throw new Exception("[mail_to] missing!!");
                }
                toArray = patternEntry.get("mail_to").trim().split("[;,]");
                LOGGER.trace("..mail to  [" + patternEntry.get("mail_to") + "]");
                if (patternEntry.get("mail_cc") != null) {
                    ccArray = patternEntry.get("mail_cc").trim().split("[;,]");
                    LOGGER.trace("..mail cc [" + patternEntry.get("mail_cc") + "]");
                }
                if (patternEntry.get("mail_bcc") != null) {
                    ccArray = patternEntry.get("mail_bcc").trim().split("[;,]");
                    LOGGER.trace("..mail bcc [" + patternEntry.get("mail_bcc") + "]");
                }
                if (patternEntry.get("reply_to") == null) {
                    throw new Exception("[reply_to] missing!!");
                }
                mailFrom = patternEntry.get("reply_to");
                LOGGER.trace("..reply_to [" + patternEntry.get("reply_to") + "]");
                break;
            }
        }
        session.getProperties().put("mail.smtp.host", smtpHost);
        session.getProperties().put("mail.smtp.port", smtpPort);
        MimeMessage forward = new MimeMessage(session);
        forward.setSubject("Fw. " + message.getSubject());
        forward.setFrom(new InternetAddress(mailFrom));
        for (int i = 0; i < toArray.length; i++) {
            forward.addRecipient(Message.RecipientType.TO, new InternetAddress(toArray[i]));
        }
        for (int i = 0; i < ccArray.length; i++) {
            forward.addRecipient(Message.RecipientType.CC, new InternetAddress(ccArray[i]));
        }
        for (int i = 0; i < bccArray.length; i++) {
            forward.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccArray[i]));
        }
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(forwardSubject);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(message.getDataHandler());
        multipart.addBodyPart(messageBodyPart);
        forward.setContent(multipart);
        Transport.send(forward);
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    final class BounceMailStatus {

        public final static int REQUESTED = 0;
        public final static int DELIVERED = 1;
        public final static int CANCELLED = 1001;
        public final static int HAS_ERRORS = 1002;
    }

}