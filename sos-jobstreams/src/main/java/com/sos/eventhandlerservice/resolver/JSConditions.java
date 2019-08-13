package com.sos.eventhandlerservice.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class JSConditions {

    public List<JSCondition> getListOfConditions(String expression) {
        List<JSCondition> listOfConditions = new ArrayList<JSCondition>();
        expression = expression.replaceAll("\\(", "");
        expression = expression.replaceAll("\\)", "");
        expression = expression.replaceAll("&&", "");
        expression = expression.replaceAll("\\|\\|", "");
        StringTokenizer tokens = new StringTokenizer(expression);
        while (tokens.hasMoreTokens()) {
            String s = tokens.nextToken().trim();
            if (!"and".equalsIgnoreCase(s) && !"or".equalsIgnoreCase(s) && !"not".equalsIgnoreCase(s)) {
                JSCondition jsCondition = new JSCondition(s);
                listOfConditions.add(jsCondition);
            }
        }
        return listOfConditions;

    }

}
