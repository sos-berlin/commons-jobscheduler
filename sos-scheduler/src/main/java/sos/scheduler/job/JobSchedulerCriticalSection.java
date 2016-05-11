package sos.scheduler.job;

import sos.spooler.Job_impl;
import sos.spooler.Spooler;

public class JobSchedulerCriticalSection {

    private static final String GREEN = "GREEN";
    private static final String RED = "RED";
    private static final String ORANGE = "ORANGE";
    private long delay = 200;
    private long dDelay = 2000;
    private String mutex = "section";
    private String taskId = "";
    private Spooler spooler;
    private Job_impl job;

    public JobSchedulerCriticalSection(Job_impl job, String mutex) {
        this.spooler = job.spooler;
        this.job = job;
        this.mutex = mutex + "_MUTEX";
        this.taskId = job.spooler_task.id() + "";
        job.spooler_log.debug9("Critical section \"" + mutex + "\" initialized.");
    }

    public JobSchedulerCriticalSection(Job_impl job, String mutex, long delay) {
        this(job, mutex);
        this.delay = delay;
        this.dDelay = delay * 10;
    }

    /** Enters a critical section.<br/>
     * Method waits until it is allowed to enter
     * 
     * @param timeNeeded time in milliseconds needed until caller is ready to
     *            exit the critical section again (call exit()). If timeNeeded
     *            has passed without calling exit, other task may enter the
     *            critical section. */
    public void enter(long timeNeeded) {
        long now = System.currentTimeMillis();
        String status = spooler.var(mutex);
        try {
            while (!(status == null || GREEN.equalsIgnoreCase(status) || status.isEmpty())) {
                String[] split = status.split(";");
                long timeout = Long.parseLong(split[2]);
                if (timeout < now) {
                    status = GREEN;
                } else {
                    try {
                        Thread.sleep(5);
                    } catch (Exception ex) {
                        //
                    }
                    now = System.currentTimeMillis();
                    status = spooler.var(mutex);
                }
            }
            goOrange(now + dDelay);
            status = spooler.var(mutex);
            if (status.startsWith(GREEN)) {
                enter(timeNeeded);
                return;
            }
            String[] split = status.split(";");
            if (split[1].equalsIgnoreCase(taskId)) {
                goRed(timeNeeded);
            } else {
                enter(timeNeeded);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            job.spooler_log.info("Status: " + status);
            throw e;
        }
    }

    private final void goOrange(long until) {
        spooler.set_var(mutex, ORANGE + ";" + taskId + ";" + until);
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            //
        }
    }

    private final void goRed(long timeNeeded) {
        long now = System.currentTimeMillis();
        spooler.set_var(mutex, RED + ";" + taskId + ";" + (now + timeNeeded));
    }

    public void exit() {
        String status = spooler.var(mutex);
        if (status != null) {
            if (status.startsWith(GREEN)) {
                job.spooler_log.info("Trying to exit critical section \"" + mutex + "\", but" + " it is already GREEN");
                return;
            }
            String[] split = status.split(";");
            if (split[1].equalsIgnoreCase(taskId)) {
                spooler.set_var(mutex, GREEN);
                job.spooler_log.debug3("Exited critical section \"" + mutex + "\".");
            } else {
                job.spooler_log.info("Trying to exit critical section \"" + mutex + "\", but" + " it already belongs to task " + split[1]);
            }
        }
    }

}