package sos.scheduler.LaunchAndObserve;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.JSOrderId;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;

@JSOptionClass(name = "JobSchedulerLaunchAndObserveOptionsSuperClass", description = "JobSchedulerLaunchAndObserveOptionsSuperClass")
public class JobSchedulerLaunchAndObserveOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -4070202283420425512L;
    private final String conClassName = "JobSchedulerLaunchAndObserveOptionsSuperClass";
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerLaunchAndObserveOptionsSuperClass.class);

    @JSOptionDefinition(name = "check_for_regexp", description = "Text pattern to search for in log file", key = "check_for_regexp", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp check_for_regexp = new SOSOptionRegExp(this, conClassName + ".check_for_regexp", "Text pattern to search for in log file", "true", "true", false);

    public SOSOptionRegExp getcheck_for_regexp() {
        return check_for_regexp;
    }

    public void setcheck_for_regexp(SOSOptionRegExp p_check_for_regexp) {
        this.check_for_regexp = p_check_for_regexp;
    }

    @JSOptionDefinition(name = "check_inactivity", description = "Check job for inactivity", key = "check_inactivity", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean check_inactivity = new SOSOptionBoolean(this, conClassName + ".check_inactivity", "Check job for inactivity", "true", "true", false);

    public SOSOptionBoolean getcheck_inactivity() {
        return check_inactivity;
    }

    public void setcheck_inactivity(SOSOptionBoolean p_check_log_file) {
        this.check_inactivity = p_check_log_file;
    }

    @JSOptionDefinition(name = "check_interval", description = "This parameter specifies the interval in seconds", key = "check_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger check_interval = new SOSOptionInteger(this, conClassName + ".check_interval", "This parameter specifies the interval in seconds", "60", "60", false);

    public SOSOptionInteger getcheck_interval() {
        return check_interval;
    }

    public void setcheck_interval(SOSOptionInteger p_check_interval) {
        this.check_interval = p_check_interval;
    }

    @JSOptionDefinition(name = "job_name", description = "The name of a job.", key = "job_name", type = "JSJobName", mandatory = true)
    public JSJobName job_name = new JSJobName(this, conClassName + ".job_name", "The name of a job.", " ", " ", true);

    public JSJobName getjob_name() {
        return job_name;
    }

    public void setjob_name(JSJobName p_job_name) {
        this.job_name = p_job_name;
    }

    @JSOptionDefinition(name = "kill_job", description = "kill job due to Inactivity", key = "kill_job", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean kill_job = new SOSOptionBoolean(this, conClassName + ".kill_job", "kill job due to Inactivity", "true", "true", false);

    public SOSOptionBoolean getkill_job() {
        return kill_job;
    }

    public void setkill_job(SOSOptionBoolean p_kill_job) {
        this.kill_job = p_kill_job;
    }

    @JSOptionDefinition(name = "lifetime", description = "Lifetime of the Job", key = "lifetime", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime lifetime = new SOSOptionTime(this, conClassName + ".lifetime", "Lifetime of the Job", "0", "0", false);

    public SOSOptionTime getlifetime() {
        return lifetime;
    }

    public void setlifetime(SOSOptionTime p_lifetime) {
        this.lifetime = p_lifetime;
    }

    @JSOptionDefinition(name = "mail_on_nonactivity", description = "send eMail due to Inactivity", key = "mail_on_nonactivity", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_on_nonactivity = new SOSOptionBoolean(this, conClassName + ".mail_on_nonactivity", "send eMail due to Inactivity", "true", "true", false);

    public SOSOptionBoolean getmail_on_nonactivity() {
        return mail_on_nonactivity;
    }

    public void setmail_on_nonactivity(SOSOptionBoolean p_mail_on_nonactivity) {
        this.mail_on_nonactivity = p_mail_on_nonactivity;
    }

    @JSOptionDefinition(name = "mail_on_restart", description = "send eMail with restart of job", key = "mail_on_restart", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_on_restart = new SOSOptionBoolean(this, conClassName + ".mail_on_restart", "send eMail with restart of job", "true", "true", false);

    public SOSOptionBoolean getmail_on_restart() {
        return mail_on_restart;
    }

    public void setmail_on_restart(SOSOptionBoolean p_mail_on_restart) {
        this.mail_on_restart = p_mail_on_restart;
    }

    @JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch", key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString order_jobchain_name = new SOSOptionString(this, conClassName + ".order_jobchain_name", "The name of the jobchain which belongs to the order The name of the jobch", " ", " ", false);

    public SOSOptionString getorder_jobchain_name() {
        return order_jobchain_name;
    }

    public void setorder_jobchain_name(SOSOptionString p_order_jobchain_name) {
        this.order_jobchain_name = p_order_jobchain_name;
    }

    @JSOptionDefinition(name = "OrderId", description = "The name or the identification of an order.", key = "OrderId", type = "JSOrderId", mandatory = false)
    public JSOrderId OrderId = new JSOrderId(this, conClassName + ".OrderId", "The name or the identification of an order.", " ", " ", false);

    public JSOrderId getOrderId() {
        return OrderId;
    }

    public void setOrderId(JSOrderId p_OrderId) {
        this.OrderId = p_OrderId;
    }

    @JSOptionDefinition(name = "restart", description = "Restart the observed Job This value o", key = "restart", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean restart = new SOSOptionBoolean(this, conClassName + ".restart", "Restart the observed Job This value o", "true", "true", false);

    public SOSOptionBoolean getrestart() {
        return restart;
    }

    public void setrestart(SOSOptionBoolean p_restart) {
        this.restart = p_restart;
    }

    @JSOptionDefinition(name = "scheduler_host", description = "This parameter specifies the host name or IP addre", key = "scheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName scheduler_host = new SOSOptionHostName(this, conClassName + ".scheduler_host", "This parameter specifies the host name or IP addre", "", "localhost", true);

    public SOSOptionHostName getscheduler_host() {
        return scheduler_host;
    }

    public void setscheduler_host(SOSOptionHostName p_scheduler_host) {
        this.scheduler_host = p_scheduler_host;
    }

    @JSOptionDefinition(name = "scheduler_port", description = "The TCP-port for which a JobScheduler, see parameter sche", key = "scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber scheduler_port = new SOSOptionPortNumber(this, conClassName + ".scheduler_port", "The TCP-port for which a JobScheduler, see parameter sche", "0", "4444", true);

    public SOSOptionPortNumber getscheduler_port() {
        return scheduler_port;
    }

    public void setscheduler_port(SOSOptionPortNumber p_scheduler_port) {
        this.scheduler_port = p_scheduler_port;
    }

    public JobSchedulerLaunchAndObserveOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerLaunchAndObserveOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerLaunchAndObserveOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    private String getAllOptionsAsString() {
        String strT = conClassName + "\n";
        final StringBuilder strBuffer = new StringBuilder();
        strT += this.toString();
        return strT;
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
        try {
            setChildClasses(pobjJSSettings);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setChildClasses(HashMap<String, String> pobjJSSettings) throws Exception {
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
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}