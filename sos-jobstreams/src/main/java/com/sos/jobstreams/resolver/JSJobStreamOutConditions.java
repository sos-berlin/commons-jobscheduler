package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jitl.jobstreams.db.DBItemOutConditionWithConfiguredEvent;

public class JSJobStreamOutConditions {

    Map<JSJobStreamConditionKey, JSOutConditions> listOfJobStreamOutConditions;    

    public JSJobStreamOutConditions() {
        super();
        this.listOfJobStreamOutConditions = new HashMap<JSJobStreamConditionKey, JSOutConditions>();
    }
    
    public void addOutCondition(JSOutCondition outCondition) {
        JSJobStreamConditionKey jobConditionKey = new JSJobStreamConditionKey(outCondition);
        JSOutConditions jsOutConditions = listOfJobStreamOutConditions.get(jobConditionKey);
        if (jsOutConditions == null) {
            jsOutConditions = new JSOutConditions();
        }

        jsOutConditions.addOutCondition(outCondition);
        listOfJobStreamOutConditions.put(jobConditionKey, jsOutConditions);
    }

    public JSOutConditions getOutConditions(JSJobStreamConditionKey jobConditionKey) {
        return this.listOfJobStreamOutConditions.get(jobConditionKey);
    }

    public void setListOfJobStreamOutConditions(List<DBItemOutConditionWithConfiguredEvent> listOfOutConditions) {
        for (DBItemOutConditionWithConfiguredEvent itemOutConditionWithEvent : listOfOutConditions) {
            JSOutCondition jsOutCondition=null;
            JSJobStreamConditionKey jobStreamConditionKey = new JSJobStreamConditionKey(itemOutConditionWithEvent);
            JSOutConditions outConditions = listOfJobStreamOutConditions.get(jobStreamConditionKey);
            if (outConditions != null && outConditions.getListOfOutConditions() != null) {
                jsOutCondition = outConditions.getListOfOutConditions().get(itemOutConditionWithEvent.getDbItemOutCondition().getId());
            }
            if (jsOutCondition == null) {
                jsOutCondition = new JSOutCondition();
            }
            JSOutConditionEvent outConditionEvent = new JSOutConditionEvent();
            outConditionEvent.setItemOutCondition(itemOutConditionWithEvent.getDbItemOutConditionEvent());
            jsOutCondition.addEvent(outConditionEvent);
            jsOutCondition.setItemOutCondition(itemOutConditionWithEvent.getDbItemOutCondition());
            addOutCondition(jsOutCondition);
        }
    }

    public Map<JSJobStreamConditionKey, JSOutConditions> getListOfJobOutConditions() {
        return listOfJobStreamOutConditions;
    }

}
