package com.sos.jobstreams.classes;

 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.jobscheduler.RuntimeResolver;
import com.sos.joc.Globals;
import com.sos.joc.model.calendar.Period;
import com.sos.joc.model.joe.schedule.RunTime;
import com.sos.joe.common.XmlSerializer;

import sos.xml.SOSXMLXPath;

public class JobStreamScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStreamScheduler.class);
    private com.sos.joc.model.plan.RunTime plan;
    private  List<Long> listOfStartTimes;

    public JobStreamScheduler() {
        super();
    }

    public void schedule(RunTime runTime) throws JsonProcessingException, Exception {
        String actDate = todayAsString();
        SOSXMLXPath xml = new SOSXMLXPath(new StringBuffer(Globals.xmlMapper.writeValueAsString(XmlSerializer.serializeAbstractSchedule(runTime))));
        RuntimeResolver r = new RuntimeResolver();
        plan = r.resolve(xml, actDate, actDate, "");
        PeriodResolver periodResolver = new PeriodResolver();
        for (Period p : plan.getPeriods()) {
            periodResolver.addStartTimes(p);
        }
        listOfStartTimes = periodResolver.getStartTimes();
    }

    private String todayAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateS = formatter.format(new Date());
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return dateS;
    }

    public com.sos.joc.model.plan.RunTime getPlan() {
        return plan;
    }

    
    public List<Long> getListOfStartTimes() {
        return listOfStartTimes;
    }

}
