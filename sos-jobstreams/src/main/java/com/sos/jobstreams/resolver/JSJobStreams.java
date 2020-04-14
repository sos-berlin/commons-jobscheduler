package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.jobstreams.db.DBItemJobStream;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamStarters;
import com.sos.jitl.jobstreams.db.FilterJobStreamHistory;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarters;

public class JSJobStreams {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobStreams.class);
    Map<Long, JSJobStream> listOfJobStreams;

    public JSJobStreams() {
        super();
        this.listOfJobStreams = new HashMap<Long, JSJobStream>();
    }

    public void addJobStream(JSJobStream jobStream) {
        this.listOfJobStreams.put(jobStream.getId(), jobStream);
    }

    public void removeJobStream(Long jobStreamKey) {
        this.listOfJobStreams.remove(jobStreamKey);
    }

    public JSJobStream getJobStream(Long jobStreamKey) {
        return this.listOfJobStreams.get(jobStreamKey);
    }

    public void setListOfJobStreams(List<DBItemJobStream> listOfJobStreams, Map<Long, JSJobStreamStarter> listOfJobStreamStarterGlobal, SOSHibernateSession sosHibernateSession) throws JsonParseException,
            JsonMappingException, JsonProcessingException, IOException, Exception {
        
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);

        for (DBItemJobStream itemJobStream : listOfJobStreams) {
            JSJobStream jsStreamStream = new JSJobStream();
            jsStreamStream.setItemJobStream(itemJobStream);
            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(itemJobStream.getId());
            List<DBItemJobStreamStarter> listOfJobStreamStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);
            jsStreamStream.setJobStreamStarters(listOfJobStreamStarters,listOfJobStreamStarterGlobal, sosHibernateSession);
            
            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
            filterJobStreamHistory.setJobStreamId(itemJobStream.getId());
            filterJobStreamHistory.setRunning(true);
            List<DBItemJobStreamHistory> listOfJobStreamHistory = dbLayerJobStreamHistory.getJobStreamHistoryList(filterJobStreamHistory, 0);
            jsStreamStream.setJobStreamHistory(listOfJobStreamHistory, sosHibernateSession);

            
            addJobStream(jsStreamStream);
         }
    }

    public Map<Long, JSJobStream> getListOfJobStreams() {
        return listOfJobStreams;
    }

    public void removeJobStreams(JSJobStream jobStream) {
        this.removeJobStream(jobStream.getId());
    }

    public JSJobStreamStarter getNextStarter() {
        Date next = null;
        JSJobStreamStarter nextJsJobStreamStarter = null;
        for (JSJobStream jsJobStream : listOfJobStreams.values()) {
            for (JSJobStreamStarter jsJobStreamStarter : jsJobStream.getListOfJobStreamStarter()) {
                if (next == null || (jsJobStreamStarter.getNextStartFromList() != null) && jsJobStreamStarter.getNextStartFromList().before(next)) {
                    next = jsJobStreamStarter.getNextStartFromList();
                    if (next != null) {
                        nextJsJobStreamStarter = jsJobStreamStarter;
                        nextJsJobStreamStarter.setNextStart(next);
                    }
                }
            }
        }
        return nextJsJobStreamStarter;
    }

}
