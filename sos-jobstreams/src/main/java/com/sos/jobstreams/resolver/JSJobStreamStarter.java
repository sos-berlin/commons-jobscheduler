package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.jitl.jobstreams.db.DBItemJobStreamParameter;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jobstreams.classes.JobStarter;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.jobstreams.classes.JobStreamScheduler;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.joe.schedule.RunTime;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.xml.SOSXMLXPath;

public class JSJobStreamStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobStreamStarter.class);
    private Map<String, String> listOfParameters;
    private DBItemJobStreamStarter itemJobStreamStarter;
    private Date nextStart;
    private String normalizedJob;
    JobStreamScheduler jobStreamScheduler;
    private String jobStreamName;

    public Map<String, String> getListOfParameters() {
        return listOfParameters;
    }

    public JSJobStreamStarter() {
        super();
        listOfParameters = new HashMap<String, String>();
    }

    public DBItemJobStreamStarter getItemJobStreamStarter() {
        return itemJobStreamStarter;
    }

    public RunTime getRunTime() throws JsonParseException, JsonMappingException, IOException {
        if (itemJobStreamStarter.getRunTime() != null) {
            return Globals.objectMapper.readValue(itemJobStreamStarter.getRunTime(), RunTime.class);
        }
        return null;
    }

    public void setListOfParameters(Map<String, String> listOfParameters) {
        this.listOfParameters = listOfParameters;
    }

    public void setItemJobStreamStarter(DBItemJobStreamStarter itemJobStreamStarter) throws JsonParseException, JsonMappingException,
            JsonProcessingException, IOException, Exception {
        this.itemJobStreamStarter = itemJobStreamStarter;
        jobStreamScheduler = new JobStreamScheduler();
        if (this.getRunTime() != null) {
            jobStreamScheduler.schedule(this.getRunTime());
        }
       // this.normalizedJob = normalizePath(itemJobStreamStarter.getJob());

    }

    public void setListOfParameters(List<DBItemJobStreamParameter> listOfJobStreamParameters) {
        for (DBItemJobStreamParameter dbItemJobStreamParameter : listOfJobStreamParameters) {
            listOfParameters.put(dbItemJobStreamParameter.getName(), dbItemJobStreamParameter.getValue());
        }
    }

    public com.sos.joc.model.plan.RunTime getPlan() {
        return jobStreamScheduler.getPlan();
    }

    public Date getNextStartFromList() {
        jobStreamScheduler.getListOfStartTimes().sort(null);
        Collections.reverse(jobStreamScheduler.getListOfStartTimes());
        Date now = new Date();
        Date result = null;
        for (Long start : jobStreamScheduler.getListOfStartTimes()) {
            if (start > now.getTime()) {
                result = new Date(start);
            }
        }
        return result;

    }

    public JobStreamScheduler getJobStreamScheduler() {
        return jobStreamScheduler;
    }

    public void setNextStart(Date nextStart) {
        this.nextStart = nextStart;
    }

    public Date getNextStart() {
        return nextStart;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return ("/" + path.trim()).replaceAll("//+", "/").replaceFirst("/$", "");
    }

    public Long startJob(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor) throws Exception {
        JobStarter jobStarter = new JobStarter();
        JobStarterOptions jobStartOptions = new JobStarterOptions();
        jobStartOptions.setJob(this.normalizedJob);
        jobStartOptions.setJobStream(this.jobStreamName);
        jobStartOptions.setNormalizedJob(this.normalizedJob);

        String jobXml = jobStarter.buildJobStartXml(jobStartOptions, "");
        String answer = "";

        LOGGER.trace("JSInConditionCommand:startJob XML for job start ist: " + jobXml);
        if (schedulerXmlCommandExecutor != null) {
            answer = schedulerXmlCommandExecutor.executeXml(jobXml);
            LOGGER.trace(answer);
        } else {
            answer = "<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><spooler> <answer time=\\\"2020-03-18T14:23:09.033Z\\\"> <ok> <task enqueued=\\\"2020-03-18T14:23:09.045Z\\\" force_start=\\\"no\\\"id=\\\"298620\\\" job=\\\"/myJob4711\\\" log_file=\\\"D:/documents/sos-berlin.com/scheduler_joc_cockpit/logs/scheduler-2020-03-18-134738.scheduler_joc_cockpit.log\\\" name=\\\"\\\"start_at=\\\"2020-03-18T14:23:09.040Z\\\" state=\\\"none\\\" steps=\\\"0\\\" task=\\\"298620\\\"> <log level=\\\"info\\\"/> <environment count=\\\"12\\\"> <variable name=\\\"JS_DATE_YY\\\" value=\\\"200318\\\"/> <variable name=\\\"JS_TIME\\\" value=\\\"13:55:33\\\"/> <variable name=\\\"JS_JOBSTREAM\\\" value=\\\"test212\\\"/> <variable name=\\\"JS_CENTURY\\\" value=\\\"20\\\"/> <variable name=\\\"JS_YEAR_YY\\\" value=\\\"20\\\"/> <variable name=\\\"JS_DAY\\\"value=\\\"18\\\"/> <variable name=\\\"JS_MONTH\\\" value=\\\"03\\\"/> <variable name=\\\"JS_MONTH_NAME\\\" value=\\\"März\\\"/> <variablename=\\\"JS_YEAR_YYYY\\\" value=\\\"2020\\\"/> <variable name=\\\"JS_DATE_YYYY\\\" value=\\\"20200318\\\"/> <variable name=\\\"JS_FOLDER\\\" value=\\\"/\\\"/> <variable name=\\\"JS_JOBNAME\\\" value=\\\"/myJob4711\\\"/> </environment> </task> </ok> </answer></spooler>";
            LOGGER.debug("Start job will be ignored as running in debug  mode.: " + jobStartOptions.getJob());
        }
        
        SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(answer);
        Long taskId = Long.valueOf(xPathSchedulerXml.selectSingleNodeValue("/spooler/answer/ok/task/@id"));
        return taskId;

    }

    public void setJobStreamName(String jobStream) {
        this.jobStreamName = jobStream;
    }

}
