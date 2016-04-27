package sos.scheduler.managed.db;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJdbcUrl;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionUserName;

@JSOptionClass(name = "JobSchedulerManagedDBReportJobOptionsSuperClass", description = "JobSchedulerManagedDBReportJobOptionsSuperClass")
public class JobSchedulerManagedDBReportJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "JobSchedulerManagedDBReportJobOptionsSuperClass";

    @JSOptionDefinition(name = "Adjust_column_names", description = "Character conversion for column names", key = "Adjust_column_names",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean Adjust_column_names = new SOSOptionBoolean(this, CLASSNAME + ".Adjust_column_names", "Character conversion for column names", 
            "true", "true", false);

    public SOSOptionBoolean getAdjust_column_names() {
        return Adjust_column_names;
    }

    public void setAdjust_column_names(final SOSOptionBoolean p_Adjust_column_names) {
        Adjust_column_names = p_Adjust_column_names;
    }

    @JSOptionDefinition(name = "Column_names_case_sensitivity", description = "Let Column names as is", key = "Column_names_case_sensitivity",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean Column_names_case_sensitivity = new SOSOptionBoolean(this, CLASSNAME + ".Column_names_case_sensitivity", "Let Column names as is",
            "false", "false", false);

    public SOSOptionBoolean getColumn_names_case_sensitivity() {
        return Column_names_case_sensitivity;
    }

    public void setColumn_names_case_sensitivity(final SOSOptionBoolean p_Column_names_case_sensitivity) {
        Column_names_case_sensitivity = p_Column_names_case_sensitivity;
    }

    @JSOptionDefinition(name = "command", description = "Database Commands for the Job", key = "command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString command = new SOSOptionString(this, CLASSNAME + ".command", "Database Commands for the Job", " ", " ", false);
    public SOSOptionString SQL_Command = (SOSOptionString) command.SetAlias(CLASSNAME + ".SQL_Command");

    public SOSOptionString getcommand() {
        return command;
    }

    public void setcommand(final SOSOptionString p_command) {
        command = p_command;
    }


    @JSOptionDefinition(name = "database_connection", description = "database connection from table SCHEDULER_MANAGED_CONNECTIONS",
            key = "database_connection", type = "SOSOptionString", mandatory = false)
    public SOSOptionString database_connection = new SOSOptionString(this, CLASSNAME + ".database_connection", 
            "database connection from table SCHEDULER_MANAGED_CONNECTIONS", " ", " ", false);

    public SOSOptionString getdatabase_connection() {
        return database_connection;
    }

    public void setdatabase_connection(final SOSOptionString p_database_connection) {
        database_connection = p_database_connection;
    }

    @JSOptionDefinition(name = "db_class", description = "SOS Connection class", key = "db_class", type = "SOSOptionString", mandatory = false)
    public SOSOptionString db_class = new SOSOptionString(this, CLASSNAME + ".db_class", "SOS Connection class", " ", " ", false);

    public SOSOptionString getdb_class() {
        return db_class;
    }

    public void setdb_class(final SOSOptionString p_db_class) {
        db_class = p_db_class;
    }

    @JSOptionDefinition(name = "db_driver", description = "Name of the jd", key = "db_driver", type = "SOSOptionString", mandatory = false)
    public SOSOptionString db_driver = new SOSOptionString(this, CLASSNAME + ".db_driver", "Name of the jd", " ", " ", false);

    public SOSOptionString getdb_driver() {
        return db_driver;
    }

    public void setdb_driver(final SOSOptionString p_db_driver) {
        db_driver = p_db_driver;
    }

    @JSOptionDefinition(name = "db_password", description = "database password", key = "db_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword db_password = new SOSOptionPassword(this, CLASSNAME + ".db_password", "database password", " ", " ", false);

    public SOSOptionPassword getdb_password() {
        return db_password;
    }

    public void setdb_password(final SOSOptionPassword p_db_password) {
        db_password = p_db_password;
    }

    @JSOptionDefinition(name = "db_url", description = "jdbc url", key = "db_url", type = "SOSOptionUrl", mandatory = false)
    public SOSOptionJdbcUrl db_url = new SOSOptionJdbcUrl(this, CLASSNAME + ".db_url", "jdbc url", "", "", false);

    public SOSOptionJdbcUrl getdb_url() {
        return db_url;
    }

    public void setdb_url(final SOSOptionJdbcUrl p_db_url) {
        db_url = p_db_url;
    }

    @JSOptionDefinition(name = "db_user", description = "database user", key = "db_user", type = "SOSOptionUserName", mandatory = false)
    public SOSOptionUserName db_user = new SOSOptionUserName(this, CLASSNAME + ".db_user", "database user", " ", " ", false);

    public SOSOptionUserName getdb_user() {
        return db_user;
    }

    public void setdb_user(final SOSOptionUserName p_db_user) {
        db_user = p_db_user;
    }

    @JSOptionDefinition(name = "exec_returns_resultset", description = "If stored proc", key = "exec_returns_resultset", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean exec_returns_resultset = new SOSOptionBoolean(this, CLASSNAME + ".exec_returns_resultset", "If stored proc", "false", "false", false);

    public SOSOptionBoolean getexec_returns_resultset() {
        return exec_returns_resultset;
    }

    public void setexec_returns_resultset(final SOSOptionBoolean p_exec_returns_resultset) {
        exec_returns_resultset = p_exec_returns_resultset;
    }

    @JSOptionDefinition(name = "Max_No_Of_Records_To_Process", description = "Max Number of lines in the result table to process",
            key = "Max_No_Of_Records_To_Process", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger Max_No_Of_Records_To_Process = new SOSOptionInteger(this, CLASSNAME + ".Max_No_Of_Records_To_Process", 
            "Max Number of lines in the result table to process", "-1", "-1", false);

    public SOSOptionInteger getMax_No_Of_Records_To_Process() {
        return Max_No_Of_Records_To_Process;
    }

    public void setMax_No_Of_Records_To_Process(final SOSOptionInteger p_Max_No_Of_Records_To_Process) {
        Max_No_Of_Records_To_Process = p_Max_No_Of_Records_To_Process;
    }

    @JSOptionDefinition(name = "scheduler_order_report_asbody", description = "This setting d", key = "scheduler_order_report_asbody",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean scheduler_order_report_asbody = new SOSOptionBoolean(this, CLASSNAME + ".scheduler_order_report_asbody", 
            "This setting d", "false", "false", false);

    public SOSOptionBoolean getscheduler_order_report_asbody() {
        return scheduler_order_report_asbody;
    }

    public void setscheduler_order_report_asbody(final SOSOptionBoolean p_scheduler_order_report_asbody) {
        scheduler_order_report_asbody = p_scheduler_order_report_asbody;
    }

    @JSOptionDefinition(name = "scheduler_order_report_body", description = "", key = "scheduler_order_report_body", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString scheduler_order_report_body = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_body", "", " ", " ", false);

    public SOSOptionString getscheduler_order_report_body() {
        return scheduler_order_report_body;
    }

    public void setscheduler_order_report_body(final SOSOptionString p_scheduler_order_report_body) {
        scheduler_order_report_body = p_scheduler_order_report_body;
    }

    @JSOptionDefinition(name = "scheduler_order_report_filename", description = "", key = "scheduler_order_report_filename", type = "SOSOptionOutFileName",
            mandatory = false)
    public SOSOptionOutFileName scheduler_order_report_filename = new SOSOptionOutFileName(this, CLASSNAME + ".scheduler_order_report_filename", "", 
            "report_[date]_[taskid].xml", "report_[date]_[taskid].xml", false);

    public SOSOptionOutFileName getscheduler_order_report_filename() {
        return scheduler_order_report_filename;
    }

    public void setscheduler_order_report_filename(final SOSOptionOutFileName p_scheduler_order_report_filename) {
        scheduler_order_report_filename = p_scheduler_order_report_filename;
    }

    @JSOptionDefinition(name = "scheduler_order_report_mailbcc", description = "One or more e-", key = "scheduler_order_report_mailbcc",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_order_report_mailbcc = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_mailbcc", "One or more e-", " ", " ", 
            false);

    public SOSOptionString getscheduler_order_report_mailbcc() {
        return scheduler_order_report_mailbcc;
    }

    public void setscheduler_order_report_mailbcc(final SOSOptionString p_scheduler_order_report_mailbcc) {
        scheduler_order_report_mailbcc = p_scheduler_order_report_mailbcc;
    }

    @JSOptionDefinition(name = "scheduler_order_report_mailcc", description = "One or more e-", key = "scheduler_order_report_mailcc",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_order_report_mailcc = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_mailcc", "One or more e-", " ", " ",
            false);

    public SOSOptionString getscheduler_order_report_mailcc() {
        return scheduler_order_report_mailcc;
    }

    public void setscheduler_order_report_mailcc(final SOSOptionString p_scheduler_order_report_mailcc) {
        scheduler_order_report_mailcc = p_scheduler_order_report_mailcc;
    }

    @JSOptionDefinition(name = "scheduler_order_report_mailto", description = "report_mailto: recipients of a report", key = "scheduler_order_report_mailto",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_order_report_mailto = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_mailto", 
            "report_mailto: recipients of a report", " ", " ", false);

    public SOSOptionString getscheduler_order_report_mailto() {
        return scheduler_order_report_mailto;
    }

    public void setscheduler_order_report_mailto(final SOSOptionString p_scheduler_order_report_mailto) {
        scheduler_order_report_mailto = p_scheduler_order_report_mailto;
    }

    @JSOptionDefinition(name = "scheduler_order_report_path", description = "", key = "scheduler_order_report_path", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString scheduler_order_report_path = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_path", "", " ", " ", false);

    public SOSOptionString getscheduler_order_report_path() {
        return scheduler_order_report_path;
    }

    public void setscheduler_order_report_path(final SOSOptionString p_scheduler_order_report_path) {
        scheduler_order_report_path = p_scheduler_order_report_path;
    }

    @JSOptionDefinition(name = "scheduler_order_report_send_if_no_result", description = "This setting s", key = "scheduler_order_report_send_if_no_result",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean scheduler_order_report_send_if_no_result = new SOSOptionBoolean(this, CLASSNAME + ".scheduler_order_report_send_if_no_result", 
            "This setting s", "false", "false", false);

    public SOSOptionBoolean getscheduler_order_report_send_if_no_result() {
        return scheduler_order_report_send_if_no_result;
    }

    public void setscheduler_order_report_send_if_no_result(final SOSOptionBoolean p_scheduler_order_report_send_if_no_result) {
        scheduler_order_report_send_if_no_result = p_scheduler_order_report_send_if_no_result;
    }

    @JSOptionDefinition(name = "scheduler_order_report_send_if_result", description = "This setting s", key = "scheduler_order_report_send_if_result",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean scheduler_order_report_send_if_result = new SOSOptionBoolean(this, CLASSNAME + ".scheduler_order_report_send_if_result", 
            "This setting s", "true", "true", false);

    public SOSOptionBoolean getscheduler_order_report_send_if_result() {
        return scheduler_order_report_send_if_result;
    }

    public void setscheduler_order_report_send_if_result(final SOSOptionBoolean p_scheduler_order_report_send_if_result) {
        scheduler_order_report_send_if_result = p_scheduler_order_report_send_if_result;
    }

    @JSOptionDefinition(name = "scheduler_order_report_stylesheet", description = "", key = "scheduler_order_report_stylesheet", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString scheduler_order_report_stylesheet = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_stylesheet", "", 
            "config/default.xslt", "config/default.xslt", false);
    public SOSOptionString report_stylesheet = (SOSOptionString) scheduler_order_report_stylesheet.SetAlias(CLASSNAME + ".report_stylesheet");

    public SOSOptionString getscheduler_order_report_stylesheet() {
        return scheduler_order_report_stylesheet;
    }

    public void setscheduler_order_report_stylesheet(final SOSOptionString p_scheduler_order_report_stylesheet) {
        scheduler_order_report_stylesheet = p_scheduler_order_report_stylesheet;
    }

    @JSOptionDefinition(name = "scheduler_order_report_subject", description = "report_subject", key = "scheduler_order_report_subject",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_order_report_subject = new SOSOptionString(this, CLASSNAME + ".scheduler_order_report_subject", "report_subject", " ", 
            " ", false);
    public SOSOptionString report_subject = (SOSOptionString) scheduler_order_report_subject.SetAlias(CLASSNAME + ".report_subject");

    public SOSOptionString getscheduler_order_report_subject() {
        return scheduler_order_report_subject;
    }

    public void setscheduler_order_report_subject(final SOSOptionString p_scheduler_order_report_subject) {
        scheduler_order_report_subject = p_scheduler_order_report_subject;
    }

    public JobSchedulerManagedDBReportJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerManagedDBReportJobOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerManagedDBReportJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
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