package sos.scheduler.job;

import sos.spooler.Job_impl;

public class Job_ImplOrderTest extends Job_impl {

    @Override
    public boolean spooler_init() {
        try {

            return true;
        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {

            return true;// order
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void spooler_exit() {
    }

}