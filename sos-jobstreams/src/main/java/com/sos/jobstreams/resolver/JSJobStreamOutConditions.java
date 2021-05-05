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

    public void setListOfJobStreamOutConditions(JSJobOutConditions jsJobOutConditions) {
        for (Map.Entry<JSJobConditionKey, JSOutConditions> entry : jsJobOutConditions.getListOfJobOutConditions().entrySet()) {
            for (JSOutCondition jsOutcondition : entry.getValue().getListOfOutConditions().values()) {
                JSJobStreamConditionKey jsJobStreamConditionKey = new JSJobStreamConditionKey();
                jsJobStreamConditionKey.setJobSchedulerId(entry.getKey().getJobSchedulerId());
                jsJobStreamConditionKey.setJobStream(jsOutcondition.getJobStream());
                if (this.listOfJobStreamOutConditions.get(jsJobStreamConditionKey) == null) {
                    this.listOfJobStreamOutConditions.put(jsJobStreamConditionKey, new JSOutConditions());
                }
                this.listOfJobStreamOutConditions.get(jsJobStreamConditionKey).getListOfOutConditions().put(jsOutcondition.getId(), jsOutcondition);
            }
        }
    }

    public Map<JSJobStreamConditionKey, JSOutConditions> getListOfJobOutConditions() {
        return listOfJobStreamOutConditions;
    }

}
