package sos.mail.options;

import java.util.HashMap;

import org.apache.log4j.Logger;

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

/** \class Mail2ActionOptionsSuperClass - Pro Email eine oder mehrere Aktionen
 * ausführen
 *
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see Mail2ActionOptions. The "real" Option class
 * will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\oh\AppData\Local\Temp\scheduler_editor-2042452986889562531.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\ProgramData\sos-berlin.com\jobscheduler\
 * scheduler.oh\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl
 * from http://www.sos-berlin.com at 20121019122956 \endverbatim \section
 * OptionsTable Tabelle der vorhandenen Optionen
 *
 * Tabelle mit allen Optionen
 *
 * MethodName Title Setting Description IsMandatory DataType InitialValue
 * TestValue
 *
 *
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		Mail2ActionOptionsSuperClass.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim */
@JSOptionClass(name = "Mail2ActionOptionsSuperClass", description = "Mail2ActionOptionsSuperClass")
public class SOSMailProcessInboxOptionsSuperClass extends JSOptionsClass {

    /**
	 *
	 */
    private static final long serialVersionUID = 3232286541348142006L;
    private final String conClassName = "SOSMailProcessInboxOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SOSMailProcessInboxOptionsSuperClass.class);

    /** \var mailPassword : The password for login at the mail server. */
    @JSOptionDefinition(name = "mailPassword", description = "", key = "mailPassword", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword mailPassword = new SOSOptionPassword(this, conClassName + ".mailPassword", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmailPassword :
     *
     * \details The password for login at the mail server.
     *
     * \return */
    public SOSOptionPassword getmailPassword() {
        return mailPassword;
    }

    /** \brief setmailPassword :
     *
     * \details The password for login at the mail server.
     *
     * @param mailPassword : */
    public void setmailPassword(final SOSOptionPassword p_mailPassword) {
        mailPassword = p_mailPassword;
    }

    /** \var mail_action : You can use these commands: dump: write the content of
     * the email in file located in mail_dump_dir order: For each mail a order
     * will be generated command: The body of the email will be used as a
     * command. delete: The email will be deleted. You can concatenate several
     * commands by using a comma. They will be handles one ofter the other. */
    @JSOptionDefinition(name = "mail_action", description = "", key = "mail_action", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_action = new SOSOptionString(this, conClassName + ".mail_action", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_action :
     *
     * \details You can use these commands: dump: write the content of the email
     * in file located in mail_dump_dir order: For each mail a order will be
     * generated command: The body of the email will be used as a command.
     * delete: The email will be deleted. You can concatenate several commands
     * by using a comma. They will be handles one ofter the other.
     *
     * \return */
    public SOSOptionString getmail_action() {
        return mail_action;
    }

    /** \brief setmail_action :
     *
     * \details You can use these commands: dump: write the content of the email
     * in file located in mail_dump_dir order: For each mail a order will be
     * generated command: The body of the email will be used as a command.
     * delete: The email will be deleted. You can concatenate several commands
     * by using a comma. They will be handles one ofter the other.
     *
     * @param mail_action : */
    public void setmail_action(final SOSOptionString p_mail_action) {
        mail_action = p_mail_action;
    }

    /** \var mail_body_pattern : You can specify a regular expression to get only
     * mails matching this filter in the body. */
    @JSOptionDefinition(name = "mail_body_pattern", description = "", key = "mail_body_pattern", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp mail_body_pattern = new SOSOptionRegExp(this, conClassName + ".mail_body_pattern", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_body_pattern :
     *
     * \details You can specify a regular expression to get only mails matching
     * this filter in the body.
     *
     * \return */
    public SOSOptionRegExp getmail_body_pattern() {
        return mail_body_pattern;
    }

    /** \brief setmail_body_pattern :
     *
     * \details You can specify a regular expression to get only mails matching
     * this filter in the body.
     *
     * @param mail_body_pattern : */
    public void setmail_body_pattern(final SOSOptionRegExp p_mail_body_pattern) {
        mail_body_pattern = p_mail_body_pattern;
    }

    /** \var mail_dump_dir : */
    @JSOptionDefinition(name = "mail_dump_dir", description = "", key = "mail_dump_dir", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName mail_dump_dir = new SOSOptionFolderName(this, conClassName + ".mail_dump_dir", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_dump_dir :
     *
     * \details
     *
     *
     * \return */
    public SOSOptionFolderName getmail_dump_dir() {
        return mail_dump_dir;
    }

    /** \brief setmail_dump_dir :
     *
     * \details
     *
     *
     * @param mail_dump_dir : */
    public void setmail_dump_dir(final SOSOptionFolderName p_mail_dump_dir) {
        mail_dump_dir = p_mail_dump_dir;
    }

    /** \var mail_host : The host name of the mail server. */
    @JSOptionDefinition(name = "mail_host", description = "", key = "mail_host", type = "SOSOptionHostName", mandatory = true)
    public SOSOptionHostName mail_host = new SOSOptionHostName(this, conClassName + ".mail_host", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    /** \brief getmail_host :
     *
     * \details The host name of the mail server.
     *
     * \return */
    public SOSOptionHostName getmail_host() {
        return mail_host;
    }

    /** \brief setmail_host :
     *
     * \details The host name of the mail server.
     *
     * @param mail_host : */
    public void setmail_host(final SOSOptionHostName p_mail_host) {
        mail_host = p_mail_host;
    }

    /** \var mail_jobchain : If the command is"order" an order will be generated
     * for the job chain defined in mail_jobchain. The order gets all parameters
     * of the job. Additionally the following parameters will be set.
     * "mail_from": Sender of mail "mail_from_name" "mail_message_id": message
     * id of mail "mail_subject": The subject "mail_body": The body
     * "mail_send_at": Date of sending the mail */
    @JSOptionDefinition(name = "mail_jobchain", description = "", key = "mail_jobchain", type = "JSJobChainName", mandatory = false)
    public JSJobChainName mail_jobchain = new JSJobChainName(this, conClassName + ".mail_jobchain", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_jobchain :
     *
     * \details If the command is"order" an order will be generated for the job
     * chain defined in mail_jobchain. The order gets all parameters of the job.
     * Additionally the following parameters will be set. "mail_from": Sender of
     * mail "mail_from_name" "mail_message_id": message id of mail
     * "mail_subject": The subject "mail_body": The body "mail_send_at": Date of
     * sending the mail
     *
     * \return */
    public JSJobChainName getmail_jobchain() {
        return mail_jobchain;
    }

    /** \brief setmail_jobchain :
     *
     * \details If the command is"order" an order will be generated for the job
     * chain defined in mail_jobchain. The order gets all parameters of the job.
     * Additionally the following parameters will be set. "mail_from": Sender of
     * mail "mail_from_name" "mail_message_id": message id of mail
     * "mail_subject": The subject "mail_body": The body "mail_send_at": Date of
     * sending the mail
     *
     * @param mail_jobchain : */
    public void setmail_jobchain(final JSJobChainName p_mail_jobchain) {
        mail_jobchain = p_mail_jobchain;
    }

    /** \var mail_message_folder : The name of the incoming box, which should be
     * polled. */
    @JSOptionDefinition(name = "mail_message_folder", description = "", key = "mail_message_folder", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_message_folder = new SOSOptionString(this, conClassName + ".mail_message_folder", // HashMap-Key
    "", // Titel
    "INBOX", // InitValue
    "INBOX", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_message_folder :
     *
     * \details The name of the incoming box, which should be polled.
     *
     * \return */
    public SOSOptionString getmail_message_folder() {
        return mail_message_folder;
    }

    /** \brief setmail_message_folder :
     *
     * \details The name of the incoming box, which should be polled.
     *
     * @param mail_message_folder : */
    public void setmail_message_folder(final SOSOptionString p_mail_message_folder) {
        mail_message_folder = p_mail_message_folder;
    }

    /** \var mail_order_id : With the command "order" an order will be generated
     * for the jobchain defined in mail_jobchain. If you want to specify a
     * special order id, you can use this parameter. We recommend to use the
     * automatically generated order id and leave this parameter empty. */
    @JSOptionDefinition(name = "mail_order_id", description = "", key = "mail_order_id", type = "JSOrderId", mandatory = false)
    public JSOrderId mail_order_id = new JSOrderId(this, conClassName + ".mail_order_id", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_order_id :
     *
     * \details With the command "order" an order will be generated for the
     * jobchain defined in mail_jobchain. If you want to specify a special order
     * id, you can use this parameter. We recommend to use the automatically
     * generated order id and leave this parameter empty.
     *
     * \return */
    public JSOrderId getmail_order_id() {
        return mail_order_id;
    }

    /** \brief setmail_order_id :
     *
     * \details With the command "order" an order will be generated for the
     * jobchain defined in mail_jobchain. If you want to specify a special order
     * id, you can use this parameter. We recommend to use the automatically
     * generated order id and leave this parameter empty.
     *
     * @param mail_order_id : */
    public void setmail_order_id(final JSOrderId p_mail_order_id) {
        mail_order_id = p_mail_order_id;
    }

    /** \var mail_order_state : With the command "order" an order will be
     * generated for the jobchain defined in mail_jobchain. If you want to
     * specify a special order state, you can use this parameter. If an order
     * state is specified, the order starts in the job chain with this state. If
     * leave this empty, the order will start at the beginning of the job chain. */
    @JSOptionDefinition(name = "mail_order_state", description = "", key = "mail_order_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode mail_order_state = new SOSOptionJobChainNode(this, conClassName + ".mail_order_state", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_order_state :
     *
     * \details With the command "order" an order will be generated for the
     * jobchain defined in mail_jobchain. If you want to specify a special order
     * state, you can use this parameter. If an order state is specified, the
     * order starts in the job chain with this state. If leave this empty, the
     * order will start at the beginning of the job chain.
     *
     * \return */
    public SOSOptionJobChainNode getmail_order_state() {
        return mail_order_state;
    }

    /** \brief setmail_order_state :
     *
     * \details With the command "order" an order will be generated for the
     * jobchain defined in mail_jobchain. If you want to specify a special order
     * state, you can use this parameter. If an order state is specified, the
     * order starts in the job chain with this state. If leave this empty, the
     * order will start at the beginning of the job chain.
     *
     * @param mail_order_state : */
    public void setmail_order_state(final SOSOptionJobChainNode p_mail_order_state) {
        mail_order_state = p_mail_order_state;
    }

    /** \var mail_order_title : With the command "order" an order will be
     * generated for the jobchain defined in mail_jobchain. If you want to
     * specify a special title, you can use this parameter. You can see the
     * title in the operations gui of Job Scheduler. */
    @JSOptionDefinition(name = "mail_order_title", description = "", key = "mail_order_title", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_order_title = new SOSOptionString(this, conClassName + ".mail_order_title", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_order_title :
     *
     * \details With the command "order" an order will be generated for the
     * jobchain defined in mail_jobchain. If you want to specify a special
     * title, you can use this parameter. You can see the title in the
     * operations gui of Job Scheduler.
     *
     * \return */
    public SOSOptionString getmail_order_title() {
        return mail_order_title;
    }

    /** \brief setmail_order_title :
     *
     * \details With the command "order" an order will be generated for the
     * jobchain defined in mail_jobchain. If you want to specify a special
     * title, you can use this parameter. You can see the title in the
     * operations gui of Job Scheduler.
     *
     * @param mail_order_title : */
    public void setmail_order_title(final SOSOptionString p_mail_order_title) {
        mail_order_title = p_mail_order_title;
    }

    /** \var mail_port : The port of the mail server . */
    @JSOptionDefinition(name = "mail_port", description = "", key = "mail_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber mail_port = new SOSOptionPortNumber(this, conClassName + ".mail_port", // HashMap-Key
    "", // Titel
    "110", // InitValue
    "110", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_port :
     *
     * \details The port of the mail server .
     *
     * \return */
    public SOSOptionPortNumber getmail_port() {
        return mail_port;
    }

    /** \brief setmail_port :
     *
     * \details The port of the mail server .
     *
     * @param mail_port : */
    public void setmail_port(final SOSOptionPortNumber p_mail_port) {
        mail_port = p_mail_port;
    }

    /** \var mail_scheduler_host : Hostname of the Job Scheduler that should
     * execute the command. If empty the actual running Job Scheduler containing
     * this job will be uses. */
    @JSOptionDefinition(name = "mail_scheduler_host", description = "", key = "mail_scheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName mail_scheduler_host = new SOSOptionHostName(this, conClassName + ".mail_scheduler_host", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_scheduler_host :
     *
     * \details Hostname of the Job Scheduler that should execute the command.
     * If empty the actual running Job Scheduler containing this job will be
     * uses.
     *
     * \return */
    public SOSOptionHostName getmail_scheduler_host() {
        return mail_scheduler_host;
    }

    /** \brief setmail_scheduler_host :
     *
     * \details Hostname of the Job Scheduler that should execute the command.
     * If empty the actual running Job Scheduler containing this job will be
     * uses.
     *
     * @param mail_scheduler_host : */
    public void setmail_scheduler_host(final SOSOptionHostName p_mail_scheduler_host) {
        mail_scheduler_host = p_mail_scheduler_host;
    }

    /** \var mail_scheduler_port : Port of the Job Scheduler that should execute
     * the command. If empty the actual running Job Scheduler containing this
     * job will be uses. */
    @JSOptionDefinition(name = "mail_scheduler_port", description = "", key = "mail_scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber mail_scheduler_port = new SOSOptionPortNumber(this, conClassName + ".mail_scheduler_port", // HashMap-Key
    "", // Titel
    "0", // InitValue
    "0", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_scheduler_port :
     *
     * \details Port of the Job Scheduler that should execute the command. If
     * empty the actual running Job Scheduler containing this job will be uses.
     *
     * \return */
    public SOSOptionPortNumber getmail_scheduler_port() {
        return mail_scheduler_port;
    }

    /** \brief setmail_scheduler_port :
     *
     * \details Port of the Job Scheduler that should execute the command. If
     * empty the actual running Job Scheduler containing this job will be uses.
     *
     * @param mail_scheduler_port : */
    public void setmail_scheduler_port(final SOSOptionPortNumber p_mail_scheduler_port) {
        mail_scheduler_port = p_mail_scheduler_port;
    }

    /** \var max_mails_to_process : */
    @JSOptionDefinition(name = "max_mails_to_process", description = "", key = "max_mails_to_process", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger max_mails_to_process = new SOSOptionInteger(this, conClassName + ".max_mails_to_process", // HashMap-Key
    "", // Titel
    "0", // InitValue
    "1000", // DefaultValue
    false // isMandatory
    );

    /** \brief getmax_mails_to_process :
     *
     * \details
     *
     *
     * \return */
    public SOSOptionInteger getmax_mails_to_process() {
        return max_mails_to_process;
    }

    /** \brief setmax_mails_to_process :
     *
     * \details
     *
     *
     * @param mail_server_timeout : */
    public void setmax_mails_to_process(final SOSOptionInteger p_max_mails_to_process) {
        max_mails_to_process = p_max_mails_to_process;
    }

    /** \var mail_server_timeout : */
    @JSOptionDefinition(name = "mail_server_timeout", description = "", key = "mail_server_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger mail_server_timeout = new SOSOptionInteger(this, conClassName + ".mail_server_timeout", // HashMap-Key
    "", // Titel
    "0", // InitValue
    "0", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_server_timeout :
     *
     * \details
     *
     *
     * \return */
    public SOSOptionInteger getmail_server_timeout() {
        return mail_server_timeout;
    }

    /** \brief setmail_server_timeout :
     *
     * \details
     *
     *
     * @param mail_server_timeout : */
    public void setmail_server_timeout(final SOSOptionInteger p_mail_server_timeout) {
        mail_server_timeout = p_mail_server_timeout;
    }

    /** \var mail_server_type : Possible values: -IMAP -POP3 */
    @JSOptionDefinition(name = "mail_server_type", description = "", key = "mail_server_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_server_type = new SOSOptionString(this, conClassName + ".mail_server_type", // HashMap-Key
    "", // Titel
    "POP3", // InitValue
    "POP3", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_server_type :
     *
     * \details Possible values: -IMAP -POP3
     *
     * \return */
    public SOSOptionString getmail_server_type() {
        return mail_server_type;
    }

    /** \brief setmail_server_type :
     *
     * \details Possible values: -IMAP -POP3
     *
     * @param mail_server_type : */
    public void setmail_server_type(final SOSOptionString p_mail_server_type) {
        mail_server_type = p_mail_server_type;
    }

    /** \var mail_set_seen : After processing the email it will be set to
     * "readed" */
    @JSOptionDefinition(name = "mail_set_seen", description = "", key = "mail_set_seen", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_set_seen = new SOSOptionBoolean(this, conClassName + ".mail_set_seen", // HashMap-Key
    "", // Titel
    "false", // InitValue
    "false", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_set_seen :
     *
     * \details After processing the email it will be set to "readed"
     *
     * \return */
    public SOSOptionBoolean getmail_set_seen() {
        return mail_set_seen;
    }

    /** \brief setmail_set_seen :
     *
     * \details After processing the email it will be set to "readed"
     *
     * @param mail_set_seen : */
    public void setmail_set_seen(final SOSOptionBoolean p_mail_set_seen) {
        mail_set_seen = p_mail_set_seen;
    }

    /** \var mail_subject_filter : You can specify a filter to get only mails
     * having this filter in the subject. If you want to use regular
     * expressions, you can use the parameter mail_subject_pattern. */
    @JSOptionDefinition(name = "mail_subject_filter", description = "", key = "mail_subject_filter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mail_subject_filter = new SOSOptionString(this, conClassName + ".mail_subject_filter", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_subject_filter :
     *
     * \details You can specify a filter to get only mails having this filter in
     * the subject. If you want to use regular expressions, you can use the
     * parameter mail_subject_pattern.
     *
     * \return */
    public SOSOptionString getmail_subject_filter() {
        return mail_subject_filter;
    }

    /** \brief setmail_subject_filter :
     *
     * \details You can specify a filter to get only mails having this filter in
     * the subject. If you want to use regular expressions, you can use the
     * parameter mail_subject_pattern.
     *
     * @param mail_subject_filter : */
    public void setmail_subject_filter(final SOSOptionString p_mail_subject_filter) {
        mail_subject_filter = p_mail_subject_filter;
    }

    /** \var mail_subject_pattern : You can specify a regular expression to get
     * only mails matching this expression. */
    @JSOptionDefinition(name = "mail_subject_pattern", description = "", key = "mail_subject_pattern", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp mail_subject_pattern = new SOSOptionRegExp(this, conClassName + ".mail_subject_pattern", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_subject_pattern :
     *
     * \details You can specify a regular expression to get only mails matching
     * this expression.
     *
     * \return */
    public SOSOptionRegExp getmail_subject_pattern() {
        return mail_subject_pattern;
    }

    /** \brief setmail_subject_pattern :
     *
     * \details You can specify a regular expression to get only mails matching
     * this expression.
     *
     * @param mail_subject_pattern : */
    public void setmail_subject_pattern(final SOSOptionRegExp p_mail_subject_pattern) {
        mail_subject_pattern = p_mail_subject_pattern;
    }

    /** \var mail_use_seen : If mail_use_seen is true, mail_set_seen will be set
     * to true automatically. Only mails will be processed, which are not
     * readed. */
    @JSOptionDefinition(name = "mail_use_seen", description = "", key = "mail_use_seen", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_use_seen = new SOSOptionBoolean(this, conClassName + ".mail_use_seen", // HashMap-Key
    "", // Titel
    "true", // InitValue
    "true", // DefaultValue
    false // isMandatory
    );

    /** \brief getmail_use_seen :
     *
     * \details If mail_use_seen is true, mail_set_seen will be set to true
     * automatically. Only mails will be processed, which are not readed.
     *
     * \return */
    public SOSOptionBoolean getmail_use_seen() {
        return mail_use_seen;
    }

    /** \brief setmail_use_seen :
     *
     * \details If mail_use_seen is true, mail_set_seen will be set to true
     * automatically. Only mails will be processed, which are not readed.
     *
     * @param mail_use_seen : */
    public void setmail_use_seen(final SOSOptionBoolean p_mail_use_seen) {
        mail_use_seen = p_mail_use_seen;
    }

    /** \var mail_user : The user for login at the mailserver. */
    @JSOptionDefinition(name = "mail_user", description = "", key = "mail_user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName mail_user = new SOSOptionUserName(this, conClassName + ".mail_user", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    /** \brief getmail_user :
     *
     * \details The user for login at the mailserver.
     *
     * \return */
    public SOSOptionUserName getmail_user() {
        return mail_user;
    }

    /** \brief setmail_user :
     *
     * \details The user for login at the mailserver.
     *
     * @param mail_user : */
    public void setmail_user(final SOSOptionUserName p_mail_user) {
        mail_user = p_mail_user;
    }

    /** \option min_file_age \type SOSOptionTime \brief min_file_age - Minimum
     * age of an Object
     *
     * \details Objects, which are younger than min_file_age are not processed
     *
     * \mandatory: false
     *
     * \created 31.12.2012 15:48:02 by KB */
    @JSOptionDefinition(name = "min_file_age", description = "Objects, which are younger than min_file_age are not processed", key = "min_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTimeHorizon min_file_age = new SOSOptionTimeHorizon( // ...
    this, // ....
    conClassName + ".min_file_age", // ...
    "Objects, which are younger than min_file_age are not processed", // ...
    "", // ...
    "", // ...
    false);
    public SOSOptionTimeHorizon MinAge = (SOSOptionTimeHorizon) min_file_age.SetAlias("min_age");

    public String getmin_file_age() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getmin_file_age";

        return min_file_age.Value();
    } // public String getmin_file_age

    public SOSMailProcessInboxOptionsSuperClass setmin_file_age(final String pstrValue) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setmin_file_age";
        min_file_age.Value(pstrValue);
        return this;
    } // public SOSMailProcessInboxOptionsSuperClass setmin_file_age

    public SOSMailProcessInboxOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public Mail2ActionOptionsSuperClass

    public SOSMailProcessInboxOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public Mail2ActionOptionsSuperClass

    //

    public SOSMailProcessInboxOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public Mail2ActionOptionsSuperClass (HashMap JSSettings)

    /** \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
     * Optionen als String
     *
     * \details
     *
     * \see toString \see toOut */
    private String getAllOptionsAsString() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAllOptionsAsString";
        String strT = conClassName + "\n";
        final StringBuffer strBuffer = new StringBuffer();
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this,
        // JSOptionsClass.IterationTypes.toString, strBuffer);
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
        // strBuffer);
        strT += this.toString(); // fix
        //
        return strT;
    } // private String getAllOptionsAsString ()

    /** \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
     *
     * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
     * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
     * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
     * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
     * werden die Schlüssel analysiert und, falls gefunden, werden die
     * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
     *
     * Nicht bekannte Schlüssel werden ignoriert.
     *
     * \see JSOptionsClass::getItem
     *
     * @param pobjJSSettings
     * @throws Exception */
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

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    /** \brief CommandLineArgs - Übernehmen der Options/Settings aus der
     * Kommandozeile
     *
     * \details Die in der Kommandozeile beim Starten der Applikation
     * angegebenen Parameter werden hier in die HashMap übertragen und danach
     * den Optionen als Wert zugewiesen.
     *
     * \return void
     *
     * @param pstrArgs
     * @throws Exception */
    @Override
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class Mail2ActionOptionsSuperClass