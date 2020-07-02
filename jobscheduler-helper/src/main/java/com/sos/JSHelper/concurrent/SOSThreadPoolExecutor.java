package com.sos.JSHelper.concurrent;

/** @author KB */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SOSThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSThreadPoolExecutor.class);
    // Parallel running Threads(Executor) on System
    public int corePoolSize = 5;
    // Maximum Threads allowed in Pool
    public int maxPoolSize = 8;
    // Keep alive time for waiting threads for jobs(Runnable)
    public long keepAliveTime = 10;
    // This is the one who manages and start the work
    public ExecutorService objThreadPool = null;

    public SOSThreadPoolExecutor() {
        objThreadPool = Executors.newFixedThreadPool(corePoolSize);
    }

    public SOSThreadPoolExecutor(final int pintCorePoolSize) {
        objThreadPool = Executors.newFixedThreadPool(pintCorePoolSize);
        corePoolSize = pintCorePoolSize;
    }

    public Future<?> runTask(final Runnable task) {
        return objThreadPool.submit(task);
    }

    public void shutDown() {
        objThreadPool.shutdown();
    }

    public static void main(final String args[]) {
        SOSThreadPoolExecutor mtpe = new SOSThreadPoolExecutor(10);
        int cpus = Runtime.getRuntime().availableProcessors();
        LOGGER.debug("max avl cpus = " + cpus);
        for (int i = 0; i < 19; i++) {
            mtpe.runTask(new WorkerRunnable(i));
        }
        mtpe.shutDown();
        LOGGER.debug("Finished! ");
    }

    private static class WorkerRunnable implements Runnable {

        private final int jobNr;

        public WorkerRunnable(final int jobNr) {
            this.jobNr = jobNr;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    long intNo = Thread.currentThread().getId();
                    LOGGER.debug("Task " + jobNr + ", calculated " + i + ", Thread ID = " + intNo);
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    LOGGER.error(ie.getMessage(), ie);
                }
            }
        }
    }

}