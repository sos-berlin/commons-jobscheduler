package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.exception.SOSInvalidDataException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.jobstreams.db.DBItemCalendarWithUsages;
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
    Map<String, JSJobStream> listOfJobStreamNames;

    public JSJobStreams() {
        super();
        this.listOfJobStreams = new HashMap<Long, JSJobStream>();
        this.listOfJobStreamNames = new HashMap<String, JSJobStream>();
    }

    public void addJobStream(JSJobStream jobStream) {
        this.listOfJobStreams.put(jobStream.getId(), jobStream);
        this.listOfJobStreamNames.put(jobStream.getJobStream(), jobStream);
    }

    public void removeJobStream(Long jobStreamKey) {
        this.listOfJobStreams.remove(jobStreamKey);
    }

    public JSJobStream getJobStream(Long jobStreamKey) {
        return this.listOfJobStreams.get(jobStreamKey);
    }

    public JSJobStream getJobStreamByName(String jobStreamKey) {
        return this.listOfJobStreamNames.get(jobStreamKey);
    }

    public void setListOfJobStreams(EventHandlerSettings settings, List<DBItemJobStream> listOfJobStreams,
            Map<Long, JSJobStreamStarter> listOfJobStreamStarterGlobal, Map<String, List<DBItemCalendarWithUsages>> listOfCalendarUsages,
            SOSHibernateSession sosHibernateSession) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException,
            SOSInvalidDataException, DOMException, SOSHibernateException, ParseException, TransformerException {

        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);

        for (DBItemJobStream itemJobStream : listOfJobStreams) {
            JSJobStream jsStreamStream = new JSJobStream();
            jsStreamStream.setItemJobStream(itemJobStream);
            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(itemJobStream.getId());
            List<DBItemJobStreamStarter> listOfJobStreamStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);
            jsStreamStream.setJobStreamStarters(settings, listOfJobStreamStarters, listOfJobStreamStarterGlobal, listOfCalendarUsages,
                    sosHibernateSession);

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
        LOGGER.debug("Get next starter");
        JSJobStreamStarter nextJsJobStreamStarter = null;
        for (JSJobStream jsJobStream : listOfJobStreams.values()) {
            LOGGER.trace("starters for: " + jsJobStream.getJobStream());
            for (JSJobStreamStarter jsJobStreamStarter : jsJobStream.getListOfJobStreamStarter()) {
                LOGGER.trace("starter: " + jsJobStreamStarter.getItemJobStreamStarter().getTitle());
                LOGGER.trace("next start from list: " + jsJobStreamStarter.getNextStartFromList());
                if (next == null || (jsJobStreamStarter.getNextStartFromList() != null) && (jsJobStreamStarter.getNextStartFromList() == next
                        || jsJobStreamStarter.getNextStartFromList().before(next))) {
                    next = jsJobStreamStarter.getNextStartFromList();
                    if (next != null) {
                        LOGGER.trace("next start: " + next);
                        jsJobStreamStarter.setNextStart(next);
                        nextJsJobStreamStarter = jsJobStreamStarter;
                    }
                }
            }
        }
        if (nextJsJobStreamStarter != null) {
            LOGGER.debug("--> Next starter is: " + nextJsJobStreamStarter.getItemJobStreamStarter().getTitle() + " at " + nextJsJobStreamStarter
                    .getNextStart());
        } else {
            LOGGER.debug("Could not find a next starter");
        }
        return nextJsJobStreamStarter;
    }

    public void showLastStarts(String id) {
        LOGGER.debug(id + ": Show all lastStart");
        for (JSJobStream jsJobStream : listOfJobStreams.values()) {
            for (JSJobStreamStarter jsJobStreamStarter : jsJobStream.getListOfJobStreamStarter()) {
                LOGGER.trace(jsJobStream.getJobStream() + "." + jsJobStreamStarter.getItemJobStreamStarter().getTitle() + " last start: " + new Date(
                        jsJobStreamStarter.getLastStart()));
            }
        }
    }

    public JSJobStreamStarter reInitLastStart(JSJobStreamStarter nextStarter) {
        if (nextStarter != null) {
            for (JSJobStream jsJobStream : listOfJobStreams.values()) {

                for (JSJobStreamStarter jsJobStreamStarter : jsJobStream.getListOfJobStreamStarter()) {

                    if (nextStarter.getItemJobStreamStarter().getId().equals(jsJobStreamStarter.getItemJobStreamStarter().getId())) {
                        LOGGER.trace(jsJobStream.getJobStream() + "." + jsJobStreamStarter.getItemJobStreamStarter().getTitle()
                                + " last start: reinit " + new Date(jsJobStreamStarter.getLastStart()) + " --> " + new Date(nextStarter
                                        .getLastStart()));
                        jsJobStreamStarter.setLastStart(nextStarter.getLastStart());
                        return jsJobStreamStarter;
                    }
                }

            }
        }
        return nextStarter;
    }
}
