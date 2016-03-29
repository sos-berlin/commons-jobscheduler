package sos.scheduler.job;

import sos.scheduler.misc.SchedulerMailer;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author Andreas Liebert */
public class MonitorSchedulerSendMail extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process_after(final boolean result) throws Exception {
        try {
            Variable_set params = spooler.create_variable_set();
            params.merge(spooler_task.params());
            if (spooler_job.order_queue() != null) {
                params.merge(spooler_task.order().params());
            }
            boolean mailOnSuccess = true;
            boolean mailOnError = true;
            if (params.value("monitor_mail_on_success") != null) {
                String mos = params.value("monitor_mail_on_success");
                if ("no".equalsIgnoreCase(mos) || "false".equalsIgnoreCase(mos) || "0".equalsIgnoreCase(mos)) {
                    mailOnSuccess = false;
                }
            }

            if (params.value("monitor_mail_on_error") != null) {
                String moe = params.value("monitor_mail_on_error");
                if ("no".equalsIgnoreCase(moe) || "false".equalsIgnoreCase(moe) || "0".equalsIgnoreCase(moe)) {
                    mailOnError = false;
                }
            }
            if (mailOnSuccess && result || mailOnError && !result) {
                SchedulerMailer mailer = new SchedulerMailer(this);
                mailer.getSosMail().send();
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured processing mail: " + e, e);
        }
        return result;
    }

}