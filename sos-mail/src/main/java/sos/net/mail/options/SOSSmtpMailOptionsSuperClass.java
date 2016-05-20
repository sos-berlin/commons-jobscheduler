package sos.net.mail.options;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionMailAdress;
import com.sos.JSHelper.Options.SOSOptionMailSubject;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;

@JSOptionClass(name = "SOSSmtpMailOptionsSuperClass", description = "SOSSmtpMailOptionsSuperClass")
abstract public class SOSSmtpMailOptionsSuperClass extends JSOptionsClass implements ISOSSmtpMailOptions {

    private static final long serialVersionUID = -7729084542776568895L;
    private static final String CLASSNAME = "SOSSmtpMailOptionsSuperClass";

    @JSOptionDefinition(name = "attachment", description = "title Filename and path", key = "attachment", type = "SOSOptionString", mandatory = false)
    public SOSOptionString attachment = new SOSOptionString(this, CLASSNAME + ".attachment", "title Filename and path", "", "", false);

    @Override
    public SOSOptionString getAttachment() {
        return attachment;
    }

    @Override
    public void setAttachment(final SOSOptionString p_attachment) {
        attachment = p_attachment;
    }

    @JSOptionDefinition(name = "security_protocol", description = "security_protocol", key = "security_protocol", type = "SOSOptionString", mandatory = false)
    public SOSOptionString security_protocol = new SOSOptionString(this, CLASSNAME + ".security_protocol", "title security_protocol", "", "", false);

    @Override
    public SOSOptionString getSecurityProtocol() {
        return security_protocol;
    }

    @Override
    public void setSecurityProtocol(final SOSOptionString p_security_protocol) {
        security_protocol = p_security_protocol;
    }

    @JSOptionDefinition(name = "attachment_charset", description = "title charset of attac", key = "attachment_charset", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString attachment_charset = new SOSOptionString(this, CLASSNAME + ".attachment_charset", "title charset of attac", "iso-8859-1", 
            "iso-8859-1", false);

    @Override
    public SOSOptionString getAttachmentCharset() {
        return attachment_charset;
    }

    @Override
    public void setAttachmentCharset(final SOSOptionString p_attachment_charset) {
        attachment_charset = p_attachment_charset;
    }

    @JSOptionDefinition(name = "attachment_content_type", description = "title content_type of", key = "attachment_content_type", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString attachment_content_type = new SOSOptionString(this, CLASSNAME + ".attachment_content_type", "title content_type of", 
            "application/octet-stream", "application/octet-stream", false);

    @Override
    public SOSOptionString getAttachmentContentType() {
        return attachment_content_type;
    }

    @Override
    public void setAttachmentContentType(final SOSOptionString p_attachment_content_type) {
        attachment_content_type = p_attachment_content_type;
    }

    @JSOptionDefinition(name = "attachment_encoding", description = "title encoding of atta", key = "attachment_encoding", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString attachment_encoding = new SOSOptionString(this, CLASSNAME + ".attachment_encoding", "title encoding of atta", "Base64", "Base64", false);

    @Override
    public SOSOptionString getAttachmentEncoding() {
        return attachment_encoding;
    }

    @Override
    public void setAttachmentEncoding(final SOSOptionString p_attachment_encoding) {
        attachment_encoding = p_attachment_encoding;
    }

    @JSOptionDefinition(name = "bcc", description = "title bcc recipient(s)", key = "bcc", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress bcc = new SOSOptionMailAdress(this, CLASSNAME + ".bcc", "title bcc recipient(s)", "", "", false);
    public SOSOptionMailAdress FileNotificationBCC = (SOSOptionMailAdress) bcc.setAlias("file_notification_bcc");

    @Override
    public SOSOptionMailAdress getBcc() {
        return bcc;
    }

    @Override
    public void setBcc(final SOSOptionMailAdress p_bcc) {
        bcc = p_bcc;
    }

    @JSOptionDefinition(name = "body", description = "title Mail body", key = "body", type = "SOSOptionString", mandatory = false)
    public SOSOptionString body = new SOSOptionString(this, CLASSNAME + ".body", "title Mail body", "", "", false);
    public SOSOptionString FileNotificationBody = (SOSOptionString) body.setAlias("file_notification_body");

    @Override
    public SOSOptionString getBody() {
        return body;
    }

    @Override
    public void setBody(final SOSOptionString p_body) {
        body = p_body;
    }

    @JSOptionDefinition(name = "cc", description = "title cc recipient(s)", key = "cc", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress cc = new SOSOptionMailAdress(this, CLASSNAME + ".cc", "title cc recipient(s)", "", "", false);
    public SOSOptionMailAdress FileNotificationCC = (SOSOptionMailAdress) cc.setAlias("file_notification_cc");

    @Override
    public SOSOptionMailAdress getCc() {
        return cc;
    }

    @Override
    public void setCc(final SOSOptionMailAdress p_cc) {
        cc = p_cc;
    }

    @JSOptionDefinition(name = "charset", description = "title charset of the m", key = "charset", type = "SOSOptionString", mandatory = false)
    public SOSOptionString charset = new SOSOptionString(this, CLASSNAME + ".charset", "title charset of the m", "iso-8859-1", "iso-8859-1", false);

    @Override
    public SOSOptionString getCharset() {
        return charset;
    }

    @Override
    public void setCharset(final SOSOptionString p_charset) {
        charset = p_charset;
    }

    @JSOptionDefinition(name = "content_type", description = "title content_type of", key = "content_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString content_type = new SOSOptionString(this, CLASSNAME + ".content_type", "title content_type of", "text/plain", "text/plain", false);

    @Override
    public SOSOptionString getContentType() {
        return content_type;
    }

    @Override
    public void setContentType(final SOSOptionString p_content_type) {
        content_type = p_content_type;
    }

    @JSOptionDefinition(name = "encoding", description = "title encoding of the", key = "encoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString encoding = new SOSOptionString(this, CLASSNAME + ".encoding", "title encoding of the", "7bit", "7bit", false);

    @Override
    public SOSOptionString getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(final SOSOptionString p_encoding) {
        encoding = p_encoding;
    }

    @JSOptionDefinition(name = "from", description = "title mail sender", key = "from", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress from = new SOSOptionMailAdress(this, CLASSNAME + ".from", "title mail sender", "", "", false);
    public SOSOptionMailAdress MailFrom = (SOSOptionMailAdress) from.setAlias("mail_from");

    @Override
    public SOSOptionMailAdress getFrom() {
        return from;
    }

    @Override
    public void setFrom(final SOSOptionMailAdress p_from) {
        from = p_from;
    }

    @JSOptionDefinition(name = "from_name", description = "title name of the send", key = "from_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString from_name = new SOSOptionString(this, CLASSNAME + ".from_name", "title name of the send", "", "", false);

    @Override
    public SOSOptionString getFromName() {
        return from_name;
    }

    @Override
    public void setFromName(final SOSOptionString p_from_name) {
        from_name = p_from_name;
    }

    @JSOptionDefinition(name = "host", description = "title mail server host", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, CLASSNAME + ".host", "title mail server host", "", "", false);
    public SOSOptionHostName SMTPHost = (SOSOptionHostName) host.setAlias("mail_smtp", "smtp_host");

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public void setHost(final SOSOptionHostName p_host) {
        host = p_host;
    }

    @JSOptionDefinition(name = "port", description = "title mail server port", key = "port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, CLASSNAME + ".port", "title mail server port", "25", "25", false);

    public SOSOptionPortNumber MailPort = (SOSOptionPortNumber) port.setAlias("mail_port", "smtp_port");

    @Override
    public SOSOptionPortNumber getPort() {
        return port;
    }

    @Override
    public void setPort(final SOSOptionPortNumber p_port) {
        port = p_port;
    }

    @JSOptionDefinition(name = "queue_directory", description = "title Mail queue direc", key = "queue_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queue_directory = new SOSOptionString(this, CLASSNAME + ".queue_directory", "title Mail queue direc", "", "", false);
    public SOSOptionString MailQueueDir = (SOSOptionString) queue_directory.setAlias("mail_queue_dir");

    @Override
    public SOSOptionString getQueueDirectory() {
        return queue_directory;
    }

    @Override
    public void setQueueDirectory(final SOSOptionString p_queue_directory) {
        queue_directory = p_queue_directory;
    }

    @JSOptionDefinition(name = "reply_to", description = "title reply address", key = "reply_to", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress reply_to = new SOSOptionMailAdress(this, CLASSNAME + ".reply_to", "title reply address", "", "", false);

    @Override
    public SOSOptionMailAdress getReplyTo() {
        return reply_to;
    }

    @Override
    public void setReplyTo(final SOSOptionMailAdress p_reply_to) {
        reply_to = p_reply_to;
    }

    @JSOptionDefinition(name = "smtp_password", description = "title smtp user passwo", key = "smtp_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword smtp_password = new SOSOptionPassword(this, CLASSNAME + ".smtp_password", "title smtp user passwo", "", "", false);

    @Override
    public SOSOptionPassword getSmtpPassword() {
        return smtp_password;
    }

    @Override
    public void setSmtpPassword(final SOSOptionPassword p_smtp_password) {
        smtp_password = p_smtp_password;
    }

    @JSOptionDefinition(name = "smtp_user", description = "title smtp username", key = "smtp_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionString smtp_user = new SOSOptionString(this, CLASSNAME + ".smtp_user", "title smtp username", "", "", false);

    @Override
    public SOSOptionString getSmtpUser() {
        return smtp_user;
    }

    @Override
    public void setSmtpUser(final SOSOptionString p_smtp_user) {
        smtp_user = p_smtp_user;
    }

    @JSOptionDefinition(name = "subject", description = "title Mail Subject", key = "subject", type = "SOSOptionString", mandatory = false)
    public SOSOptionMailSubject subject = new SOSOptionMailSubject(this, CLASSNAME + ".subject", "title Mail Subject", "", "", false);
    public SOSOptionMailSubject FileNotificationSubject = (SOSOptionMailSubject) subject.setAlias("file_notification_subject");

    @Override
    public SOSOptionMailSubject getSubject() {
        return subject;
    }

    @Override
    public void setSubject(final SOSOptionMailSubject p_subject) {
        subject = p_subject;
    }

    @JSOptionDefinition(name = "to", description = "title mail recipient(s", key = "to", type = "SOSOptionMailAdress", mandatory = true)
    public SOSOptionMailAdress to = new SOSOptionMailAdress(this, CLASSNAME + ".to", "title mail recipient(s", "", "", true);
    public SOSOptionMailAdress FileNotificationTo = (SOSOptionMailAdress) to.setAlias("file_notification_to");

    @Override
    public SOSOptionMailAdress getTo() {
        return to;
    }

    @Override
    public void setTo(final SOSOptionMailAdress p_to) {
        to = p_to;
    }

    public SOSSmtpMailOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSSmtpMailOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSSmtpMailOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
    
}