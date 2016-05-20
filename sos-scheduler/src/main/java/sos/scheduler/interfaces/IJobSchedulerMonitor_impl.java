package sos.scheduler.interfaces;

/** @author Joacim Zschimmer */
public interface IJobSchedulerMonitor_impl {

    static final boolean continueWithProcess = true;
    static final boolean continueWithProcessBefore = true;
    static final boolean continueWithTaskAfter = false;

    public boolean spooler_task_before() throws Exception;

    public void spooler_task_after() throws Exception;

    public boolean spooler_process_before() throws Exception;

    public boolean spooler_process_after(boolean spooler_process_result) throws Exception;

}