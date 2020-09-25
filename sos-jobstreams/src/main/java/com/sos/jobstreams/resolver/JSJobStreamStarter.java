package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.classes.JobStreamScheduler;
import com.sos.jitl.jobstreams.db.DBItemJobStreamParameter;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jobstreams.classes.JobStarter;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.joc.Globals;
import com.sos.joc.model.joe.schedule.RunTime;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.xml.SOSXMLXPath;

public class JSJobStreamStarter {

    private static final String MAX_DATE = "01-01-2038";
    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobStreamStarter.class);
    private Map<String, String> listOfParameters;
    private DBItemJobStreamStarter itemJobStreamStarter;
    private Date nextStart;
    private List<JSStarterJob> listOfJobs;
    private JobStreamScheduler jobStreamScheduler;
    private String jobStreamName;
    private Long lastStart;

    public Map<String, String> getListOfParameters() {
        return listOfParameters;
    }

    public JSJobStreamStarter() {
        super();
        lastStart = 0L;
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

    public void schedule() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
        LOGGER.debug("schedule for:" + this.getItemJobStreamStarter().getTitle());
        jobStreamScheduler = new JobStreamScheduler(Constants.settings.getTimezone());
        if (this.getRunTime() != null) {
            Calendar c = Calendar.getInstance();
            Date from = new Date();
            Date to = new Date();
            c.setTime(to);
            c.add(Calendar.DATE, 3);
            to = c.getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            String max = MAX_DATE;
            Date maxDate = formatter.parse(max);
 
            do {
                jobStreamScheduler.schedule(from, to, this.getRunTime(), true);
                c.setTime(to);
                c.add(Calendar.MONTH, 1);
                from = to;
                to = c.getTime();
            } while (jobStreamScheduler.getListOfStartTimes().isEmpty() && to.before(maxDate));
        }
    }

    public void setItemJobStreamStarter(DBItemJobStreamStarter itemJobStreamStarter) throws JsonParseException, JsonMappingException,
            JsonProcessingException, IOException, Exception {
        LOGGER.trace("Setting starter with timeZone: " + Constants.settings.getTimezone());
        this.itemJobStreamStarter = itemJobStreamStarter;
        schedule();
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
        
        LOGGER.trace(this.jobStreamName + "--> last start:" + new Date(lastStart));
        if (jobStreamScheduler.getListOfStartTimes() != null) {
            for (Long start : jobStreamScheduler.getListOfStartTimes()) {
                LOGGER.trace("check start:" + new Date(start));
                if (start > lastStart) {
                    Date nextStart = new Date(start);
                    LOGGER.trace("---> next start:" + nextStart);
                    return nextStart;
                }
            }
        }
        return null;

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

    public List<JobStarterOptions> startJobs(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor) throws Exception {
        JobStarter jobStarter = new JobStarter();
        List<JobStarterOptions> listOfHandledJobs = new ArrayList<JobStarterOptions>();
        for (JSStarterJob jsStarterJob : listOfJobs) {
            JobStarterOptions jobStartOptions = new JobStarterOptions();
            jobStartOptions.setJob(jsStarterJob.getDbItemJobStreamStarterJob().getJob());
            jobStartOptions.setJobStream(this.jobStreamName);
            jobStartOptions.setListOfParameters(listOfParameters);
            jobStartOptions.setSkipOutCondition(jsStarterJob.getDbItemJobStreamStarterJob().getSkipOutCondition());

            jobStartOptions.setNormalizedJob(normalizePath(jsStarterJob.getDbItemJobStreamStarterJob().getJob()));
            String at = "";
            if (jsStarterJob.getDbItemJobStreamStarterJob().getDelay() != null) {
                at = "now+" + String.valueOf(jsStarterJob.getDbItemJobStreamStarterJob().getDelay());
            }

            boolean startToday = jsStarterJob.isStartToday();
            if (startToday) {
                String jobXml = jobStarter.buildJobStartXml(jobStartOptions, at);
                String answer = "";

                LOGGER.trace("JSInConditionCommand:startJob XML for job start is: " + jobXml);
                if (schedulerXmlCommandExecutor != null) {
                    answer = schedulerXmlCommandExecutor.executeXml(jobXml);
                    LOGGER.trace(answer);
                } else {
                    answer = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler> <answer time=\"2020-03-18T14:23:09.033Z\"> <ok> <task enqueued=\"2020-03-18T14:23:09.045Z\" force_start=\"no\" id=\"298620\" job=\"/myJob4711\" log_file=\"D:/documents/sos-berlin.com/scheduler_joc_cockpit/logs/scheduler-2020-03-18-134738.scheduler_joc_cockpit.log\" name=\"\" start_at=\"2020-03-18T14:23:09.040Z\" state=\"none\" steps=\"0\" task=\"298620\"> <log level=\"info\"/>  </task> </ok> </answer></spooler>";
                    LOGGER.debug("Start job will be ignored as running in debug  mode.: " + jobStartOptions.getJob());
                }
                SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(new StringBuffer(answer));
                Long taskId = Long.valueOf(xPathSchedulerXml.selectSingleNodeValue("/spooler/answer/ok/task/@id"));
                jobStartOptions.setTaskId(taskId);
            } else {
                jobStartOptions.setSkipped(true);
            }
            listOfHandledJobs.add(jobStartOptions);
        }

        return listOfHandledJobs;

    }

    public void setJobStreamName(String jobStream) {
        this.jobStreamName = jobStream;
    }

    public List<JSStarterJob> getListOfJobs() {
        return listOfJobs;
    }

    public void setListOfJobs(List<JSStarterJob> listOfJobs) {
        this.listOfJobs = listOfJobs;
    }

    public String getAllJobNames() {
        String result = ":";
        for (JSStarterJob jsStarterJob : listOfJobs) {
            result = result + jsStarterJob.getDbItemJobStreamStarterJob().getJob() + ":";
        }
        return result;
    }
    
    public void setLastStart(long lastStart) {
        LOGGER.debug("set last start for " + this.jobStreamName + ":" + this.itemJobStreamStarter.getTitle() + " " + this.getNextStart());
        this.lastStart = lastStart;
    }
   
    public Long getLastStart() {
        return  lastStart;
     }
     
    public String getEndJob() {
        return normalizePath(itemJobStreamStarter.getEndOfJobStream());
    }

}
