package com.sos.jobstreams.resolver;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jobstreams.db.DBItemInConditionWithCommand;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.exception.SOSInvalidDataException;
import com.sos.exception.SOSMissingDataException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;

public class JSJobInConditions {

    Map<JSJobConditionKey, JSInConditions> listOfJobInConditions;
    private EventHandlerSettings settings;
    private Boolean haveGlobalConditions = false;

    public JSJobInConditions(EventHandlerSettings settings) {
        super();
        this.settings = settings;
        this.listOfJobInConditions = new HashMap<JSJobConditionKey, JSInConditions>();
    }

    public void addInCondition(JSInCondition inCondition) {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey(inCondition);
        JSInConditions jsInConditions = listOfJobInConditions.get(jobConditionKey);
        if (jsInConditions == null) {
            jsInConditions = new JSInConditions();
        }
        this.haveGlobalConditions = this.haveGlobalConditions || inCondition.getExpression().contains("global:");
        jsInConditions.addInCondition(inCondition);
        listOfJobInConditions.put(jobConditionKey, jsInConditions);
    }

    public JSInConditions getInConditions(JSJobConditionKey jobConditionKey) {
        return this.listOfJobInConditions.get(jobConditionKey);
    }

    public void setListOfJobInConditions(SOSHibernateSession sosHibernateSession, List<DBItemInConditionWithCommand> listOfInConditions) {
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
            jsInCondition.setListOfDates(sosHibernateSession, settings.getSchedulerId());
            addInCondition(jsInCondition);
        }
    }

    public Map<JSJobConditionKey, JSInConditions> getListOfJobInConditions() {
        return listOfJobInConditions;
    }

    
    public Boolean getHaveGlobalConditions() {
        return haveGlobalConditions;
    }

}
