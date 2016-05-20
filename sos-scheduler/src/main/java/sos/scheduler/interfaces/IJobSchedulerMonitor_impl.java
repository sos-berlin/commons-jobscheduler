package sos.scheduler.interfaces;

/** @author KB */
/** @author Joacim Zschimmer */
public interface IJobSchedulerMonitor_impl {

    final static boolean continueWithProcess = true;
    final static boolean continueWithProcessBefore = true;
    final static boolean continueWithTaskAfter = false;

    public boolean spooler_task_before() throws Exception;

    public void spooler_task_after() throws Exception;

    public boolean spooler_process_before() throws Exception;

    public boolean spooler_process_after(boolean spooler_process_result) throws Exception;

}