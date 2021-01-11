package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.db.DBItemJobStreamTaskContext;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamsTaskContext;
import com.sos.jobstreams.classes.JobStarterOptions;

public class JobStreamContexts {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStreamContexts.class);
    private Map<UUID, List<JobStarterOptions>> listOfContexts;
    private Map<Long, UUID> listOfTaskIds;
    private Map<UUID, List<Long>> taskIdsOfContext;

    
    public JobStreamContexts() {
        super();
        listOfContexts = new HashMap<UUID, List<JobStarterOptions>>();
        listOfTaskIds = new HashMap<Long, UUID>();
        taskIdsOfContext = new HashMap<UUID,List<Long>>();
    }

    public void addTaskToContext(UUID contextId, String schedulerId, JobStarterOptions startedJob, SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        LOGGER.debug(String.format("adding task %s to context %s", startedJob.getTaskId(), contextId.toString()));
        if (listOfContexts.get(contextId) == null) {
            LOGGER.debug("init list of tasks for context " + contextId.toString());
            List<JobStarterOptions> l = new ArrayList<JobStarterOptions>();
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
        LOGGER.debug("store contextid:" + contextId + " for job " + startedJob.getJob() + " with taskId " + startedJob.getTaskId());
        dbLayerJobStreamsTaskContext.store(dbItemJobStreamTaskContext);
        LOGGER.debug("adding contextid:" + contextId);
        listOfContexts.get(contextId).add(startedJob);
        LOGGER.debug("adding taskid " + startedJob.getTaskId());
        listOfTaskIds.put(startedJob.getTaskId(), contextId);
        if (taskIdsOfContext.get(contextId) == null) {
            taskIdsOfContext.put(contextId, new ArrayList<Long>());
        }
        taskIdsOfContext.get(contextId).add(startedJob.getTaskId()); 

    }

    public UUID getContext(Long taskId) {
        return listOfTaskIds.get(taskId);
    }
    
    public List<Long> getTaskIdsOfContext(UUID context) {
        return taskIdsOfContext.get(context);
    }

    public Map<UUID, List<JobStarterOptions>> getListOfContexts() {
        return listOfContexts;
    }

    public void setTaskToContext(List<DBItemJobStreamTaskContext> listOfTaskContext) {
        for (DBItemJobStreamTaskContext dbItemJobStreamTaskContext : listOfTaskContext) {
            UUID contextId = UUID.fromString(dbItemJobStreamTaskContext.getJobStreamHistoryId());
            if (listOfContexts.get(contextId) == null) {
                LOGGER.debug("init list of tasks for context " + contextId.toString());
                List<JobStarterOptions> l = new ArrayList<JobStarterOptions>();
                listOfContexts.put(contextId, l);
            }
            
            JobStarterOptions jobStarterOptions = new JobStarterOptions();
            jobStarterOptions.setJob(dbItemJobStreamTaskContext.getJob());
            jobStarterOptions.setJobStream(dbItemJobStreamTaskContext.getJobStream());
            jobStarterOptions.setTaskId(dbItemJobStreamTaskContext.getTaskId());
            listOfContexts.get(contextId).add(jobStarterOptions);
            listOfTaskIds.put(dbItemJobStreamTaskContext.getTaskId(), contextId);
            if (taskIdsOfContext.get(contextId) == null) {
                taskIdsOfContext.put(contextId, new ArrayList<Long>());
            }
            taskIdsOfContext.get(contextId).add(dbItemJobStreamTaskContext.getTaskId()); 
        }
    }

}
