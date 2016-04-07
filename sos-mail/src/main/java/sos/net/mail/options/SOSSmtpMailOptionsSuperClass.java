package sos.net.mail.options;

import java.util.HashMap;

import org.apache.log4j.Logger;

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

    /**
	 *
	 */
    private static final long serialVersionUID = -7729084542776568895L;
    private final String conClassName = "SOSSmtpMailOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SOSSmtpMailOptionsSuperClass.class);

    @JSOptionDefinition(name = "attachment", description = "title Filename and path", key = "attachment", type = "SOSOptionString", mandatory = false)
    public SOSOptionString attachment = new SOSOptionString(this, conClassName + ".attachment", // HashMap-Key
            "title Filename and path", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getattachment() {
        return attachment;
    }

    @Override
    public void setattachment(final SOSOptionString p_attachment) {
        attachment = p_attachment;
    }
    @JSOptionDefinition(name = "security_protocol", description = "security_protocol", key = "security_protocol", type = "SOSOptionString", mandatory = false)
    public SOSOptionString security_protocol = new SOSOptionString(this, conClassName + ".security_protocol", // HashMap-Key
            "title security_protocol", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getsecurity_protocol() {
        return security_protocol;
    }

    @Override
    public void setsecurity_protocol(final SOSOptionString p_security_protocol) {
        security_protocol = p_security_protocol;
    }


    
    @JSOptionDefinition(name = "attachment_charset", description = "title charset of attac", key = "attachment_charset", type = "SOSOptionString", mandatory = false)
    public SOSOptionString attachment_charset = new SOSOptionString(this, conClassName + ".attachment_charset", // HashMap-Key
            "title charset of attac", // Titel
            "iso-8859-1", // InitValue
            "iso-8859-1", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getattachment_charset() {
        return attachment_charset;
    }

    @Override
    public void setattachment_charset(final SOSOptionString p_attachment_charset) {
        attachment_charset = p_attachment_charset;
    }

    @JSOptionDefinition(name = "attachment_content_type", description = "title content_type of", key = "attachment_content_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString attachment_content_type = new SOSOptionString(this, conClassName + ".attachment_content_type", // HashMap-Key
            "title content_type of", // Titel
            "application/octet-stream", // InitValue
            "application/octet-stream", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getattachment_content_type() {
        return attachment_content_type;
    }

    @Override
    public void setattachment_content_type(final SOSOptionString p_attachment_content_type) {
        attachment_content_type = p_attachment_content_type;
    }

    @JSOptionDefinition(name = "attachment_encoding", description = "title encoding of atta", key = "attachment_encoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString attachment_encoding = new SOSOptionString(this, conClassName + ".attachment_encoding", // HashMap-Key
            "title encoding of atta", // Titel
            "Base64", // InitValue
            "Base64", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getattachment_encoding() {
        return attachment_encoding;
    }

    @Override
    public void setattachment_encoding(final SOSOptionString p_attachment_encoding) {
        attachment_encoding = p_attachment_encoding;
    }

    @JSOptionDefinition(name = "bcc", description = "title bcc recipient(s)", key = "bcc", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress bcc = new SOSOptionMailAdress(this, conClassName + ".bcc", // HashMap-Key
            "title bcc recipient(s)", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    public SOSOptionMailAdress FileNotificationBCC = (SOSOptionMailAdress) bcc.SetAlias("file_notification_bcc");

    @Override
    public SOSOptionMailAdress getbcc() {
        return bcc;
    }

    @Override
    public void setbcc(final SOSOptionMailAdress p_bcc) {
        bcc = p_bcc;
    }

    /** \var body : title Mail body Mail body */
    @JSOptionDefinition(name = "body", description = "title Mail body", key = "body", type = "SOSOptionString", mandatory = false)
    public SOSOptionString body = new SOSOptionString(this, conClassName + ".body", // HashMap-Key
            "title Mail body", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString FileNotificationBody = (SOSOptionString) body.SetAlias("file_notification_body");

    @Override
    public SOSOptionString getbody() {
        return body;
    }

    @Override
    public void setbody(final SOSOptionString p_body) {
        body = p_body;
    }

    /** \var cc : title cc recipient(s) cc recipient(s) */
    @JSOptionDefinition(name = "cc", description = "title cc recipient(s)", key = "cc", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress cc = new SOSOptionMailAdress(this, conClassName + ".cc", // HashMap-Key
            "title cc recipient(s)", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );
    public SOSOptionMailAdress FileNotificationCC = (SOSOptionMailAdress) cc.SetAlias("file_notification_cc");

    @Override
    public SOSOptionMailAdress getcc() {
        return cc;
    }

    @Override
    public void setcc(final SOSOptionMailAdress p_cc) {
        cc = p_cc;
    }

    @JSOptionDefinition(name = "charset", description = "title charset of the m", key = "charset", type = "SOSOptionString", mandatory = false)
    public SOSOptionString charset = new SOSOptionString(this, conClassName + ".charset", // HashMap-Key
            "title charset of the m", // Titel
            "iso-8859-1", // InitValue
            "iso-8859-1", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getcharset() {
        return charset;
    }

    @Override
    public void setcharset(final SOSOptionString p_charset) {
        charset = p_charset;
    }

    @JSOptionDefinition(name = "content_type", description = "title content_type of", key = "content_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString content_type = new SOSOptionString(this, conClassName + ".content_type", // HashMap-Key
            "title content_type of", // Titel
            "text/plain", // InitValue
            "text/plain", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getcontent_type() {
        return content_type;
    }

    @Override
    public void setcontent_type(final SOSOptionString p_content_type) {
        content_type = p_content_type;
    }

    @JSOptionDefinition(name = "encoding", description = "title encoding of the", key = "encoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString encoding = new SOSOptionString(this, conClassName + ".encoding", // HashMap-Key
            "title encoding of the", // Titel
            "7bit", // InitValue
            "7bit", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getencoding() {
        return encoding;
    }

    @Override
    public void setencoding(final SOSOptionString p_encoding) {
        encoding = p_encoding;
    }

    /** \var from : title mail sender mail sender */
    @JSOptionDefinition(name = "from", description = "title mail sender", key = "from", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress from = new SOSOptionMailAdress(this, conClassName + ".from", // HashMap-Key
            "title mail sender", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );
    public SOSOptionMailAdress MailFrom = (SOSOptionMailAdress) from.SetAlias("mail_from");

    @Override
    public SOSOptionMailAdress getfrom() {
        return from;
    }

    @Override
    public void setfrom(final SOSOptionMailAdress p_from) {
        from = p_from;
    }

    /** \var from_name : title name of the send name of the sender */
    @JSOptionDefinition(name = "from_name", description = "title name of the send", key = "from_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString from_name = new SOSOptionString(this, conClassName + ".from_name", // HashMap-Key
            "title name of the send", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getfrom_name() {
        return from_name;
    }

    @Override
    public void setfrom_name(final SOSOptionString p_from_name) {
        from_name = p_from_name;
    }

    /** \var host : title mail server host mail server host */
    @JSOptionDefinition(name = "host", description = "title mail server host", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, conClassName + ".host", // HashMap-Key
            "title mail server host", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );
    public SOSOptionHostName SMTPHost = (SOSOptionHostName) host.SetAlias("mail_smtp", "smtp_host");

    @Override
    public SOSOptionHostName gethost() {
        return host;
    }

    @Override
    public void sethost(final SOSOptionHostName p_host) {
        host = p_host;
    }

    /** \var port : title mail server port mail server port */
    @JSOptionDefinition(name = "port", description = "title mail server port", key = "port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, conClassName + ".port", // HashMap-Key
            "title mail server port", // Titel
            "25", // InitValue
            "25", // DefaultValue
            false // isMandatory
    );

    public SOSOptionPortNumber MailPort = (SOSOptionPortNumber) port.SetAlias("mail_port", "smtp_port");

    @Override
    public SOSOptionPortNumber getport() {
        return port;
    }

    @Override
    public void setport(final SOSOptionPortNumber p_port) {
        port = p_port;
    }

    @JSOptionDefinition(name = "queue_directory", description = "title Mail queue direc", key = "queue_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queue_directory = new SOSOptionString(this, conClassName + ".queue_directory", // HashMap-Key
            "title Mail queue direc", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString MailQueueDir = (SOSOptionString) queue_directory.SetAlias("mail_queue_dir");

    @Override
    public SOSOptionString getqueue_directory() {
        return queue_directory;
    }

    @Override
    public void setqueue_directory(final SOSOptionString p_queue_directory) {
        queue_directory = p_queue_directory;
    }

    @JSOptionDefinition(name = "reply_to", description = "title reply address", key = "reply_to", type = "SOSOptionMailAdress", mandatory = false)
    public SOSOptionMailAdress reply_to = new SOSOptionMailAdress(this, conClassName + ".reply_to", // HashMap-Key
            "title reply address", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionMailAdress getreply_to() {
        return reply_to;
    }

    @Override
    public void setreply_to(final SOSOptionMailAdress p_reply_to) {
        reply_to = p_reply_to;
    }

    @JSOptionDefinition(name = "smtp_password", description = "title smtp user passwo", key = "smtp_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword smtp_password = new SOSOptionPassword(this, conClassName + ".smtp_password", // HashMap-Key
            "title smtp user passwo", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionPassword getsmtp_password() {
        return smtp_password;
    }

    @Override
    public void setsmtp_password(final SOSOptionPassword p_smtp_password) {
        smtp_password = p_smtp_password;
    }

    @JSOptionDefinition(name = "smtp_user", description = "title smtp username", key = "smtp_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionString smtp_user = new SOSOptionString(this, conClassName + ".smtp_user", // HashMap-Key
            "title smtp username", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @Override
    public SOSOptionString getsmtp_user() {
        return smtp_user;
    }

    @Override
    public void setsmtp_user(final SOSOptionString p_smtp_user) {
        smtp_user = p_smtp_user;
    }

    @JSOptionDefinition(name = "subject", description = "title Mail Subject", key = "subject", type = "SOSOptionString", mandatory = false)
    public SOSOptionMailSubject subject = new SOSOptionMailSubject(this, conClassName + ".subject", // HashMap-Key
            "title Mail Subject", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );
    public SOSOptionMailSubject FileNotificationSubject = (SOSOptionMailSubject) subject.SetAlias("file_notification_subject");

    @Override
    public SOSOptionMailSubject getsubject() {
        return subject;
    }

    @Override
    public void setsubject(final SOSOptionMailSubject p_subject) {
        subject = p_subject;
    }

    @JSOptionDefinition(name = "to", description = "title mail recipient(s", key = "to", type = "SOSOptionMailAdress", mandatory = true)
    public SOSOptionMailAdress to = new SOSOptionMailAdress(this, conClassName + ".to", // HashMap-Key
            "title mail recipient(s", // Titel
            "", // InitValue
            "", // DefaultValue
            true // isMandatory
    );
    public SOSOptionMailAdress FileNotificationTo = (SOSOptionMailAdress) to.SetAlias("file_notification_to");

    @Override
    public SOSOptionMailAdress getto() {
        return to;
    }

    @Override
    public void setto(final SOSOptionMailAdress p_to) {
        to = p_to;
    }

    public SOSSmtpMailOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public SOSSmtpMailOptionsSuperClass

    public SOSSmtpMailOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public SOSSmtpMailOptionsSuperClass

    //

    public SOSSmtpMailOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public SOSSmtpMailOptionsSuperClass (HashMap JSSettings)

    private String getAllOptionsAsString() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAllOptionsAsString";
        String strT = conClassName + "\n";
        final StringBuffer strBuffer = new StringBuffer();

        strT += this.toString(); // fix
        return strT;
    } // private String getAllOptionsAsString ()

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    @Override
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class SOSSmtpMailOptionsSuperClass