package com.sos.jobstreams.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobstreams.db.FilterEvents;
import com.sos.jobstreams.classes.EventDate;

public class JSCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSCondition.class);

    private String conditionType;
    private String conditionParam;
    private String conditionJobStream;
    private String conditionDate;
    private String conditionValue;
    private String conditionJob;
    private String conditionJobChain;
    private String conditionQuery;
    private String eventName;
    private boolean globalEvent;
    private boolean haveDate;

    public JSCondition(String condition) {
        haveDate = false;
        conditionType = getConditionType(condition);
        conditionParam = getConditionTypeParam(condition);
        conditionJobStream = getConditionJobStream(conditionParam);
        conditionJob = getConditionJob(conditionParam);
        conditionJobChain = getConditionJobChain(conditionParam);
        conditionQuery = getConditionQuery(conditionParam);
        conditionDate = getConditionDate(conditionParam);
        eventName = getConditionEventName(conditionParam);
        conditionValue = condition;
        globalEvent = typeIsGlobalEvent();
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventNameWithType() {
        String e = "";
        if (this.typeIsGlobalEvent()) {
            e = "global:" + this.getEventName();
        } else {
            e = this.getEventName();
        }
        return e;
    }

    private String getConditionDate(String conditionParam) {
        if (conditionParam != null) {
            String[] conditionParts = conditionParam.split("\\[");
            if (conditionParts.length > 1) {
                String s = conditionParts[1].trim();
                try {
                    s = s.replaceAll("]", "");
                } catch (java.util.regex.PatternSyntaxException e) {
                    LOGGER.warn(e.getMessage());
                }
                haveDate = true;
                return s.trim();
            }
        }
        return "today";

    }

    private String getConditionJob(String conditionParam) {
        String s = "";
        if (conditionParam.indexOf(".") >= 0) {
            s = conditionParam.split("\\.")[0];
        }
        return s;
    }

    private String getConditionJobChain(String conditionParam) {
        return getConditionJob(conditionParam);
    }

    private String getConditionQuery(String conditionParam) {
        String s = conditionParam;
        if (conditionParam.indexOf(".") >= 0) {
            s = conditionParam.split("\\.")[1];
        }
        return s;
    }

    private String getConditionType(String condition) {
        String[] conditionParts = condition.split(":");
        if (conditionParts.length == 1) {
            return "event";
        } else {
            return conditionParts[0].replaceAll("_", "").toLowerCase();
        }
    }

    private String getConditionTypeParam(String condition) {
        String[] s = condition.split(":");
        if (s.length > 1) {
            String param = s[0];
            try {
                param = condition.replaceAll(s[0] + ":", "");
            } catch (java.util.regex.PatternSyntaxException e) {
                LOGGER.warn(e.getMessage());
            }

            return param;
        } else {
            return s[0];
        }
    }

    private String getConditionJobStream(String conditionParam) {
        String s = "";
        String p = "";
        String[] conditionParts = conditionParam.split("\\[");
        if (conditionParts.length > 1) {
           p = conditionParts[0];
        }else {
            p = conditionParam;
        }
        if (p.indexOf(".") >= 0) {
            s = p.substring(0, p.indexOf("."));
        }
        return s;
    }

    public boolean typeIsEvent() {
        return ("event".equals(conditionType) || "global".equals(conditionType));
    }

    public boolean typeIsGlobalEvent() {
        return ("global".equals(conditionType));
    }

    public boolean typeIsLocalEvent() {
        return ("event".equals(conditionType));
    }

    private String getConditionEventName(String eventName) {
        if (typeIsEvent()) {
            eventName = eventName.replace("[" + this.getConditionDate() + "]", "");
            eventName = eventName.replace(this.getConditionJobStream() + ".", "");
        } else {
            eventName = "";
        }
        return eventName;
    }

    public String getConditionType() {
        return conditionType;
    }

    public String getConditionParam() {
        return conditionParam;
    }

    public String getConditionJobStream() {
        return conditionJobStream;
    }

    public String getConditionDate() {
        return conditionDate;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public String getConditionJob() {
        return conditionJob;
    }

    public String getConditionQuery() {
        return conditionQuery;
    }

    public String getConditionJobChain() {
        return conditionJobChain;
    }

    public boolean isGlobalEvent() {
        return globalEvent;
    }

    public boolean isHaveDate() {
        return haveDate;
    }

    public boolean isNonContextEvent() {
        return (this.typeIsEvent() && (this.isHaveDate() || this.isGlobalEvent() || !"".equals(this.getConditionJobStream())));
    }

    public FilterEvents getFilterEventsNonContextEvent(String jobSchedulerId) {
        FilterEvents filterEvents = new FilterEvents();
        filterEvents.setSchedulerId(jobSchedulerId);
        filterEvents.setGlobalEvent(this.isGlobalEvent());
        filterEvents.setEvent(this.eventName);
        if (this.isHaveDate()) {
            EventDate eventDate = new EventDate();
            String session = eventDate.getEventDate(this.getConditionDate());
            filterEvents.setSession(session);
        }
        if (!("".equals(this.getConditionJobStream()))) {
            filterEvents.setJobStream(this.getConditionJobStream());
        }
        return filterEvents;

    }

}
