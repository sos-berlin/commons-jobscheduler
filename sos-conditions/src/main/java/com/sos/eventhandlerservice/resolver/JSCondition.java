package com.sos.eventhandlerservice.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSCondition.class);

    private String conditionType;
    private String conditionParam;
    private String conditionWorkflow;
    private String conditionDate;
    private String conditionValue;

    public JSCondition(String condition) {
        conditionType = getConditionType(condition);
        conditionParam = getConditionTypeParam(condition);
        conditionWorkflow = getConditionWorkflow(conditionParam);
        conditionDate = getConditionDate(conditionParam);
        conditionValue = condition;
    }

    private String getConditionDate(String conditionParam) {
        if (conditionParam != null) {
            String[] conditionParts = conditionParam.split("\\[");
            if (conditionParts.length > 1) {
                String s = conditionParts[1].trim();
                s = s.replaceAll("]", "");
                return s.trim();
            }
        }
        return "today";

    }

    private String getConditionType(String condition) {
        String[] conditionParts = condition.split(":");
        if (conditionParts.length == 1) {
            return "event";
        } else {
            return conditionParts[0];
        }

    }

    private String getConditionTypeParam(String condition) {
        String s = condition.replaceFirst("event:", "").replaceFirst("fileexist:", "").replaceFirst("returncode:", "");
        return s;
    }

    private String getConditionWorkflow(String conditionParam) {
        String s = "";
        if (conditionParam.indexOf(".") >= 0) {
            s = conditionParam.substring(0, conditionParam.indexOf("."));
        }
        return s;
    }

    public Integer getConditionIntegerParam() {
        Integer i = null;

        try {
            i = Integer.parseInt(conditionParam);
        } catch (NumberFormatException e) {
            LOGGER.warn("Wrong Integer value in " + conditionParam);
        }
        return i;
    }

    public String getConditionType() {
        return conditionType;
    }

    public String getConditionParam() {
        return conditionParam;
    }

    public String getConditionWorkflow() {
        return conditionWorkflow;
    }

    public String getConditionDate() {
        return conditionDate;
    }

    public String getConditonValue() {
        return conditionValue;
    }

    public String getEventName() {
        String event = conditionParam;
        event = event.replace("[" + this.getConditionDate() + "]", "");
        event = event.replace(this.getConditionWorkflow() + ".", "");        
        return event;
    }
}
