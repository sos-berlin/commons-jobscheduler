package com.sos.eventhandlerservice.classes;

import java.util.Calendar;

import com.sos.hibernate.classes.ClassList;
import com.sos.jitl.classes.event.EventHandlerSettings;

public class Constants {
    
    public static enum OutConditionEventCommand {
        create, delete
    }

    public static final String DBItemEvent = com.sos.eventhandlerservice.db.DBItemEvent.class.getSimpleName();
    public static final String EVENTS_TABLE_SEQUENCE = "JSTREAM_EVENTS_ID_SEQ";
    public static final String EVENTS_TABLE = "JSTREAM_EVENTS";

    public static final String DBItemConsumedInCondition = com.sos.eventhandlerservice.db.DBItemConsumedInCondition.class.getSimpleName();
    public static final String CONSUMED_IN_CONDITIONS_TABLE_SEQUENCE = "JSTREAM_CONSUMED_INCOND_ID_SEQ";
    public static final String CONSUMED_IN_CONDITIONS_TABLE = "JSTREAM_CONSUMED_IN_CONDITIONS";

    public static final String DBItemInCondition = com.sos.eventhandlerservice.db.DBItemInCondition.class.getSimpleName();
    public static final String IN_CONDITIONS_TABLE_SEQUENCE = "JSTREAM_IN_COND_ID_SEQ";
    public static final String IN_CONDITIONS_TABLE = "JSTREAM_IN_CONDITIONS";

    public static final String DBItemOutConditionEvent = com.sos.eventhandlerservice.db.DBItemOutConditionEvent.class.getSimpleName();
    public static final String OUT_CONDITION_EVENTS_TABLE_SEQUENCE = "JSTREAM_OUT_COND_EV_ID_SEQ";
    public static final String OUT_CONDITION_EVENTS_TABLE = "JSTREAM_OUT_CONDITION_EVENTS";

    public static final String DBItemOutCondition = com.sos.eventhandlerservice.db.DBItemOutCondition.class.getSimpleName();
    public static final String OUT_CONDITIONS_TABLE_SEQUENCE = "JSTREAM_OUT_COND_ID_SEQ";
    public static final String OUT_CONDITIONS_TABLE = "JSTREAM_OUT_CONDITIONS";

    public static final String DBItemInConditionCommand = com.sos.eventhandlerservice.db.DBItemInConditionCommand.class.getSimpleName();
    public static final String IN_CONDITION_COMMANDS_TABLE_SEQUENCE = "JSTREAM_IN_COND_CMD_ID_SEQ";
    public static final String IN_CONDITION_COMMANDS_TABLE = "JSTREAM_IN_CONDITION_COMMANDS";

    public static  EventHandlerSettings settings=null;

    
    public static String getSession() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(month) + "." + String.valueOf(dayOfMonth);
    }

    public static ClassList getConditionsClassMapping() {
        ClassList cl = new ClassList();
        cl.add(com.sos.eventhandlerservice.db.DBItemOutCondition.class);
        cl.add(com.sos.eventhandlerservice.db.DBItemOutConditionEvent.class);
        cl.add(com.sos.eventhandlerservice.db.DBItemInCondition.class);
        cl.add(com.sos.eventhandlerservice.db.DBItemInConditionCommand.class);
        cl.add(com.sos.eventhandlerservice.db.DBItemConsumedInCondition.class);
        cl.add(com.sos.eventhandlerservice.db.DBItemEvent.class);
        return cl;
    }
}
