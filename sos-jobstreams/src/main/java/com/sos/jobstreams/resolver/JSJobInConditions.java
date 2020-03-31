package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.jobstreams.db.DBItemInConditionWithCommand;

public class JSJobInConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobInConditions.class);

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

            jsInCondition.addCommand(inConditionCommand);
            jsInCondition.setItemInCondition(itemInConditionWithCommand.getDbItemInCondition());
            jsInCondition.setListOfDates(sosHibernateSession, settings.getSchedulerId());
            if (itemInConditionWithCommand.getConsumedForContext() != null) {
                for (String context : itemInConditionWithCommand.getConsumedForContext()) {
                    try {
                        jsInCondition.setConsumed(UUID.fromString(context));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Could not create the contextId from: " + context);
                    }
                }
            }
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
