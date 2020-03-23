package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.jobstreams.db.DBItemJobStream;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamStarters;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarters;

public class JSJobStreams {

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

    public void setListOfJobStreams(List<DBItemJobStream> listOfJobStreams,SOSHibernateSession sosHibernateSession) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        
        for (DBItemJobStream itemJobStream : listOfJobStreams) {
            JSJobStream jsStreamStream = new JSJobStream();
            jsStreamStream.setItemJobStream(itemJobStream);
            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(itemJobStream.getId()); 
            List<DBItemJobStreamStarter> listOfJobStreamStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);
            jsStreamStream.setJobStreamStarters(listOfJobStreamStarters,sosHibernateSession);
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
       Date now = new Date();
       JSJobStreamStarter nextJsJobStreamStarter = null;
       for (JSJobStream jsJobStream : listOfJobStreams.values()){
           for (JSJobStreamStarter jsJobStreamStarter:jsJobStream.getListOfJobStreamStarter()) {
               if (next == null || jsJobStreamStarter.getNextStartFromList().before(next)){
                  next =  jsJobStreamStarter.getNextStartFromList();
                  nextJsJobStreamStarter = jsJobStreamStarter;
                  nextJsJobStreamStarter.setNextStart(next);
               }
               
           }
       }
    return nextJsJobStreamStarter;
    }

}
