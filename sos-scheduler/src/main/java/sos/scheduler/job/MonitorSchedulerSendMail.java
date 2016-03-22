/*
 * MonitorSchedulerSendMail.java Created on 22.08.2007
 */
package sos.scheduler.job;

import sos.scheduler.misc.SchedulerMailer;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** This monitor sends an email after process using the parameters of
 * {@link SchedulerMailer}<br>
 * Additional parameters:<br>
 * monitor_mail_on_success - send email if job returned true (default true)
 * monitor_mail_on_error - send email if job returned false (default true)
 * 
 * @author Andreas Liebert */
public class MonitorSchedulerSendMail extends JobSchedulerJobAdapter {

    @SuppressWarnings("unused")
    private final String conSVNVersion = "$Id$";

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
                if (mos.equalsIgnoreCase("no") || mos.equalsIgnoreCase("false") || mos.equalsIgnoreCase("0"))
                    mailOnSuccess = false;
            }

            if (params.value("monitor_mail_on_error") != null) {
                String moe = params.value("monitor_mail_on_error");
                if (moe.equalsIgnoreCase("no") || moe.equalsIgnoreCase("false") || moe.equalsIgnoreCase("0"))
                    mailOnError = false;
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
