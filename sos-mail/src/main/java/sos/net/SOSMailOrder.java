package sos.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSSettings;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.textprocessor.SOSDocumentFactoryTextProcessor;
import sos.util.SOSLogger;
import sos.textprocessor.SOSPlainTextProcessor;
import sos.util.SOSStandardLogger;
import sos.textprocessor.SOSTextProcessor;

public class SOSMailOrder extends SOSMail {

    protected SOSConnection sosConnection;
    protected boolean hasLocalizedTemplates = true;
    protected SOSPlainTextProcessor mailPlainTextProcessor = null;
    protected SOSDocumentFactoryTextProcessor mailDocumentFactoryTextProcessor = null;
    private int mailingId = 0;
    private int jobId;
    private String messageId;
    private String topic = null;
    private String topicIdentifier = null;
    private String clientIdentifier = null;
    private String reference = null;
    private String subjectTemplate = null;
    private int subjectTemplateType;
    private String bodyTemplate = null;
    private int bodyTemplateType;
    private int id = -1;
    private String modifiedBy = "SOSMailOrder";
    private HashMap replacements = new HashMap();
    private int status = 0;
    private String statusText = null;
    private Date targeted = null;
    private Date delivered = null;
    public final static int TEMPLATE_TYPE_PLAIN = 0;
    public final static int TEMPLATE_TYPE_FACTORY = 1;
    public final static int TEMPLATE_TYPE_PLAIN_FILE = 100;
    public final static int TEMPLATE_TYPE_FACTORY_FILE = 101;
    public final static int EMAIL_STATUS_REQUESTED = 0;
    public final static int EMAIL_STATUS_DELIVERED = 1;
    public final static int EMAIL_STATUS_WITH_ERRORS = 1001;

    public SOSMailOrder(SOSSettings sosSettings, SOSConnection conn) throws Exception {
        super(sosSettings);
        sosConnection = conn;
    }

    public SOSMailOrder(SOSSettings sosSettings, String language, SOSConnection conn) throws Exception {
        super(sosSettings, language);
        sosConnection = conn;
    }

    public SOSMailOrder(String host, SOSConnection conn) throws Exception {
        super(host);
        sosConnection = conn;
    }

    public SOSMailOrder(String host, String user, String password, SOSConnection conn) throws Exception {
        super(host, user, password);
        sosConnection = conn;
    }

    public SOSMailOrder(String host, String port, String user, String password, SOSConnection conn) throws Exception {
        super(host, port, user, password);
        sosConnection = conn;
    }

    public void initProcessors() throws Exception {
        try {
            if (sosSettings != null && sosSettings instanceof SOSConnectionSettings) {
                this.mailPlainTextProcessor = new SOSPlainTextProcessor((SOSConnectionSettings) sosSettings);
                this.mailPlainTextProcessor.setHasLocalizedTemplates(this.hasLocalizedTemplates());
                this.mailPlainTextProcessor.getTemplates(this.getSectionMailTemplates(), this.getApplicationMailTemplates());
                this.mailPlainTextProcessor.getScripts(this.getSectionMailScripts(), this.getApplicationMailScripts());
                this.mailDocumentFactoryTextProcessor = new SOSDocumentFactoryTextProcessor((SOSConnectionSettings) sosSettings);
                this.mailDocumentFactoryTextProcessor.setHasLocalizedTemplates(this.hasLocalizedTemplates());
                this.mailDocumentFactoryTextProcessor.getTemplates(this.getSectionMailTemplatesFactory(), this.getApplicationMailTemplatesFactory());
                this.mailDocumentFactoryTextProcessor.getScripts(this.getSectionMailScripts(), this.getApplicationMailScripts());
            }
        } catch (Exception e) {
            throw new Exception("failed to initialize processors: " + e);
        }
    }

    public void load(int id) throws Exception {
        HashMap data = new HashMap();
        try {
            data =
                    sosConnection.getSingle("SELECT " + "\"MAILING_ID\", \"JOB_ID\", \"MESSAGE_ID\", \"TOPIC\", "
                            + "\"TOPIC_IDENTIFIER\", \"CLIENT_IDENTIFIER\", \"REFERENCE\", \"MAIL_FROM\", "
                            + "\"FROM_NAME\", \"MAIL_TO\", \"CC_TO\", \"BCC_TO\", \"REPLY_TO\", "
                            + "\"PRIORITY\", \"SUBJECT\", \"SUBJECT_TEMPLATE\", \"SUBJECT_TEMPLATE_TYPE\", "
                            + "\"BODY_TEMPLATE\", \"BODY_TEMPLATE_TYPE\", \"REPLACEMENTS\", \"LANGUAGE\", "
                            + "\"CHARSET\", \"ENCODING\", \"CONTENT_TYPE\", \"STATUS\", \"STATUS_TEXT\", " + "\"TARGETED\", \"DELIVERED\" " + "FROM "
                            + SOSMail.tableMails + " " + "WHERE \"ID\"=" + id);
            if (data.isEmpty()) {
                throw new Exception("Mail not found.");
            }
            setMailingId(Integer.parseInt(data.get("mailing_id").toString()));
            setJobId(Integer.parseInt(data.get("job_id").toString()));
            if (data.get("message_id") != null) {
                setMessageId(data.get("message_id").toString());
            }
            if (data.get("topic") != null) {
                setTopic(data.get("topic").toString());
            }
            if (data.get("topic_identifier") != null) {
                setTopicIdentifier(data.get("topic_identifier").toString());
            }
            if (data.get("client_identifier") != null) {
                setClientIdentifier(data.get("client_identifier").toString());
            }
            if (data.get("reference") != null) {
                setReference(data.get("reference").toString());
            }
            if (data.get("mail_from") != null) {
                setFrom(data.get("mail_from").toString());
            }
            if (data.get("from_name") != null) {
                setFromName(data.get("from_name").toString());
            }
            if (data.get("mail_to") != null) {
                addRecipient(data.get("mail_to").toString());
            }
            if (data.get("cc_to") != null) {
                addCC(data.get("cc_to").toString());
            }
            if (data.get("bcc_to") != null) {
                addBCC(data.get("bcc_to").toString());
            }
            if (data.get("reply_to") != null) {
                setReplyTo(data.get("reply_to").toString());
            }
            int priority = Integer.parseInt(data.get("priority").toString());
            switch (priority) {
            case PRIORITY_HIGHEST:
                this.setPriorityHighest();
                break;
            case PRIORITY_HIGH:
                this.setPriorityHigh();
                break;
            case PRIORITY_LOW:
                this.setPriorityLow();
                break;
            case PRIORITY_LOWEST:
                this.setPriorityLowest();
                break;
            default:
                this.setPriorityNormal();
                break;
            }
            if (data.get("subject") != null) {
                setSubject(data.get("subject").toString());
            }
            if (data.get("subject_template") != null) {
                setSubjectTemplate(data.get("subject_template").toString());
            }
            if (data.get("subject_template_type") != null) {
                int tt = Integer.parseInt(data.get("subject_template_type").toString());
                setSubjectTemplateType(tt);
            }
            if (data.get("body_template") != null) {
                setBodyTemplate(data.get("body_template").toString());
            }
            if (data.get("body_template_type") != null) {
                int tt = Integer.parseInt(data.get("body_template_type").toString());
                setBodyTemplateType(tt);
            }
            clearReplacements();
            if (data.get("replacements") != null && !data.get("replacements").toString().isEmpty()) {
                String[] replacementList = data.get("replacements").toString().split("\\|");
                for (int i = 0; i < replacementList.length; i++) {
                    String[] replacementEntry = replacementList[i].split("\\^");
                    if (replacementEntry.length == 2) {
                        addReplacement(replacementEntry[0], replacementEntry[1]);
                    }
                }
            }
            if (data.get("language") != null && !data.get("language").toString().isEmpty()) {
                setLanguage(data.get("language").toString());
            }
            if (data.get("charset") != null && !data.get("charset").toString().isEmpty()) {
                setCharset(data.get("charset").toString());
            }
            if (data.get("encoding") != null && !data.get("encoding").toString().isEmpty()) {
                setEncoding(data.get("encoding").toString());
            }
            if (data.get("content_type") != null && !data.get("content_type").toString().isEmpty()) {
                setContentType(data.get("content_type").toString());
            }
            if (data.get("status") != null) {
                int st = Integer.parseInt(data.get("status").toString());
                setStatus(st);
            }
            if (data.get("status_text") != null) {
                setStatusText(data.get("status_text").toString());
            }
            if (data.get("targeted") != null && !data.get("targeted").toString().isEmpty()) {
                setTargeted(SOSDate.getTime(data.get("targeted").toString()));
            }
            if (data.get("delivered") != null && !data.get("delivered").toString().isEmpty()) {
                setDelivered(SOSDate.getTime(data.get("delivered").toString()));
            }
            String body = sosConnection.getClob("SELECT \"BODY\" FROM " + SOSMail.tableMails + " WHERE \"ID\"=" + id);
            setBody(body);
            this.id = id;
            loadAttachments();
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": could not load mail [" + id + "]: " + e);
        }
    }

    private void loadAttachments() throws Exception {
        ArrayList data = new ArrayList();
        try {
            data =
                    sosConnection.getArray("SELECT " + "\"FILENAME\", \"CHARSET\",  \"ENCODING\", \"CONTENT_TYPE\" " + "FROM "
                            + SOSMail.tableMailAttachments + " " + "WHERE \"ID\"=" + this.id);
            if (data != null) {
                log("Found " + data.size() + " attachments.", SOSLogger.DEBUG3);
                Iterator iter = data.iterator();
                while (iter.hasNext()) {
                    HashMap att = (HashMap) iter.next();
                    String filename = att.get("filename").toString();
                    File file = new File(filename);
                    SOSMailAttachment attachment = new SOSMailAttachment(this, file);
                    if (att.get("charset") != null && !att.get("charset").toString().isEmpty()) {
                        attachment.setCharset(att.get("charset").toString());
                    }
                    if (att.get("encoding") != null && !att.get("encoding").toString().isEmpty()) {
                        attachment.setEncoding(att.get("encoding").toString());
                    }
                    if (att.get("content_type") != null && !att.get("content_type").toString().isEmpty()) {
                        attachment.setContentType(att.get("content_type").toString());
                    }
                    addAttachment(attachment);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error occured loading attachments: " + e, e);
        }
    }

    public void store() throws Exception {
        if (this.id == -1) {
            create();
        }
        String statement = "UPDATE " + SOSMail.tableMails + " SET ";
        statement += updateField("MAILING_ID", getMailingId());
        statement += updateField("JOB_ID", getJobId());
        statement += updateField("MESSAGE_ID", getMessageId());
        statement += updateField("TOPIC", getTopic());
        statement += updateField("TOPIC_IDENTIFIER", getTopicIdentifier());
        statement += updateField("CLIENT_IDENTIFIER", getClientIdentifier());
        statement += updateField("REFERENCE", getReference());
        statement += updateField("MAIL_FROM", getFrom());
        statement += updateField("FROM_NAME", getFromName());
        statement += updateField("MAIL_TO", getRecipientsAsString());
        statement += updateField("CC_TO", getCCsAsString());
        statement += updateField("BCC_TO", getBCCsAsString());
        statement += updateField("REPLY_TO", getReplyTo());
        String prio = getMessage().getHeader("X-Priority", null);
        if (prio != null && !prio.isEmpty()) {
            try {
                int iPrio = Integer.parseInt(prio.substring(0, 1));
                statement += updateField("PRIORITY", iPrio);
            } catch (Exception e) {
                //
            }
        }
        statement += updateField("SUBJECT", getSubject());
        statement += updateField("SUBJECT_TEMPLATE", getSubjectTemplate());
        statement += updateField("SUBJECT_TEMPLATE_TYPE", getSubjectTemplateType());
        statement += updateField("BODY_TEMPLATE", getBodyTemplate());
        statement += updateField("BODY_TEMPLATE_TYPE", getBodyTemplateType());
        statement += updateField("REPLACEMENTS", getReplacementsAsString());
        statement += updateField("LANGUAGE", getLanguage());
        statement += updateField("CHARSET", getCharset());
        statement += updateField("ENCODING", getEncoding());
        statement += updateField("CONTENT_TYPE", getContentType());
        statement += updateField("STATUS", getStatus());
        statement += updateField("STATUS_TEXT", getStatusText(), 250);
        statement += updateField("TARGETED", getTargeted());
        statement += updateField("DELIVERED", getDelivered());
        statement += updateField("MODIFIED_BY", getModifiedBy());
        statement += "\"MODIFIED\"=%now WHERE \"ID\"=" + this.id;
        try {
            sosConnection.execute(statement);
            if (getBody() != null && !getBody().isEmpty()) {
                sosConnection.updateClob(SOSMail.tableMails, "BODY", getBody(), "\"ID\"=" + this.id);
            }
            storeAttachments();
            sosConnection.commit();
        } catch (Exception e) {
            try {
                sosConnection.rollback();
            } catch (Exception ex) {
                // do not handle errors while error handling
            }
            throw new Exception("Error occured storing mail: " + e.getMessage(), e);
        }
    }

    private static final String updateField(String fieldname, int value) {
        return ("\"" + fieldname + "\"=" + value + ", ");
    }

    private static final String updateField(String fieldname, Date value) throws Exception {
        if (value == null) {
            return ("\"" + fieldname + "\"=NULL, ");
        }
        try {
            return ("\"" + fieldname + "\"=%timestamp_iso('" + SOSDate.getTimeAsString(value) + "'), ");
        } catch (Exception e) {
            throw new Exception("Error occured creating date: " + e);
        }
    }

    private static final String updateField(String fieldname, String value) {
        if (value == null) {
            return "";
        }
        value = value.replaceAll("'", "''");
        return ("\"" + fieldname + "\"='" + value + "', ");
    }

    private static final String updateField(String fieldname, String value, int limit) {
        if (value == null) {
            return "";
        }
        value = (value.length() > limit ? value.substring(value.length() - limit) : value);
        return updateField(fieldname, value);
    }

    private void storeAttachments() throws Exception {
        sosConnection.execute("DELETE FROM " + SOSMail.tableMailAttachments + " WHERE \"ID\"=" + this.id);
        Iterator iter = attachmentList.values().iterator();
        try {
            while (iter.hasNext()) {
                SOSMailAttachment attachment = (SOSMailAttachment) iter.next();
                String statement =
                        "INSERT INTO " + tableMailAttachments + " (\"ID\", \"FILENAME\", \"CHARSET\", \"ENCODING\","
                                + " \"CONTENT_TYPE\", \"CREATED\", \"CREATED_BY\", " + " \"MODIFIED\", \"MODIFIED_BY\") VALUES " + " (" + this.id
                                + ", '" + attachment.getFile().getAbsolutePath() + "', " + "'" + attachment.getCharset() + "', " + "'"
                                + attachment.getEncoding() + "', " + "'" + attachment.getContentType() + "', " + "%now, '" + getClass().getName()
                                + "', %now, '" + getClass().getName() + "')";
                sosConnection.execute(statement);
            }
        } catch (Exception e) {
            throw new Exception("Error occured storing attachments: " + e, e);
        }
    }

    protected void create() throws Exception {
        String statement =
                "INSERT INTO " + SOSMail.tableMails + " " + " (\"CREATED\", \"CREATED_BY\", \"MODIFIED\", \"MODIFIED_BY\")" + " VALUES (%now, '"
                        + getClass().getName() + "', %now, '" + getClass().getName() + "')";
        try {
            sosConnection.execute(statement);
            String val = sosConnection.getLastSequenceValue(SOSMail.mailsSequence);
            int iVal = Integer.parseInt(val);
            this.id = iVal;
        } catch (Exception e) {
            throw new Exception("Error occured creating Mail order: " + e, e);
        }
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopicIdentifier() {
        return topicIdentifier;
    }

    public void setTopicIdentifier(String topicIdentifier) {
        this.topicIdentifier = topicIdentifier;
    }

    public String getMessageId() {
        return messageId;
    }

    protected void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean send() throws Exception {
        String message = "";
        boolean rc = false;
        try {
            rc = super.send();
            setStatus(EMAIL_STATUS_DELIVERED);
            setMessageId(getMessage().getMessageID());
            setDelivered(SOSDate.getCurrentTime());
            if (!rc) {
                message = "mail was NOT sent but stored for later dequeueing, reason was: " + getLastError();
            }
            setStatusText(message);
        } catch (Exception e) {
            setStatus(EMAIL_STATUS_WITH_ERRORS);
            if (e.getMessage() != null) {
                setStatusText(e.getMessage());
            } else {
                setStatusText(e.toString());
            }
            setDelivered(null);
            store();
            throw e;
        }
        store();
        return rc;
    }

    protected boolean prepareJavaMail() throws Exception {
        if (getSubject() == null || getSubject().trim().isEmpty()) {
            processSubject();
        }
        if (getBody() == null || getBody().trim().isEmpty()) {
            processBody();
        }
        return super.prepareJavaMail();
    }

    public void setSectionMailScripts(String sectionMailScripts) {
        mailDocumentFactoryTextProcessor = null;
        mailPlainTextProcessor = null;
        super.setSectionMailScripts(sectionMailScripts);
    }

    public void setSectionMailTemplates(String sectionMailTemplates) {
        mailDocumentFactoryTextProcessor = null;
        mailPlainTextProcessor = null;
        super.setSectionMailTemplates(sectionMailTemplates);
    }

    public void setSectionMailTemplatesFactory(String sectionMailTemplatesFactory) {
        mailDocumentFactoryTextProcessor = null;
        mailPlainTextProcessor = null;
        super.setSectionMailTemplatesFactory(sectionMailTemplatesFactory);
    }

    private void processSubject() throws Exception {
        if (getSubjectTemplate() != null && !getSubjectTemplate().isEmpty()) {
            if (mailDocumentFactoryTextProcessor == null) {
                initProcessors();
            }
            SOSTextProcessor processor = null;
            if (getSubjectTemplateType() == TEMPLATE_TYPE_FACTORY || getSubjectTemplateType() == TEMPLATE_TYPE_FACTORY_FILE) {
                processor = mailDocumentFactoryTextProcessor;
            } else {
                processor = mailPlainTextProcessor;
            }
            if (getSubjectTemplateType() == TEMPLATE_TYPE_FACTORY_FILE || getSubjectTemplateType() == TEMPLATE_TYPE_PLAIN_FILE) {
                File subjectTemplateFile = new File(getSubjectTemplate());
                processor.setLanguage(getLanguage());
                setSubject(processor.process(readFile(subjectTemplateFile), replacements));
            } else {
                if (!getLanguage().equals(processor.getLanguage())) {
                    processor.setLanguage(getLanguage());
                    processor.setForceReload(true);
                    processor.getTemplates();
                }
                setSubject(processor.process(processor.getTemplate(getSubjectTemplate()), replacements));
            }
        }
    }

    private void processBody() throws Exception {
        if (getBodyTemplate() != null && !getBodyTemplate().trim().isEmpty()) {
            if (mailDocumentFactoryTextProcessor == null) {
                initProcessors();
            }
            SOSTextProcessor processor = null;
            if (getBodyTemplateType() == TEMPLATE_TYPE_FACTORY || getBodyTemplateType() == TEMPLATE_TYPE_FACTORY_FILE) {
                processor = mailDocumentFactoryTextProcessor;
            } else {
                processor = mailPlainTextProcessor;
            }
            if (getBodyTemplateType() == TEMPLATE_TYPE_FACTORY_FILE || getBodyTemplateType() == TEMPLATE_TYPE_PLAIN_FILE) {
                File bodyTemplateFile = new File(getBodyTemplate());
                processor.setLanguage(getLanguage());
                setBody(processor.process(readFile(bodyTemplateFile), replacements));
            } else {
                if (!getLanguage().equals(processor.getLanguage())) {
                    processor.setLanguage(getLanguage());
                    processor.setForceReload(true);
                    processor.getTemplates();
                }
                setBody(processor.process(processor.getTemplate(getBodyTemplate()), replacements));
            }
        }
    }

    private String readFile(File file) throws Exception {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        StringBuilder content = new StringBuilder();
        byte buffer[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            content.append(new String(buffer, 0, bytesRead));
        }
        in.close();
        return content.toString();
    }

    protected String getClientIdentifier() {
        return clientIdentifier;
    }

    protected void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    protected String getReference() {
        return reference;
    }

    protected void setReference(String reference) {
        this.reference = reference;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public int getSubjectTemplateType() {
        return subjectTemplateType;
    }

    public void setSubjectTemplateType(int subjectTemplateType) {
        this.subjectTemplateType = subjectTemplateType;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public int getBodyTemplateType() {
        return bodyTemplateType;
    }

    public void setBodyTemplateType(int bodyTemplateType) {
        this.bodyTemplateType = bodyTemplateType;
    }

    public void addReplacement(String key, String value) {
        replacements.put(key, value);
    }

    public String getReplacement(String key) {
        Object value = replacements.get(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    protected String getReplacementsAsString() {
        String rc = "";
        Iterator keys = replacements.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            rc += key + "^" + replacements.get(key).toString();
            if (keys.hasNext()) {
                rc += "|";
            }
        }
        return rc;
    }

    public void clearReplacements() {
        replacements.clear();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Date getDelivered() {
        return delivered;
    }

    public void setDelivered(Date delivered) {
        this.delivered = delivered;
    }

    public Date getTargeted() {
        return targeted;
    }

    public void setTargeted(Date targeted) {
        this.targeted = targeted;
    }

    public int getId() {
        return id;
    }

    public void initOrder() throws Exception {
        super.init();
        delivered = null;
        id = -1;
        mailingId = 0;
        messageId = "";
        reference = null;
        clearReplacements();
        status = EMAIL_STATUS_REQUESTED;
        statusText = "";
        targeted = null;
        topic = null;
        topicIdentifier = null;
    }

    public boolean hasLocalizedTemplates() {
        return hasLocalizedTemplates;
    }

    public void setHasLocalizedTemplates(boolean hasLocalizedTemplates) {
        this.hasLocalizedTemplates = hasLocalizedTemplates;
    }

    public static void main(String[] args) throws Exception {
        String mailto = "mo@sos-berlin.com";
        SOSLogger logger = new SOSStandardLogger(9);
        SOSConnection conn =
                SOSConnection.createInstance("SOSMSSQLConnection", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                        "jdbc:sqlserver://8of9:2433;sendStringParametersAsUnicode=false;selectMethod=cursor;databaseName=ehp_bkk", "ehp_bkk",
                        "ehp_bkk", logger);
        conn.connect();
        SOSSettings settings = new SOSConnectionSettings(conn, "SETTINGS", logger);
        SOSMailOrder order = new SOSMailOrder(settings, conn);
        order.setSOSLogger(logger);
        order.addRecipient(mailto);
        order.setLanguage("en");
        order.setSubjectTemplate("default_subject");
        order.setSubjectTemplateType(TEMPLATE_TYPE_PLAIN);
        order.setBodyTemplate("default_body");
        order.setBodyTemplateType(TEMPLATE_TYPE_PLAIN);
        order.send();
        conn.disconnect();
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

}