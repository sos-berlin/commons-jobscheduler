package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.jobstreams.db.DBItemJobStream;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBItemJobStreamParameter;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarterJob;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamParameters;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamsStarterJobs;
import com.sos.jitl.jobstreams.db.FilterJobStreamParameters;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarterJobs;

public class JSJobStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobStream.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private DBItemJobStream itemJobStream;
    private List<JSJobStreamStarter> listOfJobStreamStarter;
    private JSHistory jsHistory;

    public JSJobStream() {
        super();
        itemJobStream = new DBItemJobStream();
        listOfJobStreamStarter = new ArrayList<JSJobStreamStarter>();
        jsHistory = new JSHistory();
    }

    public void setItemJobStream(DBItemJobStream itemJobStream) {
        this.itemJobStream = itemJobStream;
    }

    public Long getId() {
        return itemJobStream.getId();
    }

    public String getJobSchedulerId() {
        return itemJobStream.getSchedulerId();
    }

    public String getJobStream() {
        return itemJobStream.getJobStream();
    }

    public String getFolder() {
        return itemJobStream.getFolder();
    }

    public void setSchedulerId(String schedulerId) {
        itemJobStream.setSchedulerId(schedulerId);
    }

    public void setJobStream(String jobStream) {
        itemJobStream.setJobStream(jobStream);
    }

    public void setState(String state) {
        itemJobStream.setState(state);
    }

    public void setCreated(Date created) {
        itemJobStream.setCreated(created);
    }

    public void addJobStreamStarter(JSJobStreamStarter jobStreamStarter) {
        listOfJobStreamStarter.add(jobStreamStarter);
    }

    public void setJobStreamStarters(EventHandlerSettings settings, List<DBItemJobStreamStarter> listOfJobStreamStarters,
            Map<Long, JSJobStreamStarter> listOfJobStreamStarterGlobal, SOSHibernateSession sosHibernateSession) throws JsonParseException,
            JsonMappingException, JsonProcessingException, IOException, Exception {
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        for (DBItemJobStreamStarter dbItemJobStreamStarter : listOfJobStreamStarters) {
            JSJobStreamStarter jobStreamStarter = new JSJobStreamStarter();
            jobStreamStarter.setItemJobStreamStarter(dbItemJobStreamStarter);
            jobStreamStarter.setJobStreamName(itemJobStream.getJobStream());

            DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
            FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
            filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
            List<DBItemJobStreamStarterJob> listOfStarterJobs = dbLayerJobStreamsStarterJobs.getJobStreamStarterJobsList(filterJobStreamStarterJobs,
                    0);
            List<JSStarterJob> listOfJSStarterJobs = new ArrayList<JSStarterJob>();
            for (DBItemJobStreamStarterJob dbItemJobStreamStarterJob : listOfStarterJobs) {

                JSStarterJob jsStarterJob = new JSStarterJob();
                jsStarterJob.setDbItemJobStreamStarterJob(dbItemJobStreamStarterJob);
                jsStarterJob.setListOfDates(sosHibernateSession,settings);
                listOfJSStarterJobs.add(jsStarterJob);
            }

            jobStreamStarter.setListOfJobs(listOfJSStarterJobs);

            FilterJobStreamParameters filterJobStreamParameters = new FilterJobStreamParameters();
            filterJobStreamParameters.setJobStreamStarterId(jobStreamStarter.getItemJobStreamStarter().getId());
            List<DBItemJobStreamParameter> listOfJobStreamParameters = dbLayerJobStreamParameters.getJobStreamParametersList(
                    filterJobStreamParameters, 0);
            jobStreamStarter.setListOfParameters(listOfJobStreamParameters);
            listOfJobStreamStarterGlobal.put(dbItemJobStreamStarter.getId(), jobStreamStarter);
            this.addJobStreamStarter(jobStreamStarter);
        }
    }

    public List<JSJobStreamStarter> getListOfJobStreamStarter() {
        if (listOfJobStreamStarter == null) {
            listOfJobStreamStarter = new ArrayList<JSJobStreamStarter>();
        }
        return listOfJobStreamStarter;
    }

    public List<JSHistoryEntry> getListOfJobStreamHistory() {
        return this.jsHistory.getListOfHistoryEntries();
    }

    public void setJobStreamHistory(List<DBItemJobStreamHistory> listOfJobStreamHistory, SOSHibernateSession sosHibernateSession) {
        for (DBItemJobStreamHistory dbItemJobStreamHistory : listOfJobStreamHistory) {
            JSHistoryEntry jsHistoryEntry = new JSHistoryEntry();
            jsHistoryEntry.setItemJobStreamHistory(dbItemJobStreamHistory);
            this.jsHistory.getListOfHistoryEntries().add(jsHistoryEntry);
        }
    }

    public JSHistory getJsHistory() {
        return jsHistory;
    }

}
