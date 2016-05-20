package sos.mail.options;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.JSOrderId;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJobChainNode;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTimeHorizon;
import com.sos.JSHelper.Options.SOSOptionUserName;

@JSOptionClass(name = "Mail2ActionOptionsSuperClass", description = "Mail2ActionOptionsSuperClass")
public class SOSMailProcessInboxOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 3232286541348142006L;
    private static final String CLASSNAME = "SOSMailProcessInboxOptionsSuperClass";

    @JSOptionDefinition(name = "mailPassword", description = "", key = "mailPassword", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword mailPassword = new SOSOptionPassword(this, CLASSNAME + ".mailPassword", "", "", "", false);

    public SOSOptionPassword getMailPassword() {
        return mailPassword;
    }

    public void setMailPassword(final SOSOptionPassword pMailPassword) {
        mailPassword = pMailPassword;
    }

    @JSOptionDefinition(name = "mail_action", description = "", key = "mail_action", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailAction = new SOSOptionString(this, CLASSNAME + ".mail_action", "", "", "", false);

    public SOSOptionString getMailAction() {
        return mailAction;
    }

    public void setMailAction(final SOSOptionString pMailAction) {
        mailAction = pMailAction;
    }

    @JSOptionDefinition(name = "mail_body_pattern", description = "", key = "mail_body_pattern", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp mailBodyPattern = new SOSOptionRegExp(this, CLASSNAME + ".mail_body_pattern", "", "", "", false);

    public SOSOptionRegExp getMailBodyPattern() {
        return mailBodyPattern;
    }

    public void setMailBodyPattern(final SOSOptionRegExp pMailBodyPattern) {
        mailBodyPattern = pMailBodyPattern;
    }

    @JSOptionDefinition(name = "mail_dump_dir", description = "", key = "mail_dump_dir", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName mailDumpDir = new SOSOptionFolderName(this, CLASSNAME + ".mail_dump_dir", "", "", "", false);

    public SOSOptionFolderName getMailDumpDir() {
        return mailDumpDir;
    }

    public void setMailDumpDir(final SOSOptionFolderName pMailDumpDir) {
        mailDumpDir = pMailDumpDir;
    }

    @JSOptionDefinition(name = "mail_host", description = "", key = "mail_host", type = "SOSOptionHostName", mandatory = true)
    public SOSOptionHostName mailHost = new SOSOptionHostName(this, CLASSNAME + ".mail_host", "", "", "", true);

    public SOSOptionHostName getMailHost() {
        return mailHost;
    }

    public void setMailHost(final SOSOptionHostName pMailHost) {
        mailHost = pMailHost;
    }

    @JSOptionDefinition(name = "mail_jobchain", description = "", key = "mail_jobchain", type = "JSJobChainName", mandatory = false)
    public JSJobChainName mailJobchain = new JSJobChainName(this, CLASSNAME + ".mail_jobchain", "", "", "", false);

    public JSJobChainName getMailJobchain() {
        return mailJobchain;
    }

    public void setMailJobchain(final JSJobChainName pMailJobchain) {
        mailJobchain = pMailJobchain;
    }

    @JSOptionDefinition(name = "mail_message_folder", description = "", key = "mail_message_folder", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailMessageFolder = new SOSOptionString(this, CLASSNAME + ".mail_message_folder", "", "INBOX", "INBOX", false);

    public SOSOptionString getMailMessageFolder() {
        return mailMessageFolder;
    }

    public void setMailMessageFolder(final SOSOptionString pMailMessageFolder) {
        mailMessageFolder = pMailMessageFolder;
    }

    @JSOptionDefinition(name = "mail_order_id", description = "", key = "mail_order_id", type = "JSOrderId", mandatory = false)
    public JSOrderId mailOrderId = new JSOrderId(this, CLASSNAME + ".mail_order_id", "", "", "", false);

    public JSOrderId getMailOrderId() {
        return mailOrderId;
    }

    public void setMailOrderId(final JSOrderId pMailOrderId) {
        mailOrderId = pMailOrderId;
    }

    @JSOptionDefinition(name = "mail_order_state", description = "", key = "mail_order_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode mailOrderState = new SOSOptionJobChainNode(this, CLASSNAME + ".mail_order_state", "", "", "", false);

    public SOSOptionJobChainNode getMailOrderState() {
        return mailOrderState;
    }

    public void setMailOrderState(final SOSOptionJobChainNode pMailOrderState) {
        mailOrderState = pMailOrderState;
    }

    @JSOptionDefinition(name = "mail_order_title", description = "", key = "mail_order_title", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailOrderTitle = new SOSOptionString(this, CLASSNAME + ".mail_order_title", "", "", "", false);

    public SOSOptionString getMailOrderTitle() {
        return mailOrderTitle;
    }

    public void setMailOrderTitle(final SOSOptionString pMailOrderTitle) {
        mailOrderTitle = pMailOrderTitle;
    }

    @JSOptionDefinition(name = "mail_port", description = "", key = "mail_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber mailPort = new SOSOptionPortNumber(this, CLASSNAME + ".mail_port", "", "110", "110", false);

    public SOSOptionPortNumber getMailPort() {
        return mailPort;
    }

    public void setMailPort(final SOSOptionPortNumber pMailPort) {
        mailPort = pMailPort;
    }

    @JSOptionDefinition(name = "mail_scheduler_host", description = "", key = "mail_scheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName mailSchedulerHost = new SOSOptionHostName(this, CLASSNAME + ".mail_scheduler_host", "", "", "", false);

    public SOSOptionHostName getMailSchedulerHost() {
        return mailSchedulerHost;
    }

    public void setMailSchedulerHost(final SOSOptionHostName pMailSchedulerHost) {
        mailSchedulerHost = pMailSchedulerHost;
    }

    @JSOptionDefinition(name = "mail_scheduler_port", description = "", key = "mail_scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber mailSchedulerPort = new SOSOptionPortNumber(this, CLASSNAME + ".mail_scheduler_port", "", "0", "0", false);

    public SOSOptionPortNumber getMailSchedulerPort() {
        return mailSchedulerPort;
    }

    public void setMailSchedulerPort(final SOSOptionPortNumber pMailSchedulerPort) {
        mailSchedulerPort = pMailSchedulerPort;
    }

    @JSOptionDefinition(name = "max_mails_to_process", description = "", key = "max_mails_to_process", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger maxMailsToProcess = new SOSOptionInteger(this, CLASSNAME + ".max_mails_to_process", "", "0", "1000", false);

    public SOSOptionInteger getMaxMailsToProcess() {
        return maxMailsToProcess;
    }

    public void setMaxMailsToProcess(final SOSOptionInteger pMaxMailsToProcess) {
        maxMailsToProcess = pMaxMailsToProcess;
    }

    @JSOptionDefinition(name = "mail_server_timeout", description = "", key = "mail_server_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger mailServerTimeout = new SOSOptionInteger(this, CLASSNAME + ".mail_server_timeout", "", "0", "0", false);

    public SOSOptionInteger getMailServerTimeout() {
        return mailServerTimeout;
    }

    public void setMailServerTimeout(final SOSOptionInteger pMailServerTimeout) {
        mailServerTimeout = pMailServerTimeout;
    }

    @JSOptionDefinition(name = "mail_server_type", description = "", key = "mail_server_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailServerType = new SOSOptionString(this, CLASSNAME + ".mail_server_type", "", "POP3", "POP3", false);

    public SOSOptionString getMailServerType() {
        return mailServerType;
    }

    public void setMailServerType(final SOSOptionString pMailServerType) {
        mailServerType = pMailServerType;
    }

    @JSOptionDefinition(name = "mail_set_seen", description = "", key = "mail_set_seen", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailSetSeen = new SOSOptionBoolean(this, CLASSNAME + ".mail_set_seen", "", "false", "false", false);

    public SOSOptionBoolean getMailSetSeen() {
        return mailSetSeen;
    }

    public void setMailSetSeen(final SOSOptionBoolean pMailSetSeen) {
        mailSetSeen = pMailSetSeen;
    }

    @JSOptionDefinition(name = "mail_subject_filter", description = "", key = "mail_subject_filter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailSubjectFilter = new SOSOptionString(this, CLASSNAME + ".mail_subject_filter", "", "", "", false);

    public SOSOptionString getMailSubjectFilter() {
        return mailSubjectFilter;
    }

    public void setMailSubjectFilter(final SOSOptionString pMailSubjectFilter) {
        mailSubjectFilter = pMailSubjectFilter;
    }

    @JSOptionDefinition(name = "mail_subject_pattern", description = "", key = "mail_subject_pattern", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp mailSubjectPattern = new SOSOptionRegExp(this, CLASSNAME + ".mail_subject_pattern", "", "", "", false);

    public SOSOptionRegExp getMailSubjectPattern() {
        return mailSubjectPattern;
    }

    public void setMailSubjectPattern(final SOSOptionRegExp pMailSubjectPattern) {
        mailSubjectPattern = pMailSubjectPattern;
    }

    @JSOptionDefinition(name = "mail_use_seen", description = "", key = "mail_use_seen", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailUseSeen = new SOSOptionBoolean(this, CLASSNAME + ".mail_use_seen", "", "true", "true", false);

    public SOSOptionBoolean getMailUseSeen() {
        return mailUseSeen;
    }

    public void setMailUseSeen(final SOSOptionBoolean pMailUseSeen) {
        mailUseSeen = pMailUseSeen;
    }

    @JSOptionDefinition(name = "mail_user", description = "", key = "mail_user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName mailUser = new SOSOptionUserName(this, CLASSNAME + ".mail_user", "", "", "", true);

    public SOSOptionUserName getMailUser() {
        return mailUser;
    }

    public void setMailUser(final SOSOptionUserName pMailUser) {
        mailUser = pMailUser;
    }

    @JSOptionDefinition(name = "min_file_age", description = "Objects, which are younger than min_file_age are not processed", key = "min_file_age",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTimeHorizon minFileAge = new SOSOptionTimeHorizon(this, CLASSNAME + ".min_file_age", 
            "Objects, which are younger than min_file_age are not processed", "", "", false);
    public SOSOptionTimeHorizon minAge = (SOSOptionTimeHorizon) minFileAge.setAlias("min_age");

    public String getMinFileAge() {
        return minFileAge.getValue();
    }

    public SOSMailProcessInboxOptionsSuperClass setMinFileAge(final String pstrValue) {
        minFileAge.setValue(pstrValue);
        return this;
    }

    public SOSMailProcessInboxOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSMailProcessInboxOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSMailProcessInboxOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
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