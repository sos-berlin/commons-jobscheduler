package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.db.DBItemJobStreamTaskContext;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamsTaskContext;
import com.sos.jobstreams.classes.JobStarterOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStreamContexts {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStreamContexts.class);
    private Map<UUID, List<Long>> listOfContexts;
    private Map<Long, UUID> listOfTaskIds;

    public JobStreamContexts() {
        super();
        listOfContexts = new HashMap<UUID, List<Long>>();
        listOfTaskIds = new HashMap<Long, UUID>();
    }

    public void addTaskToContext(UUID contextId, String schedulerId, JobStarterOptions startedJob, SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        LOGGER.debug(String.format("adding task %s to context %s", startedJob.getTaskId(), contextId.toString()));
        if (listOfContexts.get(contextId) == null) {
            LOGGER.debug("init list of tasks for context " + contextId.toString());
            List<Long> l = new ArrayList<Long>();
            listOfContexts.put(contextId, l);
        }
        DBLayerJobStreamsTaskContext dbLayerJobStreamsTaskContext = new DBLayerJobStreamsTaskContext(sosHibernateSession);
        DBItemJobStreamTaskContext dbItemJobStreamTaskContext = new DBItemJobStreamTaskContext();
        dbItemJobStreamTaskContext.setCreated(new Date());
        dbItemJobStreamTaskContext.setJobStreamHistoryId(contextId.toString());
        dbItemJobStreamTaskContext.setJob(startedJob.getJob());
        dbItemJobStreamTaskContext.setJobStream(startedJob.getJobStream());
        dbItemJobStreamTaskContext.setTaskId(startedJob.getTaskId());
        dbItemJobStreamTaskContext.setSchedulerId(schedulerId);
        LOGGER.debug("store contextid:" + contextId);
        dbLayerJobStreamsTaskContext.store(dbItemJobStreamTaskContext);
        LOGGER.debug("adding contextid:" + contextId);
        listOfContexts.get(contextId).add(startedJob.getTaskId());
        LOGGER.debug("adding taskid " + startedJob.getTaskId());
        listOfTaskIds.put(startedJob.getTaskId(), contextId);
    }

    public UUID getContext(Long taskId) {
        return listOfTaskIds.get(taskId);
    }

    public Map<UUID, List<Long>> getListOfContexts() {
        return listOfContexts;
    }

    public void setTaskToContext(List<DBItemJobStreamTaskContext> listOfTaskContext) {
        for (DBItemJobStreamTaskContext dbItemJobStreamTaskContext : listOfTaskContext) {
            UUID contextId = UUID.fromString(dbItemJobStreamTaskContext.getJobStreamHistoryId());
            if (listOfContexts.get(contextId) == null) {
                LOGGER.debug("init list of tasks for context " + contextId.toString());
                List<Long> l = new ArrayList<Long>();
                listOfContexts.put(contextId, l);
            }
            listOfContexts.get(contextId).add(dbItemJobStreamTaskContext.getTaskId());
            listOfTaskIds.put(dbItemJobStreamTaskContext.getTaskId(), contextId);
        }
    }

}
