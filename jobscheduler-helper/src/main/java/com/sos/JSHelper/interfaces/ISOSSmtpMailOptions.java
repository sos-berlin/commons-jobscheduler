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

    public abstract SOSOptionString getAttachment();

    public abstract void setAttachment(SOSOptionString pAttachment);

    public abstract SOSOptionString getAttachmentCharset();

    public abstract void setAttachmentCharset(SOSOptionString pAttachmentCharset);

    public abstract SOSOptionString getAttachmentContentType();

    public abstract void setAttachmentContentType(SOSOptionString pAttachmentContentType);

    public abstract SOSOptionString getAttachmentEncoding();

    public abstract void setAttachmentEncoding(SOSOptionString pAttachmentEncoding);

    public abstract SOSOptionMailAdress getBcc();

    public abstract void setBcc(SOSOptionMailAdress pBcc);

    public abstract SOSOptionString getBody();

    public abstract void setBody(SOSOptionString pBody);

    public abstract SOSOptionMailAdress getCc();

    public abstract void setCc(SOSOptionMailAdress pCc);

    public abstract SOSOptionString getCharset();

    public abstract void setCharset(SOSOptionString pCharset);

    public abstract SOSOptionString getContentType();

    public abstract void setContentType(SOSOptionString pContentType);

    public abstract SOSOptionString getEncoding();

    public abstract void setEncoding(SOSOptionString pEncoding);

    public abstract SOSOptionMailAdress getFrom();

    public abstract void setFrom(SOSOptionMailAdress pFrom);

    public abstract SOSOptionString getFromName();

    public abstract void setFromName(SOSOptionString pFromName);

    public abstract SOSOptionHostName getHost();

    public abstract void setHost(SOSOptionHostName pHost);

    public abstract SOSOptionPortNumber getPort();

    public abstract void setPort(SOSOptionPortNumber pPort);

    public abstract SOSOptionString getQueueDirectory();

    public abstract void setQueueDirectory(SOSOptionString pQueueDirectory);

    public abstract SOSOptionMailAdress getReplyTo();

    public abstract void setReplyTo(SOSOptionMailAdress pReplyTo);

    public abstract SOSOptionPassword getSmtpPassword();

    public abstract void setSmtpPassword(SOSOptionPassword pSmtpPassword);

    public abstract SOSOptionString getSmtpUser();

    public abstract void setSmtpUser(SOSOptionString pSmtpUser);

    public abstract SOSOptionString getSecurityProtocol();

    public abstract void setSecurityProtocol(SOSOptionString pSecurityProtocol);

    public abstract SOSOptionMailSubject getSubject();

    public abstract void setSubject(SOSOptionMailSubject pSubject);

    public abstract SOSOptionMailAdress getTo();

    public abstract void setTo(SOSOptionMailAdress pTo);

    public abstract void setAllOptions(HashMap<String, String> pobjJSSettings) throws Exception;

    public abstract void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception;

    public abstract void commandLineArgs(String[] pstrArgs) throws Exception;
    
}