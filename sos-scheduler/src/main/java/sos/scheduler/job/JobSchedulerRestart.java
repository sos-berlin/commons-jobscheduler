package sos.scheduler.job;

import sos.spooler.Job_impl;

/** @author andreas.pueschel@sos-berlin.com
 *
 *         restarts the Job Scheduler */
public class JobSchedulerRestart extends Job_impl {

    private int timeout = 600;

    public boolean spooler_process() {

        // spooler.let_run_terminate_and_restart();
        if (spooler_task.params().var("timeout") != null && spooler_task.params().var("timeout").length() > 0) {
            timeout = Integer.parseInt(spooler_task.params().var("timeout"));
            spooler_log.info(".. job parameter [timeout]: " + timeout);
        }

        if (timeout > 0)
            spooler.terminate_and_restart(timeout);
        else
            spooler.terminate_and_restart();

        return false;
    }

}
