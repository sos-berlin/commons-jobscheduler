package com.sos.eventhandlerservice.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.eventhandlerservice.db.DBItemOutCondition;

public class JSOutConditions {

    Map<Long, JSOutCondition> listOfOutConditions;

    public JSOutConditions() {
        super();
        this.listOfOutConditions = new HashMap<Long, JSOutCondition>();
    }

    public void addOutCondition(JSOutCondition outCondition) {
        this.listOfOutConditions.put(outCondition.getId(), outCondition);
    }

    public JSOutCondition getCondition(Long conditionKey) {
        return this.listOfOutConditions.get(conditionKey);
    }

    public void setListOfOutConditions(List<DBItemOutCondition> listOfOutConditions) {
        for (DBItemOutCondition itemOutCondition : listOfOutConditions) {
            JSOutCondition jsOutCondition = new JSOutCondition();
            jsOutCondition.setItemOutCondition(itemOutCondition);
            addOutCondition(jsOutCondition);
        }
    }

    public Map<Long, JSOutCondition> getListOfOutConditions() {
        return listOfOutConditions;
    }

}
