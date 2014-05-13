package sos.scheduler.job;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.SearchTerm;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;

import sos.net.SOSMail;
import sos.net.SOSMailAttachment;
import sos.net.SOSMailReceiver;
import sos.net.SOSMimeMessage;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import sos.util.SOSClassUtil;
import sos.util.SOSString;

/**
 * baunced mail handler
 * 
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id$
 */

public class JobSchedulerMailBounceHandler extends JobSchedulerMailJob {

    /** the contents of the MAIL_BOUNCE_PATTERN_TABLE_LIST as a list object */
    private List mailBouncePatternTableList = new ArrayList();

    /** the sos receiver client */
    private SOSMailReceiver receiver = null;

    /** patterm map object <patternId,pattern> to be initialized in spooler_init */
    private LinkedHashMap patternMap = null;

    private HashMap patternActions = null;

    /** the pattern id of matched bounce message body */
    private String patternId = null;

    /** the forwarding mail object */
    SOSMail forwardMessage = null;

    /** the sos mime mail object */
    SOSMimeMessage sosMimeMessage = null;

    String smtpHost = "";

    String smtpPort = "25";

    String receiverHost = "";

    String receiverPort = "110";

    /** the protocol to be used */
    String protocol = "imap";

    String pop3_user = "";

    String pop3_password = "";

    String[] toArray = null;

    String[] ccArray = null;

    String[] bccArray = null;

    String from = "";

    String fromName = "";

    private String forwardSubject = "Here is the original message:\n\n";

    private boolean handleBouncedMailOnly = true;

    private String bounceDirectory;

    private int retryInterval = -1;

    private static final String X_SOSMAIL_DELIVERY_COUNTER_HEADER = "X-SOSMail-delivery-counter";

    private String xSOSMailDeliveryCounterHeader;

    private String jobChainName = "scheduler_send_bounced_mails";
    
    /** the id of the message in the MAILS table */ 
    private String mailId = null;
    
 

    /*
     * 
     * 
     * @see sos.spooler.Job_impl#spooler_init()
     */
    public boolean spooler_init() {

        try {

            if (!super.spooler_init())
                return false;

            getLogger().debug3("Calling " + SOSClassUtil.getMethodName());

            this.getJobSettings().setKeysToLowerCase();

            // set the job properties
            setJobSettings();

            // populate the mailBouncePatternTableList
            getMailBouncePatterns();

            // populate the mailBouncePatternList
            setMailBouncePatterns();

            // initialized pattern action
            setPatternActions();

            if (spooler_task != null)
                this.setJobId(spooler_task.id());
            if (spooler_job != null)
                this.setJobName(spooler_job.name());
            if (spooler_job != null)
                this.setJobTitle(spooler_job.title());

            if (spooler_task.params().var("handle_bounced_mail_only") != null) {
                if (spooler_task.params().var("handle_bounced_mail_only")
                        .length() > 0) {
                    if (spooler_task.params().var(
                            "handle_bounced_mail_only").equalsIgnoreCase(
                            "true"))
                        setHandleBouncedMailOnly(true);
                    spooler_log
                            .debug6(".. job parameter [handle_bounced_mail_only]: ["
                                    + spooler_task.params().var(
                                            "handle_bounced_mail_only")
                                    + "]");
                }
            }

            
            if (spooler_task.params().var("bounce_directory") == null || 
            		spooler_task.params().var("bounce_directory").length() ==  0) {
            	
            	throw new Exception("No bounce directory specified in entry [bounce_directory] of job section in file " + spooler.ini_path());
            	
            }
            
            setBounceDirectory(spooler_task.params().var("bounce_directory"));
            
            spooler_log.debug6(".. job parameter [bounce_directory]: ["
                                        + spooler_task.params().var(
                                                "bounce_directory") + "]");
            if (!(new File(getBounceDirectory()).exists()))
                            ;
              new File(getBounceDirectory()).mkdirs();


            //createJobChain();

        } catch (Exception e) {
            try {
                this.getLogger().error(SOSClassUtil.getMethodName() + ": " + e.getMessage());
            } catch (Exception ex) {
            }
            return false;
        }

        return true;
    }//spooler_init

    /**
     * this is our main program.
     */
    public boolean spooler_process() throws Exception {

    	boolean expunge		= false; // delete marked messages
        Folder folder 		= null;
        Message[] messages 	= {};
        SOSMimeMessage sosMimeMessage = null;

        try {

            this.getLogger().debug3("Calling " + SOSClassUtil.getMethodName());

            receiver = new SOSMailReceiver(receiverHost, receiverPort, pop3_user,
                    pop3_password);

            receiver.setLogger(this.getLogger());

            receiver.connect(protocol);

            folder = receiver.openFolder("INBOX", receiver.READ_WRITE);

            // processed bounced emails selected??
            if (this.isHandleBouncedMessagesOnly())
                messages = folder.search(getBounceSerachTerm());
            else
                messages = folder.getMessages();

            getLogger().debug5("total found messages: " + messages.length);

            for (int i = 0; i < messages.length; i++) {
                sosMimeMessage = new SOSMimeMessage(messages[i], this
                        .getLogger());
                if (sosMimeMessage.isBounce()) {
                    handleBounceMessage(sosMimeMessage);
                }// if
            }// for
            
            this.getConnection().commit();
            expunge = true;

        } finally {
            try {
                receiver.closeFolder(expunge);
            } catch (Exception e) {
            }
            try {
                receiver.disconnect();
            } catch (Exception e) {
            }
            try {
                this.getConnection().disconnect();
            } catch (Exception e) {
            }
        }
        return false;
    }// spooler_process

    /**
     * sets the job properties.
     * 
     * @throws Exception
     */
    private void setJobSettings() throws Exception {

        this.setJobProperties(this.getJobSettings().getSection(
                "job " + spooler_job.name()));

        Properties ini = this.getJobProperties();

        // pop3 or imap
        if (ini.getProperty("protocol") == null)
            throw new Exception("[protocol] missing!!");
        protocol = ini.getProperty("protocol");
        this.getLogger().debug5("..protocol [" + protocol + "]");

        // get pop3 host
        if (ini.getProperty("pop3_host") == null)
            throw new Exception("[pop3_host] missing!!");
        receiverHost = ini.getProperty("pop3_host");
        this.getLogger().debug5("..receiver host [" + receiverHost + "]");

        // get smtp host
        if (ini.getProperty("smtp_host") == null)
            throw new Exception("[smtp host] missing!!");
        smtpHost = ini.getProperty("smtp_host");
        this.getLogger().debug5("..smtp host [" + smtpHost + "]");

        // smtp port
        if (ini.getProperty("smtp_port") == null)
            throw new Exception("[smtp port] missing!!");
        smtpPort = ini.getProperty("smtp_port");
        this.getLogger().debug5("..smtp port [" + smtpPort + "]");

        // user
        if (ini.getProperty("pop3_user") == null)
            throw new Exception("[pop3_user] missing!!");
        pop3_user = ini.getProperty("pop3_user");
        this.getLogger().debug5("..pop3 user [" + pop3_user + "]");

        // password
        if (ini.getProperty("pop3_password") == null)
            throw new Exception("[pop3_password] missing!!");
        pop3_password = ini.getProperty("pop3_password");
        this.getLogger().debug5("..pop3 password [****]");

     /*   
        // to
        if (ini.getProperty("to") == null)
            throw new Exception("[to] missing!!");
        toArray = ini.getProperty("to").trim().split("[;,]");
        this.getLogger().debug5("..to array [" + ini.getProperty("to") + "]");

        // get cc
        if (ini.getProperty("cc") == null)
            throw new Exception("[cc] missing!!");
        ccArray = ini.getProperty("cc").trim().split(";");
        this.getLogger().debug5("..cc array [" + ini.getProperty("cc") + "]");

        // get bcc
        if (ini.getProperty("bcc") == null)
            throw new Exception("[bcc] missing!!");
        bccArray = ini.getProperty("bcc").trim().split(";");
        this.getLogger().debug5("..bcc array [" + ini.getProperty("bcc") + "]");

        // get from address
        if (ini.getProperty("from") == null)
            throw new Exception("[from] missing!!");
        from = ini.getProperty("from");
        this.getLogger().debug5("..bcc array [" + from + "]");

        // get fromName
        if (ini.getProperty("fromname") == null)
            throw new Exception("[fromname] missing!!");
        fromName = ini.getProperty("fromname");
        this.getLogger().debug5("..from name [" + fromName + "]");
*/
        // get forwardSubject
        if (ini.getProperty("forward_subject") == null)
            throw new Exception("[forward_subject] missing!!");
        
        forwardSubject = ini.getProperty("forward_subject");
        this.getLogger().debug5("..forward subject [" + forwardSubject + "]");

    }// setJobSettings

    /**
     * sets the proxy settings if needed.
     * 
     */
    private void setProxy() {
        // set proxy if needed
        /*
         * System.getProperties().put("proxySet","true");
         * System.getProperties().put("proxyHost","");
         * System.getProperties().put("proxyPort","3128");
         */
    }

    /**
     * return the search item for the bounced message.
     * 
     * Note: bounced messages should have: - an empty return-path -
     * Content-Type: multipart/report
     * 
     * @throws Exception
     * 
     */
    public final SearchTerm getBounceSerachTerm() throws Exception {
        this.getLogger().debug5("Calling " + SOSClassUtil.getMethodName());
        return new AndTerm(new HeaderTerm("Return-Path", "<>"), new HeaderTerm(
                "Content-Type", "multipart/report"));
    }// SearchTerm

    /**
     * returns the action for the specified bounced message.
     * 
     * @param sosMimeMessage
     * @return the action for the specified bounced message otherwise null
     * @throws Exception
     */
    public String getPatternAction(SOSMimeMessage sosMimeMessage)
            throws Exception {
        this.getLogger().debug3("Calling " + SOSClassUtil.getMethodName());

        String actionPattern = null;
        Matcher matcher = null;
        HashMap patternEntry = new HashMap();
        String inputString = null;
        Pattern pattern;

        inputString = sosMimeMessage.getPlainTextBody();

        //for (Map.Entry entry : patternMap.entrySet()) {
        Iterator iterator = patternMap.entrySet().iterator();
        Map.Entry entry = null;
        for (;iterator.hasNext(); ) {
        	entry= (Map.Entry)iterator.next();
            patternId = (String)entry.getKey();
            pattern = (Pattern)entry.getValue();

            matcher = pattern.matcher(inputString);
            
            this.getLogger().debug5(
                    "..try find string matched [" + inputString + "]"
                            + pattern.pattern());

            if (matcher.find()) {
                this.getLogger().debug5(
                        "string matched [" + inputString + "]"
                                + pattern.pattern());
                this.getLogger().info(
                        "..pattern captured \"" + pattern.pattern() + "\"");
                break;
            }// if
        } // for

        // parse the pattern action
        for (int i = 0; i < this.mailBouncePatternTableList.size(); i++) {
            patternEntry = (HashMap) this.mailBouncePatternTableList.get(i);
            if (patternEntry.get("pattern_id").toString().equals(patternId)) {
                actionPattern = patternEntry.get("action").toString();
                break;
            }// if
        }// for

        /*
         * for(Map.Entry<String, String> entry : patternActions.entrySet() ) {
         * if ( actionPattern.equals(entry.getKey())) return entry.getValue();
         * }//for
         */
        return (String)patternActions.get(actionPattern);

        // return null;

    }// getPatternAction

    /**
     * populate the mailBouncePatternTableList
     * 
     * @throws Exception
     */
    private void getMailBouncePatterns() throws Exception {
        this.getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        StringBuffer query = new StringBuffer();

        query
                .append("SELECT \"PATTERN_ID\",\"PATTERN\",\"ACTION\",\"MAX_RETRIES\",");
        query
                .append("\"RETRY_INTERVAL\",\"MAIL_TO\",\"CC_TO\",\"BCC_TO\",\"REPLY_TO\",");
        query
                .append("\"SUBJECT\",\"SUBJECT_TEMPLATE\",\"SUBJECT_TEMPLATE_TYPE\" FROM ");
        query.append(this.getTableMailBouncePatterns());
        query.append(" ORDER BY \"ORDERING\" ASC");
        this.getLogger().debug5("..query [" + query + "]");
        mailBouncePatternTableList.addAll(this.getConnection().getArray(
                query.toString()));

        if (mailBouncePatternTableList.isEmpty())
            throw new Exception(SOSClassUtil.getMethodName() + ": No entries found!!");
    }// getMailBouncePatterns

    /**
     * - reads all the entries of table MAIL_BOUNCE_PATTERNS_TABLE and populate
     * mailBouncePatternTableList -
     * 
     * 
     * @throws Exception
     */
    private void setMailBouncePatterns() throws Exception {
        this.getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        HashMap record = new HashMap();

        //patternMap 	   = new LinkedHashMap<String, Pattern>();
        patternMap 	   = new LinkedHashMap();

        String patternId 	 = "";
        String patternString = "";
        Pattern pattern = null;

        // process the table entries

        for (int i = 0; i < mailBouncePatternTableList.size(); i++) {
            record = (HashMap) mailBouncePatternTableList.get(i);
            if ((record.get("pattern_id") != null)
                    && (record.get("pattern") != null)) {
                getLogger().debug5(
                        record.get("pattern_id") + "=" + "="
                                + record.get("pattern").toString());
                // compile the pattern string
                patternId = record.get("pattern_id").toString();
                patternString = record.get("pattern").toString();
                pattern = Pattern.compile(patternString, Pattern.MULTILINE
                        | Pattern.CASE_INSENSITIVE);
                this.getLogger().debug6(
                        "..pattern \"" + record.get("pattern").toString()
                                + "\" compiled successfully.");
                patternMap.put(patternId, pattern);

            }// if
        }// for

    }// setMailBouncePatterns

    /**
     * 
     * @param sosMimeMessage
     * @throws Exception
     */
    public void handleBounceMessage(SOSMimeMessage sosMimeMessage)
            throws Exception {

        String bouncedMailAction = "";
        

        int bouncedMailStatus = -1;

        // java 1.5
        //Vector<SOSMailAttachment> sosMailAttachmentList = sosMimeMessage
         //       .getSosMailAttachments();
        
        Vector sosMailAttachmentList = sosMimeMessage.getSosMailAttachments();

        this.getLogger().info("..bounced message found:");

        // get the original message ...
        Iterator it = sosMailAttachmentList.iterator();
        SOSMailAttachment sosMailAttachment = null;
        for (;it.hasNext();) {
        	sosMailAttachment = (SOSMailAttachment)it.next();
        	// check if the attachment = message
            if (sosMailAttachment.getContentType().equalsIgnoreCase(
                    "message/rfc822")) {
                SOSMimeMessage originalMessage = sosMimeMessage
                        .getAttachedSosMimeMessage(receiver.getSession(),
                        		sosMailAttachment.getContent());

                this
                        .setXSOSMailDeliveryCounterHeader(getXSOSMailDeliveryCounterHeader(originalMessage));

                this.getLogger().info("....originally message attributes:");
                this.getLogger().info(
                        "....message id [" + originalMessage.getMessageId()
                                + "]");
                this.getLogger().info(
                        "....from [" + originalMessage.getFromAddress() + "]");
                this
                        .getLogger()
                        .info(
                                "....sent date ["
                                        + originalMessage
                                                .getSentDateAsString("yyyy-MM-dd HH:mm:ss")
                                        + "]");
                
                // .. get the mails.id of the originalMessage.getMessageId if exists
                setMailId(getMailId(originalMessage.getMessageId()));
                
                if(SOSString.isEmpty(getMailId())) // .. skip if no entry available 
                	continue;
                
                // .. then update the "MAILS" table 
                this.updateTableMails(originalMessage.getMessageId());
                
                if (!SOSString.isEmpty(getXSOSMailDeliveryCounterHeader())) {
                    this
                            .getLogger()
                            .info(
                                    "...current "
                                            + JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER
                                            + " ["
                                            + this
                                                    .getXSOSMailDeliveryCounterHeader()
                                            + "]");
                }//if

                bouncedMailAction = this
                        .getPatternAction(sosMimeMessage);

                this.getLogger().info(
                        "..available pattern action for this bounce ["
                                + bouncedMailAction + "]");

                
                // handle action...
                if (bouncedMailAction.equalsIgnoreCase("drop")) { // delete
                                                                        // bounced
                                                                        // message
                    this.getLogger().info(
                            "..Message with ID ["
                                    + sosMimeMessage.getMessageId()
                                    + "] is marked for delete.");
                    sosMimeMessage.deleteMessage();
                    bouncedMailStatus = BounceMailStatus.CANCELLED;
                } else if (bouncedMailAction.equalsIgnoreCase("store")) {
                    String fileName = getLogDirectory() + File.separator
                            + sosMimeMessage.getMessageId();
                    sosMimeMessage.dumpMessageToFile(fileName, true,false);
                    bouncedMailStatus = BounceMailStatus.REQUESTED;                    
                    
                } else if (bouncedMailAction.equalsIgnoreCase("retry")) {
                    // check if retry sending allowed
                    if (isRetrySendingAllowed(originalMessage.getMessageId())) {

                        setRetryInterval(getRetryInterval(originalMessage));
 
                        // incr. the X-Header if exists otherwise add a new one
                        int currentHeaderValue;
                        if ( (currentHeaderValue=originalMessage.incrementHeader(JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER)) == -1) {
                        	currentHeaderValue = 1;
                        	originalMessage.setHeader(JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER,"1");
                        	getLogger().debug5(".. [" + JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER + "] set.");
                        }
                        getLogger().debug5("..current value of \"" + JobSchedulerMailBounceHandler.X_SOSMAIL_DELIVERY_COUNTER_HEADER + "\" [" +currentHeaderValue + "]");
                        
                        getLogger().debug5("..query directory [" + bounceDirectory + " ] set.");
                        originalMessage.setQueueDir(bounceDirectory);
                        originalMessage.dumpMessageToFile(true,false);
                        this.getLogger().debug5("..currently dumped message [" +originalMessage.getDumpedFileName() + "]" );
                        
                        if (!orderRetrySend(originalMessage))
                        	continue;
                    }
                    bouncedMailStatus = BounceMailStatus.DELIVERED;
                } else if (bouncedMailAction.equalsIgnoreCase("forward")) {
                    forwardMessage(sosMimeMessage.getMessage(),receiver.getSession());
                    bouncedMailStatus = BounceMailStatus.DELIVERED;
                }
                updateMailBouncesTable(originalMessage, bouncedMailStatus);
            }// if
        }// for
    } // handleBounceMessage

    /**
     * 
     * @param sosMimeMessage
     * @param bouncedMailStatusCode
     * @throws Exception
     */
    public void updateMailBouncesTable(SOSMimeMessage sosMimeMessage, int bouncedMailStatusCode)
            throws Exception {
    	
        StringBuffer query 				= new StringBuffer();
        String bouncedMailStatusText  	= "";

        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());        
        
        if(bouncedMailStatusCode == BounceMailStatus.REQUESTED) {
        	bouncedMailStatusText = "requested";
        } else if (bouncedMailStatusCode == BounceMailStatus.CANCELLED) {
        	bouncedMailStatusText = "CANCELLED";
        } else if (bouncedMailStatusCode == BounceMailStatus.DELIVERED) {
        	bouncedMailStatusText = "DELIVERED";
        } else if (bouncedMailStatusCode == BounceMailStatus.HAS_ERRORS) {
        	bouncedMailStatusText = "HAS_ERRORS";
        } //if
        
        query = new StringBuffer();
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
        query.append("%now,'");// created
        query.append(this.getJobName());
        query.append("',%now,'");
        query.append(this.getJobName());
        query.append("')");
        this.getLogger().debug5("update MAIL_BOUNCES_TABLE: " + query);
        this.getConnection().execute(query.toString());
        this.getConnection().commit();
    } // updateMailBouncesTable

    /**
     * @return Returns handleBouncedMailOnly.
     */
    public boolean isHandleBouncedMessagesOnly() {
        return handleBouncedMailOnly;
    }// isHandleBouncedMailOnly

    /**
     * @param handleBouncedMailOnly
     *            The handleBouncedMessagesOnly to set.
     */
    public void setHandleBouncedMailOnly(boolean handleBouncedMailOnly) {
        this.handleBouncedMailOnly = handleBouncedMailOnly;
    }// setHandleBouncedMessagesOnly

    /**
     *
     * 
     * @throws Exception
     */
    public void setPatternActions() throws Exception {
        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());

        //this.patternActions = new HashMap<String, String>();
        this.patternActions = new HashMap();
        this.patternActions.put("0", "drop");
        this.patternActions.put("10", "store");
        this.patternActions.put("100", "retry");
        this.patternActions.put("200", "forward");
    }// setPatternActions

    /**
     * returns the retry count value of the message with the ID ="messageId"
     * otherwise -1.
     * 
     * @param messageId
     * @return integer value represents the retry count value with the ID
     *         ="messageId" otherwise 0.
     * @throws Exception
     */
    public int getRetryCounter(String messageId) throws Exception {
        this.getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        HashMap result = new HashMap();
        StringBuffer query = new StringBuffer();
        query.append("SELECT \"RETRY_COUNT\" FROM ");
        query.append(this.getTableMails());
        query.append(" WHERE \"MESSAGE_ID\" ='");
        query.append(messageId);
        query.append("'");
        getLogger().debug5("..query[" + query.toString() + "]");
        result = getConnection().getSingle(query.toString());

        if (result.get("retry_count") != null
                && !SOSString.isEmpty(result.get("retry_count").toString())) {
            try {
                getLogger().info(
                        "..message with ID [" + messageId
                                + "] is already delivered ["
                                + result.get("retry_count").toString() + "] time(s)");
            } catch (Exception e) {
            }
            return Integer.parseInt(result.get("retry_count").toString());
        }
        return -1;
    }// getRetryCount


    /**
     * update data in the MAILS table for the message with the ID "messageId"
     * 
     * @param messageId the message ID
     * @throws Exception
     */
    public void updateTableMails(String messageId) throws Exception {
        StringBuffer query = new StringBuffer();
        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
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
        this.getLogger().debug5("update table \"MAILS\": " + query);
        this.getConnection().execute(query.toString());

    }// updateTableMails
    
    
    /**
     * returns the ID of the specified messageId from 
     * the MAILS table if exists, -1 otherwise
     * 
     * @param messageId
     * @return int represents the ID of the messageId in the "MAILS" table
     * @throws Exception
     */
    private String getMailId(String messageId) throws Exception {
        StringBuffer query = new StringBuffer();

        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        query.append("SELECT \"ID\" FROM ");
        query.append(this.getTableMails());
        query.append(" WHERE \"MESSAGE_ID\"='");
        query.append(messageId);
        query.append("'");
        this.getLogger().debug5(".. query: " + query);
        return this.getConnection().getSingleValue(query.toString());

    }// getMailId
    

    /**
     * 
     * @param messageId
     * @return boolean
     * @throws Exception
     */
    public boolean isRetrySendingAllowed(String messageId) throws Exception {
        int retryCounter = -1;
        HashMap patternEntry = new HashMap();

        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        // get the retry counter from the MAILS table if exists
        retryCounter = this.getRetryCounter(messageId);

        // ..if not, may be it is set as X-SOSMAIL-delivery-counter
        if (retryCounter <= 0) {
            if (!SOSString.isEmpty(this.getXSOSMailDeliveryCounterHeader())) {
                retryCounter = Integer.parseInt(this
                        .getXSOSMailDeliveryCounterHeader());
            }
        }

        getLogger().debug3(".. current retry counter [" + retryCounter + "]");

        if (retryCounter > 0) {
            for (int i = 0; i < this.mailBouncePatternTableList.size(); i++) {
                patternEntry = (HashMap) this.mailBouncePatternTableList.get(i);
                
                if (patternEntry.get("pattern_id") != null
                        && patternEntry.get("pattern_id").toString().equals(
                                patternId)) {
                	
                    if (patternEntry.get("max_retries") != null)
                        return (Integer.parseInt(patternEntry
                                .get("max_retries").toString()) > retryCounter);
                }// if
            }// for
        }// if
        return false;
    }// isRetrySendingAllowed

    /**
     * 
     * @param sosMimeMessage
     * @throws Exception
     */
    public int getRetryInterval(SOSMimeMessage sosMimeMessage) throws Exception {

        StringBuffer query = new StringBuffer();
        String retryItervalString = null;

        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());

        int atIndex = sosMimeMessage.getFromAddress().indexOf('@');
        String domain = sosMimeMessage.getFromAddress().substring(atIndex + 1,
                sosMimeMessage.getFromAddress().length());
        retryInterval = -1;

        query.append("SELECT \"RETRY_INTERVAL\" FROM ");
        query.append(this.getTableMailBounceDeliveries());
        query.append(" WHERE \"DOMAIN\"='");
        query.append(domain);
        query.append("'");
        this.getLogger().debug5("..query [" + query + "]");

        retryItervalString = getConnection().getSingleValue(query.toString());

        // check if we got a valid retryIterval..
        if (!SOSString.isEmpty(retryItervalString)) {
            retryInterval = Integer.parseInt(retryItervalString);
        }

        // .. since retryIterval is not valid, we try to get it from
        // MAIL_BOUNCE_PATTERN_RETRIES
        if (retryInterval <= 0) {
            query = new StringBuffer();
            query.append("SELECT \"RETRY_INTERVAL\" FROM ");
            query.append(this.getTableMailBouncePatternRetries());
            query.append(" WHERE \"PATTERN_ID\"=");
            query.append(patternId);

            this.getLogger().debug5("..query [" + query + "]");

            retryItervalString = getConnection().getSingleValue(
                    query.toString());

            // check if we got a valid retryIterval
            if (!SOSString.isEmpty(retryItervalString)) {
                retryInterval = Integer.parseInt(retryItervalString);
            }
        }
        this.getLogger().debug5("..current retry interval [" + retryInterval + "]");
        return retryInterval;
    }// getRetryInterval

    /**
     * @return Returns the bounceDirectory.
     */
    public String getBounceDirectory() {
        return bounceDirectory;
    }

    /**
     * @param bounceDirectory
     *            The bounceDirectory to set.
     */
    public void setBounceDirectory(String bounceDirectory) {
        this.bounceDirectory = bounceDirectory;
    }

    /**
     * @return Returns the retryInterval.
     */
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * @param retryInterval
     *            The retryInterval to set.
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * @return Returns the xSOSMailDeliveryCounterHeader.
     */
    public String getXSOSMailDeliveryCounterHeader() {
        return xSOSMailDeliveryCounterHeader;
    }

    /**
     * returns the value of the X_SOSMAIL_DELIVERY_COUNTER_HEADER header if exists otherwise null.
     * 
     * @param sosMimeMessage
     * @return String represents the value of the X_SOSMAIL_DELIVERY_COUNTER_HEADER
     * @throws Exception
     */
    private String getXSOSMailDeliveryCounterHeader(
            SOSMimeMessage sosMimeMessage) throws Exception {
        return sosMimeMessage.getHeaderValue(X_SOSMAIL_DELIVERY_COUNTER_HEADER);
    }// getXSOSMailDeliveryCounterHeader

    /**
     * @param mailDeliveryCounterHeader
     *            The xSOSMailDeliveryCounterHeader to set.
     */
    private void setXSOSMailDeliveryCounterHeader(
            String mailDeliveryCounterHeader) {
        xSOSMailDeliveryCounterHeader = mailDeliveryCounterHeader;
    }

    /**
     * 
     * @param sosMimeMessage
     * @return true if success otherwise false
     * @throws Exception
     */
    public boolean orderRetrySend(SOSMimeMessage sosMimeMessage) throws Exception {

        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        
        // create parameters
        Variable_set orderData = spooler.create_variable_set();
        orderData.set_var("file", new File(sosMimeMessage.getDumpedFileName()).getName());
        
        if(!SOSString.isEmpty(getMailId())) {
        	orderData.set_var("id", getMailId());
        }

        Order order = spooler.create_order();
        
        //order.set_id(sosMimeMessage.getMessageId());
        
        order.set_title(this.getJobTitle() + "." + jobChainName);
        
        order.set_payload(orderData);

        if ( !spooler.job_chain_exists(jobChainName)) {
            getLogger().warn("could not find job chain: " + jobChainName);
        	return false;
        }
        
        spooler.job_chain(jobChainName).add_or_replace_order(order);

        // setback order
        spooler_task.job().set_delay_order_after_setback(1,
                this.getRetryInterval());

        getLogger().debug3(".. order " + order.title() + " added.");

        return true;
        
    }//retrySend

    /**
     * @return Returns the jobChainName.
     */
    public String getJobChainName() {
        return jobChainName;
    }

    /**
     * @param jobChainName The jobChainName to be set.
     */
    public void setJobChainName(String jobChainName) {
        this.jobChainName = jobChainName;
    }
    

    /**
     *  
     * @throws Exception
     */
    private void createJobChain() throws Exception {
        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
        if (spooler.job_chain_exists(jobChainName))
            return;
        Job_chain jobChain = spooler.create_job_chain();
        getLogger().debug5(".. job chain [" + jobChainName + "] created.");
        jobChain.set_name(this.getJobName() + "." + jobChainName);
        jobChain.add_job(jobChainName, "0", "100", "1100");
        getLogger().debug3(".. job [" + jobChainName + "] added.");
        jobChain.add_end_state("100"); // on success
        jobChain.add_end_state("1100");// on error
        spooler.add_job_chain(jobChain);
        getLogger().debug3("Calling " + SOSClassUtil.getMethodName());
    }//createJobChain
    
    
    /**
     * forwards the spicified message
     * 
     * @param message
     * @param session
     * @throws Exception
     */
    public void forwardMessage(MimeMessage message,Session session) throws Exception {
        
        getLogger().debug3("Calling "+SOSClassUtil.getMethodName());
        
        String[] toArray = {};

        String[] ccArray = {};

        String[] bccArray = {};

        String mailFrom = "";
        
        HashMap patternEntry = new HashMap();
        
        // set the mail properties
        for (int i = 0; i < this.mailBouncePatternTableList.size(); i++) {
            patternEntry = (HashMap) this.mailBouncePatternTableList.get(i);
            if (patternEntry.get("pattern_id") != null && patternEntry.get("pattern_id").toString().equals(patternId)) {
                
                // get mail_to
                if (patternEntry.get("mail_to") == null)
                    throw new Exception("[mail_to] missing!!");
                toArray = patternEntry.get("mail_to").toString().trim().split("[;,]");
                this.getLogger().debug6("..mail to  [" + patternEntry.get("mail_to").toString() + "]");
                
                // get mail_cc (optional)
                if (patternEntry.get("mail_cc") != null) {
                ccArray = patternEntry.get("mail_cc").toString().trim().split("[;,]");
                this.getLogger().debug6("..mail cc [" + patternEntry.get("mail_cc").toString() + "]");
                }
                
                // get mail_bcc (optionaö)
                if (patternEntry.get("mail_bcc") != null) {
               ccArray = patternEntry.get("mail_bcc").toString().trim().split("[;,]");
                this.getLogger().debug6("..mail bcc [" + patternEntry.get("mail_bcc").toString() + "]");
                }
                
                // get mail_from
                if (patternEntry.get("reply_to") == null)
                    throw new Exception("[reply_to] missing!!");
                mailFrom = patternEntry.get("reply_to").toString();
                this.getLogger().debug6("..reply_to [" + patternEntry.get("reply_to").toString() + "]");

                break;
            }// if
        }// for
        
        // Setup the smtp mail server
        session.getProperties().put("mail.smtp.host", smtpHost);
        
        session.getProperties().put("mail.smtp.port", smtpPort);

        MimeMessage forward = new MimeMessage(session);
        
        forward.setSubject("Fw. "+message.getSubject());
        
        forward.setFrom(new InternetAddress(mailFrom));
        
        for(int i=0;i<toArray.length;i++) {
          forward.addRecipient(Message.RecipientType.TO, new InternetAddress(toArray[i]));
        }
        
        for(int i=0;i<ccArray.length;i++) {
            forward.addRecipient(Message.RecipientType.CC, new InternetAddress(ccArray[i]));
        }

        for(int i=0;i<bccArray.length;i++) {
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
        
       }//forwardMessage

    /**
     * returns the id of the message that could be find in the "MAILS" table.
     * 
     * @return String represents the message id from the "MAILS" table
     */
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
