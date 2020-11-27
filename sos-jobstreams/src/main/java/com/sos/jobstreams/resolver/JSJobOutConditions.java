package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jitl.jobstreams.db.DBItemOutCondition;
import com.sos.jitl.jobstreams.db.DBItemOutConditionEvent;
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithConfiguredEvent;

public class JSJobOutConditions {

    Map<JSJobConditionKey, JSOutConditions> listOfJobOutConditions;

    public JSJobOutConditions() {
        super();
        this.listOfJobOutConditions = new HashMap<JSJobConditionKey, JSOutConditions>();
    }

    public void addOutCondition(JSOutCondition outCondition) {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey(outCondition);
        JSOutConditions jsOutConditions = listOfJobOutConditions.get(jobConditionKey);
        if (jsOutConditions == null) {
            jsOutConditions = new JSOutConditions();
        }

        jsOutConditions.addOutCondition(outCondition);
        listOfJobOutConditions.put(jobConditionKey, jsOutConditions);
    }

    public JSOutConditions getOutConditions(JSJobConditionKey jobConditionKey) {
        return this.listOfJobOutConditions.get(jobConditionKey);
    }

    public void setListOfJobOutConditions(List<DBItemOutConditionWithConfiguredEvent> listOfOutConditions) {
        for (DBItemOutConditionWithConfiguredEvent itemOutConditionWithEvent : listOfOutConditions) {
            JSOutCondition jsOutCondition = null;
            JSJobConditionKey jobConditionKey = new JSJobConditionKey(itemOutConditionWithEvent);
            JSOutConditions outConditions = listOfJobOutConditions.get(jobConditionKey);
            if (outConditions != null && outConditions.getListOfOutConditions() != null) {
                jsOutCondition = outConditions.getListOfOutConditions().get(itemOutConditionWithEvent.getOutId());
            }
            if (jsOutCondition == null) {
                jsOutCondition = new JSOutCondition();
            }
            JSOutConditionEvent outConditionEvent = new JSOutConditionEvent();

            DBItemOutConditionEvent itemOutConditionEvent = new DBItemOutConditionEvent();
            itemOutConditionEvent.setCommand(itemOutConditionWithEvent.getCommand());
            itemOutConditionEvent.setCreated(itemOutConditionWithEvent.getCreated());
            itemOutConditionEvent.setEvent(itemOutConditionWithEvent.getEvent());
            itemOutConditionEvent.setGlobalEvent(itemOutConditionWithEvent.getGlobalEvent());
            itemOutConditionEvent.setId(itemOutConditionWithEvent.getoEventId());
            itemOutConditionEvent.setOutConditionId(itemOutConditionWithEvent.getOutId());

            outConditionEvent.setItemOutCondition(itemOutConditionEvent);

            DBItemOutCondition itemOutCondition = new DBItemOutCondition();
            itemOutCondition.setCreated(itemOutConditionWithEvent.getCreated());
            itemOutCondition.setExpression(itemOutConditionWithEvent.getExpression());
            itemOutCondition.setFolder(itemOutConditionWithEvent.getFolder());
            itemOutCondition.setId(itemOutConditionWithEvent.getOutId());
            itemOutCondition.setJob(itemOutConditionWithEvent.getJob());
            itemOutCondition.setJobStream(itemOutConditionWithEvent.getJobStream());
            itemOutCondition.setSchedulerId(itemOutConditionWithEvent.getJobSchedulerId());

            jsOutCondition.addEvent(outConditionEvent);
            jsOutCondition.setItemOutCondition(itemOutCondition);
            addOutCondition(jsOutCondition);

        }
    }

    public Map<JSJobConditionKey, JSOutConditions> getListOfJobOutConditions() {
        return listOfJobOutConditions;
    }

}
