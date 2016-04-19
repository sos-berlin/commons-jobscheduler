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

    public SOSOptionPassword getmailPassword() {
        return mailPassword;
    }

    public void setmailPassword(final SOSOptionPassword p_mailPassword) {
        mailPassword = p_mailPassword;
    }

    @JSOptionDefinition(name = "mail_action", description = "", key = "mail_action", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_action = new SOSOptionString(this, CLASSNAME + ".mail_action", "", "", "", false);

    public SOSOptionString getmail_action() {
        return mail_action;
    }

    public void setmail_action(final SOSOptionString p_mail_action) {
        mail_action = p_mail_action;
    }

    @JSOptionDefinition(name = "mail_body_pattern", description = "", key = "mail_body_pattern", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp mail_body_pattern = new SOSOptionRegExp(this, CLASSNAME + ".mail_body_pattern", "", "", "", false);

    public SOSOptionRegExp getmail_body_pattern() {
        return mail_body_pattern;
    }

    public void setmail_body_pattern(final SOSOptionRegExp p_mail_body_pattern) {
        mail_body_pattern = p_mail_body_pattern;
    }

    @JSOptionDefinition(name = "mail_dump_dir", description = "", key = "mail_dump_dir", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName mail_dump_dir = new SOSOptionFolderName(this, CLASSNAME + ".mail_dump_dir", "", "", "", false);

    public SOSOptionFolderName getmail_dump_dir() {
        return mail_dump_dir;
    }

    public void setmail_dump_dir(final SOSOptionFolderName p_mail_dump_dir) {
        mail_dump_dir = p_mail_dump_dir;
    }

    @JSOptionDefinition(name = "mail_host", description = "", key = "mail_host", type = "SOSOptionHostName", mandatory = true)
    public SOSOptionHostName mail_host = new SOSOptionHostName(this, CLASSNAME + ".mail_host", "", "", "", true);

    public SOSOptionHostName getmail_host() {
        return mail_host;
    }

    public void setmail_host(final SOSOptionHostName p_mail_host) {
        mail_host = p_mail_host;
    }

    @JSOptionDefinition(name = "mail_jobchain", description = "", key = "mail_jobchain", type = "JSJobChainName", mandatory = false)
    public JSJobChainName mail_jobchain = new JSJobChainName(this, CLASSNAME + ".mail_jobchain", "", "", "", false);

    public JSJobChainName getmail_jobchain() {
        return mail_jobchain;
    }

    public void setmail_jobchain(final JSJobChainName p_mail_jobchain) {
        mail_jobchain = p_mail_jobchain;
    }

    @JSOptionDefinition(name = "mail_message_folder", description = "", key = "mail_message_folder", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_message_folder = new SOSOptionString(this, CLASSNAME + ".mail_message_folder", "", "INBOX", "INBOX", false);

    public SOSOptionString getmail_message_folder() {
        return mail_message_folder;
    }

    public void setmail_message_folder(final SOSOptionString p_mail_message_folder) {
        mail_message_folder = p_mail_message_folder;
    }

    @JSOptionDefinition(name = "mail_order_id", description = "", key = "mail_order_id", type = "JSOrderId", mandatory = false)
    public JSOrderId mail_order_id = new JSOrderId(this, CLASSNAME + ".mail_order_id", "", "", "", false);

    public JSOrderId getmail_order_id() {
        return mail_order_id;
    }

    public void setmail_order_id(final JSOrderId p_mail_order_id) {
        mail_order_id = p_mail_order_id;
    }

    @JSOptionDefinition(name = "mail_order_state", description = "", key = "mail_order_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode mail_order_state = new SOSOptionJobChainNode(this, CLASSNAME + ".mail_order_state", "", "", "", false);

    public SOSOptionJobChainNode getmail_order_state() {
        return mail_order_state;
    }

    public void setmail_order_state(final SOSOptionJobChainNode p_mail_order_state) {
        mail_order_state = p_mail_order_state;
    }

    @JSOptionDefinition(name = "mail_order_title", description = "", key = "mail_order_title", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_order_title = new SOSOptionString(this, CLASSNAME + ".mail_order_title", "", "", "", false);

    public SOSOptionString getmail_order_title() {
        return mail_order_title;
    }

    public void setmail_order_title(final SOSOptionString p_mail_order_title) {
        mail_order_title = p_mail_order_title;
    }

    @JSOptionDefinition(name = "mail_port", description = "", key = "mail_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber mail_port = new SOSOptionPortNumber(this, CLASSNAME + ".mail_port", "", "110", "110", false);

    public SOSOptionPortNumber getmail_port() {
        return mail_port;
    }

    public void setmail_port(final SOSOptionPortNumber p_mail_port) {
        mail_port = p_mail_port;
    }

    @JSOptionDefinition(name = "mail_scheduler_host", description = "", key = "mail_scheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName mail_scheduler_host = new SOSOptionHostName(this, CLASSNAME + ".mail_scheduler_host", "", "", "", false);

    public SOSOptionHostName getmail_scheduler_host() {
        return mail_scheduler_host;
    }

    public void setmail_scheduler_host(final SOSOptionHostName p_mail_scheduler_host) {
        mail_scheduler_host = p_mail_scheduler_host;
    }

    @JSOptionDefinition(name = "mail_scheduler_port", description = "", key = "mail_scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber mail_scheduler_port = new SOSOptionPortNumber(this, CLASSNAME + ".mail_scheduler_port", "", "0", "0", false);

    public SOSOptionPortNumber getmail_scheduler_port() {
        return mail_scheduler_port;
    }

    public void setmail_scheduler_port(final SOSOptionPortNumber p_mail_scheduler_port) {
        mail_scheduler_port = p_mail_scheduler_port;
    }

    @JSOptionDefinition(name = "mail_server_timeout", description = "", key = "mail_server_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger mail_server_timeout = new SOSOptionInteger(this, CLASSNAME + ".mail_server_timeout", "", "0", "0", false);

    public SOSOptionInteger getmail_server_timeout() {
        return mail_server_timeout;
    }

    public void setmail_server_timeout(final SOSOptionInteger p_mail_server_timeout) {
        mail_server_timeout = p_mail_server_timeout;
    }

    @JSOptionDefinition(name = "mail_server_type", description = "", key = "mail_server_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_server_type = new SOSOptionString(this, CLASSNAME + ".mail_server_type", "", "POP3", "POP3", false);

    public SOSOptionString getmail_server_type() {
        return mail_server_type;
    }

    public void setmail_server_type(final SOSOptionString p_mail_server_type) {
        mail_server_type = p_mail_server_type;
    }

    @JSOptionDefinition(name = "mail_set_seen", description = "", key = "mail_set_seen", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_set_seen = new SOSOptionBoolean(this, CLASSNAME + ".mail_set_seen", "", "false", "false", false);

    public SOSOptionBoolean getmail_set_seen() {
        return mail_set_seen;
    }

    public void setmail_set_seen(final SOSOptionBoolean p_mail_set_seen) {
        mail_set_seen = p_mail_set_seen;
    }

    @JSOptionDefinition(name = "mail_subject_filter", description = "", key = "mail_subject_filter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_subject_filter = new SOSOptionString(this, CLASSNAME + ".mail_subject_filter", "", "", "", false);

    public SOSOptionString getmail_subject_filter() {
        return mail_subject_filter;
    }

    public void setmail_subject_filter(final SOSOptionString p_mail_subject_filter) {
        mail_subject_filter = p_mail_subject_filter;
    }

    @JSOptionDefinition(name = "mail_subject_pattern", description = "", key = "mail_subject_pattern", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp mail_subject_pattern = new SOSOptionRegExp(this, CLASSNAME + ".mail_subject_pattern", "", "", "", false);

    public SOSOptionRegExp getmail_subject_pattern() {
        return mail_subject_pattern;
    }

    public void setmail_subject_pattern(final SOSOptionRegExp p_mail_subject_pattern) {
        mail_subject_pattern = p_mail_subject_pattern;
    }

    @JSOptionDefinition(name = "mail_use_seen", description = "", key = "mail_use_seen", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_use_seen = new SOSOptionBoolean(this, CLASSNAME + ".mail_use_seen", "", "true", "true", false);

    public SOSOptionBoolean getmail_use_seen() {
        return mail_use_seen;
    }

    public void setmail_use_seen(final SOSOptionBoolean p_mail_use_seen) {
        mail_use_seen = p_mail_use_seen;
    }

    @JSOptionDefinition(name = "mail_user", description = "", key = "mail_user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName mail_user = new SOSOptionUserName(this, CLASSNAME + ".mail_user", "", "", "", true);

    public SOSOptionUserName getmail_user() {
        return mail_user;
    }

    public void setmail_user(final SOSOptionUserName p_mail_user) {
        mail_user = p_mail_user;
    }

    @JSOptionDefinition(name = "min_file_age", description = "Objects, which are younger than min_file_age are not processed", key = "min_file_age",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTimeHorizon min_file_age = new SOSOptionTimeHorizon(this, CLASSNAME + ".min_file_age", 
            "Objects, which are younger than min_file_age are not processed", "", "", false);
    public SOSOptionTimeHorizon MinAge = (SOSOptionTimeHorizon) min_file_age.SetAlias("min_age");

    public String getmin_file_age() {
        return min_file_age.Value();
    }

    public SOSMailProcessInboxOptionsSuperClass setmin_file_age(final String pstrValue) {
        min_file_age.Value(pstrValue);
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
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}