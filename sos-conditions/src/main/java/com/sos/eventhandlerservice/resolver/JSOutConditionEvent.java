package com.sos.eventhandlerservice.resolver;

import com.sos.eventhandlerservice.db.DBItemOutConditionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSOutConditionEvent {

    public static enum OutConditionEventCommand {
        create, delete
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JSOutConditionEvent.class);
    private DBItemOutConditionEvent itemOutConditionEvent;

    public void setItemOutCondition(DBItemOutConditionEvent itemOutConditionEvent) {
        this.itemOutConditionEvent = itemOutConditionEvent;
    }

    public Long getId() {
        return itemOutConditionEvent.getId();
    }

    public Long getOutConditionId() {
        return itemOutConditionEvent.getOutConditionId();
    }

    public String getEvent() {
        return itemOutConditionEvent.getEvent();
    }

    public String getEventValue() {
        String[] s = itemOutConditionEvent.getEvent().split(":");
        String e = itemOutConditionEvent.getEvent();
        if (s.length > 1) {
            e = e.replaceFirst(".*:", "");
        }
            
        return e;
    }
    
    public String getCommand() {
        String[] s = itemOutConditionEvent.getEvent().split(":");
        String command = OutConditionEventCommand.create.name();
        if (s.length > 1) {
            try {
                command = OutConditionEventCommand.valueOf(s[0]).name();
            } catch (IllegalArgumentException e) {
                LOGGER.warn("unknown command: " + s[0] + " assuming [create]");
            }
        }
        return command;
    }

    public boolean isCreateCommand() {
        return OutConditionEventCommand.create.name().equals(getCommand());
    }

    public boolean isDeleteCommand() {
        return OutConditionEventCommand.delete.name().equals(getCommand());
    }
}
