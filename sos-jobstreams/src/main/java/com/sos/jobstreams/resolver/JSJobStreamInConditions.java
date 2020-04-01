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

public class JSJobStreamInConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobStreamInConditions.class);

    Map<JSJobStreamConditionKey, JSInConditions> listOfJobStreamInConditions;
    private EventHandlerSettings settings;

    public JSJobStreamInConditions(EventHandlerSettings settings) {
        super();
        this.settings = settings;
        this.listOfJobStreamInConditions = new HashMap<JSJobStreamConditionKey, JSInConditions>();
    }

    public void addInCondition(JSInCondition inCondition) {
        JSJobStreamConditionKey jobstreamConditionKey = new JSJobStreamConditionKey(inCondition);
        JSInConditions jsInConditions = listOfJobStreamInConditions.get(jobstreamConditionKey);
        if (jsInConditions == null) {
            jsInConditions = new JSInConditions();
        }
        jsInConditions.addInCondition(inCondition);
        listOfJobStreamInConditions.put(jobstreamConditionKey, jsInConditions);
    }

    public JSInConditions getInConditions(JSJobStreamConditionKey jobConditionKey) {
        return this.listOfJobStreamInConditions.get(jobConditionKey);
    }

    public Map<JSJobStreamConditionKey, JSInConditions> getListOfJobStreamInConditions() {
        return listOfJobStreamInConditions;
    }

    public void setListOfJobInConditions(JSJobInConditions jsJobInConditions) {
        for (Map.Entry<JSJobConditionKey, JSInConditions> entry : jsJobInConditions.getListOfJobInConditions().entrySet()) {
            for (JSInCondition jsIncondition : entry.getValue().getListOfInConditions().values()) {
                JSJobStreamConditionKey jsJobStreamConditionKey = new JSJobStreamConditionKey();
                jsJobStreamConditionKey.setJobSchedulerId(entry.getKey().getJobSchedulerId());
                jsJobStreamConditionKey.setJobStream(jsIncondition.getJobStream());
                if (this.getListOfJobStreamInConditions().get(jsJobStreamConditionKey) == null) {
                    this.getListOfJobStreamInConditions().put(jsJobStreamConditionKey, new JSInConditions());
                }
                this.getListOfJobStreamInConditions().get(jsJobStreamConditionKey).getListOfInConditions().put(jsIncondition.getId(), jsIncondition);
            }
        }
    }

}
