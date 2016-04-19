package com.sos.JSHelper.interfaces;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionMailAdress;
import com.sos.JSHelper.Options.SOSOptionMailSubject;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;

public interface ISOSSmtpMailOptions {

    public abstract SOSOptionString getattachment();

    public abstract void setattachment(SOSOptionString p_attachment);

    public abstract SOSOptionString getattachment_charset();

    public abstract void setattachment_charset(SOSOptionString p_attachment_charset);

    public abstract SOSOptionString getattachment_content_type();

    public abstract void setattachment_content_type(SOSOptionString p_attachment_content_type);

    public abstract SOSOptionString getattachment_encoding();

    public abstract void setattachment_encoding(SOSOptionString p_attachment_encoding);

    public abstract SOSOptionMailAdress getbcc();

    public abstract void setbcc(SOSOptionMailAdress p_bcc);

    public abstract SOSOptionString getbody();

    public abstract void setbody(SOSOptionString p_body);

    public abstract SOSOptionMailAdress getcc();

    public abstract void setcc(SOSOptionMailAdress p_cc);

    public abstract SOSOptionString getcharset();

    public abstract void setcharset(SOSOptionString p_charset);

    public abstract SOSOptionString getcontent_type();

    public abstract void setcontent_type(SOSOptionString p_content_type);

    public abstract SOSOptionString getencoding();

    public abstract void setencoding(SOSOptionString p_encoding);

    public abstract SOSOptionMailAdress getfrom();

    public abstract void setfrom(SOSOptionMailAdress p_from);

    public abstract SOSOptionString getfrom_name();

    public abstract void setfrom_name(SOSOptionString p_from_name);

    public abstract SOSOptionHostName gethost();

    public abstract void sethost(SOSOptionHostName p_host);

    public abstract SOSOptionPortNumber getport();

    public abstract void setport(SOSOptionPortNumber p_port);

    public abstract SOSOptionString getqueue_directory();

    public abstract void setqueue_directory(SOSOptionString p_queue_directory);

    public abstract SOSOptionMailAdress getreply_to();

    public abstract void setreply_to(SOSOptionMailAdress p_reply_to);

    public abstract SOSOptionPassword getsmtp_password();

    public abstract void setsmtp_password(SOSOptionPassword p_smtp_password);

    public abstract SOSOptionString getsmtp_user();

    public abstract void setsmtp_user(SOSOptionString p_smtp_user);

    public abstract SOSOptionString getsecurity_protocol();

    public abstract void setsecurity_protocol(SOSOptionString p_security_protocol);

    public abstract SOSOptionMailSubject getsubject();

    public abstract void setsubject(SOSOptionMailSubject p_subject);

    public abstract SOSOptionMailAdress getto();

    public abstract void setto(SOSOptionMailAdress p_to);

    public abstract void setAllOptions(HashMap<String, String> pobjJSSettings) throws Exception;

    public abstract void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception;

    public abstract void CommandLineArgs(String[] pstrArgs) throws Exception;
}