package com.sos.jobstreams.resolver;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;

public class JSHistoryEntry {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSHistoryEntry.class);

    private DBItemJobStreamHistory itemJobStreamHistory;

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

  
}
