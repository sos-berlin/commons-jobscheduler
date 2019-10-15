package com.sos.jobstreams.classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.sos.jitl.checkhistory.HistoryHelper;

public class FunctionResolver {

    public String resolveFunctions(String paramValue) {
        String newValue = paramValue;
        String[] functions = paramValue.split("%%");
        for (int i = 0; i < functions.length; i++) { 
            String f = functions[i];
            String functionName = HistoryHelper.getMethodName(f);
            String parameter = HistoryHelper.getParameter(f);
            f = f.replaceAll("\\)","\\\\)");
            f = f.replaceAll("\\(","\\\\(");
            String s = resolveFunction(functionName, parameter);
            if (!s.isEmpty()) {
                newValue = newValue.replaceAll("\\%\\%" + f + "\\%\\%", s);
            }
        }
        return newValue;
    }
    
    private String todayAsIso() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); 
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    private String resolveFunction(String functionName, String parameter) {

        switch (functionName) {  
        case "today":
            return todayAsIso();
         default:
            break;
        }
        return "";
    }
}
