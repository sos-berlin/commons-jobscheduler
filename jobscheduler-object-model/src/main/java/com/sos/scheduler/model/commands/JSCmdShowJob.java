package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Job;

public class JSCmdShowJob extends ShowJob {

    public static enum enu4What {
        task_queue, job_params, job_orders, job_commands, description, log, run_time, task_history, source;

        public String getText() {
            return this.name();
        }
    }

    public JSCmdShowJob(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setWhat(enu4What penuT) {
        super.setWhat(penuT.getText());
    }

    public void setWhat(enu4What[] penuT) {
        String strT = "";
        for (enu4What enuState4What : penuT) {
            strT += enuState4What.getText() + " ";
        }
        super.setWhat(strT);
    }

    public Job getJobAnswer() {
        return null;
    }

    public JSCmdShowJob maxOrders(final int pintMaxOrders) {
        super.setMaxOrders(BigInteger.valueOf(pintMaxOrders));
        return this;
    }

    public JSCmdShowJob maxTaskHistory(final int pintMaxTaskHistory) {
        super.setMaxTaskHistory(BigInteger.valueOf(pintMaxTaskHistory));
        return this;
    }

    public String getJobName() {
        return super.getJob();
    }

    public void setJobName(String jobName) {
        super.setJob(jobName);
    }

}