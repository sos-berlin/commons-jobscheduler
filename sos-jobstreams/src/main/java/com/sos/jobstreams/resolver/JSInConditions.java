package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jitl.jobstreams.db.DBItemInCondition;

public class JSInConditions {

    Map<Long, JSInCondition> listOfInConditions;

    public JSInConditions() {
        super();
        this.listOfInConditions = new HashMap<Long, JSInCondition>();
    }

    public void addInCondition(JSInCondition inCondition) {
        this.listOfInConditions.put(inCondition.getId(), inCondition);
    }

    public JSInCondition getCondition(Long conditionKey) {
        return this.listOfInConditions.get(conditionKey);
    }

    public void setListOfInConditions(List<DBItemInCondition> listOfInConditions) {
        for (DBItemInCondition itemInCondition : listOfInConditions) {
            JSInCondition jsInCondition = new JSInCondition();
            jsInCondition.setItemInCondition(itemInCondition);
            addInCondition(jsInCondition);
        }
    }

    public Map<Long, JSInCondition> getListOfInConditions() {
        return listOfInConditions;
    }

}
