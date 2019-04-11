package com.sos.eventhandlerservice.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.eventhandlerservice.db.DBItemInConditionWithCommand;

public class JSJobInConditions {

    Map<JSJobConditionKey, JSInConditions> listOfJobInConditions;

    public JSJobInConditions() {
        super();
        this.listOfJobInConditions = new HashMap<JSJobConditionKey, JSInConditions>();
    }

    public void addInCondition(JSInCondition inCondition) {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey(inCondition);
        JSInConditions jsInConditions = listOfJobInConditions.get(jobConditionKey);
        if (jsInConditions == null) {
            jsInConditions = new JSInConditions();
        }

        jsInConditions.addInCondition(inCondition);
        listOfJobInConditions.put(jobConditionKey, jsInConditions);
    }

    public JSInConditions getInConditions(JSJobConditionKey jobConditionKey) {
        return this.listOfJobInConditions.get(jobConditionKey);
    }

    public void setListOfJobInConditions(List<DBItemInConditionWithCommand> listOfInConditions) {
        for (DBItemInConditionWithCommand itemInConditionWithCommand : listOfInConditions) {
            JSInCondition jsInCondition = null;
            JSJobConditionKey jobConditionKey = new JSJobConditionKey(itemInConditionWithCommand);
            JSInConditions inConditions = listOfJobInConditions.get(jobConditionKey);
            if (inConditions != null && inConditions.getListOfInConditions() != null) {
                jsInCondition = inConditions.getListOfInConditions().get(itemInConditionWithCommand.getDbItemInCondition().getId());
            }
            if (jsInCondition == null) {
                jsInCondition = new JSInCondition();
            }
            JSInConditionCommand inConditionCommand = new JSInConditionCommand();
            inConditionCommand.setItemInConditionCommand(itemInConditionWithCommand.getDbItemInConditionCommand());
            jsInCondition.setConsumed(itemInConditionWithCommand.isConsumed());
            jsInCondition.addCommand(inConditionCommand);
            jsInCondition.setItemInCondition(itemInConditionWithCommand.getDbItemInCondition());
            addInCondition(jsInCondition);
        }
    }

    public Map<JSJobConditionKey, JSInConditions> getListOfJobInConditions() {
        return listOfJobInConditions;
    }

}
