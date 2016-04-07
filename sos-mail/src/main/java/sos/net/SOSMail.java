package sos.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.FieldPosition; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import sos.settings.SOSSettings;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Exceptions.JSNotImplementedException;
import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;

public class SOSMail {

    protected String host = "";
    protected String port = "25";
    protected String user = "";
    protected String password = "";
    protected int timeout = 30000;
    protected String subject = "";
    protected String from = "";
    protected String fromName = "";
    protected String replyTo = "";
    protected String queueDir = "";
    protected String body = "";
    protected String alternativeBody = "";
    protected String language = "de";
    protected String dateFormat = "dd.MM.yyyy";
    protected String datetimeFormat = "dd.MM.yyyy HH:mm";
    protected HashMap dateFormats = new HashMap();
    protected HashMap datetimeFormats = new HashMap();
    protected String attachmentCharset = "iso-8859-1";
    protected String charset = "iso-8859-1";
    protected String alternativeCharset = "iso-8859-1";
    protected String contentType = "text/plain";
    protected String alternativeContentType = "text/html";
    protected String encoding = "7bit";
    protected String alternativeEncoding = "7bit";
    protected String attachmentEncoding = "Base64";
    protected String attachmentContentType = "application/octet-stream";
    protected LinkedList toList = new LinkedList();
    protected LinkedList ccList = new LinkedList();
    protected LinkedList bccList = new LinkedList();
    protected TreeMap attachmentList = new TreeMap();
    protected Properties templates = new Properties();
    protected SOSSettings sosSettings = null;
    protected String tableSettings = "SETTINGS";
    protected String applicationMail = "email";
    protected String sectionMail = "mail_server";
    protected String applicationMailTemplates = "email_templates";
    protected String sectionMailTemplates = "mail_templates";
    protected String applicationMailScripts = "email";
    protected String sectionMailScripts = "mail_start_scripts_factory";
    protected String applicationMailTemplatesFactory = "email_templates_factory";
    protected String sectionMailTemplatesFactory = "mail_templates";
    protected SOSLogger sosLogger = null;
    private static final Logger LOGGER = Logger.getLogger(SOSMail.class);
    private boolean sendToOutputStream = false;
    private byte[] messageBytes;
    private MimeMessage message = null;
    private SOSMailAuthenticator authenticator = null;
    private final ArrayList file_input_streams = new ArrayList();
    private ByteArrayOutputStream raw_email_byte_stream = null;
    private String lastError = "";
    private boolean changed = false;
    private final String queuePattern = "yyyy-MM-dd.HHmmss.S";
    private String queuePraefix = "sos.";
    private String lastGeneratedFileName = "";
    private String loadedMessageId = "";
    private boolean messageReady = false;
    private int priority = -1;
    
    private String securityProtocol="";
    private Session session = null;
    public static String tableMails = "MAILS";
    public static String tableMailAttachments = "MAIL_ATTACHMENTS";
    public static String mailsSequence = "MAILS_ID_SEQ";
    public static final int PRIORITY_HIGHEST = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_NORMAL = 3;
    public static final int PRIORITY_LOW = 4;
    public static final int PRIORITY_LOWEST = 5;

    abstract class My_data_source implements DataSource {

        final String name;
        final String content_type;

        public My_data_source(final File new_filename, final String content_type) {
            name = new_filename.getName();
            this.content_type = content_type;
        }

        @Override
        public String getContentType() {
            return content_type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public OutputStream getOutputStream() {
            throw new RuntimeException(getClass().getName() + " hat keinen OutputStream");
        }
    }

    class File_data_source extends My_data_source {

        final File file;

        public File_data_source(final File file, final String content_type) {
            super(file, content_type);
            this.file = file;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            FileInputStream f = new FileInputStream(file);
            file_input_streams.add(f);
            return f;
        }
    }

    public SOSMail(final String host) throws Exception {
        if (host != null) {
            this.host = host;
        }
        this.init();
    }

    public SOSMail(final String host_, final String user_, final String password_) throws Exception {
        if (host_ != null) {
            host = host_;
        }
        if (user_ != null) {
            user = user_;
        }
        if (password_ != null) {
            password = password_;
        }
        this.init();
    }

    public SOSMail(final String host_, final String port_, final String user_, final String password_) throws Exception {
        if (host_ != null) {
            host = host_;
        }
        if (port_ != null) {
            port = port_;
        }
        if (user_ != null) {
            user = user_;
        }
        if (password_ != null) {
            password = password_;
        }
        this.init();
    }

    public SOSMail(final SOSSettings sosSettings) throws Exception {
        this.getSettings(sosSettings);
        this.init();
    }

    public SOSMail(final SOSSettings sosSettings, final String language) throws Exception {
        this.getSettings(sosSettings, language);
        this.init();
    }

    private void initPriority() throws MessagingException {
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
            break;
        }
    }

    public void init() throws Exception {
        dateFormats.put("de", "dd.MM.yyyy");
        dateFormats.put("en", "MM/dd/yyyy");
        datetimeFormats.put("de", "dd.MM.yyyy HH:mm");
        datetimeFormats.put("en", "MM/dd/yyyy HH:mm");
        this.initLanguage();
        this.initMessage();
        clearRecipients();
        clearAttachments();
    }

    public void initMessage() throws Exception {
        createMessage(createSession());
        initPriority();
    }

    public Session createSession() throws Exception {
        Properties props = System.getProperties();
        props.put("mail.host", host);
        props.put("mail.port", port);
        props.put("mail.smtp.timeout", String.valueOf(timeout));
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.class", "com.sun.mail.SMTPTransport");
        if (!user.isEmpty()) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }
        
        if (securityProtocol.equalsIgnoreCase("ssl")){
            props.put("mail.smtp.ssl.enable", "true");
            if (port.equals("25")){
                props.put("mail.smtp.port", "465"); 
            }
        }
        
        authenticator = new SOSMailAuthenticator(user, password);
        session = Session.getInstance(props, authenticator);
        return session;
    }

    
    public String getSecurityProtocol() {
        return securityProtocol;
    }

    
    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public void createMessage(final Session session) throws Exception {
        message = new MimeMessage(session);
    }

    public void initLanguage() throws Exception {
        if (dateFormats.containsKey(this.getLanguage()) && datetimeFormats.containsKey(this.getLanguage())) {
            this.setDateFormat(dateFormats.get(this.getLanguage()).toString());
            this.setDatetimeFormat(datetimeFormats.get(this.getLanguage()).toString());
        } else {
            this.setDateFormat(dateFormats.get("de").toString());
            this.setDatetimeFormat(datetimeFormats.get("de").toString());
        }
    }

    public void log(String s, final int level) throws Exception {
        s = "SOSMail." + s;
        if (sosLogger != null) {
            switch (level) {
            case SOSLogger.DEBUG1:
                sosLogger.debug1(s);
                break;
            case SOSLogger.DEBUG2:
                sosLogger.debug2(s);
                break;
            case SOSLogger.DEBUG3:
                sosLogger.debug3(s);
                break;
            case SOSLogger.DEBUG4:
                sosLogger.debug4(s);
                break;
            case SOSLogger.DEBUG5:
                sosLogger.debug5(s);
                break;
            case SOSLogger.DEBUG6:
                sosLogger.debug6(s);
                break;
            case SOSLogger.DEBUG7:
                sosLogger.debug7(s);
                break;
            case SOSLogger.DEBUG8:
                sosLogger.debug8(s);
                break;
            case SOSLogger.DEBUG9:
                sosLogger.debug9(s);
                break;
            case SOSLogger.INFO:
                sosLogger.info(s);
                break;
            case SOSLogger.WARN:
                sosLogger.warn(s);
                break;
            case SOSLogger.ERROR:
                sosLogger.error(s);
                break;
            }
        }
    }

    private void getSettings(final SOSSettings sosSettings, final String language) throws Exception {
        if (language != null) {
            this.setLanguage(language);
        }
        this.getSettings(sosSettings);
    }

    private String getEntry(final String val, final Properties entries, final String key) {
        String erg = val;
        if (entries.containsKey(key)) {
            if (!entries.getProperty(key).isEmpty()) {
                erg = entries.getProperty(key);
            }
        }
        return erg;
    }

    private void getSettings(final SOSSettings sosSettings) throws Exception {
        if (sosSettings == null) {
            throw new Exception(SOSClassUtil.getMethodName() + ": missing settings object.");
        }
        this.sosSettings = sosSettings;
        Properties entries = this.sosSettings.getSection(applicationMail, sectionMail);
        if (entries.size() == 0) {
            throw new Exception(SOSClassUtil.getMethodName() + ": missing settings entries in section \"" + sectionMail + "\".");
        }
        host = getEntry(host, entries, "host");
        port = getEntry(port, entries, "port");
        user = getEntry(user, entries, "smtp_user");
        password = getEntry(password, entries, "smtp_pass");
        from = getEntry(from, entries, "from");
        from = getEntry(from, entries, "mail_from");
        fromName = getEntry(fromName, entries, "from_name");
        fromName = getEntry(fromName, entries, "mail_from_name");
        replyTo = getEntry(replyTo, entries, "reply_to");
        replyTo = getEntry(replyTo, entries, "mail_reply_to");
        queueDir = getEntry(queueDir, entries, "queue_directory");
        queueDir = getEntry(queueDir, entries, "mail_queue_directory");
        String priorityStr = new String("1");
        priority = Integer.parseInt(getEntry(priorityStr, entries, "priority"));
        language = getEntry(language, entries, "language");
        subject = getEntry(subject, entries, "subject");
        contentType = getEntry(contentType, entries, "content_type");
        charset = getEntry(charset, entries, "charset");
        encoding = getEntry(encoding, entries, "encoding");
        attachmentEncoding = getEntry(attachmentEncoding, entries, "attachment_encoding");
        attachmentEncoding = getEntry(attachmentEncoding, entries, "file_encoding");
        if (entries.getProperty("smtp_timeout") != null && !entries.getProperty("smtp_timeout").isEmpty()) {
            timeout = 1000 * Integer.parseInt(entries.getProperty("smtp_timeout"));
        }
        if (from == null && entries.containsKey("mail_from") && entries.getProperty("mail_from").length() > 0) {
            from = entries.getProperty("mail_from");
        }
    }

    public boolean getTemplates(final SOSSettings sosSettings, final String language) throws Exception {
        if (language != null) {
            this.setLanguage(language);
        }
        return this.getTemplates(sosSettings);
    }

    public boolean getTemplates(final SOSSettings sosSettings) throws Exception {
        if (sosSettings == null) {
            throw new Exception(SOSClassUtil.getMethodName() + ": missing settings object.");
        }
        this.sosSettings = sosSettings;
        templates = this.sosSettings.getSection(this.getApplicationMailTemplates(), this.getSectionMailTemplates() + "_" + language);
        if (templates.size() == 0) {
            throw new Exception(SOSClassUtil.getMethodName() + ": missing settings entries for application \"" + applicationMailTemplates
                    + "\" in section \"" + sectionMailTemplates + "\".");
        }
        return true;
    }

    public String substituteSubject(final String template, final HashMap replacements) throws Exception {
        if (!templates.containsKey(template + "_subject")) {
            throw new Exception("substituteSubject(): template does not exist: " + template + "_subject");
        }
        return substitute(templates.get(template + "_subject").toString(), replacements, false);
    }

    public String substituteBody(final String template, final HashMap replacements, final boolean nl2br) throws Exception {
        if (!templates.containsKey(template + "_body")) {
            throw new Exception("substituteBody(): template does not exist: " + template + "_body");
        }
        return substitute(templates.get(template + "_body").toString(), replacements, nl2br);
    }

    public String substituteBody(final String template, final HashMap replacements) throws Exception {
        if (!templates.containsKey(template + "_body")) {
            throw new Exception("substituteBody(): template does not exist: " + template + "_body");
        }
        return substitute(templates.get(template + "_body").toString(), replacements, false);
    }

    @Deprecated
    public String substitute(String content, final HashMap replacements, final boolean nl2br) throws Exception {
        Object key = null;
        Object value = null;
        if ("de".equalsIgnoreCase(this.getLanguage())) {
            this.setDateFormat("dd.MM.yyyy");
            this.setDatetimeFormat("dd.MM.yyyy HH:mm");
        } else if ("en".equalsIgnoreCase(this.getLanguage())) {
            this.setDateFormat("MM/dd/yyyy");
            this.setDatetimeFormat("MM/dd/yyyy HH:mm");
        }
        content = content.replaceAll("&\\(date\\)", SOSDate.getCurrentDateAsString(this.getDateFormat()));
        content = content.replaceAll("&\\(datetime\\)", SOSDate.getCurrentTimeAsString(this.getDatetimeFormat()));
        content = content.replaceAll("&\\#\\(date\\)", SOSDate.getCurrentDateAsString(this.getDateFormat()));
        content = content.replaceAll("&\\#\\#\\(datetime\\)", SOSDate.getCurrentTimeAsString(this.getDatetimeFormat()));
        if (nl2br) {
            content = content.replaceAll("\n", "<br/>");
        }
        if (replacements != null) {
            Iterator keys = replacements.keySet().iterator();
            while (keys.hasNext()) {
                key = keys.next();
                if (key != null) {
                    value = replacements.get(key.toString());
                    if (value != null) {
                        try {
                            content =
                                    content.replaceAll("&\\#\\(" + key.toString() + "\\)", SOSDate.getDateAsString(SOSDate.getDate(value.toString()),
                                            this.getDateFormat()));
                            content =
                                    content.replaceAll("&\\#\\#\\(" + key.toString() + "\\)", SOSDate.getDateTimeAsString(
                                            SOSDate.getDate(value.toString()), this.getDatetimeFormat()));
                        } catch (Exception ex) {
                            // ignore this error: replacement is not convertible
                            // to date
                        }
                        Locale defaultLocale = Locale.getDefault();
                        try {
                            double doubleValue = Double.parseDouble(value.toString());
                            if ("de".equalsIgnoreCase(this.getLanguage())) {
                                Locale.setDefault(Locale.GERMAN);
                            } else if ("en".equalsIgnoreCase(this.getLanguage())) {
                                Locale.setDefault(Locale.US);
                            }
                            DecimalFormat formatter = new DecimalFormat("#,###.00");
                            content = content.replaceAll("&\\$\\(" + key.toString() + "\\)", formatter.format(doubleValue).toString());
                        } catch (Exception ex) {
                        } finally {
                            Locale.setDefault(defaultLocale);
                        }
                        content = content.replaceAll("&\\(" + key.toString() + "\\)", value.toString());
                    }
                }
            }
        }
        content = content.replaceAll("&\\#\\(.*\\)", "");
        content = content.replaceAll("&\\#\\#\\(.*\\)", "");
        content = content.replaceAll("&\\$\\(.*\\)", "");
        return content.replaceAll("&\\(.*\\)", "");
    }

    public void addRecipient(String recipient) throws Exception {
        String token = "";
        warn("addRecipient", recipient);
        if (recipient == null) {
            throw new Exception(SOSClassUtil.getMethodName() + ": recipient has no value.");
        }
        recipient = recipient.replace(',', ';');
        StringTokenizer t = new StringTokenizer(recipient, ";");
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            if (!toList.contains(token)) {
                toList.add(token);
            }
            log(SOSClassUtil.getMethodName() + "-->" + token, SOSLogger.DEBUG9);
        }
        changed = true;
    }

    public void addCC(String recipient) throws Exception {
        String token = "";
        warn("addCC", recipient);
        if (recipient == null) {
            throw new Exception(SOSClassUtil.getMethodName() + ": CC recipient has no value.");
        }
        recipient = recipient.replace(',', ';');
        StringTokenizer t = new StringTokenizer(recipient, ";");
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            if (!toList.contains(token) && !ccList.contains(token)) {
                ccList.add(token);
                log(SOSClassUtil.getMethodName() + "-->" + token, SOSLogger.DEBUG9);
            } else {
                log(SOSClassUtil.getMethodName() + "--> Ignored:" + token, SOSLogger.DEBUG9);
            }
        }
        changed = true;
    }

    public void addBCC(String recipient) throws Exception {
        String token = "";
        warn("addBCC", recipient);
        if (recipient == null) {
            throw new Exception(SOSClassUtil.getMethodName() + ": BCC recipient has no value.");
        }
        recipient = recipient.replace(',', ';');
        StringTokenizer t = new StringTokenizer(recipient, ";");
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            if (!ccList.contains(token) && !toList.contains(token) && !bccList.contains(token)) {
                bccList.add(token);
                log(SOSClassUtil.getMethodName() + "-->" + token, SOSLogger.DEBUG9);
            } else {
                log(SOSClassUtil.getMethodName() + "--> Ignored:" + token, SOSLogger.DEBUG9);
            }
        }
        changed = true;
    }

    private void closeAttachments() throws Exception {
        Exception exception = null;
        for (int i = 0; i < file_input_streams.size(); i++) {
            try {
                ((FileInputStream) file_input_streams.get(i)).close();
            } catch (Exception x) {
                if (exception == null) {
                    exception = x;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    public void addAttachment(final SOSMailAttachment att) throws Exception {
        warn("addAttachment", att.getFile().getAbsolutePath());
        attachmentList.put(att.getFile().getAbsolutePath(), att);
        changed = true;
    }

    public void addAttachment(final String filename) throws Exception {
        warn("addAttachment", filename);
        File f = new File(filename);
        SOSMailAttachment att = new SOSMailAttachment(this, f);
        att.setCharset(getAttachmentCharset());
        att.setEncoding(getAttachmentEncoding());
        att.setContentType(getAttachmentContentType());
        attachmentList.put(filename, att);
        changed = true;
    }

    public void addAttachment(final String filename, final String params) throws Exception {
        String name = "";
        String value = "";
        String token = "";
        int counter = 0;
        warn("addAttachment", filename + "(" + params + ")");
        StringTokenizer t = new StringTokenizer(params, ",");
        File f = new File(filename);
        SOSMailAttachment att = new SOSMailAttachment(this, f);
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            StringTokenizer vv = new StringTokenizer(token, "=");
            if (vv.countTokens() == 1) {
                name = "content-type";
                value = vv.nextToken();
                counter += 1;
            } else {
                name = vv.nextToken().trim();
                try {
                    value = vv.nextToken().trim();
                } catch (NoSuchElementException e) {
                    value = "";
                }
            }
            if ("content-type".equalsIgnoreCase(name)) {
                att.setContentType(value);
            } else if ("charset".equalsIgnoreCase(name)) {
                att.setCharset(value);
            } else if ("encoding".equalsIgnoreCase(name)) {
                att.setEncoding(value);
            } else {
                throw new Exception("USING of .addAttachment is wrong. ==> " + params
                        + ", rigth using is: [content-type-value],[content-type=<value>],[charset=<value>],[encoding=<value>]");
            }
            if (counter > 1) {
                throw new Exception("USING of .addAttachment is wrong. ==> " + params
                        + ", rigth using is: [content-type-value],[content-type=<value>],[charset=<value>],[encoding=<value>]");
            }
        }
        attachmentList.put(filename, att);
        changed = true;
    }

    private void add_file(final SOSMailAttachment att) throws Exception {
        if (!att.getFile().exists()) {
            throw new Exception("Datei " + att.getFile().getAbsolutePath() + " fehlt");
        }
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource data_source = new File_data_source(att.getFile(), att.getContentType());
        DataHandler data_handler = new DataHandler(data_source);
        attachment.setDataHandler(data_handler);
        attachment.setFileName(att.getFile().getName());
        if (att.getContentType().startsWith("text/")) {
            String s = "";
            FileReader fr = new FileReader(att.getFile());
            for (int c; (c = fr.read()) != -1;) {
                s += (char) c;
            }
            attachment.setText(s, att.getCharset());
            fr.close();
        }
        Object m = message.getContent();
        if (!(m instanceof MimeMultipart)) {
            throw new RuntimeException(getClass().getName() + "mime_message.getContent() liefert nicht MimeMultiPart");
        }
        ((MimeMultipart) m).addBodyPart(attachment);
        attachment.setHeader("Content-Transfer-Encoding", att.getEncoding());
    }

    public void loadFile(final File messageFile) throws Exception {
        FileInputStream messageInputStream = null;
        try {
            messageInputStream = new FileInputStream(messageFile);
            message = new MimeMessage(createSession(), messageInputStream);
            loadedMessageId = message.getMessageID();
            raw_email_byte_stream = new ByteArrayOutputStream();
            message.writeTo(raw_email_byte_stream);
            messageBytes = raw_email_byte_stream.toByteArray();
            messageReady = true;
        } catch (Exception x) {
            throw new Exception("Fehler beim Lesen der eMail. " + messageFile);
        } finally {
            if (messageInputStream != null) {
                messageInputStream.close();
            }
        }
    }

    public void unloadMessage() {
        messageReady = false;
        loadedMessageId = "";
        message = null;
    }

    public boolean send() throws Exception {
        return sendJavaMail();
    }

    public boolean send(final boolean send) throws Exception {
        if (send) {
            return send();
        } else {
            return prepareJavaMail();
        }
    }

    private boolean sendJavaMail() throws Exception {
        try {
            prepareJavaMail();
            String sTO = this.getRecipientsAsString();
            String logMessage = SOSClassUtil.getMethodName() + "-->" + "sending email:" + "   host:port=" + host + ":" + port + "   to=" + sTO;
            String sCC = this.getCCsAsString();
            if (!"".equals(sCC)) {
                logMessage += "   sCC=" + sCC;
            }
            String sBCC = this.getBCCsAsString();
            if (!"".equals(sBCC)) {
                logMessage += "   sBCC=" + sBCC;
            }
            log(logMessage, SOSLogger.INFO);
            log(SOSClassUtil.getMethodName() + "-->" + "Subject=" + subject, SOSLogger.DEBUG6);
            log(SOSClassUtil.getMethodName() + "-->" + dumpHeaders(), SOSLogger.DEBUG6);
            log(SOSClassUtil.getMethodName() + "-->" + dumpMessageAsString(false), SOSLogger.DEBUG9);
            if (!sendToOutputStream) {
                Transport t = session.getTransport("smtp");
                message.setSentDate(new Date());
                System.setProperty("mail.smtp.port", port);
                System.setProperty("mail.smtp.host", host);
                if (user.isEmpty()) {
                    t.connect();
                } else {
                    t.connect(host, user, password);
                }
                t.sendMessage(message, message.getAllRecipients());
                t.close();
                raw_email_byte_stream = new ByteArrayOutputStream();
                message.writeTo(raw_email_byte_stream);
                messageBytes = raw_email_byte_stream.toByteArray();
                changed = true;
            }
            return true;
        } catch (javax.mail.AuthenticationFailedException ee) {
            lastError = "AuthenticationFailedException while connecting to " + host + ":" + port + " " + user + "/******** -->" + ee.getMessage();
            try {
                dumpMessageToFile(true);
            } catch (Exception e) {
                log(SOSClassUtil.getMethodName() + ":" + e.getMessage(), SOSLogger.WARN);
            }
            return false;
        } catch (javax.mail.MessagingException e) {
            // ist ein Fehler, bei dem es lohnt, zwischenzuspeichern?
            if (queueDir.length() > 0 && e.getMessage().startsWith("Could not connect to SMTP host")
                    || e.getMessage().startsWith("Unknown SMTP host") || e.getMessage().startsWith("Read timed out")
                    || e.getMessage().startsWith("Exception reading response")) {
                lastError = e.getMessage() + " ==> " + host + ":" + port + " " + user + "/********";
                try {
                    dumpMessageToFile(true);
                } catch (Exception ee) {
                    log(SOSClassUtil.getMethodName() + ":" + e.getMessage(), SOSLogger.WARN);
                }
                return false;
            } else {
                throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
            }
        } catch (SocketTimeoutException e) {
            if (queueDir.length() > 0) {
                lastError = e.getMessage() + " ==> " + host + ":" + port + " " + user + "/********";
                try {
                    dumpMessageToFile(true);
                } catch (Exception ee) {
                    log(SOSClassUtil.getMethodName() + ":" + e.getMessage(), SOSLogger.WARN);
                }
                return false;
            } else {
                throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
            }
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
        }

    }

    private boolean haveAlternative() {
        return !"".equals(alternativeBody) && attachmentList.isEmpty();
    }

    protected boolean prepareJavaMail() throws Exception {
        try {
            if (messageReady) {
                message.saveChanges();
                return true;
            }
            if (!changed) {
                return true;
            }
            changed = false;
            if ("text/html".equals(this.getContentType())) {
                body = body.replaceAll("\\\\n", "<br>");
            } else {
                body = body.replaceAll("\\\\n", "\n");
            }
            String t = "";
            if (toList.isEmpty()) {
                throw new Exception(SOSClassUtil.getMethodName() + ": no recipient specified.");
            }
            if (from == null || from.isEmpty()) {
                throw new Exception(SOSClassUtil.getMethodName() + ": no sender specified.");
            }
            if (fromName != null && !fromName.isEmpty()) {
                message.setFrom(new InternetAddress(from, fromName));
            } else {
                message.setFrom(new InternetAddress(from));
            }
            message.setSentDate(new Date());
            if (replyTo != null && !replyTo.isEmpty()) {
                InternetAddress fromAddrs[] = new InternetAddress[1];
                fromAddrs[0] = new InternetAddress(replyTo);
                message.setReplyTo(fromAddrs);
            }
            if (!toList.isEmpty()) {
                InternetAddress toAddrs[] = new InternetAddress[toList.size()];
                int i = 0;
                for (ListIterator e = toList.listIterator(); e.hasNext();) {
                    t = e.next().toString();
                    toAddrs[i++] = new InternetAddress(t);
                }
                message.setRecipients(MimeMessage.RecipientType.TO, toAddrs);
            }
            InternetAddress toAddrs[] = new InternetAddress[ccList.size()];
            int i = 0;
            for (ListIterator e = ccList.listIterator(); e.hasNext();) {
                t = e.next().toString();
                toAddrs[i++] = new InternetAddress(t);
            }
            message.setRecipients(MimeMessage.RecipientType.CC, toAddrs);
            toAddrs = new InternetAddress[bccList.size()];
            i = 0;
            for (ListIterator e = bccList.listIterator(); e.hasNext();) {
                t = e.next().toString();
                toAddrs[i++] = new InternetAddress(t);
            }
            message.setRecipients(MimeMessage.RecipientType.BCC, toAddrs);
            if (subject != null) {
                message.setSubject(subject);
            }
            if (!attachmentList.isEmpty() || !"".equals(alternativeBody)) {
                MimeBodyPart bodypart = null;
                MimeBodyPart alternativeBodypart = null;
                MimeMultipart multipart = null;
                if (this.haveAlternative()) {
                    multipart = new MimeMultipart("alternative");
                } else {
                    multipart = new MimeMultipart();
                }
                bodypart = new MimeBodyPart();
                if (contentType.startsWith("text/")) {
                    bodypart.setContent(body, contentType + ";charset= " + charset);
                } else {
                    bodypart.setContent(body, contentType);
                }
                multipart.addBodyPart(bodypart);
                if (this.haveAlternative()) {
                    alternativeBodypart = new MimeBodyPart();
                    if (contentType.startsWith("text/")) {
                        alternativeBodypart.setContent(alternativeBody, alternativeContentType + ";charset= " + alternativeCharset);
                    } else {
                        alternativeBodypart.setContent(alternativeBody, alternativeContentType);
                    }
                    multipart.addBodyPart(alternativeBodypart);
                }
                message.setContent(multipart);
                bodypart.setHeader("Content-Transfer-Encoding", encoding);
                if (alternativeBodypart != null) {
                    alternativeBodypart.setHeader("Content-Transfer-Encoding", alternativeEncoding);
                }
                for (Iterator iter = attachmentList.values().iterator(); iter.hasNext();) {
                    SOSMailAttachment attachment = (SOSMailAttachment) iter.next();
                    String content_type = attachment.getContentType();
                    if (content_type == null) {
                        throw new Exception("content_type ist null");
                    }
                    log(SOSClassUtil.getMethodName() + "-->" + "Attachment=" + attachment.getFile(), SOSLogger.DEBUG6);
                    add_file(attachment);
                }
            } else {
                message.setHeader("Content-Transfer-Encoding", encoding);
                if (contentType.startsWith("text/")) {
                    message.setContent(body, contentType + "; charset=" + charset);
                } else {
                    message.setContent(body, contentType);
                }
            }
            message.saveChanges();
            closeAttachments();
            return true;
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.getMessage(), e);
        } finally {
        }
    }

    public String dumpHeaders() throws IOException, MessagingException {
        String s = "";
        for (Enumeration e = message.getAllHeaders(); e.hasMoreElements();) {
            Header header = (Header) e.nextElement();
            s += "\n" + header.getName() + ": " + header.getValue();
        }
        return s;

    }

    private ByteArrayOutputStream messageRemoveAttachments() throws Exception {
        ByteArrayOutputStream raw_email_byte_stream_without_attachment = new ByteArrayOutputStream();
        MimeMessage mm = new MimeMessage(message);
        Object mmpo = mm.getContent();
        if (mmpo instanceof MimeMultipart) {
            MimeMultipart mmp = (MimeMultipart) mmpo;
            if (mm.isMimeType("text/plain")) {
            } else if (mm.isMimeType("multipart/*")) {
                mmp = (MimeMultipart) mm.getContent();
                for (int i = 1; i < mmp.getCount(); i++) {
                    BodyPart part = mmp.getBodyPart(i);
                    mmp.removeBodyPart(i);
                    i--;
                }
            }
            mm.setContent(mmp);
            mm.saveChanges();
        }
        mm.writeTo(raw_email_byte_stream_without_attachment);
        return raw_email_byte_stream_without_attachment;
    }

    public String dumpMessageAsString() throws Exception {
        return dumpMessageAsString(false);
    }

    private void dumpMessageToFile(final boolean withAttachment) throws Exception {
        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(queuePattern);
        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);
        lastGeneratedFileName = queueDir + "/" + queuePraefix + b + ".email";
        File f = new File(lastGeneratedFileName);
        while (f.exists()) {
            b = s.format(d, bb, fp);
            lastGeneratedFileName = queueDir + "/" + queuePraefix + b + ".email";
            f = new File(lastGeneratedFileName);
        }
        dumpMessageToFile(f, withAttachment);
    }

    public void dumpMessageToFile(final String filename, final boolean withAttachment) throws Exception {
        dumpMessageToFile(new File(filename), withAttachment);
    }

    public void dumpMessageToFile(final File file, final boolean withAttachment) throws Exception {
        try {
            this.prepareJavaMail();
            File myFile = new File(file.getAbsolutePath() + "~");
            FileOutputStream out = new FileOutputStream(myFile, true);
            out.write(dumpMessage(withAttachment));
            out.close();
            String newFilename = myFile.getAbsolutePath().substring(0, myFile.getAbsolutePath().length() - 1);
            File f = new File(newFilename);
            f.delete();
            myFile.renameTo(f);
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on dump: " + e.toString());
        }
    }

    public String dumpMessageAsString(final boolean withAttachment) throws Exception {
        byte[] bytes;
        ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;
        this.prepareJavaMail();
        if (!withAttachment) {
            raw_email_byte_stream_without_attachment = messageRemoveAttachments();
        }
        raw_email_byte_stream = new ByteArrayOutputStream();
        message.writeTo(raw_email_byte_stream);
        if (withAttachment || raw_email_byte_stream_without_attachment == null) {
            bytes = raw_email_byte_stream.toByteArray();
        } else {
            bytes = raw_email_byte_stream_without_attachment.toByteArray();
        }
        String s = new String(bytes);
        return s;
    }

    public byte[] dumpMessage() throws Exception {
        return dumpMessage(true);
    }

    public byte[] dumpMessage(final boolean withAttachment) throws Exception {
        byte[] bytes;
        ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;
        this.prepareJavaMail();
        if (!withAttachment) {
            raw_email_byte_stream_without_attachment = messageRemoveAttachments();
        }
        raw_email_byte_stream = new ByteArrayOutputStream();
        message.writeTo(raw_email_byte_stream);
        if (withAttachment || raw_email_byte_stream_without_attachment == null) {
            bytes = raw_email_byte_stream.toByteArray();
        } else {
            bytes = raw_email_byte_stream_without_attachment.toByteArray();
        }
        return bytes;
    }

    public LinkedList getRecipients() {
        return toList;
    }

    public String getRecipientsAsString() throws MessagingException {
        String s = " ";
        if (messageReady) {
            Address[] addresses = message.getRecipients(MimeMessage.RecipientType.TO);
            if (addresses != null) {
                for (Address aktAddress : addresses) {
                    s += aktAddress.toString() + ",";
                }
            }
        } else {
            for (Iterator i = toList.listIterator(); i.hasNext();) {
                s += i.next() + ",";
            }
        }
        return s.substring(0, s.length() - 1).trim();
    }

    public LinkedList getCCs() {
        return ccList;
    }

    public String getCCsAsString() throws MessagingException {
        String s = " ";
        if (messageReady) {
            Address[] addresses = message.getRecipients(MimeMessage.RecipientType.CC);
            if (addresses != null) {
                for (Address aktAddress : addresses) {
                    s += aktAddress.toString() + ",";
                }
            }
        } else {
            for (Iterator i = ccList.listIterator(); i.hasNext();) {
                s += i.next() + ",";
            }
        }
        return s.substring(0, s.length() - 1).trim();
    }

    public LinkedList getBCCs() {
        return bccList;
    }

    public String getBCCsAsString() throws MessagingException {
        String s = " ";
        if (messageReady) {
            Address[] addresses = message.getRecipients(MimeMessage.RecipientType.BCC);
            if (addresses != null) {
                for (Address aktAddress : addresses) {
                    s += aktAddress.toString() + ",";
                }
            }
        } else {
            for (Iterator i = bccList.listIterator(); i.hasNext();) {
                s += i.next() + ",";
            }
        }
        return s.substring(0, s.length() - 1).trim();
    }

    public void clearRecipients() throws Exception {
        log(SOSClassUtil.getMethodName(), SOSLogger.DEBUG9);
        toList.clear();
        ccList.clear();
        bccList.clear();
        changed = true;
    }

    public void clearAttachments() {
        attachmentList.clear();
        changed = true;
    }

    private void sendLine(final BufferedReader in, final BufferedWriter out, String s) throws Exception {
        try {
            out.write(s + "\r\n");
            out.flush();
            s = in.readLine();
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on sendLine: " + e.toString());
        }
    }

    private void sendLine(final BufferedWriter out, final String s) throws Exception {
        try {
            out.write(s + "\r\n");
            out.flush();
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on sendLine: " + e.toString());
        }
    }

    public String getQuotedName(final String name) {
        if (name.indexOf('<') > -1 && name.indexOf('>') > -1) {
            return name;
        } else {
            return '<' + name + '>';
        }
    }

    public void setTimeout(final int timeout) throws Exception {
        this.timeout = timeout;
        this.initMessage();

    }

    public int getTimeout() {
        return timeout;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) throws Exception {
        this.language = language;
        this.initLanguage();
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDatetimeFormat() {
        return datetimeFormat;
    }

    public void setDatetimeFormat(final String datetimeFormat) {
        this.datetimeFormat = datetimeFormat;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
        warn("encoding", encoding);
        changed = true;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setCharset(final String charset) {
        this.charset = charset;
        warn("charset", charset);
        changed = true;
    }

    public String getCharset() {
        return charset;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
        warn("contentType", contentType);
        changed = true;
    }

    public String getContentType() {
        return contentType;
    }

    public void setAttachmentContentType(final String attachmentContentType) {
        this.attachmentContentType = attachmentContentType;
        warn("attachmentContentType", attachmentContentType);
        changed = true;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public void setQueueDir(final String queueDir) {
        this.queueDir = queueDir;
    }

    public String getQueueDir() {
        return queueDir;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
        warn("subject", subject);
        changed = true;
    }

    public String getSubject() {
        return subject;
    }

    public void setFrom(final String from) {
        this.from = from;
        warn("from", from);
        changed = true;
    }

    public String getFrom() {
        return from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(final String fromName) {
        this.fromName = fromName;
        warn("fromName", fromName);
        changed = true;
    }

    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
        warn("replyTo", replyTo);
        changed = true;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setBody(final String body) {
        this.body = body;
        warn("body", body);
        changed = true;
    }

    public String getBody() {
        return body;
    }

    public void setApplicationMail(final String applicationMail) {
        this.applicationMail = applicationMail;
    }

    public String getApplicationMail() {
        return applicationMail;
    }

    public void setSectionMail(final String sectionMail) {
        this.sectionMail = sectionMail;
    }

    public String getSectionMail() {
        return sectionMail;
    }

    public void setApplicationMailTemplates(final String applicationMailTemplates) {
        this.applicationMailTemplates = applicationMailTemplates;
    }

    public String getApplicationMailTemplates() {
        return applicationMailTemplates;
    }

    public void setApplicationMailTemplatesFactory(final String applicationMailTemplates) {
        applicationMailTemplatesFactory = applicationMailTemplates;
    }

    public String getApplicationMailTemplatesFactory() {
        return applicationMailTemplatesFactory;
    }

    public void setSectionMailTemplates(final String sectionMailTemplates) {
        this.sectionMailTemplates = sectionMailTemplates;
    }

    public String getSectionMailTemplates() {
        return sectionMailTemplates;
    }

    public void setSectionMailTemplatesFactory(final String sectionMailTemplatesFactory) {
        this.sectionMailTemplatesFactory = sectionMailTemplatesFactory;
    }

    public String getSectionMailTemplatesFactory() {
        return sectionMailTemplatesFactory;
    }

    public void setSectionMailScripts(final String sectionMailScripts) {
        this.sectionMailScripts = sectionMailScripts;
    }

    public String getSectionMailScripts() {
        return sectionMailScripts;
    }

    public void setApplicationMailScripts(final String applicationMailScripts) {
        this.applicationMailScripts = applicationMailScripts;
    }

    public String getApplicationMailScripts() {
        return applicationMailScripts;
    }

    public void setTableSettings(final String tableSettings) {
        this.tableSettings = tableSettings;
    }

    public String getTableSettings() {
        return tableSettings;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public void setSendToOutputStream(final boolean sendToOutputStream) {
        this.sendToOutputStream = sendToOutputStream;
    }

    public void setattachmentEncoding(final String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
        warn("attachmentEncoding", attachmentEncoding);
        changed = true;
    }

    public MimeMessage getMessage() {
        return message;
    }

    public String getLastError() {
        return lastError;
    }

    public String getAttachmentEncoding() {
        return attachmentEncoding;
    }

    public String getAttachmentCharset() {
        return attachmentCharset;
    }

    public void setAttachmentCharset(final String attachmentCharset) {
        this.attachmentCharset = attachmentCharset;
        warn("attachmentCharset", attachmentCharset);
        changed = true;
    }

    public void setAttachmentEncoding(final String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
        warn("attachmentEncoding", attachmentEncoding);
        changed = true;

    }

    public void setHost(final String host) throws Exception {
        this.host = host;
        this.initMessage();
    }

    public void setPassword(final String password) throws Exception {
        this.password = password;
        this.initMessage();
    }

    public void setUser(final String user) throws Exception {
        this.user = user;
        this.initMessage();
    }

    public void setPort(final String port) throws Exception {
        this.port = port;
        this.initMessage();
    }

    public void setPriorityHighest() throws MessagingException {
        message.setHeader("Priority", "urgent");
        message.setHeader("X-Priority", "1 (Highest)");
        message.setHeader("X-MSMail-Priority", "Highest");
        changed = true;
    }

    public void setPriorityHigh() throws MessagingException {
        message.setHeader("Priority", "urgent");
        message.setHeader("X-Priority", "2 (High)");
        message.setHeader("X-MSMail-Priority", "Highest");
        changed = true;
    }

    public void setPriorityNormal() throws MessagingException {
        message.setHeader("Priority", "normal");
        message.setHeader("X-Priority", "3 (Normal)");
        message.setHeader("X-MSMail-Priority", "Normal");
        changed = true;
    }

    public void setPriorityLow() throws MessagingException {
        message.setHeader("Priority", "non-urgent");
        message.setHeader("X-Priority", "4 (Low)");
        message.setHeader("X-MSMail-Priority", "Low");
        changed = true;
    }

    public void setPriorityLowest() throws MessagingException {
        message.setHeader("Priority", "non-urgent");
        message.setHeader("X-Priority", "5 (Lowest)");
        message.setHeader("X-MSMail-Priority", "Low");
        changed = true;
    }

    public void setAlternativeBody(final String alternativeBody) {
        this.alternativeBody = alternativeBody;
    }

    public void setAlternativeCharset(final String alternativeCharset) {
        this.alternativeCharset = alternativeCharset;
    }

    public void setAlternativeContentType(final String alternativeContentType) {
        this.alternativeContentType = alternativeContentType;
    }

    public String getQueuePraefix() {
        return queuePraefix;
    }

    public String getLastGeneratedFileName() {
        return lastGeneratedFileName;
    }

    public void setQueuePraefix(final String queuePraefix) {
        this.queuePraefix = queuePraefix;
    }

    public String getLoadedMessageId() {
        return loadedMessageId;
    }

    public void setSOSLogger(final SOSLogger sosLogger) {
        this.sosLogger = sosLogger;
    }

    private void warn(final String n, final String v) {
        if (messageReady) {
            try {
                log("...setting of " + n + "=" + v + " will not be used. Loaded Message will be sent unchanged.", SOSStandardLogger.WARN);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Deprecated
    public String sendNative() throws Exception {
        Socket socket = null;
        String boundary = "DataSeparatorString";
        StringBuilder sb = new StringBuilder();
        try {

            if (host == null || host.isEmpty()) {
                throw new Exception(SOSClassUtil.getMethodName() + ": host has no value.");
            }
            if (port == null || port.isEmpty()) {
                throw new Exception(SOSClassUtil.getMethodName() + ": port has no value.");
            }
            if (toList.isEmpty()) {
                throw new Exception(SOSClassUtil.getMethodName() + ": no recipient specified.");
            }
            if (from == null || from.isEmpty()) {
                throw new Exception(SOSClassUtil.getMethodName() + ": no sender specified.");
            }
            socket = new Socket(host, Integer.parseInt(port));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "8859_1"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "8859_1"));
            sendLine(in, out, "HELO " + host);
            if (fromName != null && !fromName.isEmpty() && from != null && !from.isEmpty()) {
                sendLine(in, out, "MAIL FROM: " + fromName + getQuotedName(from));
            } else if (from != null && !from.isEmpty()) {
                sendLine(in, out, "MAIL FROM: " + getQuotedName(from));
            }
            sendLine(in, out, "DATA");
            sendLine(out, "MIME-Version: 1.0");
            if (fromName != null && !fromName.isEmpty() && from != null && !from.isEmpty()) {
                sendLine(out, "From: " + fromName + getQuotedName(from));
            } else if (from != null && !from.isEmpty()) {
                sendLine(out, "From: " + getQuotedName(from));
            }
            if (replyTo != null && !replyTo.isEmpty()) {
                sendLine(out, "Reply-To: " + getQuotedName(replyTo));
            }
            if (!toList.isEmpty()) {
                sb = new StringBuilder();
                for (ListIterator e = toList.listIterator(); e.hasNext();) {
                    sb.append(getQuotedName(e.next().toString()));
                    if (e.hasNext()) {
                        sb.append(",");
                    }
                }
                sendLine(out, "To: " + sb);
            }
            if (!ccList.isEmpty()) {
                sb = new StringBuilder();
                for (ListIterator e = ccList.listIterator(); e.hasNext();) {
                    sb.append(getQuotedName(e.next().toString()));
                    if (e.hasNext()) {
                        sb.append(",");
                    }
                }
                sendLine(out, "Cc: " + sb);
            }

            if (!bccList.isEmpty()) {
                sb = new StringBuilder();
                for (ListIterator e = bccList.listIterator(); e.hasNext();) {
                    sb.append(getQuotedName(e.next().toString()));
                    if (e.hasNext()) {
                        sb.append(",");
                    }
                }
                sendLine(out, "Bcc: " + sb);
            }
            if (subject != null) {
                sendLine(out, "Subject: " + subject);
            }
            sendLine(out, "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"");
            sendLine(out, "\r\n--" + boundary);
            if (contentType != null && !contentType.isEmpty()) {
                sendLine(out, "Content-Type: text/html; charset=\"" + charset + "\"");
            }
            if (encoding != null) {
                sendLine(out, "Content-Transfer-Encoding: " + encoding);
            }
            sendLine(out, "\r\n" + body + "\r\n\r\n");
            if (!attachmentList.isEmpty()) {
                for (Iterator i = attachmentList.values().iterator(); i.hasNext();) {
                    String[] attachment = new String[2];
                    attachment = (String[]) i.next();
                    sendLine(out, "\r\n--" + boundary);
                    sendLine(out, "Content-Type: " + attachment[1] + "; name=\"" + new File(attachment[0]).getName() + "\"");
                    sendLine(out, "Content-Disposition: attachment; filename=\"" + attachment[0] + "\"");
                    sendLine(out, "Content-Transfer-Encoding: " + attachmentEncoding + "\r\n");
                    SOSMimeBase64.encode(attachment[0], out);
                }
            }
            sendLine(out, "\r\n\r\n--" + boundary + "--\r\n");
            sendLine(in, out, ".");
            sendLine(in, out, "QUIT");
        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return sb.toString();
    }

    @Deprecated
    public String sendHostware() throws Exception {
        throw new JSNotImplementedException("hostware is no longer supported");
    }

    public static void main(final String[] args) throws Exception {
        SOSMail sosMail = new SOSMail("smtp.sos");
        sosMail.setPriorityLowest();
        sosMail.setQueueDir("c:/");
        sosMail.setFrom("xyz@sos-berlin.com");
        sosMail.setEncoding("8bit");
        sosMail.setattachmentEncoding("Base64");
        sosMail.setSubject("Betreff");
        sosMail.setReplyTo("xyz@sos-berlin.com");
        String s = "Hello\\nWorld";
        sosMail.setBody(s);
        sosMail.addRecipient("xyz@sos-berlin.com");
        SOSStandardLogger sosLogger = new SOSStandardLogger(SOSStandardLogger.DEBUG9);
        sosMail.setSOSLogger(sosLogger);
        sosMail.setPriorityLowest();
        if (!sosMail.send()) {
            sosMail.log(SOSClassUtil.getMethodName() + "-->" + sosMail.getLastError(), SOSLogger.WARN);
        }
        sosMail.clearRecipients();
    }

    public void sendMail(final ISOSSmtpMailOptions pobjO) throws Exception {
        final String strDelims = ",|;";
        try {
            SOSMail sosMail = this;
            sosMail.init();
            sosMail.setHost(pobjO.gethost().Value());
            sosMail.setPort(pobjO.getport().Value());
            sosMail.setQueueDir(pobjO.getqueue_directory().Value());
            sosMail.setFrom(pobjO.getfrom().Value());
            sosMail.setContentType(pobjO.getcontent_type().Value());
            sosMail.setEncoding(pobjO.getencoding().Value());
            String recipient = pobjO.getto().Value();
            String recipients[] = recipient.trim().split(strDelims);
            for (String recipient2 : recipients) {
                sosMail.addRecipient(recipient2.trim());
            }
            String recipientCC = pobjO.getcc().Value();
            if (!recipientCC.trim().isEmpty()) {
                String recipientsCC[] = recipientCC.trim().split(strDelims);
                for (String element : recipientsCC) {
                    sosMail.addCC(element.trim());
                }
            }
            String recipientBCC = pobjO.getbcc().Value().trim();
            if (!recipientBCC.isEmpty()) {
                String recipientsBCC[] = recipientBCC.trim().split(strDelims);
                for (String element : recipientsBCC) {
                    sosMail.addBCC(element.trim());
                }
            }
            String strAttachments = pobjO.getattachment().Value().trim();
            if (!strAttachments.isEmpty()) {
                String strAttachmentsA[] = strAttachments.trim().split(strDelims);
                for (String element : strAttachmentsA) {
                    sosMail.addAttachment(element.trim());
                }
            }
            sosMail.setSubject(pobjO.getsubject().Value());
            sosMail.setBody(pobjO.getbody().Value());
            if (sosLogger != null) {
                sosLogger.debug("sending mail: \n" + sosMail.dumpMessageAsString());
            }
            if (!sosMail.send()) {
                if (sosLogger != null) {
                    sosLogger.warn("mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory ["
                            + sosMail.getQueueDir() + "]:" + sosMail.getLastError());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception("error occurred sending mail: " + e.getMessage());
        }
    }

}
