package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSHistory.class);

    List<JSHistoryEntry> listOfHistoryEntries;

    public JSHistory() {
        super();
        this.listOfHistoryEntries = new ArrayList<JSHistoryEntry>();
    }

    public void addHistoryEntry(JSHistoryEntry historyEntry,SOSHibernateSession session) throws SOSHibernateException {
        DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(session);
        dbLayerJobStreamHistory.save(historyEntry.getItemJobStreamHistory());
        LOGGER.debug("Adding historyEntry: " + historyEntry.getContextId());
        this.listOfHistoryEntries.add(historyEntry);
    }

    public void setListOfHistoryEntries(List<DBItemJobStreamHistory> listOfHistoryEntries) {
        for (DBItemJobStreamHistory itemHistoryEntry : listOfHistoryEntries) {
            JSHistoryEntry jsHistoryEntry = new JSHistoryEntry();
            jsHistoryEntry.setItemJobStreamHistory(itemHistoryEntry);
            this.listOfHistoryEntries.add(jsHistoryEntry);
        }
    }

    public List<JSHistoryEntry> getListOfHistoryEntries () {
        return listOfHistoryEntries;
    }

}
