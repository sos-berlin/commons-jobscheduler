package com.sos.jobstreams.resolver;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jobstreams.classes.JobStarterOptions;

import sos.util.SOSString;

public class JSHistoryEntry {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSHistoryEntry.class);

    private DBItemJobStreamHistory itemJobStreamHistory;
    private boolean skipValidation=true;

    public JSHistoryEntry() {
        super();
        itemJobStreamHistory = new DBItemJobStreamHistory();
    }

    public void setItemJobStreamHistory(DBItemJobStreamHistory itemJobStreamHistory) {
        this.itemJobStreamHistory = itemJobStreamHistory;
    }

    public Long getId() {
        return itemJobStreamHistory.getId();
    }

    public UUID getContextId() {
        return UUID.fromString(itemJobStreamHistory.getContextId());
    }

    public Date getEnded() {
        return itemJobStreamHistory.getEnded();
    }

    public Date getStarted() {
        return itemJobStreamHistory.getStarted();
    }

    public Boolean isRunning() {
        return itemJobStreamHistory.getRunning();
    }

    public void setCreated(Date created) {
        itemJobStreamHistory.setCreated(created);
    }

    public DBItemJobStreamHistory getItemJobStreamHistory() {
        return itemJobStreamHistory;
    }

    public boolean endOfStream(JSJobStream jsJobStream, JobStreamContexts jobStreamContexts) {
        // Checking all starters whether there is an end job
        LOGGER.debug("Check for end job in job stream: " + jsJobStream.getJobStream());
        boolean endReached = false;
        for (JSJobStreamStarter jsJobStreamStarter : jsJobStream.getListOfJobStreamStarter()) {
            if (!"".equals(jsJobStreamStarter.getEndJob()) && jsJobStreamStarter.getEndJob() != null) {
                LOGGER.debug("end job found for starter: " + jsJobStreamStarter.getItemJobStreamStarter().getTitle() + " Job: " + jsJobStreamStarter
                        .getEndJob());

                Map<UUID, List<JobStarterOptions>> mapOfContexts = jobStreamContexts.getListOfContexts();
                List<JobStarterOptions> listOfJobStarterOptions = mapOfContexts.get(this.getContextId());
                if (listOfJobStarterOptions != null) {
                    for (JobStarterOptions jobStarterOptions : listOfJobStarterOptions) {
                        LOGGER.trace("check job: " + jobStarterOptions.getJob());
                        if (jobStarterOptions.getJob().equals(jsJobStreamStarter.getEndJob())) {
                            endReached = true;
                            LOGGER.debug("- JobStream " + jsJobStream.getJobStream() + " has reached the end of stream job: " + jsJobStreamStarter
                                    .getEndJob() + " for the instance " + this.getContextId());
                            break;
                        }
                    }
                }
            }
        }

        return endReached;
    }

    public boolean checkRunning(JSConditionResolver jsConditionResolver) {
        // get all Inconditions for this jobStream
        // running is true if there is at least one local not global event in the not consumed conditions that is created by a local out condition

        LOGGER.debug("- CheckReady - ");

        boolean running = false;
        JSJobStream jsJobStream = jsConditionResolver.getJsJobStreams().getJobStream(itemJobStreamHistory.getJobStream());
        JSJobStreamConditionKey jsJobStreamConditionKey = new JSJobStreamConditionKey();
        jsJobStreamConditionKey.setJobSchedulerId(jsJobStream.getJobSchedulerId());
        jsJobStreamConditionKey.setJobStream(jsJobStream.getJobStream());

        LOGGER.debug("- JobStream: " + jsJobStream.getJobStream());
        LOGGER.debug("- Context: " + this.getContextId());
        LOGGER.debug("- InCondition Events: ");

        JSInConditions jsInConditions = jsConditionResolver.getJsJobStreamInConditions().getListOfJobStreamInConditions().get(
                jsJobStreamConditionKey);
        JobStreamContexts jobStreamContexts = jsConditionResolver.getJobStreamContexts();
        if (jsInConditions != null && jsInConditions.getListOfInConditions() != null) {

            boolean endedByJob = this.endOfStream(jsJobStream, jobStreamContexts);
            if (!endedByJob) {

                LOGGER.debug("- local OutCondition Events: ");

                JSOutConditions jsOutConditions = jsConditionResolver.getJsJobStreamOutConditions().getListOfJobOutConditions().get(
                        jsJobStreamConditionKey);
                Set<String> localEvents = new HashSet<String>();
                if (jsOutConditions != null && jsOutConditions.getListOfOutConditions() != null) {
                    for (JSOutCondition jsOutCondition : jsOutConditions.getListOfOutConditions().values()) {
                        for (JSOutConditionEvent jsOutConditionEvent : jsOutCondition.getListOfOutConditionEvent()) {
                            if (!jsOutConditionEvent.isGlobal()) {
                                localEvents.add(jsOutConditionEvent.getEvent());
                                LOGGER.trace("- Event: " + jsOutConditionEvent.getEvent());

                            }
                        }

                    }
                }

                for (JSInCondition jsInCondition : jsInConditions.getListOfInConditions().values()) {
                    LOGGER.debug("- InCondition consumed : " + jsInCondition.isConsumed(this.getContextId()));
                    LOGGER.debug(SOSString.toString(jsInCondition));
                    if (jsInCondition.isStartToday() && !jsInCondition.isConsumed(this.getContextId())) {
                        List<JSCondition> listOfConditions = JSConditions.getListOfConditions(jsInCondition.getExpression());
                        LOGGER.debug("- InCondition expression : " + jsInCondition.getExpression());
                        for (JSCondition jsCondition : listOfConditions) {
                            if (jsCondition.getConditionJobStream() == null || jsCondition.getConditionJobStream().isEmpty()) {
                                LOGGER.debug("- InCondition event : " + jsCondition.getEventName());
                                if (jsCondition.typeIsLocalEvent() && localEvents.contains(jsCondition.getEventName())) {
                                    LOGGER.debug("- JobStream " + jsJobStream.getJobStream() + " is still running the context " + this
                                            .getContextId());
                                    running = true;
                                    break;
                                }
                            }
                        }
                        if (running) {
                            break;
                        }
                    }
                }
            }
        }

        if (!running && jsInConditions != null && jsInConditions.getListOfInConditions() != null && jsInConditions.getListOfInConditions().values() != null) {
            for (JSInCondition jsInCondition : jsInConditions.getListOfInConditions().values()) {
                LOGGER.debug("- InCondition expression : " + jsInCondition.getExpression());
                LOGGER.debug("- InCondition Job is running : " + jsInCondition.jobIsRunning(this.getContextId()));
                if (jsInCondition.isStartToday() && jsInCondition.jobIsRunning(this.getContextId())) {
                    running = true;
                    break;
                }
            }
        }
        return running;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((itemJobStreamHistory == null) ? 0 : itemJobStreamHistory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JSHistoryEntry other = (JSHistoryEntry) obj;
        if (itemJobStreamHistory == null) {
            if (other.itemJobStreamHistory != null)
                return false;
        } else if (!itemJobStreamHistory.getId().equals(other.itemJobStreamHistory.getId()))
            return false;
        return true;
    }

    
    public boolean isSkipValidation() {
        return skipValidation;
    }

    
    public void setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

}
