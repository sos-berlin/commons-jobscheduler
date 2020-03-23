package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobStreamContexts {

    private Map<Long, List<Long>> listOfContexts;

    public JobStreamContexts() {
        super();
        listOfContexts = new HashMap<Long, List<Long>>();
    }

    public void addTaskToContext(Long contextId, Long taskId) {
        if (listOfContexts.get(contextId) == null) {
            List<Long> l = new ArrayList<Long>();
            listOfContexts.put(contextId, l);
        }
        listOfContexts.get(contextId).add(taskId);
    }

    public Long getContext(String taskId) {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Map<Long, List<Long>> getListOfContexts() {
        return listOfContexts;
    }

}
