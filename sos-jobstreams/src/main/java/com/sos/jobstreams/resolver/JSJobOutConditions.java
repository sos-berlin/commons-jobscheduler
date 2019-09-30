package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jobstreams.db.DBItemOutConditionWithConfiguredEvent;

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
            JSOutCondition jsOutCondition=null;
            JSJobConditionKey jobConditionKey = new JSJobConditionKey(itemOutConditionWithEvent);
            JSOutConditions outConditions = listOfJobOutConditions.get(jobConditionKey);
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

    public Map<JSJobConditionKey, JSOutConditions> getListOfJobOutConditions() {
        return listOfJobOutConditions;
    }

}
