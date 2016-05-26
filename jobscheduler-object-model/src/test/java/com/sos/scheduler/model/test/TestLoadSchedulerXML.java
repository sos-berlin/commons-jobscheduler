package com.sos.scheduler.model.test;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSXMLFile;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.At;
import com.sos.scheduler.model.answers.Calendar;
import com.sos.scheduler.model.answers.Period;
import com.sos.scheduler.model.answers.Task;
import com.sos.scheduler.model.commands.*;
import com.sos.scheduler.model.objects.Job;
import com.sos.scheduler.model.objects.ProcessClass;
import com.sos.scheduler.model.objects.Spooler;
import com.sos.scheduler.model.objects.Spooler.Config;
import org.apache.log4j.Logger;

import java.io.File;
import java.math.BigInteger;

/** @author KB */
public class TestLoadSchedulerXML implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(TestLoadSchedulerXML.class);

        //
    @Override
    public void run() {
        try {
            LOGGER.debug("test start");
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory("localhost", 4444);
            objFactory.initMarshaller(Spooler.class);
            Spooler objSchedulerConfig = (Spooler) objFactory.unMarshall(new File("C:/Program Files (x86)/Scheduler/config/scheduler.xml"));
            Config objConfig = objSchedulerConfig.getConfig().get(0);
            LOGGER.debug(objConfig.getPort());
            LOGGER.debug(objConfig.getTcpPort());
            LOGGER.debug(objConfig.getUdpPort());
            for (ProcessClass objProcessClass : objConfig.getProcessClasses().getProcessClass()) {
                LOGGER.debug("ProcessClass = " + objProcessClass.getName());
            }
            LOGGER.debug("ready");
            Job objJob = (Job) objFactory.unMarshall(new File("C:/Program Files (x86)/Scheduler/config/live/show_env.job.xml"));
            JSXMLFile objXMLFile;
            try {
                objXMLFile = new JSXMLFile("c:/temp/t.xml");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new JobSchedulerException("JSXMLFile ended with exception", e);
            }
            objFactory.marshal(objJob, objXMLFile);
            LOGGER.debug(objXMLFile.getContent());
            JSCmdStartJob objStartJob = objFactory.createStartJob();
            objStartJob.setName("show_env");
            objStartJob.setForce("yes");
            objStartJob.setJob("show_env");
            objStartJob.setParams(new String[] { "Hallo", "Value of Hallo" });
            LOGGER.debug(objStartJob.toXMLString());
            try {
                objStartJob.run();
            } catch (JobSchedulerException e) {
                //
            } catch (Exception e) {
                //
            }
            Task objTask = objStartJob.getTask();
            LOGGER.info("task-id  = " + objTask.getTaskId());
            LOGGER.info("enqueued = " + objTask.getEnqueued());
            objStartJob.setJob("willi");
            try {
                objStartJob.run();
            } catch (JobSchedulerException e) {
                //
            } catch (Exception e) {
                //
            }
            JSCmdShowState objShowState = objFactory.createShowState();
            objShowState.setWhat("all");
            objShowState.run();
            objShowState.setWhat("");
            objShowState.run();
            JSCmdShowJob objCmdShowJob = objFactory.createShowJob();
            objCmdShowJob.setJob("show_env");
            objCmdShowJob.setWhat("all");
            objCmdShowJob.run();
            JSCmdShowJobs objCmdShowJobs = objFactory.createShowJobs();
            objCmdShowJobs.setMaxTaskHistory(BigInteger.valueOf(100));
            objCmdShowJobs.setWhat("all");
            objCmdShowJobs.run();
            JSCmdShowHistory objCmdShowHistory = objFactory.createShowHistory();
            objCmdShowHistory.setJob("show_env");
            objCmdShowHistory.setMaxOrders(BigInteger.valueOf(9999));
            objCmdShowHistory.run();
            JSCmdShowTask objCmdShowTask = objFactory.createShowTask();
            objCmdShowTask.setId(BigInteger.valueOf(3749));
            objCmdShowTask.setWhat("all");
            objCmdShowTask.run();
            JSCmdShowCalendar objSC = objFactory.createShowCalendar();
            objSC.setWhat("orders");
            objSC.setLimit(9999);
            objSC.setFrom("2011-01-21T23:00:00");
            objSC.run();
            Calendar objCalendar = objSC.getCalendar();
            for (Object objCalendarObject : objCalendar.getAtOrPeriod()) {
                if (objCalendarObject instanceof At) {
                    At objAt = (At) objCalendarObject;
                    LOGGER.debug(objFactory.answerToXMLString(objAt));
                    LOGGER.debug("Start at :" + objAt.getAt());
                    LOGGER.debug("Job Name :" + objAt.getJob());
                    LOGGER.debug("Job-Chain Name :" + objAt.getJobChain());
                    LOGGER.debug("Order Name :" + objAt.getOrder());
                } else {
                    if (objCalendarObject instanceof Period) {
                        Period objPeriod = (Period) objCalendarObject;
                        LOGGER.debug(objFactory.answerToXMLString(objPeriod));
                        LOGGER.debug("Absolute Repeat Interval :" + objPeriod.getAbsoluteRepeat());
                        LOGGER.debug("Timerange start :" + objPeriod.getBegin());
                        LOGGER.debug("Timerange end :" + objPeriod.getEnd());
                        LOGGER.debug("Job-Name :" + objPeriod.getJob());
                    }
                }
            }
        } catch (Exception je) {
            LOGGER.error(je.getMessage(), je);
        }
    }

}